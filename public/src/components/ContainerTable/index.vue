<template>
  <div>
    <h3>{{ title }}</h3>
    <div v-for="(value, name, index) in tableData" :key="index">
      <div v-if="value.length > 0">
        <h4>{{ name }}</h4>
        <el-table :data="value" style="width: 100%">
          <el-table-column prop="cid" label="containerId" width="200" />
          <el-table-column prop="host" label="host" width="200" />
          <el-table-column prop="port" label="port" width="100" />
          <el-table-column prop="status" label="status" width="100">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status | statusFilter">{{ scope.row.status|statusMsgFilter }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="logLink" width="300" show-overflow-tooltip>
            <template slot-scope="scope">
              <a :href="scope.row.logLink" target="_blank" class="buttonText">
                <el-button type="primary" icon="el-icon-view" circle />
              </a>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>
<script>
const statusMsgMap = {
  0: 'Running',
  1: 'Success',
  2: 'Fail'
}
const statusClassMap = {
  0: 'gray',
  1: 'success',
  2: 'danger'
}

export default {
  name: 'ContainerTable',
  filters: {
    statusFilter(status) {
      return statusClassMap[status]
    },
    statusMsgFilter(status) {
      return statusMsgMap[status]
    }
  },
  props: {
    title: { type: String, default: 'containerTable' },
    tableData: {
      type: Object,
      default: function() {
        return null
      }
    }
  }
}
</script>

