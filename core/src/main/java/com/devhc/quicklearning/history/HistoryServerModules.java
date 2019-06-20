package com.devhc.quicklearning.history;

import com.devhc.quicklearning.history.controllers.IndexController;
import com.devhc.quicklearning.server.jersey.JerseyModule;
import com.devhc.quicklearning.server.jersey.configuration.JerseyConfiguration;
import com.devhc.quicklearning.utils.JobUtils;
import com.google.inject.AbstractModule;

public class HistoryServerModules extends AbstractModule {

  private final HistoryServerArgs args;

  public HistoryServerModules(String args[]) {
    this.args = JobUtils.parseArgument(new HistoryServerArgs(), args);
  }


  @Override
  protected void configure() {
    bind(HistoryServerArgs.class).toInstance(args);

    JerseyConfiguration configuration = JerseyConfiguration.builder()
        .withResourceBase(args.getWebAppDir())
        .addPackage(IndexController.class.getPackage().getName())
        .addPort(args.getPort())
        .build();
    install(new JerseyModule(configuration));
  }
}
