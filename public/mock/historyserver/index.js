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
          total: items.length,
          items: items
        }
      }
    }
  },
  {
    url: '/historyserver/info/:appid',
    type: 'get',
    response: config => {
      return {
        code: 20000,
        data: info
      }
    }
  }

]
