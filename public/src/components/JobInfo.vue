<template>
  <div v-if="info">
    <div>
      <h3>
        {{ info.jobId }}
        <el-tag :type="info.status | statusFilter">{{ info.status }}</el-tag>
      </h3>
      <h3>{{info.lastUpdate}}</h3>
      <el-table :data="info.job | obj2arr" style="width: 100%">
        <el-table-column prop="name" label="name" width="200" />
        <el-table-column prop="value" label="value" width="600" />
      </el-table>
    </div>
    <div v-if="info.containers">
      <div v-if="info.containers.masterContainer">
        <h3>Master</h3>
        <a :href="info.containers.masterContainer.logLink" target="_blank" class="buttonText">
          <el-button type="primary" icon="el-icon-view" circle />
        </a>
      </div>
      <div v-if="info.containers.runningContainers">
        <container-table title="runningContainers" :table-data="info.containers.runningContainers" />
      </div>
      <div v-if="info.containers.finishContainers">
        <container-table title="finishContainers" :table-data="info.containers.finishContainers" />
      </div>
    </div>
  </div>
</template>
<script>
import ContainerTable from '@/components/ContainerTable'
const statusClassMap = {
  RUNNING: 'gray',
  SUCCESS: 'success',
  FAIL: 'danger'
}
export default {
  name: 'JobInfo',
  filters: {
    statusFilter(status) {
      return statusClassMap[status]
    },
    obj2arr(obj) {
      var arr = []
      for (var k in obj) {
        arr.push({
          name: k,
          value: JSON.stringify(obj[k])
        })
      }
      return arr
    }
  },
  components: {
    ContainerTable
  },
  props: {
    info: {
      type: Object,
      default: function() {
        return {
          jobId: '',
          status: '',
          job: {},
          containers: null
        }
      }
    }
  }
}
</script>
