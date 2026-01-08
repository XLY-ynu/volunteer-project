<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: "PingFang SC", "Helvetica Neue", Arial, sans-serif; background: #f5f7fa; color: #303133; }
.el-card { border: none; border-radius: 10px; }
.el-button { border-radius: 8px; }
.el-input__wrapper { border-radius: 8px; }
.el-table { border-radius: 8px; overflow: hidden; font-size: 13px; }
.el-table th.el-table__cell { background: #f5f7fa; color: #606266; font-weight: 600; padding: 10px 0; }
.el-dialog { border-radius: 16px; }
::-webkit-scrollbar { width: 8px; height: 8px; }
::-webkit-scrollbar-track { background: #f1f1f1; border-radius: 4px; }
::-webkit-scrollbar-thumb { background: #c0c4cc; border-radius: 4px; }
</style>
