package com.devhc.quicklearning.apps;

import com.devhc.quicklearning.utils.JobConfigJson;
import java.util.List;

public abstract class BaseApp {
  public abstract List<AppJob> getAppContainerInfo();
  public abstract String genCmds(AppJob job, int index, String suffix);
  public abstract JobConfigJson getConfig();
}
