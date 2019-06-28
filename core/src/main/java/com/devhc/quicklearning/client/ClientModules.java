package com.devhc.quicklearning.client;

import com.devhc.quicklearning.utils.ArgsUtils;
import com.devhc.quicklearning.utils.CommonUtils;
import com.google.inject.AbstractModule;
import java.util.ServiceLoader;
import javax.inject.Named;
import lombok.var;

@Named
public class ClientModules extends AbstractModule {

  private final ClientArgs clientArgs;
  private ServiceLoader<IClient> clientLoader;

  public ClientModules(String args[]) {
    clientArgs = ArgsUtils.parseArgument(new ClientArgs(), args);
    clientLoader = ServiceLoader.load(IClient.class);


  }

  @Override
  protected void configure() {
    bind(ClientArgs.class).toInstance(clientArgs);

    bind(IClient.class).toInstance(CommonUtils.getServiceByName(clientLoader, clientArgs.getMaster()));
  }
}
