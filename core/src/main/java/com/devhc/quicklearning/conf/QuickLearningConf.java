package com.devhc.quicklearning.conf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class QuickLearningConf extends YarnConfiguration {
  static {
    Configuration.addDefaultResource("quick-learning.xml");
  }


}
