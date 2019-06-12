package com.devhc.quicklearning.master;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class MasterArgs {
  @Option(name = "-w", aliases = "--webAppDir", usage = "web app dir")
  private String webAppDir = "webAppDir";

  @Option(name = "-s", aliases = "--scheduler", usage = "scheduler")
  private String scheduler = "local";


  @Option(name = "-p", aliases = "--port", usage = "web port")
  private int port = 0;
}
