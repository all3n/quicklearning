package com.devhc.quicklearning.utils;

import java.util.Map;

public class JobConfigJson {

  public String jobType;


  public static class RoleResource {
    public int instance_num = 1;
    public int cpu_cores = 4;
    public int gpu_cores = 0;
    public int memory_m = 4096;
    public String entry = null;
    public boolean is_worker = false;
  }

  public Map<String, RoleResource> jobs;


  public String env;
  public String job_name;
  public String docker_image;
  public String scheduler_queue;

  public int min_finish_worker_num;
  public float min_finish_worker_rate = 90;
  public int max_failover_times = 20;
  public int max_local_failover_times = 3;
  public int max_failover_wait_secs = 30 * 60;
}
