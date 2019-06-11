package com.devhc.quicklearning.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class WebServer {
  @PostConstruct
  public void init(){

  }

  public void start(){

  }



  @PreDestroy
  public void stop(){

  }

}
