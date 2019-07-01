package com.devhc.quicklearning.client;

import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.master.AppMaster;
import com.devhc.quicklearning.utils.CommonUtils;
import com.devhc.quicklearning.utils.JsonUtils;
import com.devhc.quicklearning.utils.Constants;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.devhc.quicklearning.utils.JobUtils;
import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("yarn")
public class YarnSubmitClient implements IClient{

  private String localTmpDir;
  private JobConfigJson jobConfig;
  private Logger LOG = LoggerFactory.getLogger(YarnSubmitClient.class);

  private String user;
  @Inject
  private ClientArgs args;

  private ArrayList<String> dependentFiles = new ArrayList<>();
  private YarnClient yarnClient;
  private QuickLearningConf conf;
  private ApplicationId appId;
  private YarnClientApplication app;
  private ApplicationSubmissionContext ctx;
  private FileSystem fs;
  private String appBasePath;
  private String workspaceDistFile;
  private String frameworkHdfs;
  private String frameworkLocal;
  public YarnSubmitClient(){

  }

  @Override
  public void init() throws IOException {
    System.out.println(IOUtils.toString(Objects.requireNonNull(getClass().getClassLoader().
        getResourceAsStream(Constants.BANNER)), Charsets.UTF_8));
    this.args = args;
    LOG.info("args {}", args);
    user = CommonUtils.getCurUser();
    this.localTmpDir = createLocalTmpDir(user);
    LOG.info("tmp:{}", localTmpDir);
    if (StringUtils.isNotEmpty(args.getDeps())) {
      this.dependentFiles.addAll(JobUtils.splitString(args.getDeps(), ","));
    }
    if (!(new File(args.getConfig())).exists()) {
      LOG.error("{} not exists", args.getConfig());
      System.exit(-1);
    }
    this.jobConfig = JsonUtils.parseJson(args.getConfig(), JobConfigJson.class);
  }

  private void shutdown() {
    try {
      YarnApplicationState state = yarnClient.getApplicationReport(appId).getYarnApplicationState();
      LOG.info("app shutdown");
      if (state == YarnApplicationState.RUNNING || state == YarnApplicationState.ACCEPTED
          || state == YarnApplicationState.SUBMITTED) {
        LOG.info("kill applicationId:{}", appId);
        yarnClient.killApplication(appId);
      }

      if (args.isCleanAndExit()) {
        FileSystem fs = FileSystem.get(conf);
        fs.delete(new Path(this.appBasePath), true);
        fs.close();
        FileUtils.deleteDirectory(new File(localTmpDir));
      }
    } catch (YarnException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String createLocalTmpDir(String uuid) {
    String parDir = "/tmp/quicklearning/";
    File parFile = new File(parDir);
    if (!parFile.exists()) {
      if (!parFile.mkdirs()) {
        throw new RuntimeException("Make dir failed: " + parDir);
      }
      if (!parFile.setWritable(true, false)) {
        throw new RuntimeException("Set dir writable failed: " + parDir);
      }
    }
    String dir = String
        .format("/tmp/quicklearning/%s/%s", uuid, String.valueOf(System.currentTimeMillis()));
    File file = new File(dir);
    if (!file.exists()) {
      if (!file.mkdirs()) {
        throw new RuntimeException("Make dir failed: " + dir);
      }
    }
    return dir;
  }

  private FinalApplicationStatus waitApplicationFinish(YarnClient yarnClient, ApplicationId appId)
      throws IOException, YarnException, InterruptedException {
    ApplicationReport appReport = yarnClient
        .getApplicationReport(appId);
    LOG.info(appReport.getTrackingUrl());
    while (
        appReport.getYarnApplicationState() != YarnApplicationState.FAILED &&
            appReport.getYarnApplicationState() != YarnApplicationState.FINISHED &&
            appReport.getYarnApplicationState() != YarnApplicationState.KILLED
    ) {
      Thread.sleep(1000L);
      appReport = yarnClient.getApplicationReport(appId);
    }
    return appReport.getFinalApplicationStatus();
  }

  private ContainerLaunchContext setupApplicationMasterContainer(String amStartCommand,
      Map<String, LocalResource> resourceMap) {
    ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);

    amContainer.setCommands(Collections.singletonList(amStartCommand));

    amContainer.setLocalResources(resourceMap);
    Map<String, String> appMasterEnv = setupAppMasterEnv();
    amContainer.setEnvironment(appMasterEnv);

    return amContainer;
  }

  private Map<String, String> setupAppMasterEnv() {
    Map<String, String> appMasterEnv = new HashMap<String, String>();
    String classPathSeparator = System.getProperty("path.separator");
    for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
        YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
      Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), c.trim(),
          classPathSeparator);
    }

    Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(),
        Environment.PWD.$() + File.separator + "*",
        classPathSeparator);
    appMasterEnv.put(Constants.BASE_HDFS_PATH, appBasePath);
    return appMasterEnv;
  }


  private Map<String, LocalResource> setupResourceMap() throws IOException {
    Map<String, LocalResource> resourceMap = new HashMap<String, LocalResource>();
    setResource(resourceMap, args.getConfig());
    setResource(resourceMap, frameworkHdfs, Constants.QUICK_LEARNING_DIR);
    setResource(resourceMap, workspaceDistFile, Constants.APP_DIR);
    if (StringUtils.isNotEmpty(jobConfig.env)) {
      setResource(resourceMap, jobConfig.env, Constants.ENV_DIR);
    }

    return resourceMap;
  }

  private void setResource(Map<String, LocalResource> resourceMap, String file) throws IOException {
    setResource(resourceMap, file, null);
  }

  private void setResource(Map<String, LocalResource> resourceMap, String file, String aliasName)
      throws IOException {
    {
      LOG.info("set resource file:{} alias:{}", file, aliasName);
      String fileName = CommonUtils.getName(file);
      Path resourcePath;
      if (file.startsWith("viewfs") || file.startsWith("hdfs")) {
        resourcePath = new Path(file);
      } else {
        resourcePath = new Path(this.appBasePath + fileName);
      }
      JobUtils.setResourceByPath(resourceMap, resourcePath, aliasName, conf);
    }
  }


  private void uploadDependentFiles(String basePath, ArrayList<String> dependentFileList)
      throws IOException {
    uploadLocalFileToHdfs(args.getConfig(), basePath);
    this.frameworkHdfs = uploadLocalFileToHdfs(frameworkLocal, basePath,
        Constants.QUICK_LEARNING_DIR);
    if (StringUtils.isNotEmpty(jobConfig.env) && jobConfig.env.startsWith("/")) {
      uploadLocalFileToHdfs(jobConfig.env, basePath);
    }

    this.workspaceDistFile = uploadLocalFileToHdfs(args.getWorkspace(), basePath,
        Constants.APP_DIR);
    if (dependentFileList != null) {
      uploadFilesToHdfs(dependentFileList, basePath);
    }
    LOG.info("Upload user files success.");
  }

  private void uploadFilesToHdfs(ArrayList<String> localFiles, String destPath) throws IOException {
    LOG.info("begin to upload files to hdfs");
    Iterator<String> iter = localFiles.iterator();
    while (iter.hasNext()) {
      String fileName = iter.next();
      uploadLocalFileToHdfs(fileName, destPath);
    }
    LOG.info("finish uploading files to hdfs");
  }

  private String uploadLocalFileToHdfs(String srcFilePath, String dstHdfsDir) throws IOException {
    return uploadLocalFileToHdfs(srcFilePath, dstHdfsDir, null);
  }

  private String uploadLocalFileToHdfs(String srcFilePath, String dstHdfsDir, String alias)
      throws IOException {
    FileSystem fs = FileSystem.get(conf);
    File srcFile = new File(srcFilePath);
    if (srcFile.isDirectory()) {
      if (Files.isSymbolicLink(srcFile.toPath())) {
        java.nio.file.Path actualPath = Files.readSymbolicLink(srcFile.toPath());
        srcFile = actualPath.toFile();
      }

      String fileName = StringUtils.isNotEmpty(alias) ? alias : srcFile.getName();
      String dirName = srcFile.getAbsolutePath();
      String tarFileName = String.format("%s/%s.tar.gz", this.localTmpDir, fileName);
      JobUtils.runCmd(String.format("tar -czf %s -C %s .", tarFileName, dirName, "."));
//      if (!volumes.isEmpty()) {
//        volumes += Constants.OPTION_VALUE_SEPARATOR;
//      }
//      volumes += fileName + ".tar.gz";
      Path dstFilePath = new Path(dstHdfsDir + "/" + fileName + ".tar.gz");
      fs.copyFromLocalFile(new Path(tarFileName), dstFilePath);
      fs.close();
      LOG.info("Upload file {} to {} success.", srcFilePath, dstFilePath.toString());
      return dstFilePath.toString();
    } else {
      fs.copyFromLocalFile(new Path(srcFilePath), new Path(dstHdfsDir));
      fs.close();
      String fileName = CommonUtils.getName(srcFilePath);
      String dstFilePath = Paths.get(dstHdfsDir, fileName).toString();
      LOG.info("Upload file {} to {} success.", srcFilePath, dstFilePath);
      return dstFilePath;
    }
  }


  private void createYarnApp() throws IOException, YarnException {
    this.app = yarnClient.createApplication();
    GetNewApplicationResponse res = app
        .getNewApplicationResponse();
    this.ctx = app.getApplicationSubmissionContext();
    this.appId = res.getApplicationId();
    this.appBasePath = JobUtils.genAppBasePath(conf, appId.toString(), user);

    this.frameworkLocal = this.localTmpDir + "/quicklearning" + System.currentTimeMillis();
    File frameworkLocalDir = new File(frameworkLocal);
    FileUtils.copyDirectory(new File(args.getFrameworkFile()), frameworkLocalDir);
    replaceVars(frameworkLocalDir, "/\\{APP_BASE_URL\\}", "/proxy/" + appId.toString());

    uploadDependentFiles(appBasePath, this.dependentFiles);
    Map<String, LocalResource> resourceMap = setupResourceMap();

    String appMasterClazz = AppMaster.class.getName();
    String amStartCommand = "bash " + Constants.QUICK_LEARNING_DIR
        + "/bin/" + Constants.YARN_START_SCRIPT + " " + appMasterClazz +
        " -w " + Environment.PWD.$$() + "/" + Constants.QUICK_LEARNING_DIR +
        "/web/appmaster -s yarn  -t=" + args.getType() + "  1>"
        + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
        + " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";

    ContainerLaunchContext amContainer = setupApplicationMasterContainer(amStartCommand,
        resourceMap);

    Resource capability = Records.newRecord(Resource.class);
//    capability.setMemorySize(256);
    capability.setMemory(256);
    capability.setVirtualCores(1);

    ctx.setApplicationType(args.getType());
    if (StringUtils.isNotEmpty(args.getJobName())) {
      ctx.setApplicationName(args.getJobName());
    } else if (StringUtils.isNotEmpty(jobConfig.job_name)) {
      ctx.setApplicationName(jobConfig.job_name);
    } else {
      throw new InvalidParameterException("job name must be set by param or config");
    }
    ctx.setAMContainerSpec(amContainer);
    ctx.setResource(capability);
    if (StringUtils.isNotEmpty(args.getQueue())) {
      ctx.setQueue(args.getQueue());
    } else if (StringUtils.isNotEmpty(jobConfig.scheduler_queue)) {
      ctx.setQueue(jobConfig.scheduler_queue);
    }
  }

  private void replaceVars(File file, String ori, String var) throws IOException {
    if (file.isFile()) {
      if (file.getName().endsWith(".js") || file.getName().endsWith(".html") || file.getName()
          .endsWith(".css")) {
        String contents = FileUtils.readFileToString(file).replaceAll(ori, var);
        FileUtils.writeStringToFile(file, contents);
      }
    } else if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        replaceVars(subFile, ori, var);
      }
    }
  }

  private void initYarnClient() throws IOException {
    yarnClient = YarnClient.createYarnClient();
    conf = new QuickLearningConf();
    yarnClient.init(conf);
    yarnClient.start();

    fs = FileSystem.get(conf);
  }

  @Override
  public void start() throws Exception {
    initYarnClient();
    createYarnApp();
    yarnClient.submitApplication(ctx);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        YarnSubmitClient.this.shutdown();
      }
    });
    FinalApplicationStatus appState = waitApplicationFinish(yarnClient, appId);

    System.out.println(String.format("%s:%s", appId.toString(), appState.toString()));
    if (appState == FinalApplicationStatus.SUCCEEDED) {
      System.exit(0);
    } else {
      System.exit(-1);
    }
  }


}
