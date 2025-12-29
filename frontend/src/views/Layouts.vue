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

    <el-card class="template-card" shadow="never">
      <div class="template-head">
        <div>
          <div class="template-title">播放策略模板库</div>
          <div class="template-sub">选择模板快速配置多区布局与轮播策略</div>
        </div>
        <el-button size="small" @click="onCreate">自定义布局</el-button>
      </div>
      <div class="template-grid">
        <div class="template-item" v-for="tpl in templateLibrary" :key="tpl.id" @click="openFromTemplate(tpl)">
          <div class="preview-screen template-preview">
            <div v-for="(area, idx) in tpl.areas" :key="idx" class="preview-area" :style="getAreaStyle(area)">
              {{ idx + 1 }}
            </div>
          </div>
          <div class="template-info">
            <div class="template-name">{{ tpl.name }}</div>
            <div class="template-desc">{{ tpl.description }}</div>
            <div class="template-tags">
              <el-tag v-for="tag in tpl.tags" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
          </div>
          <div class="template-actions" v-if="tpl.raw">
            <el-button size="small" text type="danger" @click.stop="removeTemplate(tpl)">删除</el-button>
          </div>
        </div>
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
            <el-button size="small" @click="openPool(item)"><el-icon><Setting /></el-icon></el-button>
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
        <el-form-item label="模板库">
          <div class="template-grid mini">
            <div
              class="template-item mini"
              v-for="tpl in templateLibrary"
              :key="tpl.id"
              :class="{ active: selectedTemplateId === tpl.id }"
              @click="selectTemplate(tpl)"
            >
              <div class="preview-screen template-preview mini">
                <div v-for="(area, idx) in tpl.areas" :key="idx" class="preview-area" :style="getAreaStyle(area)">
                  {{ idx + 1 }}
                </div>
              </div>
              <div class="template-info">
                <div class="template-name">{{ tpl.name }}</div>
                <div class="template-desc">{{ tpl.description }}</div>
              </div>
            </div>
          </div>
          <div class="template-hint">已选模板：{{ selectedTemplate?.name || '未选择' }}</div>
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
        <el-form-item label="高级调整">
          <el-switch v-model="advancedMode" active-text="手动分区" />
        </el-form-item>

        <el-form-item label="分区设置" v-if="advancedMode">
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
        <el-button @click="saveAsTemplate">存为模板</el-button>
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

    <el-dialog v-model="poolDialog" title="分区素材池" width="760px" destroy-on-close>
      <el-tabs v-model="selectedAreaTab">
        <el-tab-pane v-for="(area, idx) in areas" :key="idx" :name="String(idx)" :label="'区域 ' + (idx + 1)">
          <div class="pool-ops">
            <el-button size="small" @click="addPoolRow(idx)">添加行</el-button>
            <el-button size="small" type="primary" @click="savePool(idx)">保存该分区</el-button>
          </div>
          <el-table :data="poolItems[idx] || []" size="small">
            <el-table-column prop="sortOrder" label="#" width="60">
              <template #default="scope">
                <el-input-number v-model="scope.row.sortOrder" :min="0" :max="999" size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
              <template #default="scope">
                <el-select v-model="scope.row.type" size="small" style="width: 90px">
                  <el-option label="媒体" value="media" />
                  <el-option label="内容" value="content" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="targetId" label="选择" min-width="180">
              <template #default="scope">
                <el-select v-if="scope.row.type === 'media'" v-model="scope.row.targetId" filterable size="small" style="width: 180px" placeholder="选择媒体">
                  <el-option v-for="m in mediaList" :key="m.id" :label="m.name" :value="m.id" />
                </el-select>
                <el-input v-else v-model="scope.row.targetId" size="small" placeholder="填写内容ID" />
              </template>
            </el-table-column>
            <el-table-column prop="displayDuration" label="时长(秒)" width="120">
              <template #default="scope">
                <el-input-number v-model="scope.row.displayDuration" :min="5" :max="600" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button size="small" type="danger" text @click="removePoolRow(idx, scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { createLayout, fetchLayouts, updateLayout, deleteLayout, fetchLayoutTemplates, createLayoutTemplate, updateLayoutTemplate, deleteLayoutTemplate, fetchLayoutPools, saveLayoutPool, fetchMedia } from '../api';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const dialogVisible = ref(false);
const previewVisible = ref(false);
const poolDialog = ref(false);
const editingId = ref<number | null>(null);
const form = ref({ name: '' });
const selectedArea = ref(0);
const previewAreas = ref<any[]>([]);
const selectedAreaTab = ref('0');
const selectedTemplateId = ref('full');
const advancedMode = ref(false);
const savingTemplate = ref(false);
const mediaList = ref<any[]>([]);
const poolItems = ref<Record<number, any[]>>({});

// 分区数据 (x, y, w, h 都是百分比 0-100)
const areas = ref<any[]>([
  { x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }
]);

const builtinTemplates = [
  { id: 'full', name: '全屏独播', description: '单区全屏，独立轮播', tags: ['单区', '独立轮播'], areas: [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }] },
  { id: 'lr', name: '左右双区', description: '左右分屏，各自独立', tags: ['双区', '独立轮播'], areas: [
    { x: 0, y: 0, w: 50, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 },
    { x: 50, y: 0, w: 50, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }
  ] },
  { id: 'tb', name: '上下双区', description: '上下分屏，内容同步节奏', tags: ['双区', '同步排期'], areas: [
    { x: 0, y: 0, w: 100, h: 50, playMode: 'shared', shuffle: false, defaultDuration: 12 },
    { x: 0, y: 50, w: 100, h: 50, playMode: 'shared', shuffle: false, defaultDuration: 12 }
  ] },
  { id: 'grid4', name: '四宫格', description: '四区轮播，信息密集展示', tags: ['四区', '独立轮播'], areas: [
    { x: 0, y: 0, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 },
    { x: 50, y: 0, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 },
    { x: 0, y: 50, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 },
    { x: 50, y: 50, w: 50, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 }
  ] },
  { id: 'main-side', name: '主屏+侧栏', description: '主屏共享，侧栏独立轮播', tags: ['三分区', '主屏共享'], areas: [
    { x: 0, y: 0, w: 70, h: 100, playMode: 'shared', shuffle: false, defaultDuration: 12 },
    { x: 70, y: 0, w: 30, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 },
    { x: 70, y: 50, w: 30, h: 50, playMode: 'split', shuffle: false, defaultDuration: 10 }
  ] },
  { id: 'custom', name: '高级自定义', description: '手动设置分区，适配特殊屏幕', tags: ['自定义', '高级'], areas: [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }] }
];
const templateLibrary = ref<any[]>([...builtinTemplates]);

const selectedTemplate = computed(() => templateLibrary.value.find((tpl: any) => tpl.id === selectedTemplateId.value));

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

const loadMedia = async () => {
  const resp = await fetchMedia(1, 200);
  mediaList.value = resp.data?.data?.records || [];
};

const loadTemplates = async () => {
  try {
    const resp = await fetchLayoutTemplates();
    const remote = resp.data?.data || [];
    templateLibrary.value = [...builtinTemplates, ...remote.map((t: any) => ({
      id: t.id,
      name: t.name,
      description: t.description,
      tags: t.tags ? t.tags.split(',').map((s: string) => s.trim()).filter(Boolean) : [],
      areas: parseAreas(t.layoutJson),
      coverUrl: t.coverUrl,
      raw: t
    }))];
  } catch {
    templateLibrary.value = [...builtinTemplates];
  }
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

const selectTemplate = (tpl: any) => {
  selectedTemplateId.value = tpl.id;
  const source = tpl.areas || templateLibrary.value[0].areas;
  areas.value = JSON.parse(JSON.stringify(source)).map(normalizeArea);
  selectedArea.value = 0;
  advancedMode.value = tpl.id === 'custom';
};

const openFromTemplate = (tpl: any) => {
  editingId.value = null;
  form.value = { name: tpl.name };
  selectTemplate(tpl);
  dialogVisible.value = true;
};

const openPool = async (item: any) => {
  editingId.value = item.id;
  areas.value = parseAreas(item.layoutJson);
  if (!areas.value.length) areas.value = [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }];
  selectedAreaTab.value = '0';
  await loadPoolData(item.id);
  poolDialog.value = true;
};

const loadPoolData = async (layoutId: number) => {
  const resp = await fetchLayoutPools(layoutId);
  const list = resp.data?.data || [];
  const grouped: Record<number, any[]> = {};
  list.forEach((row: any) => {
    const idx = row.areaIndex ? row.areaIndex - 1 : 0;
    if (!grouped[idx]) grouped[idx] = [];
    grouped[idx].push({
      type: row.mediaId ? 'media' : 'content',
      targetId: row.mediaId || row.contentId,
      displayDuration: row.displayDuration || 10,
      sortOrder: row.sortOrder || 0
    });
  });
  areas.value.forEach((_, idx) => {
    if (!grouped[idx]) grouped[idx] = [];
  });
  poolItems.value = grouped;
};

const addPoolRow = (idx: number) => {
  if (!poolItems.value[idx]) poolItems.value[idx] = [];
  poolItems.value[idx].push({ type: 'media', targetId: null, displayDuration: 10, sortOrder: poolItems.value[idx].length });
};

const removePoolRow = (areaIdx: number, rowIdx: number) => {
  if (!poolItems.value[areaIdx]) return;
  poolItems.value[areaIdx].splice(rowIdx, 1);
};

const savePool = async (idx: number) => {
  if (!editingId.value) return;
  const items = (poolItems.value[idx] || []).map((row: any, i: number) => ({
    mediaId: row.type === 'media' ? row.targetId : null,
    contentId: row.type === 'content' ? row.targetId : null,
    displayDuration: row.displayDuration || 10,
    sortOrder: row.sortOrder ?? i
  }));
  await saveLayoutPool(editingId.value, idx + 1, { items });
  ElMessage.success(`区域 ${idx + 1} 已保存`);
};

const addArea = () => {
  if (!advancedMode.value) return;
  areas.value.push({ x: 0, y: 0, w: 30, h: 30, playMode: 'split', shuffle: false, defaultDuration: 12 });
  selectedArea.value = areas.value.length - 1;
};

const removeArea = (idx: number) => {
  if (!advancedMode.value) return;
  areas.value.splice(idx, 1);
  if (selectedArea.value >= areas.value.length) selectedArea.value = areas.value.length - 1;
};

const saveAsTemplate = async () => {
  if (!form.value.name) {
    ElMessage.warning('请先填写布局名称');
    return;
  }
  savingTemplate.value = true;
  try {
    const payload = {
      name: form.value.name,
      description: '自定义布局模板',
      layoutJson: JSON.stringify({ areas: areas.value }),
      tags: '自定义',
      coverUrl: ''
    };
    await createLayoutTemplate(payload);
    ElMessage.success('已保存为模板');
    await loadTemplates();
  } catch (e) {
    ElMessage.error('保存模板失败');
  } finally {
    savingTemplate.value = false;
  }
};

const removeTemplate = async (tpl: any) => {
  if (!tpl?.raw?.id) return;
  await ElMessageBox.confirm('确定删除该模板？', '提示', { type: 'warning' });
  await deleteLayoutTemplate(tpl.raw.id);
  ElMessage.success('已删除模板');
  loadTemplates();
};

const onCreate = () => {
  editingId.value = null;
  form.value = { name: '' };
  selectTemplate(templateLibrary.value[0]);
  dialogVisible.value = true;
};

const onEdit = (item: any) => {
  editingId.value = item.id;
  form.value = { name: item.name };
  areas.value = parseAreas(item.layoutJson);
  if (!areas.value.length) areas.value = [{ x: 0, y: 0, w: 100, h: 100, playMode: 'split', shuffle: false, defaultDuration: 12 }];
  selectedArea.value = 0;
  selectedTemplateId.value = 'custom';
  advancedMode.value = false;
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

onMounted(() => {
  load();
  loadTemplates();
  loadMedia();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.template-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }

.template-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.template-title { font-weight: 600; }
.template-sub { font-size: 12px; color: #909399; }
.template-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px; }
.template-grid.mini { grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); }
.template-item { border: 1px solid #ebeef5; border-radius: 10px; padding: 10px; cursor: pointer; transition: all 0.2s; background: #fff; }
.template-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.template-item.active { border-color: #409eff; box-shadow: 0 0 0 2px rgba(64,158,255,0.15); }
.template-item.mini { padding: 8px; }
.template-preview { margin-bottom: 8px; }
.template-preview.mini { padding-top: 48%; }
.template-name { font-weight: 600; font-size: 13px; }
.template-desc { font-size: 12px; color: #909399; margin-top: 4px; }
.template-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px; }
.template-hint { font-size: 12px; color: #909399; margin-top: 6px; }
.template-actions { display: flex; justify-content: flex-end; }

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
