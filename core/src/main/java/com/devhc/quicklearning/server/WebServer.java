package com.devhc.quicklearning.server;


import com.devhc.quicklearning.master.MasterArgs;
import com.devhc.quicklearning.server.jersey.JerseyServer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {

  private final JerseyServer server;
  private String host;
  private Logger LOG = LoggerFactory.getLogger(WebServer.class);
  MasterArgs args;


  @Inject
  public WebServer(MasterArgs args, JerseyServer server) {
    this.args = args;
    this.server = server;
    try {
      this.host = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }

  }


  public String webUrl() {
    return "http://" + host + ":" + getPort();
  }

  public String getHost(){
    return host;
  }

  public int getPort() {
    return server.getPort();
  }

  public void init() {

  }

  public void start() throws Exception {
    server.start();
  }

  public void join() throws Exception {
    server.join();
  }


  public void stop() throws Exception {
    server.stop();
  }

}
