package com.devhc.quicklearning.client;

import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Client {
  public Client(){

  }


  public static void main(String[] args) {
    SpringApplication.run(Client.class,args);
  }
}
