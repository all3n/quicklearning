package com.devhc.quicklearning.conf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

public class QuickLearningConf extends YarnConfiguration {
  public static final String QUICK_LEARNING_DEFAULT_CONF = "quick-learning-default.xml";
  public static final String QUICK_LEARNING_SITE_CONF = "quick-learning.xml";

  public static final String META_CONFIG = "quicklearning.meta.dir";
  public static final String QUICK_LEARNING_ZK_ADDR = "quicklearning.zk.address";

  static {
    Configuration.addDefaultResource(QUICK_LEARNING_DEFAULT_CONF);
    Configuration.addDefaultResource(QUICK_LEARNING_SITE_CONF);
  }


}
