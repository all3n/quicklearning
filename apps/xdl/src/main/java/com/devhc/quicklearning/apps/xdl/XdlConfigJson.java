package com.devhc.quicklearning.apps.xdl;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

public class XdlConfigJson {

  @Data
  @Builder
  public static class RoleResource {

    public int instance_num = 4;
    public int cpu_cores = 6;
    public long gpu_cores = 0;
    public long memory_m = 4096;
  }

  @Data
  public static class ExtendRoleResource {

    public int instance_num = 4;
    public int cpu_cores = 6;
    public long gpu_cores = 0;
    public long memory_m = 4096;
    public String script = null;
    public boolean xdl_worker = false;
  }

  @Data
  public static class AutoRebalance {

    public boolean enable = false;
    public String meta_dir = null;
  }


  @Data
  public static class Checkpoint {

    public String output_dir = null;
  }

  public String job_name;
  public String docker_image;
  public String script;
  public String dependent_dirs;

  public RoleResource worker;
  public RoleResource ps;
  public Map<String, ExtendRoleResource> extend_role = null;

  public boolean bind_cores = false;
  public AutoRebalance auto_rebalance;

  public String scheduler_queue = "default";
  public int min_finish_worker_num;
  public float min_finish_worker_rate = 90;
  public int max_failover_times = 20;
  public int max_local_failover_times = 3;
  public int max_failover_wait_secs = 30 * 60;
  
  public Checkpoint checkpoint;
}
