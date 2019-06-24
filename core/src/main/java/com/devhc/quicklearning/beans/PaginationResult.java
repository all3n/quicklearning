package com.devhc.quicklearning.beans;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * Created by wanghch on 2019/6/24.
 */
@Data
public class PaginationResult<T> {
  private List<T> items;
  private Pagination pagination;

  public PaginationResult(List<T> items, Pagination pagination) {
    this.items = items;
    this.pagination = pagination;
  }
}
