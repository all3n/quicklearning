import request from '@/utils/request'

function get_job_info(){
    return request({
        url: '/job/info',
        method: 'get'
    })
}


function get_job_containers(){
    return request({
        url: '/job/containers',
        method: 'get'
    })
}
export default {
    get_job_info,
    get_job_containers
}
