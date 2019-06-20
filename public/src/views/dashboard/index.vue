<template>
  <div class="dashboard-container" v-if="containers">
    <div v-if="info">
      <h3>info</h3>
      <el-table :data="info" style="width: 100%">
        <el-table-column prop="name" label="name" width="200"></el-table-column>
        <el-table-column prop="value" label="value" width="600"></el-table-column>
      </el-table>
    </div>
    <div v-if="containers.masterContainer">
      <h3>Master</h3>
      <a :href="containers.masterContainer.logLink" target="_blank" class="buttonText">
        <el-button type="primary" icon="el-icon-view" circle></el-button>
      </a>
    </div>
    <container-table
      title="runningContainers"
      :tableData="containers.runningContainers"
    />

    <container-table
      title="finishContainers"
      :tableData="containers.finishContainers"
    />
    
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import job_api from "@/api/job";
import ContainerTable from '@/components/ContainerTable';

export default {
  name: "Dashboard",
  components: {
    ContainerTable
  },
  props: ['info', 'containers'],
  created() {
    this.init()
  },
  computed: {
    ...mapGetters(['name'])
  },
  methods: {
    init() {
      job_api
        .get_job_info()
        .then(res => {
          this.info = _.map(res.data, function(v, k) {
            return {
              name: k,
              value: v
            }
          })
        })
        .catch(err => {
          console.log('err:', err)
        })

      job_api.get_job_containers().then(res => {
        this.containers = res.data
      })
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
