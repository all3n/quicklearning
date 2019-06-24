<template>
  <div class="dashboard-container">
    <job-info :info="info" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import hs_api from '@/api/historyserver/index'
import JobInfo from '@/components/JobInfo'
console.log(hs_api)

export default {
  name: 'HistoryInfo',
  components: {
    JobInfo
  },
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
        .get_history_info(this.$route.params.appid)
        .then(res => {
          console.log(res.data)
          this.info = res.data
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
