package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppContainerModule;
import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.docker.DockerManager;
import com.devhc.quicklearning.docker.DockerRunCommand;
import com.devhc.quicklearning.utils.CmdUtils;
import com.devhc.quicklearning.utils.CommonUtils;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.devhc.quicklearning.utils.JobUtils;
import com.devhc.quicklearning.utils.JsonUtils;
import com.google.common.base.Preconditions;
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
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
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
  private String hdfsBase;
  private String dockerContainerId;
  protected volatile boolean stoped = false;



  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      AppContainerModule<XdlArgs> appModule = new AppContainerModule<>(args, XdlArgs.class);
      moduleList.add(appModule);
      moduleList.add(new XdlModule(appModule.getAppArgs()));
      XdlContainerRunner runner = Guice.createInjector(moduleList)
          .getInstance(XdlContainerRunner.class);

      Runtime.getRuntime().addShutdownHook(new Thread(runner::shutdown));

      System.exit(runner.start());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private synchronized void shutdown() {
    if (stoped) {
      return;
    }
    dockerManager.stop(dockerContainerId);
    LOG.info("stop {}", dockerContainerId);
    stoped = true;
  }


  private int start() throws IOException {
    LOG.info("args:{}", args);
    String entry = args.getEntry();
    LOG.info("config json:{}", config);

    this.conf = new QuickLearningConf();
    this.hdfsBase = conf.getTrimmed(CommonConfigurationKeysPublic.FS_DEFAULT_NAME_KEY);

    Map<String, String> envs = System.getenv();
    this.user = envs.get(Environment.USER.toString());
    this.curDir = envs.get(Environment.PWD.toString());
    this.zkAddr = conf.getTrimmed(QuickLearningConf.QUICK_LEARNING_ZK_ADDR);
    Preconditions.checkNotNull(zkAddr, "zk addr must not be null");

    int exitCode = 0;
    // use docker run first
    if (StringUtils.isNotEmpty(config.docker_image)) {
      exitCode = runScriptInDocker();
    } else if (StringUtils.isNotEmpty(config.env)) {
      exitCode = runWithEnv(entry);
    } else {
      throw new RuntimeException("must has env or docker_images");
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




  private int runScriptInDocker() throws IOException {
    String jobType = args.getJobType();
    Preconditions.checkNotNull(config.docker_image, "docker must not be null");
    dockerManager.pull(config.docker_image);

//    var jobRes = config.jobs.get(jobType);

    var jobRes = XdlApp.getTypeJob(config, jobType).getResource();

    Preconditions.checkNotNull(jobRes, jobType + " res is null");
    Map<String, String> envs = Maps.newHashMap();
    Map<String, String> volumes = Maps.newHashMap();

    // create entry wrap shell script
    String workerDir = "/" + Constants.APP_DIR;
    String appRun = "/run-docker-app.sh";
    String script = curDir + appRun;
    var sf = new File(script);
    ArrayList<String> lines = Lists.newArrayList();

    String uid = CommonUtils.getCurUId();
    lines.add("set -x");
    lines.add("env");
    if (StringUtils.isNotEmpty(uid)) {
      lines.add(String.format("useradd -u %s %s", uid, user));
    }

    // create xdl json
    var xdlJson = XdlConfigConvertor.convert(config);
    String xdlJsonPath = "/app-xdl.json";
    var xdlJsonFile = new File(curDir + xdlJsonPath);
    String metaDir = hdfsBase + "user/" + user + "/meta/" + args.getAppId();

    xdlJson.auto_rebalance.meta_dir = metaDir;
    FileUtils
        .writeStringToFile(xdlJsonFile, JsonUtils.transformObjectToJson(xdlJson, true), "UTF-8");

    String ckptDir = xdlJson.checkpoint.output_dir;

    StringBuilder argsSb = new StringBuilder();
    argsSb.append(" --config ").append(xdlJsonPath)
        .append(" --zk_addr ").append("zfs://").append(zkAddr).append("/ql/")
        .append(config.getJobType()).append("/ps-plus/").append(args.getAppId())
        .append(" --run_mode dist")
        .append(" --task_name ").append(args.getJobType())
        .append(" --task_index ").append(args.getWorkerIndex())
        .append(" --app_id ").append(args.getAppId())
        .append(" --ckpt_dir ").append(ckptDir);

    if (args.getArgs() != null) {
      argsSb.append(args.getArgs());
    }

    String cmdArg = argsSb.toString();
    lines.add(String
        .format("su %s -c 'source /etc/profile && cd %s && %s %s'", user, workerDir,
            args.getEntry(),
            cmdArg));

    FileUtils.writeLines(sf, lines);
    sf.setExecutable(true);

    volumes.put(curDir + "/" + Constants.APP_DIR, workerDir);
    volumes.put(curDir + "/" + Constants.QUICK_LEARNING_DIR, "/" + Constants.QUICK_LEARNING_DIR);
    volumes.put(curDir + xdlJsonPath, xdlJsonPath);
    volumes.put(script, workerDir + appRun);

    if (StringUtils.isNotEmpty(args.getCuda_device())) {
      envs.put("CUDA_VISIBLE_DEVICES", args.getCuda_device());
    }
    envs.put("vp_method", "anneal"); // balance or anneal
    envs.put("meta_dir", metaDir);
    dockerContainerId =
        "QL-" + config.getJobType() + "-" + jobType +  args.getWorkerIndex() + "-" + args
            .getAppId();

    String runCommand = DockerRunCommand.builder()
        .rmMode(true)
        .image(config.docker_image)
        .cpuCores(jobRes.getVcore())
        .memory(jobRes.getMemory()) //bytes
        .script(workerDir + appRun)
        .entrypoint("bash")
        .envs(envs)
        .name(dockerContainerId)
        .volumns(volumes)
        .exposeAll(false)
        .network("host")
        .build().toString();
    LOG.info("{}", runCommand);
    return CmdUtils.exeCmd(runCommand, 1);
  }
}
