package com.devhc.quicklearning.server.jersey;

import com.devhc.quicklearning.server.jersey.configuration.JerseyConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.servlet.ServletModule;
import org.eclipse.jetty.server.Server;

import java.util.Objects;
import org.eclipse.jetty.webapp.WebAppContext;

public class JerseyModule extends AbstractModule {

    private final JerseyConfiguration jerseyConfiguration;
    private final JettyServerCreator jettyServerCreator;

    public JerseyModule(JerseyConfiguration jerseyConfiguration) {
        this(jerseyConfiguration, Server::new);
    }

    public JerseyModule(JerseyConfiguration jerseyConfiguration, JettyServerCreator jettyServerCreator) {
        this.jerseyConfiguration = Objects.requireNonNull(jerseyConfiguration);
        this.jettyServerCreator = Objects.requireNonNull(jettyServerCreator);
    }

    protected void configure() {
        Provider<Injector> injectorProvider = getProvider(Injector.class);

        install(new ServletModule());
        bind(JerseyServer.class).toInstance(new JerseyServer(jerseyConfiguration, injectorProvider::get, jettyServerCreator));
        bind(JerseyConfiguration.class).toInstance(jerseyConfiguration);
    }

}
