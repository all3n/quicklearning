package com.devhc.quicklearning.apps;

import com.devhc.quicklearning.beans.JobConfigJson;
import java.util.List;
import lombok.Data;

@Data
public abstract class BaseApp {
  private String user;
  private String masterLink;
  private String appId;


  public abstract List<AppJob> getAppContainerInfo();
  public abstract String genCmds(AppJob job, int index, String suffix);
  public abstract JobConfigJson getConfig();
}
