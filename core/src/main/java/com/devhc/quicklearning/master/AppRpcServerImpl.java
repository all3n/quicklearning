package com.devhc.quicklearning.master;

import com.devhc.quicklearning.AppMasterGrpc.AppMasterImplBase;
import com.devhc.quicklearning.Msg.Empty;
import com.devhc.quicklearning.Msg.ShutdownRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppRpcServerImpl extends AppMasterImplBase {
  private static Logger LOG = LoggerFactory.getLogger(AppRpcServerImpl.class);
  private final Object shutdownLock = new Object();

  @Override
  public void shutdown(ShutdownRequest request, StreamObserver<Empty> responseObserver) {
    LOG.info("request:{}", request);
    synchronized (shutdownLock){
      // start shutdown

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    }
  }
}
