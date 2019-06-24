package com.devhc.quicklearning.apps;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppContainer {
  private String type;
  private String cid;
  private String host;
  private int port;
  private int rpcPort;
  private AppStatus status;
  private String logLink;
  private int taskIndex;
  private long startTime;
  private long endTime;
  private String command;
}
