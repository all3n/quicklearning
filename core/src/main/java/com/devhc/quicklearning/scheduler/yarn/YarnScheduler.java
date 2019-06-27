package com.devhc.quicklearning.scheduler.yarn;

import com.devhc.quicklearning.apps.AppContainer;
import com.devhc.quicklearning.apps.AppContainers;
import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.beans.JobMeta;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.master.MasterArgs;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.scheduler.yarn.YarnResourceAllocator.YarnContainerInfo;
import com.devhc.quicklearning.server.WebServer;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.devhc.quicklearning.utils.JobUtils;
import com.devhc.quicklearning.utils.JsonUtils;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wanghuacheng
 */


@Singleton
public class YarnScheduler extends BaseScheduler {

  private Logger LOG = LoggerFactory.getLogger(YarnScheduler.class);


  @Inject
  WebServer webServer;
  private QuickLearningConf conf;

  private AMRMClient<ContainerRequest> rmClient;
  private NMClient nmClient;
  private String applicationMasterHostname;
  private String webProxyBase;
  @Inject
  MasterArgs masterArgs;
  @Inject
  BaseApp app;
  @Inject
  JobConfigJson jobConfig;

  private YarnResourceAllocator yarnResourceAlloctor;
  private Map<String, String> appEnvs = Maps.newHashMap();
  private Map<String, LocalResource> localResources = Maps.newHashMap();
  private String appBasePath;
  private String cmdSuffix;

  private int totalWorkerNum;
  private int totalFinishNum = 0;
  private int successWorkerNum = 0;
  private int failWorkerNum = 0;
  private int failJobNum = 0;
  private String metaFile;
  private ContainerId appContainerId;
  private JobMeta meta;
  private Map<String, AppJob> typeJob = Maps.newTreeMap();
  private Thread launchContainerThread;
  private volatile boolean isRunning;


  public YarnResourceAllocator getYarnResourceAlloctor() {
    return yarnResourceAlloctor;
  }

  @Override
  public void init() throws Exception {
    Map<String, String> envs = System.getenv();
    this.conf = new QuickLearningConf();
    String appMasterCid = envs.get(Environment.CONTAINER_ID.toString());
    this.appContainerId = ContainerId.fromString(appMasterCid);
    this.appId = appContainerId.getApplicationAttemptId().getApplicationId().toString();
    appEnvs.put(Constants.ENV_APP_ID, appId);
    String metaDir = conf.get(QuickLearningConf.META_CONFIG) + "/" + appId;
    this.metaFile = metaDir + "/" + "meta.json";
    var fs = FileSystem.get(conf);
    fs.mkdirs(new Path(metaDir));

    String userName = envs.get(Environment.USER.toString());
//    LOG.info("User:{}", userName);
    app.setUser(userName);
    this.appBasePath = envs.get(Constants.BASE_HDFS_PATH);
    LOG.info("BasePath:{}", appBasePath);
    this.cmdSuffix = " 1> " + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2> "
        + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";

    if (envs.containsKey(ApplicationConstants.Environment.NM_HOST.toString())) {
      this.applicationMasterHostname = envs
          .get(Environment.NM_HOST.toString());
    }
    if (envs.containsKey(ApplicationConstants.APPLICATION_WEB_PROXY_BASE_ENV)) {
      this.webProxyBase = envs.get(ApplicationConstants.APPLICATION_WEB_PROXY_BASE_ENV);
    }

    String nmHttpPort = envs.get(Environment.NM_HTTP_PORT.toString());
    this.yarnResourceAlloctor = new YarnResourceAllocator(app);
    yarnResourceAlloctor.setCmdSuffix(cmdSuffix);
    yarnResourceAlloctor.setUserName(userName);

    app.setMasterLink(String
        .format("http://%s:%s/node/containerlogs/%s/%s", applicationMasterHostname, nmHttpPort,
            appMasterCid, userName));

    this.launchContainerThread = new Thread(() -> {
      while (isRunning) {
        try {
          var yc = yarnResourceAlloctor.getContainerQueue().take();
          if (yc.getContainer() != null) {
            LOG.info("launch {}", yc);
            launchContainer(yc.getContainer(), Collections.singletonList(yc.getCmd()));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      LOG.info("launch container thread stop");
    });

    // set meta
    meta = JobMeta.builder()
        .job(jobConfig)
        .jobId(appId)
        .status("RUNNING")
        .build();
    updateMeta(getMeta());
  }

  @Override
  public AppContainers getAppContainers() {
    var appContainers = yarnResourceAlloctor.getAppContainers();
    appContainers.setMasterContainer(AppContainer.builder().logLink(app.getMasterLink()).build());
    return appContainers;
  }

  @Override
  public JobMeta getMeta() {
    meta.setContainers(getAppContainers());
    return meta;
  }


  public void updateMeta(JobMeta meta) throws IOException {
    meta.setLastUpdate(System.currentTimeMillis());
    var fs = FileSystem.get(conf);
    var outs = fs.create(new Path(metaFile), true);
    try (var writer = new PrintWriter(outs)) {
      writer.print(JsonUtils.transformObjectToJson(meta, true));
      writer.flush();
    }
    fs.close();
  }


  @Override
  public boolean start() throws Exception {
    isRunning = true;
    initClients();
    register();
    initResources();
    initAppEnvs();
    List<AppJob> apps = app.getAppContainerInfo();
    this.totalWorkerNum = 0;
    for (AppJob appJob : apps) {
      int instanceNum = appJob.getResource().getInstance();
      if (appJob.isWorker()) {
        totalWorkerNum += instanceNum;
      }
      typeJob.put(appJob.getType(), appJob);
    }
    launchContainerThread.start();
    yarnResourceAlloctor.initAllocator(typeJob);
    updateMeta(getMeta());
    boolean isSuccess = waitJobFinished();
    this.processExit(isSuccess);
    return isSuccess;
  }

  private void processExit(boolean isSuccess) throws IOException, YarnException {
    if (isSuccess) {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "app success", "");
    } else {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.FAILED, "app fail", "");
    }
  }

  private boolean waitJobFinished() throws Exception {
    this.successWorkerNum = 0;
    float deltaResponse = 1.0f / totalWorkerNum;
    float responseId = 0.01f;
    boolean success = true;
    while (successWorkerNum < totalWorkerNum) {
      this.processResponse(rmClient.allocate(responseId).getCompletedContainersStatuses());
      responseId = deltaResponse * successWorkerNum;
      Thread.sleep(1000);
      LOG.info("successWorkerNum:{}", successWorkerNum);
      if (isJobShouldFailExit()) {
        success = false;
        break;
      }
    }

    var meta = getMeta();
    meta.setStatus(success ? "SUCCESS" : "FAIL");
    updateMeta(meta);
    LOG.info("success:{} fail:{} finish:{} total:{} job finished!", successWorkerNum, failWorkerNum,
        totalFinishNum, totalWorkerNum);
    return success;
  }


  public boolean isJobShouldFailExit() {
    boolean shouldExit = false;
    // worker fail rate is high
    if ((failWorkerNum / (float) totalWorkerNum) > 0.75f) {
      LOG.info("fail rate > 0.75");
      shouldExit = true;
    }

    if (failJobNum > jobConfig.max_failover_times) {
      LOG.info("fail job num > {} max failover times", jobConfig.max_failover_times);
      shouldExit = true;
    }
    return shouldExit;
  }

  private void processResponse(List<ContainerStatus> completedContainersStatuses)
      throws Exception {
    if (completedContainersStatuses.size() == 0) {
      return;
    }
    for (ContainerStatus cs : completedContainersStatuses) {
      totalFinishNum += 1;
      ContainerId cid = cs.getContainerId();

      YarnContainerInfo cinfo = yarnResourceAlloctor.getContainerInfo(cid);
      if (cs.getExitStatus() == 0) {
        // only worker has exit 0
        yarnResourceAlloctor.successContainer(cid);
        rmClient.releaseAssignedContainer(cid);
      } else {
        yarnResourceAlloctor.failContainer(cid);
        if (failJobNum < jobConfig.max_failover_times) {
          yarnResourceAlloctor.retryJob(cinfo);

//          String cmd = app.genCmds(, ci, cmdSuffix);
          String cmd = "";
          Container container = yarnResourceAlloctor
              .getContainer(cinfo.getType(), cinfo.getIndex());
//          LOG.info("launch {} {} container:{}", ci, appJob, container);
          if (container != null) {
            launchContainer(container, Collections.singletonList(cmd));
          }

          failJobNum += 1;
        }
      }

      if (cinfo.getType().equals("worker")) {
        if (cs.getExitStatus() == 0) {
          successWorkerNum += 1;
        } else {
          failWorkerNum += 1;
        }
      }
      updateMeta(getMeta());
    }
  }

  private void initAppEnvs() {
    String javaPathSeparator = System.getProperty("path.separator");
    Apps.addToEnvironment(appEnvs, ApplicationConstants.Environment.CLASSPATH.name(),
        ApplicationConstants.Environment.PWD.$() + File.separator + "*", javaPathSeparator);

    for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
        YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
      Apps.addToEnvironment(appEnvs, ApplicationConstants.Environment.CLASSPATH.name(),
          c.trim(),
          javaPathSeparator);
    }

    LOG.info("JAVA CLASS_PATH is " + System.getProperty("java.class.path"));
  }

  private void launchContainer(Container container, List<String> cmds)
      throws IOException, YarnException {
    ContainerLaunchContext ctx = Records.newRecord(ContainerLaunchContext.class);
    ctx.setCommands(cmds);
    ctx.setEnvironment(appEnvs);
    ctx.setLocalResources(localResources);
    nmClient.startContainer(container, ctx);
  }

  /**
   * set container file from base path
   */
  private void initResources() throws IOException {
    FileSystem fs = FileSystem.get(conf);

    if (StringUtils.isNotEmpty(jobConfig.env) && (
        jobConfig.env.startsWith("viewfs://") ||
            jobConfig.env.startsWith("hdfs://")
    )
    ) {
      JobUtils.setResourceByPath(localResources, new Path(jobConfig.env), Constants.ENV_DIR, conf);
    }

    FileStatus[] localResArr = fs.listStatus(new Path(appBasePath));
    for (FileStatus localRes : localResArr) {
      if (localRes.isFile()) {
        Path lrpath = localRes.getPath();
        JobUtils.setResourceByPath(localResources, lrpath, null, conf);
      }
    }
    fs.close();
  }


  public void register() throws Exception {
    String webUrl = String.format("http://%s:%s", applicationMasterHostname, webServer.getPort());
    LOG.info("start register app master:{}", webUrl);
    RegisterApplicationMasterResponse response = rmClient
        .registerApplicationMaster(webServer.getHost(),
            webServer.getPort(), webUrl);
    yarnResourceAlloctor.setMaxResourceLimit(response.getMaximumResourceCapability());

    LOG.info("MaximumResourceCapability res:{}", response.getMaximumResourceCapability());

  }


  private void initClients() {
    this.rmClient = AMRMClient.createAMRMClient();
    this.yarnResourceAlloctor.setRmClient(rmClient);
    rmClient.init(conf);
    rmClient.start();
    LOG.info("RMClient client started.");

    nmClient = NMClient.createNMClient();
    nmClient.init(conf);
    nmClient.start();
    LOG.info("NodeManager client started.");

  }

  @Override
  public void stop() throws Exception {
    isRunning = false;
    LOG.info("stop yarn scheduler");
    updateMeta(getMeta());
    if (rmClient != null) {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
      rmClient.stop();
      rmClient = null;
    }
    if (nmClient != null) {
      nmClient.close();
      nmClient = null;
    }
  }


}
