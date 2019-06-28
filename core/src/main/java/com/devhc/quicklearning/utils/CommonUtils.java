package com.devhc.quicklearning.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ServiceLoader;
import javax.inject.Named;
import javax.naming.Name;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wanghch on 2019/6/27.
 */
public class CommonUtils {

  private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);

  public static String getName(String filePath) {
    return new File(filePath).getName();
  }

  public static String getCurUser() {
    return System.getProperty("user.name");
  }


  public static String getCurUId() {
    String uid = "";
    try {
      String curUser = getCurUser();
      Process pr2 = Runtime.getRuntime().exec("id -u " + curUser);
      BufferedReader input2 = new BufferedReader(new InputStreamReader(pr2.getInputStream()));
      uid = StringUtils.strip(input2.readLine());
      LOG.info("get current user " + curUser + " uid:" + uid);
    } catch (Exception e) {
      LOG.error(e.toString());
    }
    return uid;
  }

  public static String convertArchiveName(String fileName) {
    return fileName.substring(0, fileName.indexOf("."));
  }


  public static  Class genClass(Class baseClazz, String name, String suffix) {
    String clazzFullName =
        baseClazz.getPackage().getName() + "." + name + "." + StringUtils.capitalize(name) + suffix;
    try {
      Class targetClazz = Class.forName(clazzFullName);
      return targetClazz;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }


  public static <T> T getServiceByName(ServiceLoader<T> loader,String name){
    var iter = loader.iterator();
    while(iter.hasNext()){
      var service = iter.next();
      var nameAno = service.getClass().getAnnotation(Named.class);
      if(nameAno !=null && name.equals(nameAno.value())){
        return service;
      }
    }
    return null;
  }
}
