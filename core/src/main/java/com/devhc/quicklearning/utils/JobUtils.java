package com.devhc.quicklearning.utils;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobUtils {

  private static Logger LOG = LoggerFactory.getLogger(JobUtils.class);
  public static final FsPermission TEMP_PERM = new FsPermission("777");
  public static final String HDFS_USER_ROOT = "/user";
  public static final String HDFS_XDL_WORK_DIR = ".quicklearning";
  public static final String HDFS_PYTHON_DIR = "python";

  public static String getName(String filePath) {
    return new Path(filePath).getName();
  }

  public static String getCurUser() {
    return System.getProperty("user.name");
  }


  public static String genAppBasePath(
      Configuration conf, String appId, String user) throws IOException {
    String basePath = String.format("%s/%s/%s/%s",
        HDFS_USER_ROOT, user, HDFS_XDL_WORK_DIR, appId);
    if (basePath.startsWith("/")) {
      basePath = basePath.substring(1);
    }
    String defaultFs = conf.get("fs.defaultFS");
    basePath = defaultFs + basePath + "/";
    FileSystem fs = FileSystem.get(conf);
    Path appStagePath = new Path(basePath);
    if (!fs.exists(appStagePath)) {
      fs.mkdirs(appStagePath, TEMP_PERM);
      fs.setPermission(appStagePath, TEMP_PERM);
      LOG.info("Path:[{}] not exists, create success.", appStagePath);
    }
    return basePath;
  }


  public static ArrayList<String> splitString(String value, String seperator) {
    if (value.isEmpty()) {
      return Lists.newArrayList();
    }
    return new ArrayList<String>(Arrays.asList(value.split(seperator)));
  }


  public static void runCmd(String cmd) {
    String[] cmdArr = cmd.split(" ");
    Runtime run = Runtime.getRuntime();
    try {
      Process p = run.exec(cmdArr);
      p.waitFor();
      if (p.exitValue() == 0) {
        LOG.info("Run cmd [" + cmd + "] success.");
        return;
      } else {
        BufferedReader read = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String result = read.readLine();
        throw new RuntimeException("Run cmd [" + cmd + "] failed, " + result);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }


  public static URL fromURI(URI uri, Configuration conf) {
    URL url =
        RecordFactoryProvider.getRecordFactory(conf).newRecordInstance(
            URL.class);
    if (uri.getHost() != null) {
      url.setHost(uri.getHost());
    }
    if (uri.getUserInfo() != null) {
      url.setUserInfo(uri.getUserInfo());
    }
    url.setPort(uri.getPort());
    url.setScheme(uri.getScheme());
    url.setFile(uri.getPath());
    return url;
  }


  public static <T> T parseArgument(T obj, String args[]) {
    CmdLineParser parser = new CmdLineParser(obj);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      e.printStackTrace();
      parser.printUsage(System.err);
      System.exit(-1);
    }
    return obj;
  }
}
