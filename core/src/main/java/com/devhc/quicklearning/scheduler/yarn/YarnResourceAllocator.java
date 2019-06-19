package com.devhc.quicklearning.scheduler.yarn;

import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.AppResource;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.Resource;
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

  @Data
  @Builder
  public static class YarnContainerInfo {

    private int index;
    private String type;
  }


  @Builder
  @Data
  public static class YarnFinishContainer {

    private boolean success;
    private String type;
    private Container container;
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


  private Resource maxResourceLimit;

  public void initAllocator(List<AppJob> appJobList)
      throws IOException, YarnException, InterruptedException {
    // alloc app resource container for app defined
    for (AppJob appJob : appJobList) {
      allocAppContainers(appJob);
      priority++;
    }
    LOG.info("appAllocContainers:{} containerInfoMap:{}", appAllocContainers, containerInfoMap);


  }

  private void allocAppContainers(AppJob appJob)
      throws YarnException, IOException, InterruptedException {
    long startTime = System.currentTimeMillis();
    Resource r = convertAppResource(appJob.getResource());
    int num = appJob.getResource().getInstance();
    LOG.info("alloc {}", appJob.getType());
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
        Container[] appAllocContainerArr = new Container[num];
        for (int i = 0; i < allocFinished; i++) {
          if (i < num) {
            containerInfoMap.put(allocContainers.get(i).getId(), YarnContainerInfo.builder().
                index(i).type(appJob.getType()).build());
            appAllocContainerArr[i] = allocContainers.get(i);
          } else {
            ContainerId cid = allocContainers.get(allocFinished - i - 1).getId();
            rmClient.releaseAssignedContainer(cid);
            LOG.info("release redundance container {}", cid);
          }
        }
        this.appAllocContainers.put(appJob.getType(), appAllocContainerArr);
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

  private void logContainerStatus(String type, Container c, boolean success) {
    List<YarnFinishContainer> clist = finishContainers
        .computeIfAbsent(type, k -> Lists.newArrayList());
    clist.add(YarnFinishContainer.builder().container(c).success(success).build());
  }

  public Container successContainer(ContainerId cid) {
    if (containerInfoMap.containsKey(cid)) {
      YarnContainerInfo cinfo = containerInfoMap.get(cid);
      if (cinfo != null) {
        Container c = appAllocContainers.get(cinfo.getType())[cinfo.getIndex()];
        logContainerStatus(cinfo.getType(), c, true);
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
    r.setMemory(Math.min(appRes.getMemory(), maxResourceLimit.getMemory()));
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
    logContainerStatus(cinfo.getType(), failContainer, false);
    LOG.info("container fail : {}", failContainer);
    appAllocContainers.get(cinfo.getType())[cinfo.getIndex()] = null;
  }
}
