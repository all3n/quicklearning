package com.devhc.quicklearning.master;

import com.devhc.quicklearning.controllers.IndexController;
import com.devhc.quicklearning.servlets.IndexServlet;
import com.google.inject.servlet.ServletModule;
import javax.inject.Singleton;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppServletModule extends ServletModule {
  private static Logger LOG = LoggerFactory.getLogger(AppServletModule.class);

  @Override
  protected void configureServlets() {
    bind(DefaultServlet.class).in(Singleton.class);

    bind(IndexController.class).in(Singleton.class);



    bind(IndexServlet.class);
    serve("/test").with(IndexServlet.class);



//    bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
//    bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

  }
}
