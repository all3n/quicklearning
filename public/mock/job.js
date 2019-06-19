import Mock from 'mockjs'

const job = {
    "jobType": null, "jobs": {
        "worker": {
            "instance_num": 1, "cpu_cores": 4, "gpu_cores": 0, "memory_m": 4096, "entry": null, "is_worker": false
        }
    }, 
    "env": null,
    "job_name": "test job name", 
    "docker_image": "docker image ",
    "scheduler_queue": "adhoc", "min_finish_worker_num": 0, "min_finish_worker_rate": 90,
    "max_failover_times": 20, "max_local_failover_times": 3, "max_failover_wait_secs": 1800
}

export default [
    {
        url: '/job/info',
        type: 'get',
        response: config => {
            console.log("mock ", config);
            return {
                code: 20000,
                data: job
            }
        }
    }
]
