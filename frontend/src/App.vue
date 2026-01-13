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

/* Element Plus 图片预览层级修复 - 解决预览变黑问题 */
.el-image-viewer__wrapper {
  z-index: 2147483647 !important;
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100% !important;
  height: 100% !important;
}

.el-image-viewer__mask {
  z-index: 1 !important;
  position: absolute !important;
  width: 100% !important;
  height: 100% !important;
  top: 0 !important;
  left: 0 !important;
  background-color: rgba(0, 0, 0, 0.5) !important;
  opacity: 1 !important;
}

.el-image-viewer__canvas {
  z-index: 2 !important;
  position: absolute !important;
  width: 100% !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.el-image-viewer__img {
  max-width: 100% !important;
  max-height: 100% !important;
  object-fit: contain !important;
}

/* 关闭按钮 - 确保可点击 */
.el-image-viewer__btn.el-image-viewer__close {
  z-index: 10 !important;
  position: absolute !important;
  top: 40px !important;
  right: 40px !important;
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 左右切换按钮 */
.el-image-viewer__btn.el-image-viewer__prev,
.el-image-viewer__btn.el-image-viewer__next {
  z-index: 10 !important;
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 底部操作栏 - 旋转、缩放等按钮 */
.el-image-viewer__actions {
  z-index: 10 !important;
  position: absolute !important;
  bottom: 30px !important;
  left: 50% !important;
  transform: translateX(-50%) !important;
  pointer-events: auto !important;
}

.el-image-viewer__actions__inner {
  pointer-events: auto !important;
  cursor: pointer !important;
}

.el-image-viewer__actions__inner .el-icon {
  pointer-events: auto !important;
  cursor: pointer !important;
}

/* 修复 teleported 模式下的预览问题 */
body > .el-image-viewer__wrapper {
  z-index: 2147483647 !important;
}

/* 确保预览图片可见 */
.el-image__preview {
  cursor: pointer;
}

.el-image__inner {
  display: block !important;
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
