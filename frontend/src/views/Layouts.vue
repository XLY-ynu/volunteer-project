<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>布局管理</h3>
          <span class="subtitle">定义终端屏幕的显示布局方式</span>
        </div>
        <el-button type="primary" @click="onCreate"><el-icon><Plus /></el-icon>新增布局</el-button>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <div class="layout-grid" v-if="list.length">
        <div class="layout-item" v-for="item in list" :key="item.id">
          <div class="layout-preview" @click="onPreview(item)">
            <div class="preview-screen">
              <div v-for="(area, idx) in parseAreas(item.layoutJson)" :key="idx" class="preview-area" :style="getAreaStyle(area)">
                {{ idx + 1 }}
              </div>
            </div>
          </div>
          <div class="layout-info">
            <span class="layout-name">{{ item.name }}</span>
            <span class="layout-meta">{{ getAreaCount(item.layoutJson) }}个分区</span>
          </div>
          <div class="layout-actions">
            <el-button size="small" @click="onEdit(item)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button size="small" type="danger" plain @click="onDelete(item.id)"><el-icon><Delete /></el-icon></el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无布局，点击上方按钮创建" />
    </el-card>

    <!-- 新增/编辑布局对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑布局' : '新增布局'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="布局名称" required>
          <el-input v-model="form.name" placeholder="如：单屏全屏、左右分屏" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="选择模板">
          <div class="template-grid">
            <div
              class="template-item"
              v-for="tpl in templates"
              :key="tpl.id"
              :class="{ active: selectedTemplateId === tpl.id }"
              @click="selectTemplate(tpl)"
            >
              <div class="preview-screen mini">
                <div v-for="(area, idx) in tpl.areas" :key="idx" class="preview-area" :style="getAreaStyle(area)">
                  {{ idx + 1 }}
                </div>
              </div>
              <div class="template-name">{{ tpl.name }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="布局预览">
          <div class="editor-preview">
            <div class="preview-screen large">
              <div 
                v-for="(area, idx) in areas" 
                :key="idx" 
                class="preview-area" 
                :class="{ active: selectedTemplateId === 'custom' && selectedAreaIdx === idx }"
                :style="getAreaStyle(area)"
                @click="selectedTemplateId === 'custom' && (selectedAreaIdx = idx)"
              >
                <span class="area-label">区域 {{ idx + 1 }}</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 自定义模式下显示分区设置 -->
        <el-form-item label="分区设置" v-if="selectedTemplateId === 'custom'">
          <div class="custom-settings">
            <div class="area-tabs">
              <div 
                v-for="(area, idx) in areas" 
                :key="idx" 
                class="area-tab"
                :class="{ active: selectedAreaIdx === idx }"
                @click="selectedAreaIdx = idx"
              >
                区域 {{ idx + 1 }}
                <el-icon v-if="areas.length > 1" class="remove-btn" @click.stop="removeArea(idx)"><Close /></el-icon>
              </div>
              <el-button v-if="areas.length < 6" size="small" text type="primary" @click="addArea">
                <el-icon><Plus /></el-icon>添加
              </el-button>
            </div>
            <div class="area-params" v-if="areas[selectedAreaIdx]">
              <div class="param-group">
                <div class="param-item">
                  <span>X位置</span>
                  <el-input-number v-model="areas[selectedAreaIdx].x" :min="0" :max="90" :step="5" size="small" />
                  <span class="unit">%</span>
                </div>
                <div class="param-item">
                  <span>Y位置</span>
                  <el-input-number v-model="areas[selectedAreaIdx].y" :min="0" :max="90" :step="5" size="small" />
                  <span class="unit">%</span>
                </div>
                <div class="param-item">
                  <span>宽度</span>
                  <el-input-number v-model="areas[selectedAreaIdx].w" :min="10" :max="100" :step="5" size="small" />
                  <span class="unit">%</span>
                </div>
                <div class="param-item">
                  <span>高度</span>
                  <el-input-number v-model="areas[selectedAreaIdx].h" :min="10" :max="100" :step="5" size="small" />
                  <span class="unit">%</span>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="布局预览" width="560px">
      <div class="preview-dialog">
        <div class="preview-screen xlarge">
          <div v-for="(area, idx) in previewAreas" :key="idx" class="preview-area" :style="getAreaStyle(area)">
            <span class="area-label">区域 {{ idx + 1 }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { createLayout, fetchLayouts, updateLayout, deleteLayout } from '../api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Close } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const dialogVisible = ref(false);
const previewVisible = ref(false);
const editingId = ref<number | null>(null);
const submitting = ref(false);
const form = ref({ name: '' });
const previewAreas = ref<any[]>([]);
const selectedTemplateId = ref('full');
const selectedAreaIdx = ref(0);

// 分区数据
const areas = ref<any[]>([{ x: 0, y: 0, w: 100, h: 100 }]);

// 内置模板
const templates = [
  { id: 'full', name: '全屏', areas: [{ x: 0, y: 0, w: 100, h: 100 }] },
  { id: 'lr', name: '左右分屏', areas: [
    { x: 0, y: 0, w: 50, h: 100 },
    { x: 50, y: 0, w: 50, h: 100 }
  ] },
  { id: 'tb', name: '上下分屏', areas: [
    { x: 0, y: 0, w: 100, h: 50 },
    { x: 0, y: 50, w: 100, h: 50 }
  ] },
  { id: 'grid4', name: '四宫格', areas: [
    { x: 0, y: 0, w: 50, h: 50 },
    { x: 50, y: 0, w: 50, h: 50 },
    { x: 0, y: 50, w: 50, h: 50 },
    { x: 50, y: 50, w: 50, h: 50 }
  ] },
  { id: 'main-side', name: '主屏+侧栏', areas: [
    { x: 0, y: 0, w: 70, h: 100 },
    { x: 70, y: 0, w: 30, h: 50 },
    { x: 70, y: 50, w: 30, h: 50 }
  ] },
  { id: 'custom', name: '自定义', areas: [{ x: 0, y: 0, w: 100, h: 100 }] }
];

const load = async () => {
  const resp = await fetchLayouts();
  list.value = resp.data?.data || [];
};

const parseAreas = (json: string) => {
  try {
    const obj = JSON.parse(json);
    return obj.areas || [];
  } catch { return []; }
};

const getAreaCount = (json: string) => parseAreas(json).length;

const getAreaStyle = (area: { x: number; y: number; w: number; h: number }) => ({
  left: area.x + '%',
  top: area.y + '%',
  width: area.w + '%',
  height: area.h + '%'
});

const selectTemplate = (tpl: any) => {
  selectedTemplateId.value = tpl.id;
  areas.value = JSON.parse(JSON.stringify(tpl.areas));
  selectedAreaIdx.value = 0;
};

const addArea = () => {
  if (areas.value.length >= 6) return;
  areas.value.push({ x: 0, y: 0, w: 30, h: 30 });
  selectedAreaIdx.value = areas.value.length - 1;
};

const removeArea = (idx: number) => {
  if (areas.value.length <= 1) return;
  areas.value.splice(idx, 1);
  if (selectedAreaIdx.value >= areas.value.length) {
    selectedAreaIdx.value = areas.value.length - 1;
  }
};

const onCreate = () => {
  editingId.value = null;
  form.value = { name: '' };
  selectedTemplateId.value = 'full';
  areas.value = JSON.parse(JSON.stringify(templates[0].areas));
  selectedAreaIdx.value = 0;
  dialogVisible.value = true;
};

const onEdit = (item: any) => {
  editingId.value = item.id;
  form.value = { name: item.name };
  areas.value = parseAreas(item.layoutJson);
  if (!areas.value.length) areas.value = [{ x: 0, y: 0, w: 100, h: 100 }];
  selectedTemplateId.value = 'custom'; // 编辑时默认进入自定义模式
  selectedAreaIdx.value = 0;
  dialogVisible.value = true;
};

const onPreview = (item: any) => {
  previewAreas.value = parseAreas(item.layoutJson);
  previewVisible.value = true;
};

const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该布局？', '提示', { type: 'warning' });
  await deleteLayout(id);
  ElMessage.success('已删除');
  load();
};

const submit = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写布局名称');
    return;
  }
  submitting.value = true;
  try {
    const layoutJson = JSON.stringify({ areas: areas.value });
    if (editingId.value) {
      await updateLayout(editingId.value, { name: form.value.name, layoutJson });
      ElMessage.success('已更新');
    } else {
      await createLayout({ name: form.value.name, layoutJson });
      ElMessage.success('已创建');
    }
    dialogVisible.value = false;
    load();
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  load();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }

.layout-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.layout-item { background: #fff; border: 1px solid #ebeef5; border-radius: 12px; overflow: hidden; transition: box-shadow 0.3s; }
.layout-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.layout-preview { padding: 16px; cursor: pointer; }
.preview-screen { position: relative; width: 100%; padding-top: 56.25%; background: #f5f7fa; border-radius: 8px; overflow: hidden; }
.preview-screen.mini { padding-top: 56.25%; }
.preview-screen.large { width: 100%; max-width: 500px; height: 280px; padding-top: 0 !important; margin: 0 auto; background: #e8eaed; }
.preview-screen.xlarge { width: 100%; max-width: 500px; height: 280px; padding-top: 0 !important; margin: 0 auto; background: #e8eaed; }
.preview-area { position: absolute; background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); border: 2px solid #fff; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 14px; box-sizing: border-box; transition: all 0.2s; min-width: 30px; min-height: 30px; }
.preview-area { position: absolute; background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); border: 2px solid #fff; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 14px; box-sizing: border-box; transition: all 0.2s; }
.preview-area.active { border-color: #e6a23c; box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.4); }
.area-label { text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.layout-info { padding: 12px 16px; border-top: 1px solid #ebeef5; }
.layout-name { font-weight: 500; color: #303133; display: block; }
.layout-meta { font-size: 12px; color: #909399; }
.layout-actions { padding: 10px 16px; border-top: 1px solid #ebeef5; display: flex; gap: 8px; }

.template-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; }
.template-item { border: 2px solid #ebeef5; border-radius: 8px; padding: 8px; cursor: pointer; transition: all 0.2s; text-align: center; }
.template-item:hover { border-color: #c0c4cc; }
.template-item.active { border-color: #409eff; background: #ecf5ff; }
.template-item .preview-screen { margin-bottom: 6px; }
.template-name { font-size: 12px; color: #606266; }

.editor-preview { display: flex; justify-content: center; width: 100%; }
.preview-dialog { display: flex; justify-content: center; padding: 20px; width: 100%; }
.preview-dialog .preview-screen { width: 100%; }

/* 自定义设置 */
.custom-settings { background: #f5f7fa; border-radius: 8px; padding: 16px; }
.area-tabs { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; align-items: center; }
.area-tab { padding: 6px 12px; background: #fff; border: 1px solid #dcdfe6; border-radius: 6px; cursor: pointer; font-size: 13px; display: flex; align-items: center; gap: 6px; }
.area-tab:hover { border-color: #409eff; }
.area-tab.active { background: #409eff; color: #fff; border-color: #409eff; }
.area-tab .remove-btn { font-size: 12px; cursor: pointer; }
.area-tab.active .remove-btn:hover { color: #ffd; }

.area-params { background: #fff; border-radius: 6px; padding: 12px; }
.param-group { display: flex; gap: 16px; flex-wrap: wrap; }
.param-item { display: flex; align-items: center; gap: 8px; }
.param-item span:first-child { font-size: 13px; color: #606266; min-width: 45px; }
.param-item .unit { font-size: 12px; color: #909399; }
.param-item .el-input-number { width: 100px; }
</style>
