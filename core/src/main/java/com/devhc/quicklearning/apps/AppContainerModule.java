package com.devhc.quicklearning.apps;

import com.devhc.quicklearning.utils.JobUtils;
import com.google.inject.AbstractModule;
import lombok.Getter;

public class AppContainerModule<T > extends AbstractModule {
  @Getter
  private T appArgs;
  private Class<T> argClazz;

  public AppContainerModule(String args[], Class<T> argClazz){
    try {
      this.argClazz = argClazz;
      appArgs = JobUtils.parseArgument(argClazz.newInstance(), args);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void configure() {
    bind(argClazz).toInstance(appArgs);
  }
}
