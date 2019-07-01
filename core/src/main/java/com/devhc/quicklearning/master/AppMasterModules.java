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
import java.util.ServiceLoader;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;

public class AppMasterModules extends AbstractModule {


  private final MasterArgs masterArgs;
  private final ServiceLoader<BaseApp> serviceLoader;
  private final ServiceLoader<BaseScheduler> schedulerServiceLoader;

  public AppMasterModules(String args[]) {
    masterArgs = ArgsUtils.parseArgument(new MasterArgs(), args);
    this.serviceLoader = ServiceLoader.load(BaseApp.class);
    this.schedulerServiceLoader = ServiceLoader.load(BaseScheduler.class);
  }

  @Override
  protected void configure() {
    install(new AppServletModule());
    bind(MasterArgs.class).toInstance(masterArgs);
    bind(JobConfigJson.class).toInstance(
        JsonUtils.parseJson(masterArgs.getConfigFile(), JobConfigJson.class));
    BaseApp app = CommonUtils.getServiceByName(serviceLoader, masterArgs.getAppType());
    Preconditions.checkNotNull(app, masterArgs.getAppType() + " app type is not support");
    bind(BaseApp.class).toInstance(app);

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

    BaseScheduler schedulerService = CommonUtils
        .getServiceByName(schedulerServiceLoader, masterArgs.getScheduler());
    Preconditions
        .checkNotNull(schedulerService, masterArgs.getScheduler() + " scheduler is not support");

    bind(BaseScheduler.class).toInstance(schedulerService);

  }
}
