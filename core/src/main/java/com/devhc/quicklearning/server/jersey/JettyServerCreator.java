package com.devhc.quicklearning.server.jersey;

import org.eclipse.jetty.server.Server;

public interface JettyServerCreator {
    Server create();
}
