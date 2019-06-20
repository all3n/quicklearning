package com.devhc.quicklearning.server.rpc;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.nio.NioEventLoopGroup;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcServer {

  private static Logger LOG = LoggerFactory.getLogger(RpcServer.class);
  private RpcServerConfig config;

  private Server grpcServer;

  BindableService serviceImpl;



  @Inject
  public RpcServer(RpcServerConfig config, BindableService serviceImpl) {
    this.config = config;
    this.serviceImpl = serviceImpl;
    this.init();
  }

  public void init() {
    NioEventLoopGroup eg = new NioEventLoopGroup(config.getBossThreadNum());
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        config.getMinWorkerThreadNum(),
        config.getMaxWorkerThreadNum(),
        60, TimeUnit.SECONDS,
        new LinkedBlockingDeque<Runnable>(),
        new CustomThreadFactory(config.getName(), true));

    grpcServer = NettyServerBuilder.forPort(0)
        .addService(serviceImpl)
        .workerEventLoopGroup(eg)
        .bossEventLoopGroup(eg)
        .executor(executor)
        .build();
  }


  public void start() throws IOException {
    grpcServer.start();
    LOG.info("grpc server start {}", grpcServer.getPort());
  }

  public void stop() {
    if (grpcServer != null) {
      grpcServer.shutdown();
      LOG.info("grpc server stop");
    }
  }

  public static final class CustomThreadFactory implements ThreadFactory {

    private String baseName;
    private boolean isDaemon;
    private int count = 0;

    public CustomThreadFactory(String baseName, boolean isDaemon) {
      this.baseName = baseName;
      this.isDaemon = isDaemon;
    }

    public Thread newThread(Runnable r) {
      String name = baseName + "-" + count;
      count += 1;
      Thread out = new Thread(r, name);
      out.setDaemon(isDaemon);
      return out;
    }
  }

}
