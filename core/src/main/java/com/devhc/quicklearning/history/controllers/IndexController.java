package com.devhc.quicklearning.history.controllers;

import com.devhc.quicklearning.beans.ApiResponse;
import com.devhc.quicklearning.beans.Pagination;
import com.devhc.quicklearning.beans.PaginationResult;
import com.devhc.quicklearning.history.MetaLoader;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  public ApiResponse listApps(@QueryParam("page") int page,@QueryParam("page_size") int pageSize) throws IOException {
    return ApiResponse.ok(
        metaLoader.listApps(Pagination.builder().
            current(page).pageSize(pageSize).build())
        );
  }

  @GET
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse info(@QueryParam("appid") String appId) throws IOException {
    return ApiResponse.ok(metaLoader.getJobMeta(appId));
  }
}
