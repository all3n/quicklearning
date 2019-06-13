package com.devhc.quicklearning.master;

import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.server.WebServer;
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

  BaseScheduler scheduler;



  @Inject
  public AppMaster(MasterArgs args, BaseScheduler scheduler) throws Exception {
    this.args = args;
    this.scheduler = scheduler;
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




    scheduler.start();
//    webServer.join();
    LOG.info("---------------");

  }


  private void stop() throws Exception {
    scheduler.stop();
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
