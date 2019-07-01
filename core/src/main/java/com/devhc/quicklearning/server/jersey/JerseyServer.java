package com.devhc.quicklearning.server.jersey;

import com.devhc.quicklearning.server.jersey.configuration.JerseyConfiguration;
import com.devhc.quicklearning.server.jersey.configuration.ServerConnectorConfiguration;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JerseyServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JerseyServer.class);

  private final JerseyConfiguration jerseyConfiguration;
  private final Supplier<Injector> injectorSupplier;
  private final Server server;
  private WebAppContext webAppContext;

  JerseyServer(JerseyConfiguration jerseyConfiguration,
      Supplier<Injector> injectorSupplier,
      JettyServerCreator jettyServerCreator) {
    this.jerseyConfiguration = jerseyConfiguration;
    this.injectorSupplier = injectorSupplier;
    this.server = jettyServerCreator.create();

    configureServer();
  }

  public void start() throws Exception {
    LOGGER.info("Starting embedded jetty server");
    server.start();
  }

  public void stop() throws Exception {
    server.stop();
    LOGGER.info("Embedded jetty server stopped");
  }

  public void join() throws Exception{
    server.join();
  }

  public int getPort(){
    return ((ServerConnector)server.getConnectors()[0]).getLocalPort();
  }

  private void configureServer() {
    List<ServerConnectorConfiguration> serverConnectorConfigurations = jerseyConfiguration
        .getServerConnectors();
    serverConnectorConfigurations.forEach(configuration -> {
      ServerConnector connector = new ServerConnector(server);
      connector.setName(configuration.getName());
      connector.setHost(configuration.getHost());
      connector.setPort(configuration.getPort());
      server.addConnector(connector);
    });

    this.webAppContext = new WebAppContext();

    webAppContext.setServer(server);
    webAppContext.setResourceBase(jerseyConfiguration.getResourceBase());
    webAppContext.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

    FilterHolder holder = new FilterHolder(ServletContainer.class);

//        holder.setInitParameter(ServletProperties.FILTER_STATIC_CONTENT_REGEX, "/.*html");
    holder.setInitParameter(ServletProperties.FILTER_CONTEXT_PATH, "/");
    holder.setInitParameter(ServletProperties.FILTER_FORWARD_ON_404, "true");
    holder.setInitParameter("javax.ws.rs.Application", GuiceJerseyResourceConfig.class.getName());

    webAppContext.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));

    webAppContext.setContextPath(jerseyConfiguration.getContextPath());
    webAppContext.addEventListener(new GuiceServletContextListener() {
      @Override
      protected Injector getInjector() {
        return injectorSupplier.get();
      }
    });


    server.setHandler(webAppContext);

  }

}
