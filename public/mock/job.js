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



const containers = {
    'masterContainer': {
        'logLink': "http://localhost/masterLink"
    },
    "runningContainers": {
        "ps": [{
            "cid": 'container_id_ps', "host": "localhost", "port": 8041, "rpcPort": 0, "status": 0,
            "logLink": "http://127.0.0.1/ps"
        }],
        "scheduler": [],
         "worker": [{
            "cid": "container_id_123", "host": "127.0.0.1", "port": 8041,
            "rpcPort": 0, "status": 0, "logLink": "http://127.0.0.1"
        }]
    },
    "finishContainers": {
        "scheduler": [{
            "cid": 'container_id_scheduler', "host": "localhost", "port": 8041, "rpcPort": 0, "status": 1,
            "logLink": "http://127.0.0.1"
        }],
        "worker": [{
            "cid": 'container_id_worker', "host": "localhost", "port": 8041, "rpcPort": 0, "status": 2,
            "logLink": "http://127.0.0.1"
        }]

    }
}

export default [
    {
        url: '/job/info',
        type: 'get',
        response: config => {
            return {
                code: 20000,
                data: job
            }
        }
    },
    {
        url: '/job/containers',
        type: 'get',
        response: config => {
            return {
                code: 20000,
                data: containers
            }
        }
    }

]
