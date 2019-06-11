package com.devhc.quicklearning;

import com.devhc.quicklearning.conf.QuickLearningConf;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.SocketUtils;

@SpringBootApplication
public class AppMaster {

  private final QuickLearningConf conf;
  private static Logger LOG = LoggerFactory.getLogger(AppMaster.class);
  private InetAddress address;
  private AMRMClient<ContainerRequest> rmClient;

  @Value("${server.port}")
  private int webPort;
  private String webUrl;

  public AppMaster() {
    this.conf = new QuickLearningConf();
    try {
      address = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }


  @PostConstruct
  private void init() throws InterruptedException, IOException, YarnException {
    LOG.info("app master start");
    LOG.info("web listen {}:{}", address.getHostAddress(), webPort);
    this.webUrl = "http://" + address.getHostAddress() + ":" + webPort;

    initClients();
    register();
    for (int i = 0; i < 3600; i++) {
      Thread.sleep(1000L);
    }
    this.stop();
  }

  private void stop() throws IOException, YarnException {
    if (rmClient != null) {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
      rmClient.stop();
    }
  }


  private void initClients() {
    this.rmClient = AMRMClient.createAMRMClient();
    rmClient.init(conf);
    rmClient.start();

  }

  public void register() throws IOException, YarnException {
    RegisterApplicationMasterResponse response = rmClient.registerApplicationMaster(address.getHostAddress(), 0, "");


  }

  public static void setRandomPort(int minPort, int maxPort) {
    try {
      String userDefinedPort = System.getProperty("server.port", System.getenv("SERVER_PORT"));
      if (StringUtils.isEmpty(userDefinedPort)) {
        int port = SocketUtils.findAvailableTcpPort(minPort, maxPort);
        System.setProperty("server.port", String.valueOf(port));
        LOG.info("Server port set to {}.", port);
      }
    } catch (IllegalStateException var4) {
      LOG.warn(
          "No port available in range {}-{}. Default embedded server configuration will be used.",
          minPort, maxPort);
    }
  }

  public static void main(String[] args) {
    setRandomPort(SocketUtils.PORT_RANGE_MIN, SocketUtils.PORT_RANGE_MAX);
    SpringApplication.run(AppMaster.class, args);
  }


  public AMRMClient<ContainerRequest> getRmClient() {
    return rmClient;
  }

  public QuickLearningConf getConf() {
    return conf;
  }

}
