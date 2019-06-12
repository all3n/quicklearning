package com.devhc.quicklearning.server;


import com.devhc.quicklearning.controllers.IndexController;
import com.devhc.quicklearning.master.MasterArgs;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import java.util.EnumSet;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {

  private final String webApp;
  private Logger LOG = LoggerFactory.getLogger(WebServer.class);
  private final Server server;
  MasterArgs args;

  @Inject
  public WebServer(MasterArgs args, Injector injector){
    this.args = args;
    server = new Server(args.getPort());
    this.webApp = args.getWebAppDir();
    LOG.info("web app:{}", webApp);
    configureServer(injector);
  }

  private void configureServer(Injector injector) {
    server.setStopAtShutdown(true);
    String controllPkg = IndexController.class.getPackage().getName();
    System.out.println(controllPkg);

    ServletContextHandler ctx = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
    ctx.setResourceBase(webApp);


    ctx.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));


   ctx.addServlet(DefaultServlet.class, "/");



    server.setHandler(ctx);

  }


  public int getPort(){
    return 0;

  }

  public void init(){

  }

  public void start() throws Exception {
    server.start();
  }

  public void join() throws Exception{
    server.join();
  }



  public void stop() throws Exception {
    server.stop();

  }

}
