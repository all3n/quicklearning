package com.devhc.quicklearning.history;

import com.devhc.quicklearning.server.WebServer;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Module;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class HistoryServer {

  private static Logger LOG = LoggerFactory.getLogger(HistoryServer.class);

  @Inject
  HistoryServerArgs args;
  @Inject
  WebServer server;

  public static void main(String[] args) {

    try {
      List<Module> moduleList = Lists.newArrayList();
      moduleList.add(new HistoryServerModules(args));
      var server = Guice.createInjector(moduleList).getInstance(HistoryServer.class);
      server.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() {
    try {
      LOG.info("try to start history server");
      server.start();
      LOG.info("start web server at http://{}:{}", server.getHost(), server.getPort());
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
