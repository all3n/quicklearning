package com.devhc.quicklearning.apps;

import com.devhc.quicklearning.utils.JobConfigJson;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public abstract class BaseApp {
  @Setter
  @Getter
  private String user;
  public abstract List<AppJob> getAppContainerInfo();
  public abstract String genCmds(AppJob job, int index, String suffix);
  public abstract JobConfigJson getConfig();
}
