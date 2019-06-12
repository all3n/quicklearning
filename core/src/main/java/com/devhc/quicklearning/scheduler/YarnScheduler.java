package com.devhc.quicklearning.scheduler;

import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.server.WebServer;
import java.net.InetAddress;
import javax.inject.Inject;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;

public class YarnScheduler extends BaseScheduler {

  private AMRMClient<ContainerRequest> rmClient;
  private String webUrl;

  @Inject
  WebServer webServer;
  private QuickLearningConf conf;

  @Override
  public void init() throws Exception {
    this.conf = new QuickLearningConf();
  }


  @Override
  public void start() throws Exception {
    initClients();
    register();
    webServer.start();
    webServer.join();
  }


  public void register() throws Exception {
    RegisterApplicationMasterResponse response = rmClient
        .registerApplicationMaster(InetAddress.getLocalHost().getHostAddress(),
            webServer.getPort(), "");
  }


  private void initClients() {
    this.rmClient = AMRMClient.createAMRMClient();
    rmClient.init(conf);
    rmClient.start();

  }

  @Override
  public void stop() throws Exception {
    if (rmClient != null) {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
      rmClient.stop();
    }
  }


}
