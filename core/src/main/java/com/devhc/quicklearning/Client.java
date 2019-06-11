package com.devhc.quicklearning;

import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.utils.JobUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.hadoop.fs.FileStatus;
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
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.Records;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import static com.devhc.quicklearning.utils.JobUtils.TEMP_PERM;

@SpringBootApplication
public class Client {

  private final String localTmpDir;
  private Logger LOG = LoggerFactory.getLogger(Client.class);

  private final String user;
  @Option(name = "-t", aliases = "--type", usage = "job type")
  private String type = "xdl";

  @Option(name = "-q", aliases = "--queue", usage = "queue")
  private String queue = "default";


  @Option(name = "-c", aliases = "--config", usage = "job config")
  private String config = "config.json";

  @Option(name = "-e", aliases = "--env", usage = "env")
  private String envFile = "../ql.tar.gz";


  @Option(name = "-n", aliases = "--name", usage = "job name")
  private String jobName = "quicklearning test";

  @Option(name = "-d", aliases = "--deps", usage = "dep dirs")
  private String deps = ".";

  private ArrayList<String> dependentFiles = new ArrayList<>();


  private YarnClient yarnClient;
  private QuickLearningConf conf;
  private ApplicationId appId;
  private YarnClientApplication app;
  private ApplicationSubmissionContext ctx;
  private FileSystem fs;
  private final ApplicationArguments applicationArguments;
  private String appBasePath;

  @Autowired
  public Client(ApplicationArguments applicationArguments)
      throws IOException, YarnException, InterruptedException {
    this.applicationArguments = applicationArguments;
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(applicationArguments.getSourceArgs());
    } catch (CmdLineException e) {
      e.printStackTrace();
      parser.printUsage(System.err);
      System.exit(-1);
    }
    System.out.println(config);
    System.out.println(type);
    user = JobUtils.getCurUser();
    this.localTmpDir = createLocalTmpDir(user);

    this.dependentFiles.addAll(JobUtils.splitString(this.deps, ","));

    initYarnClient();
    createYarnApp();
    yarnClient.submitApplication(ctx);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        Client.this.shutdown();
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

  private void shutdown() {
    try {
      YarnApplicationState state = yarnClient.getApplicationReport(appId).getYarnApplicationState();
      LOG.info("app shutdown");
      if (state == YarnApplicationState.RUNNING || state == YarnApplicationState.ACCEPTED || state == YarnApplicationState.SUBMITTED) {
        LOG.info("kill applicationId:{}", appId);
        yarnClient.killApplication(appId);
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
    return appMasterEnv;
  }


  private void setupResource(Path resourcePath, LocalResource localResource) throws IOException {
    FileStatus fileStatus;
    fileStatus = fs.getFileStatus(resourcePath);

    localResource.setResource(JobUtils.fromURI(resourcePath.toUri(), null));
    localResource.setSize(fileStatus.getLen());
    localResource.setTimestamp(fileStatus.getModificationTime());
    localResource.setType(LocalResourceType.FILE);
    localResource.setVisibility(LocalResourceVisibility.PUBLIC);
  }

  private Map<String, LocalResource> setupResourceMap() throws IOException {
    FileSystem fs = FileSystem.get(conf);
    Map<String, LocalResource> resourceMap = new HashMap<String, LocalResource>();
//    for (String file : this.dependentFiles) {
////      if (file.endsWith(".jar")) {
//      String fileName = JobUtils.getName(file);
//      LocalResource defConf = Records.newRecord(LocalResource.class);
//      setupResource(new Path(this.appBasePath + fileName), defConf);
//      resourceMap.put(fileName, defConf);
////      }
//    }
    setResource(resourceMap, config);
    setResource(resourceMap, envFile);
    setResource(resourceMap, "bin/appMaster.sh");

    return resourceMap;
  }

  private void setResource(Map<String, LocalResource> resourceMap, String file) throws IOException {
    {
      String fileName = JobUtils.getName(file);
      LocalResource appXDLConfig = Records.newRecord(LocalResource.class);
      Path configPath = new Path(this.appBasePath + fileName);
      setupResource(configPath, appXDLConfig);
      resourceMap.put(fileName, appXDLConfig);
      fs.setPermission(configPath, TEMP_PERM);
    }
  }


  private void uploadDependentFiles(String basePath, ArrayList<String> dependentFileList)
      throws IOException {
    uploadLocalFileToHdfs(this.config, basePath);
    uploadLocalFileToHdfs(this.envFile, basePath);
    uploadLocalFileToHdfs("bin/appMaster.sh", basePath);
//    if (dependentFileList != null) {
//      uploadFilesToHdfs(dependentFileList, basePath);
//    }
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
    FileSystem fs = FileSystem.get(conf);
    File srcFile = new File(srcFilePath);
    if (srcFile.isDirectory()) {
      if (Files.isSymbolicLink(srcFile.toPath())) {
        java.nio.file.Path actualPath = Files.readSymbolicLink(srcFile.toPath());
        srcFile = actualPath.toFile();
      }
      String fileName = srcFile.getName();
      String dirName = srcFile.getParentFile().getAbsolutePath();
      String tarFileName = String.format("%s/%s.tar.gz", this.localTmpDir, fileName);
      JobUtils.runCmd(String.format("tar -czf %s -C %s ./%s", tarFileName, dirName, fileName));
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
      String fileName = JobUtils.getName(srcFilePath);
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
    uploadDependentFiles(appBasePath, this.dependentFiles);

    Map<String, LocalResource> resourceMap = setupResourceMap();
    String amStartCommand = "bash appMaster.sh  1>"
        + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout"
        + " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr";
    ContainerLaunchContext amContainer = setupApplicationMasterContainer(amStartCommand,
        resourceMap);

    Resource capability = Records.newRecord(Resource.class);
//    capability.setMemorySize(256);
    capability.setMemory(256);
    capability.setVirtualCores(1);

    ctx.setApplicationType(type);
    ctx.setApplicationName(jobName);
    ctx.setAMContainerSpec(amContainer);
    ctx.setResource(capability);
    ctx.setQueue(queue);

  }

  private void initYarnClient() throws IOException {
    yarnClient = YarnClient.createYarnClient();
    conf = new QuickLearningConf();
    yarnClient.init(conf);
    yarnClient.start();

    fs = FileSystem.get(conf);
  }


  public static void main(String[] args) {
    SpringApplication.run(Client.class, args);
  }
}