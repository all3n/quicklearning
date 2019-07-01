package com.devhc.quicklearning.client;

import com.devhc.quicklearning.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Module;
import java.util.List;

/**
 * Created by wanghch on 2019/6/28.
 */
public class Client {
  public static void main(String[] args) {
    try {
      List<Module> moduleList = Lists.newArrayList();
      moduleList.add(new ClientModules(args));
      IClient client = Guice.createInjector(moduleList).getInstance(IClient.class);
      client.init();
      client.start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
