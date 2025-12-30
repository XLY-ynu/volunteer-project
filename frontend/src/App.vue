<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "PingFang SC", "Helvetica Neue", Arial, sans-serif;
  background: #f5f7fa;
  color: #303133;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Element Plus 全局样式覆盖 */
.el-card {
  border: none;
  border-radius: 10px;
}

.el-card__body {
  padding: 16px;
}

.el-button {
  border-radius: 8px;
}

.el-input__wrapper {
  border-radius: 8px;
}

.el-select__wrapper {
  border-radius: 8px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  font-size: 13px;
}

.el-table th.el-table__cell {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  padding: 10px 0;
}

.el-table td.el-table__cell {
  padding: 8px 0;
}

.el-dialog {
  border-radius: 16px;
}

.el-dialog__header {
  border-bottom: 1px solid #ebeef5;
  padding: 16px 20px;
  margin: 0;
}

.el-dialog__body {
  padding: 20px;
}

.el-dialog__footer {
  border-top: 1px solid #ebeef5;
  padding: 12px 20px;
}

.el-pagination {
  justify-content: flex-end;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 通用页面样式 */
.page-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-header {
  border-radius: 10px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.subtitle {
  font-size: 12px;
  color: #909399;
}

.content-card {
  border-radius: 10px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
