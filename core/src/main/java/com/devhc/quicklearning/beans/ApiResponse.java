package com.devhc.quicklearning.beans;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponse<T> {

  T data;
  int code;


  public static ApiResponse ok(Object data) {
    return ApiResponse.builder().data(data).code(20000).build();
  }
  public static ApiResponse not_found(){
    return ApiResponse.builder().code(40400).build();
  }
}
