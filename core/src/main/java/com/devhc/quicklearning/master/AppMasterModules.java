package com.devhc.quicklearning.master;

import com.devhc.quicklearning.controllers.IndexController;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.scheduler.LocalScheduler;
import com.devhc.quicklearning.scheduler.YarnScheduler;
import com.devhc.quicklearning.utils.JobUtils;
import com.google.inject.AbstractModule;

public class AppMasterModules extends AbstractModule {


  private final MasterArgs masterArgs;

  public AppMasterModules(String args[]) {
    masterArgs = JobUtils.parseArgument(new MasterArgs(), args);
  }

  @Override
  protected void configure() {
    install(new AppServletModule());
    bind(MasterArgs.class).toInstance(masterArgs);




    if (masterArgs.getScheduler().equals("yarn")) {
      bind(BaseScheduler.class).to(YarnScheduler.class);
    } else {
      bind(BaseScheduler.class).to(LocalScheduler.class);
    }
  }
}
