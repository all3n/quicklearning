package com.devhc.quicklearning.server.rpc;

import io.grpc.BindableService;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcServerConfig {
  private int port;
  private String name;
  private int bossThreadNum;
  private int minWorkerThreadNum;
  private int maxWorkerThreadNum;




}
