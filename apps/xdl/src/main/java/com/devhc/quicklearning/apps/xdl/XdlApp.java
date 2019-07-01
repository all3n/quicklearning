package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppJob;
import com.devhc.quicklearning.apps.AppResource;
import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.master.MasterArgs;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Singleton
@Named("xdl")
public class XdlApp extends BaseApp {

  private static Logger LOG = LoggerFactory.getLogger(XdlApp.class);

  private ArrayList<AppJob> appJobs;
  private String config;
  @Inject
  BaseScheduler scheduler;
  @Inject
  MasterArgs masterArgs;
  @Inject
  private JobConfigJson conf;

  public static AppResource DEFAULT_RES = AppResource.builder()
      .instance(1)
      .memory(4096).vcore(4)
      .build();


  @Override
  public void init(){
    config = masterArgs.getConfigFile();
    intAppContainers();
  }

  public static AppJob getTypeJob(JobConfigJson conf, String type) {
    AppJob aj;
    boolean isWorker = true;
    if (type.equals("scheduler") || type.equals("ps")) {
      isWorker = false;
    }
    if (conf.jobs != null && conf.jobs.containsKey(type)) {
      var r = conf.jobs.get(type);
      aj = AppJob.builder().type(type)
          .resource(AppResource.builder()
              .vcore(r.cpu_cores).gpu(r.gpu_cores).memory(r.memory_m).instance(r.instance_num)
              .build()).isWorker(isWorker)
          .entry(r.entry)
          .build();
    } else {
      LOG.info("{} res not set,use default {}", type, DEFAULT_RES);
      aj = AppJob.builder().type(type).
          resource(DEFAULT_RES).isWorker(isWorker)
          .build();
    }

    return aj;
  }

  private void intAppContainers() {
    this.appJobs = Lists.newArrayList();
    // scheduler
    appJobs.add(getTypeJob(conf, "scheduler"));
    appJobs.add(getTypeJob(conf, "ps"));
    appJobs.add(getTypeJob(conf, "worker"));
  }

  @Override
  public List<AppJob> getAppContainerInfo() {
    return appJobs;
  }

  @Override
  public String genCmds(AppJob job, int index, String suffix) {
    // if job has entry use job first,else globa script
    String entry = job.getEntry() == null ? conf.getScript() : job.getEntry();
    entry = "python " + entry;

    return genCmd(job.getResource(), getUser(), String.valueOf(index),
        job.getType(),
        config,
        "", entry) + " " + suffix;
  }

  @Override
  public JobConfigJson getConfig() {
    return conf;
  }


  private String genCmd(AppResource resource, String xdlUser,
      String workIndex, String workType, String config,
      String args, String entry) {
    String containerClazz = XdlContainerRunner.class.getName();
    String cudaArgs = resource.getGpu() > 0 ? " -cd=" + "GPU_LIST_PLACEHOLDER" : "";
    String command =
        "bash " + Constants.QUICK_LEARNING_DIR + "/bin/" + Constants.YARN_START_SCRIPT + " "
            + containerClazz + " -c=" + config + " -j=" + workType
            + " -i=" + workIndex + " -u="
            + xdlUser
            + " -cpuset=_CPU_LIST_" + cudaArgs
            + " --appId " + scheduler.getAppId()
            + " --entry '" + entry + "'"
            + (StringUtils.isNotEmpty(args) ? " -args='" + args + "' " : "");
    LOG.info("container start command is {} ", command);
    return command;
  }
}
