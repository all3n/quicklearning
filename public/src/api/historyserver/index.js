import request from '@/utils/request'

function get_history_list() {
  return request({
    url: '/historyserver/list',
    method: 'get'
  })
}

function get_history_info(appid) {
  return request({
    url: '/historyserver/info/:appid',
    method: 'get',
    params: { 'appid': appid }
  })
}
export default {
  get_history_list,
  get_history_info
}
