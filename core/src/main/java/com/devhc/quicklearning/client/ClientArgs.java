package com.devhc.quicklearning.client;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class ClientArgs {
  @Option(name = "-t", aliases = "--type", usage = "job type")
  private String type = "mpi";

  @Option(name = "-q", aliases = "--queue", usage = "queue")
  private String queue;

  @Option(name = "-c", aliases = "--config", usage = "job config")
  private String config = "config.json";

  @Option(name = "-ff", aliases = "--frameworkFile", usage = "framework archive")
  private String frameworkFile;

  @Option(name = "-n", aliases = "--name", usage = "job name")
  private String jobName;

  @Option(name = "-d", aliases = "--deps", usage = "dep dirs")
  private String deps;

  @Option(name = "--cpu", usage = "cpu")
  private Integer cpu;

  @Option(name = "--memory", usage = "memory")
  private Integer memory;

  @Option(name = "-w", aliases = "--workspace", usage = "workspace")
  private String workspace = ".";

  @Option(name = "-clean", aliases = "--clean", usage = "clean at exit")
  private boolean cleanAndExit = true;
}
