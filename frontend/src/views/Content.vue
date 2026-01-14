<!--
 * @Author: 梁玉杰
 * @Module: 内容展示管理 - 内容管理
 * @Description: 内容管理页面，发布和管理图文内容
-->
<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>内容管理</h3>
          <span class="subtitle">发布和管理图文内容</span>
        </div>
        <div class="header-actions">
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
        <el-form-item label="排序">
          <el-select v-model="filter.sortOrder" style="width: 140px" @change="load">
            <el-option value="desc" label="最新发布" />
            <el-option value="asc" label="最早发布" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="标题/摘要" style="width: 180px" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load"><el-icon><Search /></el-icon>查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column label="封面" width="90">
          <template #default="scope">
            <div class="cover-cell">
              <el-image v-if="scope.row.coverUrl" :src="scope.row.coverUrl" fit="cover" class="cover-img" :preview-src-list="[scope.row.coverUrl]" preview-teleported />
              <div v-else class="cover-empty"><el-icon><Picture /></el-icon></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="240">
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
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.published ? 'success' : 'info'" size="small">
              {{ scope.row.published ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="scope">{{ formatDate(scope.row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button size="small" @click="edit(scope.row)"><el-icon><Edit /></el-icon>编辑</el-button>
              <el-button size="small" type="danger" plain @click="onDelete(scope.row.id)"><el-icon><Delete /></el-icon></el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination 
          layout="total, prev, pager, next" 
          :total="total" 
          :page-size="size" 
          :current-page="page" 
          @current-change="onPage" 
        />
      </div>
    </el-card>

    <!-- 发布/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑内容' : '发布内容'" width="640px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <div class="title-input-row">
            <el-input v-model="form.title" placeholder="请输入内容标题" maxlength="100" show-word-limit style="flex: 1" />
            <el-button v-if="!editingId" type="warning" plain size="small" @click="fillDemoData" style="margin-left: 10px">
              <el-icon><MagicStick /></el-icon>填充示范
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" placeholder="简短描述内容（可选）" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="封面">
          <div class="cover-upload-area">
            <div class="cover-preview" v-if="form.coverUrl">
              <el-image :src="form.coverUrl" fit="cover" class="preview-img" />
              <div class="cover-actions">
                <el-button size="small" type="danger" @click="form.coverUrl = ''"><el-icon><Delete /></el-icon></el-button>
              </div>
            </div>
            <el-upload 
              v-else 
              :headers="uploadHeaders" 
              action="/api/media/upload-cover" 
              :show-file-list="false" 
              :on-success="onCoverUploaded" 
              :before-upload="beforeCoverUpload" 
              accept="image/*" 
              class="cover-uploader"
            >
              <div class="upload-trigger">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <span>上传封面</span>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.body" type="textarea" :rows="6" placeholder="请输入正文内容" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-switch v-model="form.published" active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitting">{{ editingId ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { createContent, deleteContent, fetchCategories, fetchContent, updateContent } from '../api';
import { Plus, Search, Edit, Delete, Picture, MagicStick } from '@element-plus/icons-vue';

const list = ref<any[]>([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const categories = ref<any[]>([]);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const submitting = ref(false);
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${sessionStorage.getItem('org_token') || sessionStorage.getItem('admin_token') || ''}` }));

const form = ref({
  title: '',
  categoryId: undefined as number | undefined,
  summary: '',
  coverUrl: '',
  body: '',
  published: true
});

const filter = ref<{ categoryId?: number; published?: boolean; keyword?: string; sortOrder: string }>({
  sortOrder: 'desc'  // 默认最新发布在前
});

// 示范数据列表
const demoDataList = [
  {
    title: '志愿服务暖人心，爱心传递正能量',
    summary: '我们组织开展了一系列志愿服务活动，为社区居民送去温暖和关爱。',
    body: '近日，我们志愿服务队积极响应号召，组织志愿者深入社区开展志愿服务活动。志愿者们为老年人提供健康咨询、为儿童辅导功课、为社区环境进行清洁美化。\n\n活动中，志愿者们以饱满的热情和真诚的服务态度，赢得了社区居民的一致好评。一位老奶奶感动地说："你们就像我的亲人一样，谢谢你们的关心！"\n\n此次活动不仅传递了爱心，也弘扬了"奉献、友爱、互助、进步"的志愿精神。我们将继续开展更多有意义的志愿服务活动，为构建和谐社会贡献力量。'
  },
  {
    title: '关爱空巢老人，温暖夕阳红',
    summary: '志愿者走进社区，为空巢老人送去关爱和陪伴。',
    body: '为关爱空巢老人，让他们感受到社会的温暖，我们组织志愿者开展了"关爱空巢老人"主题活动。\n\n志愿者们来到社区空巢老人家中，帮助他们打扫卫生、整理房间，陪老人聊天解闷。有的志愿者还为老人测量血压、讲解健康知识。\n\n张奶奶今年82岁，独居多年。志愿者小李每周都会来看望她，帮她买菜、做饭。张奶奶说："有你们这些孩子来看我，我心里暖暖的。"\n\n关爱老人是中华民族的传统美德，我们将持续开展此类活动，让更多空巢老人感受到社会大家庭的温暖。'
  },
  {
    title: '环保志愿行动，共建美丽家园',
    summary: '组织志愿者参与城市环境清洁，倡导绿色生活理念。',
    body: '为提升市民环保意识，建设美丽宜居城市，我们组织开展了"环保志愿行动"。\n\n活动当天，50余名志愿者齐聚市民广场，分组对周边街道、公园进行垃圾清理。志愿者们不怕脏、不怕累，认真清理每一个角落的垃圾。\n\n同时，志愿者们还向过往市民发放环保宣传资料，讲解垃圾分类知识，倡导绿色低碳生活方式。\n\n"保护环境，人人有责。希望通过我们的行动，能带动更多人参与到环保事业中来。"志愿者代表小王说道。\n\n此次活动共清理垃圾200余公斤，发放宣传资料500余份，取得了良好的社会效果。'
  },
  {
    title: '爱心助学，点亮希望之光',
    summary: '为山区贫困学生捐赠学习用品，开展一对一帮扶活动。',
    body: '教育是改变命运的钥匙。为帮助山区贫困学生完成学业，我们组织开展了"爱心助学"公益活动。\n\n志愿者们筹集了书包、文具、图书等学习用品，前往山区希望小学进行捐赠。看到孩子们收到礼物时开心的笑容，志愿者们感到无比欣慰。\n\n活动中，我们还建立了"一对一"帮扶机制，志愿者与贫困学生结成帮扶对子，定期了解学习情况，提供学业辅导和心理关怀。\n\n小明是受助学生之一，他说："感谢叔叔阿姨们的帮助，我一定好好学习，将来也要做一个对社会有用的人。"\n\n爱心助学，我们一直在路上。'
  },
  {
    title: '文明交通劝导，共创安全出行',
    summary: '志愿者走上街头，开展文明交通劝导活动。',
    body: '为提升市民文明交通意识，减少交通事故发生，我们组织志愿者开展了文明交通劝导活动。\n\n志愿者们身穿红马甲，手持小红旗，在主要交通路口协助交警维护交通秩序。他们劝导行人遵守交通信号灯，不闯红灯、不乱穿马路；提醒非机动车走非机动车道，不逆行、不占道。\n\n"您好，请走斑马线，注意安全！"志愿者们用温和的语气、耐心的态度，赢得了市民的理解和配合。\n\n活动期间，志愿者们还向市民发放交通安全宣传单，普及交通安全知识。\n\n文明交通，从我做起。让我们共同努力，创建安全、有序、文明的交通环境。'
  },
  {
    title: '社区便民服务日，志愿服务暖民心',
    summary: '为社区居民提供免费理发、家电维修、法律咨询等便民服务。',
    body: '为更好地服务社区居民，我们组织开展了"社区便民服务日"活动。\n\n活动现场设置了多个服务点，为居民提供免费理发、家电维修、法律咨询、健康义诊等服务。志愿者们各展所长，热情服务每一位前来咨询的居民。\n\n理发点前排起了长队，志愿者理发师小张忙得不亦乐乎。"能用自己的手艺为大家服务，我感到很开心。"他说。\n\n家电维修点也很受欢迎，志愿者们帮助居民修好了电风扇、电饭煲等小家电，为居民节省了维修费用。\n\n此次活动共服务居民300余人次，受到了社区居民的热烈欢迎和高度评价。我们将定期开展此类活动，把便民服务送到居民家门口。'
  }
];
let demoDataIndex = 0;

// 填充示范数据
const fillDemoData = () => {
  const demo = demoDataList[demoDataIndex % demoDataList.length];
  form.value.title = demo.title;
  form.value.summary = demo.summary;
  form.value.body = demo.body;
  form.value.published = true;
  // 如果有分类，默认选择第一个
  if (categories.value.length > 0 && !form.value.categoryId) {
    form.value.categoryId = categories.value[0].id;
  }
  demoDataIndex++;
  ElMessage.success('已填充示范数据，请上传封面后发布');
};

const load = async () => {
  const resp = await fetchContent(page.value, size.value, filter.value.categoryId, filter.value.published, filter.value.keyword, filter.value.sortOrder);
  const data = resp.data?.data || {};
  list.value = data.records || [];
  total.value = data.total || 0;
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
  form.value = { title: '', categoryId: undefined, summary: '', coverUrl: '', body: '', published: true };
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
    published: row.published
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
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateContent(editingId.value, form.value);
      ElMessage.success('已更新');
    } else {
      await createContent(form.value);
      ElMessage.success('已发布');
    }
    dialogVisible.value = false;
    load();
  } finally {
    submitting.value = false;
  }
};

const onDelete = async (id: number) => {
  await ElMessageBox.confirm('确定删除该内容？', '提示', { type: 'warning' });
  await deleteContent(id);
  ElMessage.success('已删除');
  load();
};

const onPage = (p: number) => {
  page.value = p;
  load();
};

onMounted(() => {
  loadCategories();
  load();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }
.header-actions { display: flex; gap: 10px; }
.filter { margin-bottom: 16px; }

.cover-cell { width: 60px; height: 45px; border-radius: 6px; overflow: hidden; }
.cover-img { width: 100%; height: 100%; cursor: pointer; }
.cover-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #c0c4cc; font-size: 18px; }

.title-cell { display: flex; flex-direction: column; gap: 4px; }
.title-text { font-weight: 500; color: #303133; }
.summary-text { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }

.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }

/* 封面上传 */
.cover-upload-area { width: 180px; }
.cover-preview { position: relative; width: 180px; height: 110px; border-radius: 8px; overflow: hidden; border: 1px solid #ebeef5; }
.cover-preview .preview-img { width: 100%; height: 100%; }
.cover-actions { position: absolute; top: 4px; right: 4px; }
.cover-uploader { width: 180px; }
.upload-trigger { width: 180px; height: 110px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: border-color 0.3s; }
.upload-trigger:hover { border-color: #409eff; }
.upload-icon { font-size: 28px; color: #c0c4cc; margin-bottom: 6px; }
.upload-trigger span { font-size: 13px; color: #909399; }

/* 标题输入行 */
.title-input-row { display: flex; align-items: center; width: 100%; }
</style>
