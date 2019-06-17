package com.devhc.quicklearning.master;

import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.apps.xdl.XdlApp;
import com.devhc.quicklearning.controllers.IndexController;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.scheduler.LocalScheduler;
import com.devhc.quicklearning.scheduler.yarn.YarnScheduler;
import com.devhc.quicklearning.server.jersey.JerseyModule;
import com.devhc.quicklearning.server.jersey.configuration.JerseyConfiguration;
import com.devhc.quicklearning.server.rpc.RpcModule;
import com.devhc.quicklearning.server.rpc.RpcServerConfig;
import com.devhc.quicklearning.utils.ConfigUtils;
import com.devhc.quicklearning.utils.JobConfigJson;
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
    bind(JobConfigJson.class).toInstance(ConfigUtils.parseJson(masterArgs.getConfigFile(), JobConfigJson.class));


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

    if(masterArgs.getAppType().equals("xdl")){
      bind(BaseApp.class).to(XdlApp.class);
          //.toInstance(new XdlApp(masterArgs.getConfigFile()));
    }else{
//      throw new RuntimeException(masterArgs.getAppType()+" is not support");
    }

    if (masterArgs.getScheduler().equals("yarn")) {
      bind(BaseScheduler.class).to(YarnScheduler.class);
    } else {
      bind(BaseScheduler.class).to(LocalScheduler.class);
    }
  }
}
