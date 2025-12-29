<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>内容管理</h3>
          <span class="subtitle">发布和管理图文内容</span>
        </div>
        <div class="header-actions">
          <el-button @click="openHeadlineDialog">头条排序</el-button>
          <el-button @click="openRecommendDialog">推荐排序</el-button>
          <el-button @click="sortByWeight">权重排序</el-button>
          <el-button :disabled="selectedRows.length === 0" @click="openWeightBatchDialog">批量调整权重</el-button>
          <el-button @click="openRecommendPreviewDialog">轮播顺序预览</el-button>
          <el-button @click="openConfigDialog">轮播配置</el-button>
          <el-button @click="openStrategyDialog">推荐策略可视化</el-button>
          <el-button @click="previewPortal">推荐预览</el-button>
          <el-button type="primary" @click="onCreate"><el-icon><Plus /></el-icon>发布内容</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-form :inline="true" :model="filter" class="filter" @submit.prevent>
        <el-form-item label="分类">
          <el-select v-model="filter.categoryId" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.published" placeholder="全部" clearable style="width: 120px">
            <el-option :value="true" label="已发布" />
            <el-option :value="false" label="草稿" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="标题/摘要" style="width: 180px" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
        </el-form-item>
      </el-form>

      <el-table ref="tableRef" :data="list" stripe @selection-change="onSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column label="排序" width="60">
          <template #default>
            <el-icon class="drag-handle"><Rank /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="100">
          <template #default="scope">
            <div class="cover-cell">
              <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" fit="cover" class="cover-img" />
              <div v-else class="cover-empty"><el-icon><Picture /></el-icon></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="scope">
            <div class="title-cell">
              <span class="title-text">{{ scope.row.title }}</span>
              <span class="summary-text">{{ scope.row.summary || '暂无摘要' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="scope">
            <el-tag size="small">{{ getCategoryName(scope.row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="published" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">{{ scope.row.published ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标记" width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.headline" size="small" type="danger">头条</el-tag>
            <el-tag v-if="scope.row.recommended" size="small" type="warning" style="margin-left: 6px">推荐</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recommendWeight" label="权重" width="160">
          <template #default="scope">
            <div class="weight-cell">
              <el-progress :percentage="weightPercent(scope.row)" :show-text="false" />
              <span class="weight-value">{{ scope.row.recommendWeight ?? 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="快捷" width="160">
          <template #default="scope">
            <div class="quick-flags">
              <el-switch
                v-model="scope.row.headline"
                active-text="头条"
                inactive-text="头条"
                size="small"
                @change="toggleFlags(scope.row)"
              />
              <el-switch
                v-model="scope.row.recommended"
                active-text="推荐"
                inactive-text="推荐"
                size="small"
                @change="toggleFlags(scope.row)"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="edit(scope.row)" title="编辑"><el-icon><Edit /></el-icon></el-button>
              <el-button size="small" type="danger" @click="onDelete(scope.row.id)" title="删除"><el-icon><Delete /></el-icon></el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination layout="total, prev, pager, next" :total="total" :page-size="size" :current-page="page" @current-change="onPage" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑内容' : '发布内容'" width="680px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入内容标题" />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" placeholder="简短描述内容" />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-upload-area">
            <div class="cover-preview" v-if="form.coverUrl">
              <el-image :src="form.coverUrl" fit="cover" class="preview-img" />
              <div class="cover-actions">
                <el-button size="small" type="danger" @click="form.coverUrl = ''"><el-icon><Delete /></el-icon>移除</el-button>
              </div>
            </div>
            <el-upload v-else :headers="uploadHeaders" action="/api/media/upload-cover" :show-file-list="false" :on-success="onCoverUploaded" :before-upload="beforeCoverUpload" accept="image/*" class="cover-uploader">
              <div class="upload-trigger">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
          </div>
          <div class="cover-tip">封面图片仅作为内容附属，不会出现在媒体库中</div>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.body" type="textarea" :rows="6" placeholder="请输入正文内容" />
        </el-form-item>
        <el-form-item label="发布">
          <el-switch v-model="form.published" active-text="立即发布" inactive-text="存为草稿" />
        </el-form-item>
        <el-form-item label="标记">
          <el-checkbox v-model="form.headline">头条</el-checkbox>
          <el-checkbox v-model="form.recommended" style="margin-left: 12px">推荐</el-checkbox>
        </el-form-item>
        <el-form-item label="推荐权重">
          <el-input-number v-model="form.recommendWeight" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="headlineDialog" title="头条排序" width="520px">
      <draggable v-model="headlineList" item-key="id" handle=".drag-handle">
        <template #item="{ element }">
          <div class="recommend-item">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="recommend-title">{{ element.title }}</span>
            <el-tag size="small" type="danger">头条</el-tag>
          </div>
        </template>
      </draggable>
      <template #footer>
        <el-button @click="headlineDialog = false">取消</el-button>
        <el-button type="primary" @click="saveHeadlineOrder">保存排序</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recommendDialog" title="推荐集合排序" width="520px">
      <draggable v-model="recommendList" item-key="id" handle=".drag-handle">
        <template #item="{ element }">
          <div class="recommend-item">
            <el-icon class="drag-handle"><Rank /></el-icon>
            <span class="recommend-title">{{ element.title }}</span>
            <el-tag size="small" type="warning">推荐</el-tag>
          </div>
        </template>
      </draggable>
      <template #footer>
        <el-button @click="recommendDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRecommendOrder">保存排序</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recommendPreviewDialog" title="轮播顺序预览" width="520px">
      <div class="preview-hint">轮播数量：{{ configForm.recommendCount }} 条</div>
      <div class="preview-list">
        <div v-for="(item, index) in recommendPreviewList" :key="item.id" class="preview-item" :class="{ inactive: index >= configForm.recommendCount }">
          <span class="preview-index">{{ index + 1 }}</span>
          <span class="preview-title">{{ item.title }}</span>
          <el-tag size="small" :type="index < configForm.recommendCount ? 'success' : 'info'">
            {{ index < configForm.recommendCount ? '轮播' : '不轮播' }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="recommendPreviewDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="configDialog" title="轮播配置" width="420px">
      <el-form label-width="120px">
        <el-form-item label="推荐轮播(秒)">
          <el-input-number v-model="configForm.recommendIntervalSec" :min="3" :max="30" />
        </el-form-item>
        <el-form-item label="轮播数量">
          <el-input-number v-model="configForm.recommendCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="推荐策略">
          <el-select v-model="configForm.recommendStrategy" placeholder="选择策略" style="width: 180px">
            <el-option label="全站推荐" value="global" />
            <el-option label="本栏目优先" value="prefer" />
            <el-option label="仅本栏目" value="filter" />
          </el-select>
        </el-form-item>
        <el-form-item label="预览轮询(秒)">
          <el-input-number v-model="configForm.previewIntervalSec" :min="5" :max="60" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialog = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="weightBatchDialog" title="批量调整权重" width="420px">
      <el-form label-width="90px">
        <el-form-item label="调整方式">
          <el-radio-group v-model="weightBatchForm.mode">
            <el-radio label="set">设为</el-radio>
            <el-radio label="inc">增加</el-radio>
            <el-radio label="dec">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权重值">
          <el-input-number v-model="weightBatchForm.value" :min="0" :max="999" />
          <span class="weight-hint">已选 {{ selectedRows.length }} 条</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="weightBatchDialog = false">取消</el-button>
        <el-button type="primary" @click="applyWeightBatch">应用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="strategyDialog" title="推荐策略调优" width="520px">
      <div class="strategy-row">
        <span class="label">本栏目优先</span>
        <el-slider v-model="strategyWeights.prefer" :min="0" :max="100" show-input />
      </div>
      <div class="strategy-row">
        <span class="label">仅本栏目</span>
        <el-slider v-model="strategyWeights.filter" :min="0" :max="100" show-input />
      </div>
      <div class="strategy-row">
        <span class="label">全站推荐</span>
        <el-slider v-model="strategyWeights.global" :min="0" :max="100" show-input />
      </div>
      <div class="strategy-preview">
        <div class="bar" :style="{ width: preferPercent + '%' }">本栏目 {{ preferPercent }}%</div>
        <div class="bar secondary" :style="{ width: filterPercent + '%' }">仅本栏目 {{ filterPercent }}%</div>
        <div class="bar info" :style="{ width: globalPercent + '%' }">全站 {{ globalPercent }}%</div>
      </div>
      <template #footer>
        <el-button @click="strategyDialog = false">取消</el-button>
        <el-button type="primary" @click="saveStrategy">保存策略</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed, nextTick, onBeforeUnmount } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createContent, deleteContent, fetchCategories, fetchContent, updateContent, updateContentFlags, updateContentOrder, updateContentWeights, fetchContentConfig, updateContentConfig, fetchRecommendedContent, fetchHeadlineContent } from '../api';
import { useUserStore } from '../stores/user';
import { Plus, Search, Edit, Delete, Picture, Rank } from '@element-plus/icons-vue';
import Sortable from 'sortablejs';
import draggable from 'vuedraggable';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const categories = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const user = useUserStore();
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${user.token}` }));
const tableRef = ref();
const sortableRef = ref<Sortable | null>(null);
const recommendDialog = ref(false);
const recommendList = ref<any[]>([]);
const recommendPreviewDialog = ref(false);
const headlineDialog = ref(false);
const headlineList = ref<any[]>([]);
const configDialog = ref(false);
const configForm = ref({ recommendIntervalSec: 6, recommendCount: 6, recommendStrategy: 'prefer', previewIntervalSec: 10 });
const strategyDialog = ref(false);
const strategyWeights = ref({ prefer: 60, filter: 20, global: 20 });
const selectedRows = ref<any[]>([]);
const weightBatchDialog = ref(false);
const weightBatchForm = ref({ mode: 'set', value: 0 });

const recommendPreviewList = computed(() => {
  if (!recommendList.value.length) return [];
  return [...recommendList.value];
});

const preferPercent = computed(() => Math.min(100, Math.max(0, strategyWeights.value.prefer || 0)));
const filterPercent = computed(() => Math.min(100, Math.max(0, strategyWeights.value.filter || 0)));
const globalPercent = computed(() => Math.min(100, Math.max(0, strategyWeights.value.global || 0)));

const form = ref({
  title: '',
  categoryId: undefined as number | undefined,
  summary: '',
  coverUrl: '',
  body: '',
  published: true,
  headline: false,
  recommended: false,
  recommendWeight: 0
});

const filter = ref<{ categoryId?: number; published?: boolean; keyword?: string }>({});

const maxWeight = computed(() => {
  return Math.max(1, ...list.value.map((item) => Number(item.recommendWeight || 0)));
});

const load = async () => {
  const resp = await fetchContent(page.value, size.value, filter.value.categoryId, filter.value.published, filter.value.keyword);
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
  initSortable();
};

const loadCategories = async () => {
  const resp = await fetchCategories();
  categories.value = resp.data?.data || [];
};

const getCategoryName = (id: number) => {
  const cat = categories.value.find(c => c.id === id);
  return cat?.name || '未分类';
};

const formatDate = (d: string) => d ? d.replace('T', ' ').substring(0, 16) : '-';

const onCreate = () => {
  editingId.value = null;
  form.value = { title: '', categoryId: undefined, summary: '', coverUrl: '', body: '', published: true, headline: false, recommended: false, recommendWeight: 0 };
  dialogVisible.value = true;
};

const edit = (row: any) => {
  editingId.value = row.id;
  form.value = {
    title: row.title,
    categoryId: row.categoryId,
    summary: row.summary,
    coverUrl: row.coverUrl,
    body: row.body,
    published: row.published,
    headline: row.headline,
    recommended: row.recommended,
    recommendWeight: row.recommendWeight ?? 0
  };
  dialogVisible.value = true;
};

const beforeCoverUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片'); return false; }
  if (file.size / 1024 / 1024 > 10) { ElMessage.error('图片不能超过10MB'); return false; }
  return true;
};

const onCoverUploaded = (res: any) => {
  if (res.success && res.data?.url) {
    form.value.coverUrl = res.data.url;
    ElMessage.success('封面上传成功');
  } else {
    ElMessage.error(res.message || '上传失败');
  }
};

const submit = async () => {
  if (!form.value.title || !form.value.categoryId) {
    ElMessage.warning('标题和分类必填');
    return;
  }
  if (editingId.value) {
    await updateContent(editingId.value, form.value);
    ElMessage.success('已更新');
  } else {
    await createContent(form.value);
    ElMessage.success('已创建');
  }
  dialogVisible.value = false;
  load();
};

const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该内容？', '提示', { type: 'warning' });
  await deleteContent(id);
  ElMessage.success('已删除');
  load();
};

const toggleFlags = async (row: any) => {
  await updateContentFlags(row.id, { headline: row.headline, recommended: row.recommended });
  ElMessage.success('标记已更新');
};

const onSelectionChange = (rows: any[]) => {
  selectedRows.value = rows;
};

const weightPercent = (row: any) => {
  const weight = Number(row.recommendWeight || 0);
  return Math.round((weight / maxWeight.value) * 100);
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(() => {
  loadCategories();
  load();
  loadConfig();
});

onBeforeUnmount(() => {
  if (sortableRef.value) sortableRef.value.destroy();
});

const initSortable = () => {
  nextTick(() => {
    const tbody = tableRef.value?.$el?.querySelector('.el-table__body-wrapper tbody');
    if (!tbody) return;
    if (sortableRef.value) sortableRef.value.destroy();
    sortableRef.value = Sortable.create(tbody, {
      handle: '.drag-handle',
      animation: 150,
      onEnd: async (evt) => {
        if (evt.oldIndex == null || evt.newIndex == null) return;
        const moved = list.value.splice(evt.oldIndex, 1)[0];
        list.value.splice(evt.newIndex, 0, moved);
        const items = list.value.map((item, idx) => ({ id: item.id, sortOrder: idx + 1 }));
        await updateContentOrder(items);
        ElMessage.success('排序已更新');
      }
    });
  });
};

const openRecommendDialog = () => {
  recommendDialog.value = true;
  loadRecommendList();
};

const openHeadlineDialog = () => {
  headlineDialog.value = true;
  loadHeadlineList();
};

const saveRecommendOrder = async () => {
  const items = recommendList.value.map((item, idx) => ({ id: item.id, sortOrder: idx + 1 }));
  await updateContentOrder(items);
  ElMessage.success('推荐排序已保存');
  recommendDialog.value = false;
  load();
};

const saveHeadlineOrder = async () => {
  const items = headlineList.value.map((item, idx) => ({ id: item.id, sortOrder: idx + 1 }));
  await updateContentOrder(items);
  ElMessage.success('头条排序已保存');
  headlineDialog.value = false;
  load();
};

const loadRecommendList = async () => {
  const resp = await fetchRecommendedContent();
  recommendList.value = resp.data?.data || [];
};

const openRecommendPreviewDialog = async () => {
  await loadRecommendList();
  recommendPreviewDialog.value = true;
};

const loadHeadlineList = async () => {
  const resp = await fetchHeadlineContent();
  headlineList.value = resp.data?.data || [];
};

const sortByWeight = async () => {
  const resp = await fetchRecommendedContent();
  const items = resp.data?.data || [];
  if (!items.length) {
    ElMessage.info('暂无推荐内容可排序');
    return;
  }
  items.sort((a: any, b: any) => (b.recommendWeight || 0) - (a.recommendWeight || 0));
  const payload = items.map((item: any, idx: number) => ({ id: item.id, sortOrder: idx + 1 }));
  await updateContentOrder(payload);
  ElMessage.success('已按权重排序');
  load();
};

const openWeightBatchDialog = () => {
  if (!selectedRows.value.length) return;
  weightBatchForm.value = { mode: 'set', value: 0 };
  weightBatchDialog.value = true;
};

const applyWeightBatch = async () => {
  if (!selectedRows.value.length) return;
  const value = Number(weightBatchForm.value.value || 0);
  const items = selectedRows.value.map((row: any) => {
    const current = Number(row.recommendWeight || 0);
    let next = current;
    if (weightBatchForm.value.mode === 'set') next = value;
    if (weightBatchForm.value.mode === 'inc') next = current + value;
    if (weightBatchForm.value.mode === 'dec') next = Math.max(0, current - value);
    return { id: row.id, recommendWeight: next };
  });
  await updateContentWeights(items);
  ElMessage.success('权重已更新');
  weightBatchDialog.value = false;
  load();
};

const openConfigDialog = () => {
  configDialog.value = true;
  loadConfig();
};

const openStrategyDialog = async () => {
  await loadConfig();
  const strategy = configForm.value.recommendStrategy || 'prefer';
  if (strategy === 'prefer') {
    strategyWeights.value = { prefer: 60, filter: 20, global: 20 };
  } else if (strategy === 'filter') {
    strategyWeights.value = { prefer: 30, filter: 50, global: 20 };
  } else {
    strategyWeights.value = { prefer: 30, filter: 20, global: 50 };
  }
  strategyDialog.value = true;
};

const loadConfig = async () => {
  const resp = await fetchContentConfig();
  const data = resp.data?.data;
  if (data) {
    configForm.value.recommendIntervalSec = data.recommendIntervalSec || 6;
    configForm.value.recommendCount = data.recommendCount || 6;
    configForm.value.recommendStrategy = data.recommendStrategy || 'prefer';
    configForm.value.previewIntervalSec = data.previewIntervalSec || 10;
    const strategy = configForm.value.recommendStrategy;
    if (strategy === 'prefer') {
      strategyWeights.value = { prefer: 60, filter: 20, global: 20 };
    } else if (strategy === 'filter') {
      strategyWeights.value = { prefer: 30, filter: 50, global: 20 };
    } else {
      strategyWeights.value = { prefer: 30, filter: 20, global: 50 };
    }
  }
};

const saveConfig = async () => {
  await updateContentConfig(configForm.value);
  ElMessage.success('配置已保存');
  configDialog.value = false;
};

const saveStrategy = async () => {
  const maxVal = Math.max(preferPercent.value, filterPercent.value, globalPercent.value);
  let strategy = 'prefer';
  if (maxVal === globalPercent.value) {
    strategy = 'global';
  } else if (maxVal === filterPercent.value) {
    strategy = 'filter';
  } else {
    strategy = 'prefer';
  }
  await updateContentConfig({ recommendStrategy: strategy });
  configForm.value.recommendStrategy = strategy;
  ElMessage.success('策略已保存');
  strategyDialog.value = false;
};

const previewPortal = () => {
  window.open('/portal', '_blank');
};
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.filter { margin-bottom: 16px; }
.cover-cell { width: 70px; height: 50px; border-radius: 6px; overflow: hidden; }
.cover-img { width: 100%; height: 100%; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; font-size: 20px; }
.title-cell { display: flex; flex-direction: column; gap: 4px; }
.title-text { font-weight: 500; color: #303133; }
.summary-text { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

.cover-upload-area { width: 200px; }
.cover-preview { position: relative; width: 200px; height: 120px; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.cover-preview .preview-img { width: 100%; height: 100%; }
.cover-actions { position: absolute; bottom: 0; left: 0; right: 0; background: rgba(0,0,0,0.5); padding: 6px; display: flex; justify-content: center; }
.cover-uploader { width: 200px; }
.upload-trigger { width: 200px; height: 120px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: border-color 0.3s; }
.upload-trigger:hover { border-color: #409eff; }
.upload-icon { font-size: 32px; color: #c0c4cc; margin-bottom: 8px; }
.upload-trigger span { font-size: 14px; color: #909399; }
.cover-tip { font-size: 12px; color: #909399; margin-top: 8px; }
.quick-flags { display: flex; gap: 8px; }
.drag-handle { cursor: move; color: #909399; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.recommend-item { display: flex; align-items: center; gap: 8px; padding: 8px; border: 1px dashed #e4e7ed; border-radius: 6px; margin-bottom: 8px; }
.recommend-title { flex: 1; }
.preview-hint { margin-bottom: 10px; color: #909399; }
.preview-list { max-height: 320px; overflow: auto; display: flex; flex-direction: column; gap: 8px; }
.preview-item { display: flex; align-items: center; gap: 8px; padding: 8px 10px; border: 1px solid #ebeef5; border-radius: 6px; }
.preview-item.inactive { opacity: 0.6; }
.preview-index { width: 22px; height: 22px; border-radius: 50%; background: #f2f6fc; display: inline-flex; align-items: center; justify-content: center; font-size: 12px; color: #409eff; }
.preview-title { flex: 1; }
.weight-cell { display: flex; align-items: center; gap: 8px; }
.weight-cell :deep(.el-progress) { flex: 1; }
.weight-value { width: 32px; text-align: right; font-size: 12px; color: #606266; }
.weight-hint { margin-left: 8px; color: #909399; font-size: 12px; }
.strategy-row { display: flex; align-items: center; gap: 12px; margin: 10px 0; }
.strategy-row .label { width: 90px; text-align: right; color: #606266; }
.strategy-preview { margin-top: 12px; display: flex; flex-direction: column; gap: 6px; }
.strategy-preview .bar { height: 26px; background: #ecf5ff; color: #409eff; border-radius: 6px; padding-left: 8px; display: flex; align-items: center; font-size: 12px; }
.strategy-preview .bar.secondary { background: #fdf6ec; color: #e6a23c; }
.strategy-preview .bar.info { background: #f0f9eb; color: #67c23a; }
</style>
