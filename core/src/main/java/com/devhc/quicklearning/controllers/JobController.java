package com.devhc.quicklearning.controllers;


import static com.devhc.quicklearning.beans.ApiResponse.not_found;
import static com.devhc.quicklearning.beans.ApiResponse.ok;

import com.devhc.quicklearning.apps.AppContainer;
import com.devhc.quicklearning.apps.AppContainers;
import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.beans.ApiResponse;
import com.devhc.quicklearning.scheduler.BaseScheduler;
import com.devhc.quicklearning.scheduler.yarn.YarnResourceAllocator;
import com.devhc.quicklearning.scheduler.yarn.YarnScheduler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.var;
import org.apache.hadoop.yarn.api.records.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/job")
@Singleton
public class JobController {

  private static Logger LOG = LoggerFactory.getLogger(JobController.class);


  @Inject
  BaseApp app;


  @Inject
  BaseScheduler scheduler;

  @GET
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse jobInfo() {
    LOG.info("config:{}", app.getConfig());
    return ok(app.getConfig());
  }


  public AppContainer.AppContainerBuilder convertContainer(String type, Container v) {
    var appContainerBuilder = AppContainer.builder().cid(v.getId().toString())
        .host(v.getNodeId().getHost()).port(v.getNodeId().getPort())
        .logLink(
            "http://" + v.getNodeHttpAddress() + "/node/containerlogs/" + v.getId().toString()
                + "/" + app.getUser());
    return appContainerBuilder;
  }

  @GET
  @Path("/containers")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse containers() {
    if (scheduler instanceof YarnScheduler) {
      var yarnScheduler = (YarnScheduler) scheduler;

      YarnResourceAllocator alloc = yarnScheduler
          .getYarnResourceAlloctor();
      var runningMap = alloc.getContainerInfoMap();
      Map<String, Container[]> appAllocContainers = yarnScheduler
          .getYarnResourceAlloctor().getAppAllocContainers();

      Map<String, List<AppContainer>> runningContainers = Maps.newHashMap();
      Map<String, List<AppContainer>> finishContainers = Maps.newHashMap();
      for (var rme : runningMap.entrySet()) {
        String jobType = rme.getValue().getType();
        var cons = runningContainers
            .computeIfAbsent(jobType, k -> Lists.newArrayList());
        var v = appAllocContainers.get(jobType)[rme.getValue().getIndex()];
        if (v != null) {
          cons.add(convertContainer(jobType, v).status(0).build());
        }
      }

      for (var rc : alloc.getFinishContainers().entrySet()) {
        String type = rc.getKey();
        var cons = finishContainers.computeIfAbsent(type, k -> Lists.newArrayList());
        for (var yarnStatusContainer : rc.getValue()) {
          var v = yarnStatusContainer.getContainer();
          cons.add(convertContainer(yarnStatusContainer.getType(), v)
              .status(yarnStatusContainer.isSuccess() ? 1 : 2).build());
        }
      }

      AppContainer masterContainer = AppContainer.builder().logLink(app.getMasterLink()).build();


      var appContainers = AppContainers.builder()
          .masterContainer(masterContainer)
          .runningContainers(runningContainers)
          .finishContainers(finishContainers)
          .build();
      return ok(appContainers);
    } else {
      return not_found();
    }
  }
}
