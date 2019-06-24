package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppContainerModule;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.docker.DockerManager;
import com.devhc.quicklearning.docker.DockerRunCommand;
import com.devhc.quicklearning.utils.CmdUtils;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.utils.JobConfigJson;
import com.devhc.quicklearning.utils.JobUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.List;
import java.util.Map;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wanghuacheng this  script run in master alloc containers
 */
public class XdlContainerRunner {

  private static Logger LOG = LoggerFactory.getLogger(XdlContainerRunner.class);
  @Inject
  XdlArgs args;
  @Inject
  JobConfigJson config;
  private QuickLearningConf conf;
  private String user;
  private String curDir;

  @Inject
  DockerManager dockerManager;

  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      AppContainerModule<XdlArgs> appModule = new AppContainerModule<>(args, XdlArgs.class);
      moduleList.add(appModule);
      moduleList.add(new XdlModule(appModule.getAppArgs()));
      XdlContainerRunner runner = Guice.createInjector(moduleList)
          .getInstance(XdlContainerRunner.class);
      System.exit(runner.start());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private int start() {
    LOG.info("args:{}", args);
    String entry = args.getEntry();
    LOG.info("config json:{}", config);

    this.conf = new QuickLearningConf();

    Map<String, String> envs = System.getenv();
    this.user = envs.get(Environment.USER.toString());
    this.curDir = envs.get(Environment.PWD.toString());

    String jobType = args.getJobType();

    int exitCode = 0;
    if (jobType.equals("worker")) {
      if (StringUtils.isEmpty(entry)) {
        throw new RuntimeException("worker must has entry script");
      }
      // use docker run first
      if (StringUtils.isNotEmpty(config.docker_image)) {
        exitCode = runScriptInDocker();
      } else if (StringUtils.isNotEmpty(config.env)) {
        exitCode = runWithEnv(entry);
      } else {
        throw new RuntimeException("must has env or docker_images");
      }
    }

    return exitCode;
  }

  private int runWithEnv(String entry) {
    int exitCode;
    entry = entry.replace("{PYTHON}", Constants.ENV_DIR + "/bin/python")
        .replace("{APP_DIR}", Constants.APP_DIR);
    String argsPart = args.getArgs() == null ? "" : args.getArgs();
    String command = String.format("%s %s", entry, argsPart);
    LOG.info("command:{}", command);
    exitCode = CmdUtils.exeCmd(command, 1);
    return exitCode;
  }


  public String wrapWithCreateUser(String workDir, String entry, String args) {
    String uid = JobUtils.getCurUId();
    String jobCmd;
    if (StringUtils.isNotEmpty(uid)) {
      jobCmd = String.format("useradd -u %s %s;su %s -c 'source /etc/profile && cd %s && %s %s'",
          uid, user, user, workDir, entry, args == null ? "" : args);
    } else {
      jobCmd = entry;
    }
    return jobCmd;
  }

  private int runScriptInDocker() {
    String jobType = args.getJobType();
    dockerManager.pull(config.docker_image);

    var jobRes = config.jobs.get(jobType);
    Map<String, String> envs = Maps.newHashMap();
    Map<String, String> volumes = Maps.newHashMap();
    String workerDir = "/" + Constants.APP_DIR;

    volumes.put(curDir + "/" + Constants.APP_DIR, workerDir);
    volumes.put(curDir + "/" + Constants.QUICK_LEARNING_DIR, "/" + Constants.QUICK_LEARNING_DIR);

    String mainScript = wrapWithCreateUser(workerDir, jobRes.entry, args.getArgs());

    // xdl zk

    String runCommand = DockerRunCommand.builder()
        .rmMode(true)
        .image(config.docker_image)
        .cpuCores(jobRes.cpu_cores)
        .memory(jobRes.memory_m * 1024 * 1024) //bytes
        .script(mainScript)
        .entrypoint("bash")
        .envs(envs)
        .volumns(volumes)
        .exposeAll(false)
        .network("host")
        .build().toString();
    LOG.info("{}", runCommand);
    int exitCode = CmdUtils.exeCmd(runCommand, 1);
    return exitCode;
  }
}
