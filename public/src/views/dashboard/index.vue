<template>
  <div class="dashboard-container">
    <h3>info</h3>
    <ul>
      <li v-for="(value, name, index) in info">{{ index}}:{{name}}:{{value}}</li>
    </ul>
    <h3>runningContainers</h3>
    <ul>
      <li v-for="(value, name, index) in containers.runningContainers">{{ index}}:{{name}}:{{value}}</li>
    </ul>
    <h3>finishContainers</h3>

    <ul>
      <li v-for="(value, name, index) in containers.finishContainers">{{ index}}:{{name}}:{{value}}</li>
    </ul>
  </div>
</template>

<script>
import { mapGetters } from "vuex";
import job_api from "@/api/job";

export default {
  name: "Dashboard",
  props: ["info", "containers"],
  created() {
    this.init();
  },
  computed: {
    ...mapGetters(["name"])
  },
  methods: {
    init() {
      job_api
        .get_job_info()
        .then(res => {
          console.log("success job info");
          this.info = res.data;
        })
        .catch(err => {
          console.log("err:", err);
        });

      job_api.get_job_containers().then(res => {
        this.containers = res.data;
      });
    }
  }
};
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
