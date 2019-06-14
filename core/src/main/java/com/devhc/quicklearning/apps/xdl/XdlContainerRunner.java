package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.AppContainerModule;
import com.devhc.quicklearning.client.Client;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Module;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XdlContainerRunner {
  private static Logger LOG = LoggerFactory.getLogger(XdlContainerRunner.class);
  @Inject
  XdlArgs args;

  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      moduleList.add(new AppContainerModule<XdlArgs>(args, XdlArgs.class));
      XdlContainerRunner runner = Guice.createInjector(moduleList).getInstance(XdlContainerRunner.class);
      runner.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private void start() {
    LOG.info("args:{}", args);
    System.err.println("test error");
    System.out.println("test stdout");
  }
}
