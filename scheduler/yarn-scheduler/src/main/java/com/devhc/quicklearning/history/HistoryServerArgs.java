package com.devhc.quicklearning.history;

import lombok.Data;
import org.kohsuke.args4j.Option;

@Data
public class HistoryServerArgs {

  @Option(name = "-w", aliases = "--webAppDir", usage = "web app dir")
  private String webAppDir = "webAppDir";

  @Option(name = "-p", aliases = "--port", usage = "web port")
  private int port = 0;

}
