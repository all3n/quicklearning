import Mock from 'mockjs'
import job_data from './common/common_job'

export default [
    {
        url: '/job/info',
        type: 'get',
        response: config => {
            return {
                code: 20000,
                data: job_data.info
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
