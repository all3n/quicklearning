import request from '@/utils/request';
function get_job_info(url, params){
    console.log("call get job info");
    return request({
        url: url,
        method: 'get',
        params
    })
}


export default{
    get_job_info
}