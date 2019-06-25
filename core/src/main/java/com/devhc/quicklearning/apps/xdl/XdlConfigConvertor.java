package com.devhc.quicklearning.apps.xdl;

import com.devhc.quicklearning.apps.xdl.XdlConfigJson.AutoRebalance;
import com.devhc.quicklearning.apps.xdl.XdlConfigJson.RoleResource;
import com.devhc.quicklearning.beans.JobConfigJson;
import com.google.common.base.Preconditions;
import lombok.var;

public class XdlConfigConvertor {

  public static XdlConfigJson convert(JobConfigJson config) {
    var xc = new XdlConfigJson();
    xc.docker_image = config.getDocker_image();
    xc.dependent_dirs = "./";

    JobConfigJson.RoleResource worker = config.getJobs().get("worker");
    Preconditions.checkNotNull(worker);

    xc.worker = RoleResource.builder()
        .cpu_cores(worker.cpu_cores).gpu_cores(worker.gpu_cores).instance_num(worker.instance_num)
        .memory_m(worker.memory_m)
        .build();

    JobConfigJson.RoleResource ps = config.getJobs().get("ps");
    Preconditions.checkNotNull(ps);
    xc.ps = RoleResource.builder()
        .cpu_cores(ps.cpu_cores).gpu_cores(ps.gpu_cores).instance_num(ps.instance_num)
        .memory_m(ps.memory_m)
        .build();
    var autoRebalance = new AutoRebalance();
    autoRebalance.enable = false;
    autoRebalance.meta_dir = null;
    xc.auto_rebalance = autoRebalance;

    xc.job_name = config.getJob_name();
    xc.script = config.getScript();
    xc.scheduler_queue = config.scheduler_queue;

    xc.max_failover_times = config.max_failover_times;
    xc.max_failover_wait_secs = config.max_failover_wait_secs;
    xc.max_local_failover_times = config.max_local_failover_times;
    xc.min_finish_worker_num = config.min_finish_worker_num;
    xc.min_finish_worker_rate = config.min_finish_worker_rate;

    return xc;
  }

}
