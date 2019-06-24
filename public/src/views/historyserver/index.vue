<template>
  <div v-if="info" class="dashboard-container">
    <h3>info</h3>
    <el-table :data="info" style="width: 100%">
      <el-table-column prop="app_id" label="app_id" width="300" />
      <el-table-column prop="app_id" label="link" width="300">
        <template slot-scope="scope">
          <router-link :to="{ name: 'info', params: { appid: scope.row.app_id }}">
            <el-button icon="el-icon-search" circle />
          </router-link>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import hs_api from '@/api/historyserver/index'
console.log(hs_api)

export default {
  name: 'Dashboard',
  components: {},
  data() {
    return {
      info: null
    }
  },
  computed: {
    ...mapGetters(['name'])
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      hs_api
        .get_history_list()
        .then(res => {
          console.log(res.data)
          this.info = res.data.items
        })
        .catch(err => {
          console.log('err:', err)
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
