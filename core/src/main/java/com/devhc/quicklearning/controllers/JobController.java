package com.devhc.quicklearning.controllers;


import static com.devhc.quicklearning.beans.ApiResponse.not_found;
import static com.devhc.quicklearning.beans.ApiResponse.ok;

import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.beans.ApiResponse;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this controller used for get job's info
 * @author wanghuacheng
 */
@Path("/api/job")
@Singleton
public class JobController {

  private static Logger LOG = LoggerFactory.getLogger(JobController.class);


  @Inject
  BaseApp app;


  @Inject
  BaseScheduler scheduler;

  /**
   * get job base info from config json
   *
   * @return ApiResposne(JobConfigJson)
   */
  @GET
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse jobInfo() {
    LOG.info("config:{}", app.getConfig());
    return ok(app.getConfig());
  }


  /**
   * get job containers info
   *
   * @return ApiResponse(AppContainers)
   */
  @GET
  @Path("/containers")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse containers() {
    var res = scheduler.getAppContainers();
    if (res != null) {
      return ok(res);
    } else {
      return not_found();
    }
  }
}
