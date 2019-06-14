package com.devhc.quicklearning.apps;

import java.util.List;

public abstract class BaseApp {
  public abstract List<AppJob> getAppContainerInfo();
  public abstract String genCmds(AppJob job, int index, String suffix);
}
