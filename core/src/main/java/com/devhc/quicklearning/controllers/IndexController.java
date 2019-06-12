package com.devhc.quicklearning.controllers;


import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
@Singleton
public class IndexController {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String index(){
    return "index";
  }

  @GET
  @Path("/test2")
  @Produces(MediaType.TEXT_PLAIN)
  public String index2(){
    return "test2";
  }
}
