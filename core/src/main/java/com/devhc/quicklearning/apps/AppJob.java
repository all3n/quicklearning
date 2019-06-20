package com.devhc.quicklearning.apps;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppJob {
  private AppResource resource;
  private String name;
  private String type;
  private String entry;
  private boolean isWorker;
  private List<AppContainer> containers;
}
