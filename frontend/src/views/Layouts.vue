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
            <el-button size="small" @click="onEdit(item)"><el-icon><Edit /></el-icon></el-button>
            <el-button size="small" type="danger" @click="onDelete(item.id)"><el-icon><Delete /></el-icon></el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无布局，点击上方按钮创建" />
    </el-card>

    <!-- 新增/编辑布局对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑布局' : '新增布局'" width="700px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="布局名称" required>
          <el-input v-model="form.name" placeholder="如：单屏全屏、左右分屏" />
        </el-form-item>
        <el-form-item label="布局类型">
          <el-radio-group v-model="layoutType" @change="onLayoutTypeChange">
            <el-radio-button value="single">单屏全屏</el-radio-button>
            <el-radio-button value="lr">左右分屏</el-radio-button>
            <el-radio-button value="tb">上下分屏</el-radio-button>
            <el-radio-button value="grid4">四宫格</el-radio-button>
            <el-radio-button value="main-side">主屏+侧边</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 可视化预览 -->
        <el-form-item label="布局预览">
          <div class="editor-preview">
            <div class="preview-screen large">
              <div v-for="(area, idx) in areas" :key="idx" class="preview-area" :style="getAreaStyle(area)" :class="{ active: selectedArea === idx }" @click="selectedArea = idx">
                <span class="area-label">区域 {{ idx + 1 }}</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="播放策略" v-if="areas[selectedArea]">
          <div class="area-params">
            <div class="param-row">
              <span>轮播模式</span>
              <el-select v-model="areas[selectedArea].playMode" size="small" style="width: 160px">
                <el-option label="独立轮播" value="split" />
                <el-option label="共享轮播" value="shared" />
              </el-select>
            </div>
            <div class="param-row">
              <span>默认时长(秒)</span>
              <el-input-number v-model="areas[selectedArea].defaultDuration" :min="5" :max="60" size="small" />
            </div>
            <div class="param-row">
              <span>随机播放</span>
              <el-switch v-model="areas[selectedArea].shuffle" />
            </div>
          </div>
        </el-form-item>

        <!-- 分区参数调整（自定义模式） -->
        <el-form-item label="分区设置" v-if="layoutType === 'custom'">
          <div class="area-settings">
            <div class="area-list">
              <div v-for="(area, idx) in areas" :key="idx" class="area-item" :class="{ active: selectedArea === idx }" @click="selectedArea = idx">
                <span>区域 {{ idx + 1 }}</span>
                <el-button size="small" type="danger" link @click.stop="removeArea(idx)" v-if="areas.length > 1"><el-icon><Delete /></el-icon></el-button>
              </div>
              <el-button size="small" @click="addArea" v-if="areas.length < 6"><el-icon><Plus /></el-icon>添加分区</el-button>
            </div>
            <div class="area-params" v-if="areas[selectedArea]">
              <div class="param-row">
                <span>X位置(%)</span>
                <el-input-number v-model="areas[selectedArea].x" :min="0" :max="90" :step="5" size="small" />
              </div>
              <div class="param-row">
                <span>Y位置(%)</span>
                <el-input-number v-model="areas[selectedArea].y" :min="0" :max="90" :step="5" size="small" />
              </div>
              <div class="param-row">
                <span>宽度(%)</span>
                <el-input-number v-model="areas[selectedArea].w" :min="10" :max="100" :step="5" size="small" />
              </div>
              <div class="param-row">
                <span>高度(%)</span>
                <el-input-number v-model="areas[selectedArea].h" :min="10" :max="100" :step="5" size="small" />
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">{{ editingId ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" title="布局预览" width="600px">
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const dialogVisible = ref(false);
const previewVisible = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ name: '' });
const layoutType = ref('single');
const selectedArea = ref(0);
const previewAreas = ref<any[]>([]);

// 分区数据 (x, y, w, h 都是百分比 0-100)
const areas = ref<any[]>([
  { x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }
]);

// 预设布局模板
const layoutTemplates: Record<string, any[]> = {
  single: [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }],
  lr: [
    { x: 0, y: 0, w: 50, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 50, y: 0, w: 50, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }
  ],
  tb: [
    { x: 0, y: 0, w: 100, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 0, y: 50, w: 100, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 }
  ],
  grid4: [
    { x: 0, y: 0, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 50, y: 0, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 0, y: 50, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 50, y: 50, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 12 }
  ],
  'main-side': [
    { x: 0, y: 0, w: 70, h: 100, playMode: 'shared', shuffle: false, defaultDuration: 12 },
    { x: 70, y: 0, w: 30, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 },
    { x: 70, y: 50, w: 30, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 }
  ],
  custom: [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }]
};

const normalizeArea = (area: any) => ({
  x: area.x ?? 0,
  y: area.y ?? 0,
  w: area.w ?? 100,
  h: area.h ?? 100,
  playMode: area.playMode || 'split',
  shuffle: area.shuffle ?? false,
  defaultDuration: area.defaultDuration ?? 12
});

const load = async () => {
  const resp = await fetchLayouts();
  list.value = resp.data?.data || [];
};

const parseAreas = (json: string) => {
  try {
    const obj = JSON.parse(json);
    return (obj.areas || []).map(normalizeArea);
  } catch { return []; }
};

const getAreaCount = (json: string) => parseAreas(json).length;

const getAreaStyle = (area: { x: number; y: number; w: number; h: number }) => ({
  left: area.x + '%',
  top: area.y + '%',
  width: area.w + '%',
  height: area.h + '%'
});

const onLayoutTypeChange = (type: string) => {
  areas.value = JSON.parse(JSON.stringify(layoutTemplates[type] || layoutTemplates.single));
  selectedArea.value = 0;
};

const addArea = () => {
  areas.value.push({ x: 0, y: 0, w: 30, h: 30, playMode: 'split', shuffle: false, defaultDuration: 12 });
  selectedArea.value = areas.value.length - 1;
};

const removeArea = (idx: number) => {
  areas.value.splice(idx, 1);
  if (selectedArea.value >= areas.value.length) selectedArea.value = areas.value.length - 1;
};

const onCreate = () => {
  editingId.value = null;
  form.value = { name: '' };
  layoutType.value = 'single';
  areas.value = JSON.parse(JSON.stringify(layoutTemplates.single));
  selectedArea.value = 0;
  dialogVisible.value = true;
};

const onEdit = (item: any) => {
  editingId.value = item.id;
  form.value = { name: item.name };
  areas.value = parseAreas(item.layoutJson);
  if (!areas.value.length) areas.value = [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }];
  layoutType.value = 'custom';
  selectedArea.value = 0;
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
};

onMounted(load);
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

.layout-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.layout-item { background: #fff; border: 1px solid #ebeef5; border-radius: 12px; overflow: hidden; transition: box-shadow 0.3s; }
.layout-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.layout-preview { padding: 16px; cursor: pointer; }
.preview-screen { position: relative; width: 100%; padding-top: 56.25%; background: #f5f7fa; border-radius: 8px; overflow: hidden; }
.preview-screen.large { padding-top: 50%; }
.preview-screen.xlarge { padding-top: 56.25%; }
.preview-area { position: absolute; background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); border: 2px solid #fff; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 14px; box-sizing: border-box; transition: all 0.2s; }
.preview-area.active { border-color: #e6a23c; box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.3); }
.area-label { text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.layout-info { padding: 12px 16px; border-top: 1px solid #ebeef5; }
.layout-name { font-weight: 500; color: #303133; display: block; }
.layout-meta { font-size: 12px; color: #909399; }
.layout-actions { padding: 8px 16px; border-top: 1px solid #ebeef5; display: flex; gap: 8px; }

.editor-preview { display: flex; justify-content: center; }
.editor-preview .preview-screen { width: 400px; padding-top: 225px; }

.area-settings { display: flex; gap: 20px; }
.area-list { display: flex; flex-direction: column; gap: 8px; min-width: 120px; }
.area-item { padding: 8px 12px; background: #f5f7fa; border-radius: 6px; cursor: pointer; display: flex; justify-content: space-between; align-items: center; }
.area-item.active { background: #ecf5ff; color: #409eff; }
.area-params { flex: 1; display: flex; flex-direction: column; gap: 12px; }
.param-row { display: flex; align-items: center; gap: 12px; }
.param-row span { width: 70px; font-size: 13px; color: #606266; }
.param-row .el-input-number { width: 120px; }

.preview-dialog { display: flex; justify-content: center; padding: 20px; }
.preview-dialog .preview-screen { width: 500px; padding-top: 281px; }
</style>
