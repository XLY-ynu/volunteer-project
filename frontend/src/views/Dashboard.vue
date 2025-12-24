<template>
  <div class="page">
    <div class="header">
      <h2>仪表盘</h2>
      <div class="actions">
        <el-button type="primary" plain>新建内容</el-button>
        <el-button type="success" plain>上传资源</el-button>
        <el-button type="warning" plain>创建播放列表</el-button>
      </div>
    </div>
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card>
          <div class="card-title">资源管理</div>
          <p>上传/下载视频、图片、网页等资源；分类管理六大菜单与子菜单。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="card-title">播放设置</div>
          <p>规划布局、添加文件、参数设置、播放列表发布。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <div class="card-title">终端管理</div>
          <p>列表分发、插播管理、运行监控、属性设置。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.card-title {
  font-weight: bold;
  margin-bottom: 6px;
}
</style>
