package com.devhc.quicklearning.beans;

import com.devhc.quicklearning.apps.AppContainers;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobMeta {
  private JobConfigJson job;
  private String jobId;
  private String status;
  private AppContainers containers;

}