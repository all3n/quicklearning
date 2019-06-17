package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.AppResource;
import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.master.MasterArgs;
import com.devhc.quicklearning.utils.ConfigUtils;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.utils.JobConfigJson;
import com.devhc.quicklearning.utils.JobConfigJson.RoleResource;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdlApp extends BaseApp {

  private static Logger LOG = LoggerFactory.getLogger(XdlApp.class);

  @Inject
  private JobConfigJson conf;
  private ArrayList<AppJob> appJobs;
  private String user = "a";
  private String zkRoot = "af";
  private String zkConnectStr = "afsd";
  private String config;
  private String volumes = "fads";
  private String args;
  private String port;

  @Inject
  public XdlApp(MasterArgs args, JobConfigJson conf) {
    this.config = args.getConfigFile();
    this.conf = conf;
    intAppContainers();
  }

  private void intAppContainers() {
    this.appJobs = Lists.newArrayList();
    // scheduler
    AppJob schedulerContainer = AppJob.builder().type("scheduler").
        resource(AppResource.builder().instance(1).memory(4096).vcore(4).build()).isWorker(false)
        .build();
    appJobs.add(schedulerContainer);

    if (conf.jobs != null && conf.jobs.containsKey("worker")) {
      if(conf.jobs.containsKey("ps")) {
        RoleResource ps = conf.jobs.get("ps");
        // ps
        AppJob psContainer = AppJob.builder().type("ps").
            resource(AppResource.builder().
                instance(ps.instance_num).
                memory(ps.memory_m).
                vcore(ps.cpu_cores)
                .gpu(ps.gpu_cores).build()).isWorker(false)
            .build();
        appJobs.add(psContainer);
      }
      RoleResource worker = conf.jobs.get("worker");
      // worker
      AppJob workerContainer = AppJob.builder().type("worker").
          resource(AppResource.builder().
              instance(worker.instance_num).
              memory(worker.memory_m).
              vcore(worker.cpu_cores)
              .gpu(worker.gpu_cores)
              .build())
          .entry(worker.entry)
          .isWorker(true)
          .build();

      appJobs.add(workerContainer);
    } else {
      throw new RuntimeException("conf must has jobs, jobs must has ps and worker");
    }
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
        zkRoot, zkConnectStr, volumes, port, args, job.getEntry()) + " " + suffix;
  }


  private String genCmd(String xdlUser, String workIndex, String workType, String config,
      String xdlZKRoot,
      String zookeeperConnStr, String volumeDirInHdfs, String port, String args, String entry) {
    String containerClazz = XdlContainerRunner.class.getName();
    String command =
//        "$JAVA_HOME/bin/java" + " -Xmx256M "
        "bash " + Constants.QUICK_LEARNING_DIR + "/bin/" + Constants.YARN_START_SCRIPT + " "
            + containerClazz + " -c=" + config + " -j=" + workType
            + " -i=" + workIndex + " -z=" + zookeeperConnStr + " -r=" + xdlZKRoot + " -u="
            + xdlUser + " -v="
            + volumeDirInHdfs + " -cpuset=_CPU_LIST_" + " -cd=" + "GPU_LIST_PLACEHOLDER" + (
            port == null ? "" : (" -xp=" + port))
            + " --entry '" + entry + "'"
            + (StringUtils.isNotEmpty(args) ? " -args='" + args + "' " : "");
    LOG.info("container start command is {} ", command);
    return command;
  }
}
