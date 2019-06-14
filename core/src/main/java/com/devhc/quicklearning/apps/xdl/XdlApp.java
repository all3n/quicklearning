package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.AppResource;
import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.utils.ConfigUtils;
import com.devhc.quicklearning.utils.Constants;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdlApp extends BaseApp {

  private static Logger LOG = LoggerFactory.getLogger(XdlApp.class);

  private final SchedulerConf conf;
  private ArrayList<AppJob> appJobs;
  private String user = "a";
  private String zkRoot = "af";
  private String zkConnectStr = "afsd";
  private String config;
  private String volumes = "fads";
  private String args;
  private String port ;

  public XdlApp(String config) {
    this.config = config;
    this.conf = ConfigUtils.parseJson(config, SchedulerConf.class);
    intAppContainers();
  }

  private void intAppContainers() {
    this.appJobs = Lists.newArrayList();
    // scheduler
    AppJob schedulerContainer = AppJob.builder().type("scheduler").
        resource(AppResource.builder().instance(1).memory(4096).vcore(4).build()).isWorker(false).build();
    appJobs.add(schedulerContainer);

    // ps
    AppJob psContainer = AppJob.builder().type("ps").
        resource(AppResource.builder().
            instance(conf.ps.instance_num).
            memory(conf.ps.memory_m).
            vcore(conf.ps.cpu_cores)
            .gpu(conf.ps.gpu_cores).build()).isWorker(false)
        .build();
    appJobs.add(psContainer);

    // worker
    AppJob workerContainer = AppJob.builder().type("worker").
        resource(AppResource.builder().
            instance(conf.worker.instance_num).
            memory(conf.worker.memory_m).
            vcore(conf.worker.cpu_cores)
            .gpu(conf.worker.gpu_cores).build())
          .isWorker(true)
          .build();

    appJobs.add(workerContainer);
  }

  @Override
  public List<AppJob> getAppContainerInfo() {
    return appJobs;
  }

  @Override
  public String genCmds(AppJob job, int index, String suffix) {
    return genCmd(user, String.valueOf(index),
        job.getType(),
        config,
        zkRoot, zkConnectStr, volumes, port, args) +" "+ suffix;
  }


  private String genCmd(String xdlUser, String workIndex, String workType, String config,
      String xdlZKRoot,
      String zookeeperConnStr, String volumeDirInHdfs, String port, String args) {
    String containerClazz = XdlContainerRunner.class.getName();
    String command =
//        "$JAVA_HOME/bin/java" + " -Xmx256M "
            "bash "+ Constants.YARN_START_SCRIPT+" "
            + containerClazz + " -c=" + config + " -j=" + workType
            + " -i=" + workIndex + " -z=" + zookeeperConnStr + " -r=" + xdlZKRoot + " -u="
            + xdlUser + " -v="
            + volumeDirInHdfs + " -cpuset=_CPU_LIST_" + " -cd=" + "GPU_LIST_PLACEHOLDER" + (
            port == null ? "" : (" -xp=" + port))
            + (StringUtils.isNotEmpty(args) ? " -args='" + args + "' " : "");
    LOG.info("container start command is {} ", command);
    return command;
  }
}
