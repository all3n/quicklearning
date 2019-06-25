package com.devhc.quicklearning.master;

import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.server.WebServer;
import com.devhc.quicklearning.server.rpc.RpcServer;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Module;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMaster {

  private static Logger LOG = LoggerFactory.getLogger(AppMaster.class);
  private InetAddress address;


  MasterArgs args;

  @Inject
  WebServer webServer;

  @Inject
  RpcServer rpcServer;

  BaseScheduler scheduler;

  BaseApp app;

  @Inject
  JobConfigJson jobConfig;


  @Inject
  public AppMaster(MasterArgs args, BaseApp app, BaseScheduler scheduler) throws Exception {
    this.args = args;
    this.scheduler = scheduler;
    this.app = app;
    app.setAppId(scheduler.getAppId());
    LOG.info("master args:{}", args);

    try {
      address = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    scheduler.init();
  }

  public void start() throws Exception {
    LOG.info("app master start");
    webServer.start();
    LOG.info("web listen {}:{}", webServer.getHost(), webServer.getPort());
    rpcServer.start();

    boolean res = scheduler.start();
    if (args.getStopAtFinished() == 1) {
      this.stop();
    }
  }


  private void stop() throws Exception {
    scheduler.stop();
    webServer.stop();
    rpcServer.stop();
  }


  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      moduleList.add(new AppMasterModules(args));
      AppMaster master = Guice.createInjector(moduleList).getInstance(AppMaster.class);
      master.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
