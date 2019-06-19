package com.devhc.quicklearning.apps;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppContainer {
  private String cid;
  private String host;
  private int port;
  private int rpcPort;
  private int status;
  private String logLink;
}
