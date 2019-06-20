package com.devhc.quicklearning.scheduler;

import javax.inject.Singleton;

@Singleton
public class LocalScheduler extends BaseScheduler{

  @Override
  public boolean start() throws Exception {

    return false;
  }

  @Override
  public void stop() throws Exception {

  }

  @Override
  public void init() throws Exception {

  }
}
