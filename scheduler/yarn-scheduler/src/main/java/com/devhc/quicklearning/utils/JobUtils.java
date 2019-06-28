package com.devhc.quicklearning.utils;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.URL;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.util.Records;
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

  public static void setResourceByPath(Map<String, LocalResource> localResources, Path lrpath,
      String alias,
      Configuration conf)
      throws IOException {
//    Path lrpath = localRes.getPath();
    FileSystem fs = FileSystem.get(conf);

    URI ruri = lrpath.toUri();
    Path linkNamePath = new Path((null == ruri.getFragment())
        ? lrpath.getName()
        : ruri.getFragment());
    if (linkNamePath.isAbsolute()) {
      throw new IllegalArgumentException("Resource name must relative");
    }
    FileStatus localRes = fs.getFileLinkStatus(lrpath);
    String linkName = linkNamePath.toUri().getPath();
    LOG.info("name:{}, container res:{} {}", lrpath.getName(), lrpath.toString());
    LocalResource lr = Records.newRecord(LocalResource.class);
    lr.setResource(JobUtils.fromURI(localRes.getPath().toUri(), conf));
    lr.setSize(localRes.getLen());
    lr.setTimestamp(localRes.getModificationTime());
    if (lrpath.getName().endsWith(".tar") ||
        lrpath.getName().endsWith(".gz") ||
        lrpath.getName().endsWith(".zip")) {
      lr.setType(LocalResourceType.ARCHIVE);
    } else {
      lr.setType(LocalResourceType.FILE);
    }
    if (StringUtils.isNotEmpty(alias)) {
      linkName = alias;
    } else if (lr.getType() == LocalResourceType.ARCHIVE) {
      int dIndex = linkName.indexOf(".");
      linkName = linkName.substring(0, dIndex);
    }
    lr.setVisibility(LocalResourceVisibility.PUBLIC);
    localResources.put(linkName, lr);
    fs.close();
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







}
