package com.devhc.quicklearning.scheduler.yarn;

import com.devhc.quicklearning.apps.AppContainer;
import com.devhc.quicklearning.apps.AppContainers;
import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.AppResource;
import com.devhc.quicklearning.apps.AppStatus;
import com.devhc.quicklearning.apps.BaseApp;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceInformation;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * used for
 * alloc containers from yarn resource manager
 *
 * @author wanghuacheng
 */
public class YarnResourceAllocator {

  private final BaseApp app;
  @Getter
  @Setter
  private String userName;

  @Getter
  @Setter
  private String cmdSuffix;


  public YarnResourceAllocator(BaseApp app) {
    this.app = app;
  }

  public void retryJob(YarnContainerInfo cinfo)
      throws Exception {
    var job = typeJob.get(cinfo.getType());
    allocAppContainers(job, cinfo.getIndex());
  }

  @Data
  @Builder
  public static class YarnContainerInfo {

    private int index;
    private String type;
  }


  @Builder
  @Data
  public static class YarnFinishContainer {

    private AppStatus status;
    private String type;
    private Container container;
  }

  @Builder
  @Data
  public static class YarnContainer {

    private Container container;
    private String cmd;
  }


  private Logger LOG = LoggerFactory.getLogger(YarnResourceAllocator.class);
  private int priority = 0;
  // 10 min
  private long maxFailWaitSecs = 10 * 60;
  private AMRMClient<ContainerRequest> rmClient;
  @Getter
  private Map<String, Container[]> appAllocContainers = Maps.newHashMap();
  @Getter
  private Map<ContainerId, YarnContainerInfo> containerInfoMap = Maps.newHashMap();
  @Getter
  private Map<String, List<YarnFinishContainer>> finishContainers = Maps.newHashMap();
  private Map<String, AppJob> typeJob = Maps.newTreeMap();
  @Getter
  private LinkedBlockingDeque<YarnContainer> containerQueue = new LinkedBlockingDeque<>();


  private Resource maxResourceLimit;

  public void initAllocator(Map<String, AppJob> appJobMap)
      throws IOException, YarnException, InterruptedException {
    // alloc app resource container for app defined
    this.typeJob = appJobMap;
    for (var appJobEntry : typeJob.entrySet()) {
      var appJob = appJobEntry.getValue();
      allocAppContainers(appJob, -1);
    }
    LOG.info("appAllocContainers:{} containerInfoMap:{}", appAllocContainers, containerInfoMap);
  }

  public AppContainer.AppContainerBuilder convertContainer(String type, Container v) {
    var appContainerBuilder = AppContainer.builder()
        .type(type)
        .cid(v.getId().toString())
        .host(v.getNodeId().getHost())
        .port(v.getNodeId().getPort())
        .logLink(
            "http://" + v.getNodeHttpAddress() + "/node/containerlogs/" + v.getId().toString()
                + "/" + userName);
    return appContainerBuilder;
  }

  public AppContainers getAppContainers() {
    var runningMap = containerInfoMap;

    Map<String, List<AppContainer>> runningContainersMap = Maps.newHashMap();
    Map<String, List<AppContainer>> finishContainersMap = Maps.newHashMap();
    for (var rme : runningMap.entrySet()) {
      String jobType = rme.getValue().getType();
      var cons = runningContainersMap
          .computeIfAbsent(jobType, k -> Lists.newArrayList());
      var v = appAllocContainers.get(jobType)[rme.getValue().getIndex()];
      if (v != null) {
        cons.add(convertContainer(jobType, v).status(AppStatus.RUNNING).build());
      }
    }

    for (var rc : finishContainers.entrySet()) {
      String type = rc.getKey();
      var cons = finishContainersMap.computeIfAbsent(type, k -> Lists.newArrayList());

      for (var yarnStatusContainer : rc.getValue()) {
        var v = yarnStatusContainer.getContainer();
        cons.add(convertContainer(yarnStatusContainer.getType(), v)
            .status(yarnStatusContainer.status).build());
      }
    }

    return AppContainers.builder()
        .runningContainers(runningContainersMap)
        .finishContainers(finishContainersMap)
        .build();
  }


  /**
   * alloc container for appJob
   * if index = -1 alloc appJob.instance
   * if index >= 0 only alloc index container
   */
  private void allocAppContainers(AppJob appJob, int index)
      throws YarnException, IOException, InterruptedException {
    long startTime = System.currentTimeMillis();
    Resource r = convertAppResource(appJob.getResource());

    int num = index == -1 ? appJob.getResource().getInstance() : 1;
    LOG.info("alloc {}", appJob.getType());

    // request
    List<ContainerRequest> containerRequests = getRequests(r, num);
    for (ContainerRequest cr : containerRequests) {
      rmClient.addContainerRequest(cr);
    }

    // wait
    int allocFinished = 0;
    while (allocFinished < num) {
      AllocateResponse allocRes = rmClient
          .allocate(0.01f);
      List<Container> allocContainers = allocRes.getAllocatedContainers();
      allocFinished = allocContainers.size();
      LOG.info("finish {} {} container", appJob.getType(), allocFinished);

      if (allocFinished >= num) {
        if (index == -1) {
          // first alloc all
          Container[] appAllocContainerArr = new Container[num];
          for (int i = 0; i < allocFinished; i++) {
            if (i < num) {
              containerInfoMap.put(allocContainers.get(i).getId(), YarnContainerInfo.builder().
                  index(i).type(appJob.getType()).build());
              appAllocContainerArr[i] = allocContainers.get(i);

              String cmd = app.genCmds(appJob, i, cmdSuffix);
              containerQueue.offer(YarnContainer.builder().container(appAllocContainerArr[i])
                  .cmd(cmd)
                  .build());
            } else {
              ContainerId cid = allocContainers.get(allocFinished - i - 1).getId();
              rmClient.releaseAssignedContainer(cid);
              LOG.info("release redundance container {}", cid);
            }
          }
          this.appAllocContainers.put(appJob.getType(), appAllocContainerArr);
        } else {
          // retry
          for (int i = 0; i < allocFinished; i++) {
            if (i == 0) {
              var c = allocContainers.get(i);
              containerInfoMap.put(c.getId(),
                  YarnContainerInfo.builder().index(index).type(appJob.getType()).build());
              appAllocContainers.get(appJob.getType())[index] = c;

              String cmd = app.genCmds(appJob, index, cmdSuffix);
              containerQueue.offer(YarnContainer.builder().container(c)
                  .cmd(cmd)
                  .build());
            } else {
              LOG.info("release retry redundance container {}", allocContainers.get(i));
              rmClient.releaseAssignedContainer(allocContainers.get(i).getId());
            }
          }
        }
      }

      if (System.currentTimeMillis() - startTime > maxFailWaitSecs * 100) {
        throw new RuntimeException(appJob.getType() + " wait timeout");
      }
      Thread.sleep(200);
    }

    LOG.info("finish request container {}", appJob.getType());
    for (ContainerRequest cr : containerRequests) {
      rmClient.removeContainerRequest(cr);
    }
    priority++;
  }

  public Container getContainer(String type, int i) {
    if (appAllocContainers.containsKey(type)) {
      Container[] containers = appAllocContainers.get(type);
      if (i < containers.length) {
        return containers[i];
      }
    }
    return null;
  }

  private void logContainerStatus(String type, Container c, AppStatus status) {
    List<YarnFinishContainer> clist = finishContainers
        .computeIfAbsent(type, k -> Lists.newArrayList());
    clist.add(YarnFinishContainer.builder().container(c).status(status).build());
  }

  public Container successContainer(ContainerId cid) {
    if (containerInfoMap.containsKey(cid)) {
      YarnContainerInfo cinfo = containerInfoMap.get(cid);
      if (cinfo != null) {
        Container c = appAllocContainers.get(cinfo.getType())[cinfo.getIndex()];
        logContainerStatus(cinfo.getType(), c, AppStatus.SUCCESS);
        LOG.info("remove {}", c);
        appAllocContainers.get(cinfo.getType())[cinfo.getIndex()] = null;
        return c;
      }
    }
    return null;
  }


  public YarnContainerInfo getContainerInfo(ContainerId cid) {
    return containerInfoMap.get(cid);
  }


  public Resource convertAppResource(AppResource appRes) {
    Resource r = Records.newRecord(Resource.class);
    long memory = Math.min(appRes.getMemory(), maxResourceLimit.getMemory());
    r.setMemorySize(memory);
    long gpu = Math
        .min(appRes.getGpu(), maxResourceLimit.getResourceValue(ResourceInformation.GPU_URI));
    if (gpu > 0) {
      r.setResourceValue(ResourceInformation.GPU_URI, gpu);
    }
    r.setVirtualCores(Math.min(appRes.getVcore(), maxResourceLimit.getVirtualCores()));

    return r;
  }

  private List<ContainerRequest> getRequests(Resource r, int num) {
    List<ContainerRequest> containerRequests = Lists.newArrayList();
    Priority p = Records.newRecord(Priority.class);
    p.setPriority(this.priority);
    for (int i = 0; i < num; i++) {
      ContainerRequest containerRequest = new ContainerRequest(r, null, null, p);
      containerRequests.add(containerRequest);
    }

    return containerRequests;
  }


  public void setRmClient(
      AMRMClient<ContainerRequest> rmClient) {
    this.rmClient = rmClient;
  }

  public void setMaxResourceLimit(Resource maxResourceLimit) {
    this.maxResourceLimit = maxResourceLimit;
  }


  public void failContainer(ContainerId cid) {
    YarnContainerInfo cinfo = containerInfoMap.get(cid);

    Container failContainer = appAllocContainers.get(cinfo.getType())[cinfo.getIndex()];
    logContainerStatus(cinfo.getType(), failContainer, AppStatus.FAIL);
    LOG.info("container fail : {}", failContainer);
    appAllocContainers.get(cinfo.getType())[cinfo.getIndex()] = null;
  }
}
