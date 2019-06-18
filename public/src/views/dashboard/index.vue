<template>
  <JobInfoView v-bind:info="info" />
</template>


<script>
import job_api from "@/api/job"
import JobInfoView from '@/components/JobInfoView'

export default {
  name: "dashboardMain",
  components: {
      JobInfoView
  },
  data(){
      return {
          info: {}
      }
  },
  created() {
    console.log('dashboard created!')
    this.init()
  },
  methods: {
    init() {
      job_api
        .get_job_info('/api/job_info', {})
        .then(res => {
          var job_info = res.data
          var info =  {};
          var jobs = [];

          
          for(var jt in job_info.jobs){
            jobs.push({'jobType': jt, 'info': job_info.jobs[jt]});
          }

          var basic_info = []
          for(var k in job_info){
            if(k != "jobs"){
              basic_info.push({"name": k, "value": job_info[k]})
            }
          }

          info['jobs'] = jobs
          info['basic_info'] = basic_info

          this.info = info;
        })
        .catch(err => {
          console.log(err)
        })
    }
  }
}
</script>

<style lang="sass" scoped>

</style>

