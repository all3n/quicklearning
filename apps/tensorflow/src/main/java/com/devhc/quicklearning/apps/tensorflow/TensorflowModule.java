package com.devhc.quicklearning.apps.tensorflow;

import com.devhc.quicklearning.utils.JsonUtils;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.google.inject.AbstractModule;

public class TensorflowModule extends AbstractModule {

  private final TensorflowArgs args;

  public TensorflowModule(TensorflowArgs appArgs) {
    this.args = appArgs;
  }

  @Override
  protected void configure() {
    bind(JobConfigJson.class).toInstance(
        JsonUtils.parseJson(args.getConfig(), JobConfigJson.class));
  }
}
