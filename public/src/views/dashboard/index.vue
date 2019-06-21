<template>
  <div v-if="containers" class="dashboard-container">
    <div v-if="info">
      <h3>info</h3>
      <el-table :data="info" style="width: 100%">
        <el-table-column prop="name" label="name" width="200" />
        <el-table-column prop="value" label="value" width="600" />
      </el-table>
    </div>
    <div v-if="containers.masterContainer">
      <h3>Master</h3>
      <a :href="containers.masterContainer.logLink" target="_blank" class="buttonText">
        <el-button type="primary" icon="el-icon-view" circle />
      </a>
    </div>
    <container-table title="runningContainers" :table-data="containers.runningContainers" />
    <container-table title="finishContainers" :table-data="containers.finishContainers" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import job_api from '@/api/job'
import ContainerTable from '@/components/ContainerTable'
import { setTimeout } from 'timers';

export default {
  name: 'Dashboard',
  components: {
    ContainerTable
  },
  data() {
    return {
      info: null,
      containers: null
    }
  },
  computed: {
    ...mapGetters(['name'])
  },
  created() {
    this.init()
  },
  methods: {
     update_containers(){
      job_api.get_job_containers().then(res => {
        this.containers = res.data
        setTimeout(
          this.update_containers
        , 1000)
      })
    },
    init() {
      job_api
        .get_job_info()
        .then(res => {
          this.info = this.$_.map(res.data, function(v, k) {
            return {
              name: k,
              value: v
            }
          })
        })
        .catch(err => {
          console.log('err:', err)
        })
        this.update_containers()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }
  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
</style>
