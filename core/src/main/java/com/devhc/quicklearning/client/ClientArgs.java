package com.devhc.quicklearning.client;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class ClientArgs {
  @Option(name = "-t", aliases = "--type", usage = "job type")
  private String type = "xdl";

  @Option(name = "-q", aliases = "--queue", usage = "queue")
  private String queue = "default";


  @Option(name = "-c", aliases = "--config", usage = "job config")
  private String config = "config.json";

  @Option(name = "-e", aliases = "--env", usage = "env")
  private String envFile = "../ql.tar.gz";


  @Option(name = "-n", aliases = "--name", usage = "job name")
  private String jobName = "quicklearning test";

  @Option(name = "-d", aliases = "--deps", usage = "dep dirs")
  private String deps = ".";
}
