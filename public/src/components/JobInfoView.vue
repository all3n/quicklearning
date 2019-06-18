<template>
  <div>
    <h3>app job Info</h3>
    <!-- {{info}} -->

    <el-table :data="info.basic_info" stripe style="width: 100%" :row-class-name="tableRowClassName">
      <el-table-column prop="name" label="任务类型" width="180"></el-table-column>
      <el-table-column prop="value" label="实例数" width="180"></el-table-column>
    </el-table>

    
    <el-table :data="info.jobs" stripe style="width: 100%" :row-class-name="tableRowClassName">
      <el-table-column prop="jobType" label="任务类型" width="180"></el-table-column>
      <el-table-column prop="info.instance_num" label="实例数" width="180"></el-table-column>
      <el-table-column prop="info.cpu_cores" label="cpu_cores" width="180"></el-table-column>
      <el-table-column prop="info.memory_m" label="memory_m" width="180"></el-table-column>
      <el-table-column prop="info.gpu_cores" label="gpu_cores" width="180"></el-table-column>
      <el-table-column prop="info.entry" label="entry" width="180"></el-table-column>
    </el-table>
  </div>
</template>
<script>
export default {
  name: "JobInfoView",
  props: ["info"],
  methods: {
    tableRowClassName({ row, rowIndex }) {
      if (rowIndex === 1) {
        return "warning-row";
      } else if (rowIndex === 3) {
        return "success-row";
      }
      return "";
    }
  },
  data() {
    return {
    };
  }
};
</script>
<style>
.el-table .warning-row {
  background: oldlace;
}

.el-table .success-row {
  background: #f0f9eb;
}
</style>