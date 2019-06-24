package com.devhc.quicklearning.beans;

import lombok.Builder;
import lombok.Data;

/**
 * Created by wanghch on 2019/6/24.
 */
@Builder
@Data
public class Pagination {
  private int total;
  private int totalPages;
  private int pageSize;
  private int current;




}
