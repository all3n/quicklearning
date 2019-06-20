package com.devhc.quicklearning.client;

import com.devhc.quicklearning.utils.JobUtils;
import com.google.inject.AbstractModule;

public class ClientModules extends AbstractModule {

  private final ClientArgs clientArgs;

  public ClientModules(String args[]) {
    clientArgs = JobUtils.parseArgument(new ClientArgs(), args);
  }

  @Override
  protected void configure() {
    bind(ClientArgs.class).toInstance(clientArgs);
  }
}
