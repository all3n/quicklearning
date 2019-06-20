package com.devhc.quicklearning.beans;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
  private List<String> roles;
  private String introduction;
  private String avatar;
  private String name;
}
