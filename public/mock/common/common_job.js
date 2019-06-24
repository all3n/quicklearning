import Mock from 'mockjs'

var app_reg = /application_\d{12}_\d{4}/
var cid_reg = /application_\d{12}_\d{4}_\d{2}_\d{6}/
var rand_port = '@integer(1000, 65535)'

const data = Mock.mock({
  'items|30': [{
    id: '@id',
    app_id: app_reg
  }]
})
const status_arr = ['SUCCESS', 'RUNNING', 'FAIL']
const info = Mock.mock({
  'job': {
    'jobType': '@word',
    'jobs': {
      'worker': {
        'instance_num': 1,
        'cpu_cores': 4,
        'gpu_cores': 0,
        'memory_m': 1024,
        'entry': '@word',
        'is_worker': false
      }
    },
    'env': null,
    'job_name': '@word',
    'docker_image': 'image:tag',
    'scheduler_queue': 'default',
    'min_finish_worker_num': 0,
    'min_finish_worker_rate': 90,
    'max_failover_times': 20,
    'max_local_failover_times': 3,
    'max_failover_wait_secs': 1800
  },
  'jobId': app_reg,
  'status|1': status_arr,
  'containers': {
    'masterContainer': {
      'type': null,
      'cid': cid_reg,
      'host': null,
      'port': 0,
      'rpcPort': 0,
      'status': 0,
      'logLink': '@url',
      'taskIndex': 0,
      'startTime': 0,
      'endTime': 0,
      'command': null
    },
    'runningContainers': {
      'scheduler': [],
      'worker': []
    },
    'finishContainers': {
      'scheduler': [
        {
          'type': null,
          'cid': cid_reg,
          'host': '@domain',
          'port': rand_port,
          'rpcPort': 0,
          'status|1': status_arr,
          'logLink': '@url',
          'taskIndex': 0,
          'startTime': 0,
          'endTime': 0,
          'command': null
        }
      ],
      'worker|1-4': [
        {
          'type': null,
          'cid': cid_reg,
          'host': '@domain',
          'port': rand_port,
          'rpcPort': 0,
          'status|1': status_arr,
          'logLink': '@url',
          'taskIndex': 0,
          'startTime': 0,
          'endTime': 0,
          'command': null
        }
      ]
    }
  }
}
)

export default {
  info,
  data
}
