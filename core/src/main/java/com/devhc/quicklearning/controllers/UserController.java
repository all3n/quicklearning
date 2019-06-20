package com.devhc.quicklearning.controllers;


import com.devhc.quicklearning.apps.BaseApp;
import com.devhc.quicklearning.beans.ApiResponse;
import com.devhc.quicklearning.beans.UserInfo;
import com.google.common.collect.Lists;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/user")
@Singleton
public class UserController {
  private static Logger LOG = LoggerFactory.getLogger(UserController.class);



  @Inject
  BaseApp app;


  @POST
  @Path("/login")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse login(){
    UserInfo ui = UserInfo.builder().roles(Lists.newArrayList("admin"))
        .avatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
        .introduction("admin").name("Super Admin").build();
    return ApiResponse.ok(ui);
  }


  @GET
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  public ApiResponse info(){
    UserInfo ui = UserInfo.builder().roles(Lists.newArrayList("admin"))
        .avatar("https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
        .introduction("admin").name("Super Admin").build();
    return ApiResponse.ok(ui);
  }
}
