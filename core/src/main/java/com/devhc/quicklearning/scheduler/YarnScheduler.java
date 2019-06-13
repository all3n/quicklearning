package com.devhc.quicklearning.scheduler;

import com.devhc.quicklearning.conf.QuickLearningConf;
import com.devhc.quicklearning.master.MasterArgs;
import com.devhc.quicklearning.server.WebServer;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;

import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.protocolrecords.RegisterApplicationMasterResponse;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnScheduler extends BaseScheduler {

  private Logger LOG = LoggerFactory.getLogger(YarnScheduler.class);


  @Inject
  WebServer webServer;
  private QuickLearningConf conf;

  private AMRMClient<ContainerRequest> rmClient;
  private NMClient nmClient;
  private String applicationMasterHostname;
  private String webProxyBase;
  @Inject
  MasterArgs masterArgs;

  @Override
  public void init() throws Exception {
    this.conf = new QuickLearningConf();
    Map<String, String> envs = System.getenv();


    if (envs.containsKey(ApplicationConstants.Environment.NM_HOST.toString())) {
      this.applicationMasterHostname = envs
          .get(Environment.NM_HOST.toString());
    }
    if(envs.containsKey(ApplicationConstants.APPLICATION_WEB_PROXY_BASE_ENV)){
      this.webProxyBase = envs.get(ApplicationConstants.APPLICATION_WEB_PROXY_BASE_ENV);
    }
  }


  @Override
  public void start() throws Exception {
    initClients();
    register();
  }


  public void register() throws Exception {
    String webUrl = String.format("http://%s:%s", applicationMasterHostname, webServer.getPort());
    LOG.info("start register app master:{}", webUrl);
    RegisterApplicationMasterResponse response = rmClient
        .registerApplicationMaster(webServer.getHost(),
            webServer.getPort(), webUrl);
    LOG.info("MaximumResourceCapability res:{}", response.getMaximumResourceCapability());

  }


  private void initClients() {
    this.rmClient = AMRMClient.createAMRMClient();
    rmClient.init(conf);
    rmClient.start();
    LOG.info("RMClient client started.");

    nmClient = NMClient.createNMClient();
    nmClient.init(conf);
    nmClient.start();
    LOG.info("NodeManager client started.");

  }

  @Override
  public void stop() throws Exception {
    LOG.info("stop yarn scheduler");
    if (rmClient != null) {
      rmClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
      rmClient.stop();
    }
    if(nmClient != null){
      nmClient.close();
    }
  }


}
