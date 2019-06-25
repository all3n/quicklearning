package com.devhc.quicklearning.apps;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppResource {
  // unit: m
  private long memory;
  private int vcore;
  private int gpu;
  private int instance;
}
