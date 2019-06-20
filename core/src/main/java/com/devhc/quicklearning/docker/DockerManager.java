package com.devhc.quicklearning.docker;

import com.devhc.quicklearning.utils.CmdUtils;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DockerManager {

  private static Logger LOG = LoggerFactory.getLogger(DockerManager.class);

  public void pull(String image) {
    String dockerPullCommand = String.format("docker pull %s", image);
    long start = System.currentTimeMillis();
    CmdUtils.exeCmd(dockerPullCommand, 1);
    long use = System.currentTimeMillis() - start;
    LOG.info("pull {} use {} s", image, use / 1000);

  }


  public void stop(String containerId) {
    String dockerStopCommand = String.format("docker rm -t 30 %s", containerId);
    CmdUtils.exeCmd(dockerStopCommand, 1);
  }

}
