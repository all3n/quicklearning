package com.devhc.quicklearning.apps;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppContainers {

  private Map<String, List<AppContainer>> runningContainers;
  private Map<String, List<AppContainer>> finishContainers;

}
