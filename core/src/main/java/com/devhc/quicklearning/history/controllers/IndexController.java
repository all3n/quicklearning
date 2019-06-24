package com.devhc.quicklearning.history.controllers;

import com.devhc.quicklearning.beans.ApiResponse;
import com.devhc.quicklearning.history.MetaLoader;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Singleton
@Path("/api/historyserver")
public class IndexController {
  @Inject
  MetaLoader metaLoader;

  @Path("/")
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String index() {
    return "history index";
  }


  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse appIds() throws IOException {
    return ApiResponse.ok(metaLoader.listAppIds());
  }

  @GET
  @Path("/info/{appId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse info(@PathParam("appId") String appId) throws IOException {
    return ApiResponse.ok(metaLoader.getJobMeta(appId));
  }
}
