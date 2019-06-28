package com.devhc.quicklearning.history.beans;

import lombok.Builder;
import lombok.Data;

/**
 * Created by wanghch on 2019/6/24.
 */
@Data
@Builder
public class HistoryJob {
  private String app_id;
  private long timestamp;

}
