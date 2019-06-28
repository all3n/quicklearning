package com.devhc.quicklearning.master;

import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.controllers.IndexController;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.scheduler.LocalScheduler;
import com.devhc.quicklearning.server.jersey.JerseyModule;
import com.devhc.quicklearning.server.jersey.configuration.JerseyConfiguration;
import com.devhc.quicklearning.server.rpc.RpcModule;
import com.devhc.quicklearning.server.rpc.RpcServerConfig;
import com.devhc.quicklearning.utils.ArgsUtils;
import com.devhc.quicklearning.utils.CommonUtils;
import com.devhc.quicklearning.utils.JsonUtils;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;

public class AppMasterModules extends AbstractModule {


  private final MasterArgs masterArgs;

  public AppMasterModules(String args[]) {
    masterArgs = ArgsUtils.parseArgument(new MasterArgs(), args);
  }

  @Override
  protected void configure() {
    install(new AppServletModule());
    bind(MasterArgs.class).toInstance(masterArgs);
    bind(JobConfigJson.class).toInstance(
        JsonUtils.parseJson(masterArgs.getConfigFile(), JobConfigJson.class));

    Class appClazz = CommonUtils.genClass(BaseApp.class, masterArgs.getAppType(), "App");
    Preconditions.checkNotNull(appClazz, masterArgs.getAppType() + " not exist");
    bind(BaseApp.class).to(appClazz).in(Singleton.class);

    // config rest web jersey server
    JerseyConfiguration configuration = JerseyConfiguration.builder()
        .withResourceBase(masterArgs.getWebAppDir())
        .addPackage(IndexController.class.getPackage().getName())
        .addPort(masterArgs.getPort())
        .build();
    install(new JerseyModule(configuration));

    // config rpc server for app master
    RpcServerConfig rpcServerConfig = RpcServerConfig.builder()
        .name("appMasterRpcServer")
        .bossThreadNum(2)
        .minWorkerThreadNum(2)
        .maxWorkerThreadNum(10)
        .port(0)
        .build();

    install(new RpcModule(rpcServerConfig, new AppRpcServerImpl()));

    if (masterArgs.getScheduler().equals("local")) {
      bind(BaseScheduler.class).to(LocalScheduler.class).in(Singleton.class);
    } else {
      Class schedulerClazz = CommonUtils
          .genClass(BaseScheduler.class, masterArgs.getScheduler(), "Scheduler");
      Preconditions.checkNotNull(schedulerClazz, "scheduler class not exist");
      bind(BaseScheduler.class).to(schedulerClazz).in(Singleton.class);
    }
  }
}
