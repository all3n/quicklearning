package com.devhc.quicklearning.scheduler;


import com.devhc.quicklearning.apps.AppContainers;
import com.devhc.quicklearning.beans.JobMeta;

public abstract class BaseScheduler {

  public boolean start() throws Exception {
    return false;
  }

  public void stop() throws Exception {
  }

  public void init() throws Exception {
  }

  public AppContainers getAppContainers() {
    return AppContainers.builder().build();
  }

  public JobMeta getMeta() {
    return JobMeta.builder().build();
  }

  public String getAppId() {
    return "appid";
  }
}
