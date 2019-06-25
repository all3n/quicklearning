package com.devhc.quicklearning.apps.xdl;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class XdlArgs {

  @Option(name = "-c", aliases = "--config", usage = "job config")
  private String config = "config.json";

  @Option(name = "-j", aliases = "--jobType", usage = "jobType")
  private String jobType = "worker";

  @Option(name = "-appId", aliases = "--appId", usage = "appId")
  private String appId = "appId";

  @Option(name = "-i", aliases = "--workerIndex", usage = "workerIndex")
  private int workerIndex = 0;


  @Option(name = "-u", aliases = "--user", usage = "user name")
  private String user;


  @Option(name = "-cpuset", aliases = "--cpuset", usage = "cpuset")
  private String cpuset;

  @Option(name = "-cd", aliases = "--cuda_device", usage = "cuda_device")
  private String cuda_device;


  @Option(name = "-args", aliases = "--args", usage = "args")
  private String args;

  @Option(name = "-xp", aliases = "--export_ports", usage = "export port")
  private String exportPorts;

  @Option(name = "-e", aliases = "--entry", usage = "entry script")
  private String entry;


}
