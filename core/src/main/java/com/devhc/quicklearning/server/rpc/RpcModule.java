package com.devhc.quicklearning.server.rpc;

import com.google.inject.AbstractModule;
import io.grpc.BindableService;

public class RpcModule extends AbstractModule {

  private final RpcServerConfig config;
  private final BindableService impl;

  public RpcModule(RpcServerConfig config, BindableService impl){
    this.config = config;
    this.impl = impl;
  }


  @Override
  protected void configure() {
    System.out.printf(config.toString());
    // config rpc server
    bind(BindableService.class).toInstance(impl);
    bind(RpcServer.class).toInstance(new RpcServer(config, impl));
  }
}
