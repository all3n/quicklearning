package com.devhc.quicklearning.history.controllers;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("/api")
public class IndexController {

  @Path("/")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String index() {
    return "history index";
  }


}
