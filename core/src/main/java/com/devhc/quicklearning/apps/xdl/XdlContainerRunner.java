package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppContainerModule;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.docker.DockerManager;
import com.devhc.quicklearning.docker.DockerRunCommand;
import com.devhc.quicklearning.utils.CmdUtils;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.devhc.quicklearning.utils.JobUtils;
import com.devhc.quicklearning.utils.JsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.var;
import org.apache.commons.io.FileUtils;
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
  private String zkAddr;

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


  private int start() throws IOException {
    LOG.info("args:{}", args);
    String entry = args.getEntry();
    LOG.info("config json:{}", config);

    this.conf = new QuickLearningConf();

    Map<String, String> envs = System.getenv();
    this.user = envs.get(Environment.USER.toString());
    this.curDir = envs.get(Environment.PWD.toString());
    this.zkAddr = conf.get(QuickLearningConf.QUICK_LEARNING_ZK_ADDR);

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
    } else if (jobType.equals("scheduler")) {
      exitCode = runScriptInDocker();
    } else if (jobType.equals("ps")) {
      exitCode = runScriptInDocker();
    } else {
      throw new RuntimeException("not support " + jobType);
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

  private int runScriptInDocker() throws IOException {
    String jobType = args.getJobType();
    dockerManager.pull(config.docker_image);

    var jobRes = config.jobs.get(jobType);
    Map<String, String> envs = Maps.newHashMap();
    Map<String, String> volumes = Maps.newHashMap();


    // create entry wrap shell script
    String workerDir = "/" + Constants.APP_DIR;
    String appRun = "/run-docker-app.sh";
    String script = curDir + appRun;
    var sf = new File(script);
    ArrayList<String> lines = Lists.newArrayList();

    String uid = JobUtils.getCurUId();
    lines.add("set -x");
    lines.add("env");
    if (StringUtils.isNotEmpty(uid)) {
      lines.add(String.format("useradd -u %s %s", uid, user));
    }

    // create xdl json
    var xdlJson = XdlConfigConvertor.convert(config);
    String xdlJsonPath = "/app-xdl.json";
    var xdlJsonFile = new File(curDir + xdlJsonPath);
    String metaDir = "";
    xdlJson.auto_rebalance.meta_dir = metaDir;
    FileUtils.writeStringToFile(xdlJsonFile, JsonUtils.transformObjectToJson(xdlJson, true), "UTF-8");


    StringBuilder argsSb = new StringBuilder();
    argsSb.append(" --config ").append(xdlJsonPath)
        .append(" --zk_addr ")
        .append("zfs://" + zkAddr + "/ql/" + config.getJobType() + "/ps-plus/" + args.getAppId())
        .append(" --run_mode dist")
        .append(" --task_name ").append(args.getJobType())
        .append(" --task_index ").append(args.getWorkerIndex())
        .append(" --app_id ").append(args.getAppId());

    if (args.getArgs() != null) {
      argsSb.append(args.getArgs());
    }

    String cmdArg = argsSb.toString();
    lines.add(String
        .format("su %s -c 'source /etc/profile && cd %s && %s %s'", user, workerDir, args.getEntry(),
            cmdArg));


    FileUtils.writeLines(sf, lines);
    sf.setExecutable(true);

    volumes.put(curDir + "/" + Constants.APP_DIR, workerDir);
    volumes.put(curDir + "/" + Constants.QUICK_LEARNING_DIR, "/" + Constants.QUICK_LEARNING_DIR);
    volumes.put(curDir + xdlJsonPath, xdlJsonPath);
    volumes.put(script, workerDir + appRun);

//    String mainScript = wrapWithCreateUser(workerDir, jobRes.entry, args.getArgs());

    // xdl zk

    if (StringUtils.isNotEmpty(args.getCuda_device())) {
      envs.put("CUDA_VISIBLE_DEVICES", args.getCuda_device());
    }
//    envs.put("vp_method", "balance");
    envs.put("vp_method", "anneal");
    envs.put("meta_dir", metaDir);



    String runCommand = DockerRunCommand.builder()
        .rmMode(true)
        .image(config.docker_image)
        .cpuCores(jobRes.cpu_cores)
        .memory(jobRes.memory_m) //bytes
        .script(workerDir + appRun)
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
