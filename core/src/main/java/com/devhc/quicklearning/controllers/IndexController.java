package com.devhc.quicklearning.controllers;


import com.devhc.quicklearning.apps.BaseApp;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api")
@Singleton
public class IndexController {
  private static Logger LOG = LoggerFactory.getLogger(IndexController.class);



  @Inject
  BaseApp app;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String index(){
    return "index";
  }


}
