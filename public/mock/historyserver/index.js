import job_data from '../common/common_job'
var data = job_data.data
var info = job_data.info

export default [
  {
    url: '/historyserver/list',
    type: 'get',
    response: config => {
      const items = data.items
      return {
        code: 20000,
        data: {
          pagination: {
            "total": items.length,
            "totalPages": 1,
            "pageSize": 10,
            "current": 0
          },
          items: items
        }
      }
    }
  },
  {
    url: '/historyserver/info',
    type: 'get',
    response: config => {
      return {
        code: 20000,
        data: info
      }
    }
  }

]
