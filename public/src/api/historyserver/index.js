import request from '@/utils/request'

function get_history_list(page, page_size) {
  return request({
    url: '/historyserver/list',
    method: 'get',
    params: {
      'page': page,
      'page_size': page_size
    }
  })
}

function get_history_info(appid) {
  return request({
    url: '/historyserver/info',
    method: 'get',
    params: { 'appid': appid }
  })
}
export default {
  get_history_list,
  get_history_info
}
