package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.utils.JsonUtils;
import com.devhc.quicklearning.utils.JobConfigJson;
import com.google.inject.AbstractModule;

public class XdlModule extends AbstractModule {

  private final XdlArgs args;

  public XdlModule(XdlArgs appArgs) {
    this.args = appArgs;
  }

  @Override
  protected void configure() {
    bind(JobConfigJson.class).toInstance(
        JsonUtils.parseJson(args.getConfig(), JobConfigJson.class));
  }
}
