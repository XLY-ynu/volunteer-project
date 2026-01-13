<template>
  <div class="portal">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="logo">🤝 志愿服务平台</div>
      <nav class="nav">
        <a :class="{ active: tab === 'home' }" @click="tab = 'home'">首页</a>
        <a :class="{ active: tab === 'content' }" @click="tab = 'content'">内容浏览</a>
        <a :class="{ active: tab === 'media' }" @click="tab = 'media'">媒体观看</a>
        <a :class="{ active: tab === 'help' }" @click="tab = 'help'">发布求助</a>
        <a :class="{ active: tab === 'volunteer' }" @click="tab = 'volunteer'">成为志愿者</a>
      </nav>
      <div class="user-area">
        <template v-if="isLoggedIn">
          <div class="user-info-box">
            <el-icon class="user-avatar"><User /></el-icon>
            <span class="user-name">{{ userInfo?.nickname || userInfo?.username }}</span>
          </div>
          <el-button class="logout-btn" size="small" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </template>
        <template v-else>
          <el-button class="login-btn" size="small" @click="showLogin = true">
            <el-icon><User /></el-icon>
            登录
          </el-button>
          <el-button class="register-btn" size="small" @click="showRegister = true">
            <el-icon><Plus /></el-icon>
            注册
          </el-button>
        </template>
      </div>
    </header>
    
    <!-- 首页 -->
    <div v-if="tab === 'home'" class="content-area">
      <div class="welcome-banner">
        <h1>欢迎来到志愿服务平台</h1>
        <p>浏览志愿服务内容，观看公益视频，向志愿者组织寻求帮助</p>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="tab = 'content'">浏览内容</el-button>
          <el-button size="large" @click="tab = 'help'">发布求助</el-button>
        </div>
      </div>
      
      <h3>志愿者组织</h3>
      <div class="org-list">
        <div v-for="org in orgs" :key="org.id" class="org-card">
          <div class="org-logo">{{ org.name?.charAt(0) }}</div>
          <div class="org-info">
            <div class="org-name">{{ org.name }}</div>
            <div class="org-desc">{{ org.description || '暂无简介' }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 内容浏览 -->
    <div v-if="tab === 'content'" class="content-area">
      <h3>分类导航</h3>
      <div class="category-nav">
        <el-tag v-for="cat in categories" :key="cat.id" 
                :type="selectedCategory === cat.id ? '' : 'info'"
                @click="selectCategory(cat.id)" style="cursor: pointer; margin: 5px;">
          {{ cat.name }}
        </el-tag>
      </div>
      
      <h3>内容列表</h3>
      <div class="content-grid">
        <div v-for="item in contents" :key="item.id" class="content-card">
          <el-image 
            :src="item.coverUrl || 'https://picsum.photos/seed/' + item.id + '/300/200'" 
            :preview-src-list="[item.coverUrl || 'https://picsum.photos/seed/' + item.id + '/300/200']"
            :preview-teleported="true"
            fit="cover"
            class="cover"
            @click.stop
          />
          <div class="info" @click="showContentDetail(item)">
            <div class="title">{{ item.title }}</div>
            <div class="summary">{{ item.summary }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 媒体观看 -->
    <div v-if="tab === 'media'" class="content-area">
      <el-tabs v-model="mediaTab">
        <el-tab-pane label="图片浏览" name="image">
          <div class="media-grid">
            <div v-for="m in images" :key="m.id" class="media-card" @click="previewImage(m)">
              <img :src="m.thumbUrl || m.url" />
              <div class="name">{{ m.name }}</div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="视频播放" name="video">
          <div class="media-grid">
            <div v-for="m in videos" :key="m.id" class="media-card" @click="playVideo(m)">
              <div class="video-thumb">
                <img :src="m.thumbUrl || 'https://picsum.photos/seed/v' + m.id + '/300/200'" />
                <div class="play-icon">▶</div>
              </div>
              <div class="name">{{ m.name }}</div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 发布求助 -->
    <div v-if="tab === 'help'" class="content-area">
      <div class="help-section">
        <h3>向志愿者组织发布求助</h3>
        <el-form :model="helpForm" label-width="100px" style="max-width: 600px;">
          <el-form-item label="选择组织" required>
            <el-select v-model="helpForm.orgId" placeholder="请选择求助的组织">
              <el-option v-for="org in orgs" :key="org.id" :label="org.name" :value="org.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="求助标题" required>
            <el-input v-model="helpForm.title" placeholder="简要描述您的需求" />
          </el-form-item>
          <el-form-item label="详细内容" required>
            <el-input v-model="helpForm.content" type="textarea" :rows="4" placeholder="详细描述您需要的帮助" />
          </el-form-item>
          <el-form-item label="联系人">
            <el-input v-model="helpForm.contactName" placeholder="您的姓名" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="helpForm.contactPhone" placeholder="您的电话" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="helpForm.address" placeholder="您的地址（可选）" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitHelp" :disabled="!isLoggedIn">提交求助</el-button>
            <span v-if="!isLoggedIn" style="color: #999; margin-left: 10px;">请先登录后再提交</span>
          </el-form-item>
        </el-form>
        
        <div v-if="isLoggedIn && myHelpRequests.length > 0" style="margin-top: 40px;">
          <h3>我的求助记录</h3>
          <el-table :data="myHelpRequests">
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="orgName" label="求助组织" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reply" label="组织回复" min-width="200">
              <template #default="{ row }">
                <span v-if="row.reply" class="reply-text">{{ row.reply }}</span>
                <span v-else style="color: #999;">暂无回复</span>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="提交时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 成为志愿者 -->
    <div v-if="tab === 'volunteer'" class="content-area">
      <div class="volunteer-section">
        <div class="volunteer-banner">
          <h2>🌟 成为志愿者</h2>
          <p>加入我们，用爱心传递温暖，用行动改变世界</p>
        </div>
        
        <!-- 未登录提示 -->
        <div v-if="!isLoggedIn" class="login-prompt">
          <el-icon :size="48" color="#909399"><User /></el-icon>
          <p>请先登录后再申请成为志愿者</p>
          <el-button type="primary" @click="showLogin = true">立即登录</el-button>
        </div>
        
        <!-- 已是志愿者 -->
        <div v-else-if="volunteerStatus === 'approved'" class="volunteer-status success">
          <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
          <h3>您已经是志愿者了！</h3>
          <p>您可以前往志愿者端参与更多活动</p>
          
          <!-- 志愿者端登录信息 -->
          <div class="volunteer-login-info">
            <div class="info-title">
              <el-icon><InfoFilled /></el-icon>
              志愿者端登录信息
            </div>
            <div class="info-item">
              <span class="label">登录账号：</span>
              <span class="value">{{ volunteerPhone }}</span>
              <el-tag size="small" type="info">手机号</el-tag>
            </div>
            <div class="info-item">
              <span class="label">登录密码：</span>
              <span class="value">与当前账号密码相同</span>
            </div>
          </div>
          
          <el-button type="primary" @click="goToVolunteerPortal">进入志愿者端</el-button>
        </div>
        
        <!-- 审核中 -->
        <div v-else-if="volunteerStatus === 'pending'" class="volunteer-status pending">
          <el-icon :size="48" color="#e6a23c"><Clock /></el-icon>
          <h3>您的申请正在审核中</h3>
          <p>请耐心等待管理员审核，审核通过后您将成为正式志愿者</p>
        </div>
        
        <!-- 申请被拒绝 -->
        <div v-else-if="volunteerStatus === 'rejected'" class="volunteer-status rejected">
          <el-icon :size="48" color="#f56c6c"><CircleClose /></el-icon>
          <h3>您的申请未通过审核</h3>
          <p>您可以修改信息后重新申请</p>
          <el-button type="primary" @click="volunteerStatus = null">重新申请</el-button>
        </div>
        
        <!-- 申请表单 -->
        <div v-else class="volunteer-form-section">
          <h3>填写志愿者申请信息</h3>
          <el-form :model="volunteerForm" label-width="100px" style="max-width: 500px; margin: 0 auto;">
            <el-form-item label="姓名" required>
              <el-input v-model="volunteerForm.name" placeholder="请输入您的真实姓名" />
            </el-form-item>
            <el-form-item label="手机号" required>
              <el-input v-model="volunteerForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="volunteerForm.email" placeholder="请输入邮箱（选填）" />
            </el-form-item>
            <el-form-item label="所属单位">
              <el-input v-model="volunteerForm.organization" placeholder="学校/公司/社区等（选填）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitVolunteerApplication" :loading="applyLoading">
                提交申请
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
    
    <!-- 登录弹窗 -->
    <el-dialog v-model="showLogin" title="用户登录" width="400px">
      <el-form :model="loginForm">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLogin = false">取消</el-button>
        <el-button type="primary" @click="doLogin">登录</el-button>
      </template>
    </el-dialog>
    
    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="用户注册" width="400px">
      <el-form :model="registerForm">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="registerForm.nickname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="doRegister">注册</el-button>
      </template>
    </el-dialog>
    
    <!-- 内容详情弹窗 -->
    <el-dialog v-model="contentDetailVisible" :title="currentContent?.title" width="700px">
      <div v-if="currentContent">
        <el-image 
          v-if="currentContent.coverUrl" 
          :src="currentContent.coverUrl" 
          :preview-src-list="[currentContent.coverUrl]"
          :preview-teleported="true"
          fit="cover"
          style="width: 100%; max-height: 300px; border-radius: 8px; cursor: pointer;"
        >
          <template #placeholder>
            <div style="display: flex; align-items: center; justify-content: center; height: 200px; background: #f5f7fa;">
              加载中...
            </div>
          </template>
        </el-image>
        <p style="margin-top: 16px; line-height: 1.8;">{{ currentContent.body || currentContent.summary }}</p>
      </div>
    </el-dialog>
    
    <!-- 视频播放弹窗 -->
    <el-dialog v-model="videoVisible" :title="currentVideo?.name" width="800px">
      <video v-if="currentVideo" :src="currentVideo.url" controls style="width: 100%;" />
    </el-dialog>
    
    <!-- 图片预览 -->
    <el-image-viewer v-if="imagePreviewVisible" :url-list="[previewImageUrl]" @close="imagePreviewVisible = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { User, CircleCheck, CircleClose, Clock, Plus, SwitchButton, InfoFilled } from '@element-plus/icons-vue';
import axios from 'axios';

const tab = ref('home');
const mediaTab = ref('image');

// 用户状态
const userToken = ref(localStorage.getItem('userToken'));
const isLoggedIn = computed(() => !!userToken.value);
const userInfo = ref<any>(null);
const showLogin = ref(false);
const showRegister = ref(false);
const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ username: '', password: '', nickname: '' });

// 志愿者申请
const volunteerStatus = ref<string | null>(null);
const volunteerPhone = ref<string>('');
const volunteerForm = ref({ name: '', phone: '', email: '', organization: '' });
const applyLoading = ref(false);

// 数据
const orgs = ref<any[]>([]);
const categories = ref<any[]>([]);
const contents = ref<any[]>([]);
const selectedCategory = ref<number | null>(null);
const images = ref<any[]>([]);
const videos = ref<any[]>([]);

// 求助
const helpForm = ref({ orgId: null, title: '', content: '', contactName: '', contactPhone: '', address: '' });
const myHelpRequests = ref<any[]>([]);

// 弹窗
const contentDetailVisible = ref(false);
const currentContent = ref<any>(null);
const videoVisible = ref(false);
const currentVideo = ref<any>(null);
const imagePreviewVisible = ref(false);
const previewImageUrl = ref('');

const getHeaders = () => {
  const token = localStorage.getItem('userToken');
  return token ? { Authorization: `Bearer ${token}` } : {};
};

const loadOrgs = async () => {
  try {
    const resp = await axios.get('/api/user-portal/orgs');
    orgs.value = resp.data.data || [];
  } catch (e) { console.error(e); }
};

const loadCategories = async () => {
  try {
    const resp = await axios.get('/api/public/categories');
    categories.value = resp.data.data || [];
  } catch (e) { console.error(e); }
};

const loadContents = async () => {
  try {
    let url = '/api/public/content?size=50';
    if (selectedCategory.value) url += `&categoryId=${selectedCategory.value}`;
    const resp = await axios.get(url);
    contents.value = resp.data.data?.records || resp.data.data || [];
  } catch (e) { console.error(e); }
};

const selectCategory = (id: number) => {
  selectedCategory.value = selectedCategory.value === id ? null : id;
  loadContents();
};

const loadMedia = async () => {
  try {
    const resp = await axios.get('/api/public/media');
    const all = resp.data.data?.records || resp.data.data || [];
    images.value = all.filter((m: any) => m.type === 'image');
    videos.value = all.filter((m: any) => m.type === 'video');
  } catch (e) { console.error(e); }
};

const loadMyHelpRequests = async () => {
  if (!isLoggedIn.value) return;
  try {
    const resp = await axios.get('/api/user-portal/help-requests', { headers: getHeaders() });
    myHelpRequests.value = resp.data.data?.records || resp.data.data || [];
  } catch (e) { console.error(e); }
};

const doLogin = async () => {
  try {
    const resp = await axios.post('/api/user-portal/login', loginForm.value);
    const token = resp.data.data.token;
    localStorage.setItem('userToken', token);
    userToken.value = token;
    userInfo.value = resp.data.data;
    showLogin.value = false;
    ElMessage.success('登录成功');
    loadMyHelpRequests();
    checkVolunteerStatus();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '登录失败');
  }
};

const doRegister = async () => {
  try {
    await axios.post('/api/user-portal/register', registerForm.value);
    ElMessage.success('注册成功，请登录');
    showRegister.value = false;
    showLogin.value = true;
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '注册失败');
  }
};

const logout = () => {
  localStorage.removeItem('userToken');
  userToken.value = null;
  userInfo.value = null;
  volunteerStatus.value = null;
  myHelpRequests.value = [];
  ElMessage.success('已退出');
};

const submitHelp = async () => {
  if (!helpForm.value.orgId || !helpForm.value.title || !helpForm.value.content) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  try {
    await axios.post('/api/user-portal/help-requests', helpForm.value, { headers: getHeaders() });
    ElMessage.success('求助已提交');
    helpForm.value = { orgId: null, title: '', content: '', contactName: '', contactPhone: '', address: '' };
    loadMyHelpRequests();
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败');
  }
};

const showContentDetail = (item: any) => {
  currentContent.value = item;
  contentDetailVisible.value = true;
};

const playVideo = (m: any) => {
  currentVideo.value = m;
  videoVisible.value = true;
};

const previewImage = (m: any) => {
  previewImageUrl.value = m.url;
  imagePreviewVisible.value = true;
};

const formatTime = (t: string) => t ? new Date(t).toLocaleString() : '';
const statusText = (s: string) => ({ pending: '待处理', processing: '处理中', completed: '已完成' }[s] || s);
const statusType = (s: string) => ({ pending: 'warning', processing: 'primary', completed: 'success' }[s] || 'info');

// 检查志愿者状态
const checkVolunteerStatus = async () => {
  if (!isLoggedIn.value) {
    volunteerStatus.value = null;
    volunteerPhone.value = '';
    return;
  }
  try {
    const resp = await axios.get('/api/user-portal/volunteer-status', { headers: getHeaders() });
    volunteerStatus.value = resp.data.data?.status || null;
    volunteerPhone.value = resp.data.data?.phone || '';
  } catch (e) {
    volunteerStatus.value = null;
    volunteerPhone.value = '';
  }
};

// 提交志愿者申请
const submitVolunteerApplication = async () => {
  if (!volunteerForm.value.name || !volunteerForm.value.phone) {
    ElMessage.warning('请填写姓名和手机号');
    return;
  }
  applyLoading.value = true;
  try {
    await axios.post('/api/user-portal/become-volunteer', volunteerForm.value, { headers: getHeaders() });
    ElMessage.success('申请已提交，请等待审核');
    volunteerStatus.value = 'pending';
    volunteerForm.value = { name: '', phone: '', email: '', organization: '' };
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '申请失败');
  } finally {
    applyLoading.value = false;
  }
};

// 跳转到志愿者端
const goToVolunteerPortal = () => {
  window.location.href = '/portal';
};

onMounted(() => {
  loadOrgs();
  loadCategories();
  loadContents();
  loadMedia();
  if (isLoggedIn.value) {
    loadMyHelpRequests();
    checkVolunteerStatus();
  }
});
</script>

<style scoped>
.portal { min-height: 100vh; background: #f5f7fa; }
.header { display: flex; align-items: center; justify-content: space-between; padding: 0 40px; height: 60px; background: linear-gradient(90deg, #2c5282, #4299e1); color: #fff; }
.logo { font-size: 20px; font-weight: bold; }
.nav a { color: rgba(255,255,255,0.8); margin: 0 15px; cursor: pointer; text-decoration: none; }
.nav a.active { color: #fff; font-weight: bold; }
.user-area { display: flex; align-items: center; gap: 12px; }
.user-info-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
}
.user-avatar {
  font-size: 18px;
  color: #fff;
}
.user-name { color: #fff; font-weight: 500; font-size: 14px; }
.logout-btn { 
  background: rgba(255,100,100,0.2); 
  border: 1px solid rgba(255,150,150,0.4); 
  color: #fff; 
  border-radius: 20px;
  padding: 8px 16px;
  transition: all 0.3s;
}
.logout-btn:hover { 
  background: rgba(255,80,80,0.4); 
  border-color: rgba(255,150,150,0.6);
  transform: translateY(-2px);
}
.login-btn {
  background: rgba(255,255,255,0.95);
  border: none;
  color: #2c5282;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  transition: all 0.3s;
}
.login-btn:hover {
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.register-btn {
  background: transparent;
  border: 2px solid rgba(255,255,255,0.8);
  color: #fff;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 600;
  transition: all 0.3s;
}
.register-btn:hover {
  background: rgba(255,255,255,0.15);
  border-color: #fff;
  transform: translateY(-2px);
}

.content-area { max-width: 1200px; margin: 0 auto; padding: 30px 20px; }
.content-area h3 { color: #2c5282; margin: 20px 0 15px; }

.welcome-banner { text-align: center; padding: 60px 20px; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; border-radius: 16px; margin-bottom: 30px; }
.welcome-banner h1 { margin: 0 0 10px; }
.welcome-banner p { opacity: 0.9; margin-bottom: 20px; }
.quick-actions { display: flex; gap: 15px; justify-content: center; }

.org-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.org-card { display: flex; align-items: center; gap: 15px; padding: 20px; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.org-logo { width: 50px; height: 50px; border-radius: 50%; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; }
.org-name { font-weight: bold; color: #333; }
.org-desc { font-size: 13px; color: #666; margin-top: 4px; }

.category-nav { margin-bottom: 20px; }

.content-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.content-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: transform 0.2s; }
.content-card:hover { transform: translateY(-4px); }
.content-card .cover { width: 100%; height: 160px; display: block; cursor: pointer; }
.content-card .info { padding: 15px; cursor: pointer; }
.content-card .title { font-weight: bold; color: #333; margin-bottom: 8px; }
.content-card .summary { font-size: 13px; color: #666; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.media-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.media-card { background: #fff; border-radius: 12px; overflow: hidden; cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.media-card img { width: 100%; height: 140px; object-fit: cover; }
.media-card .name { padding: 10px; font-size: 14px; color: #333; }
.video-thumb { position: relative; }
.play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 50px; height: 50px; background: rgba(0,0,0,0.6); border-radius: 50%; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 20px; }

.help-section { background: #fff; padding: 30px; border-radius: 12px; }
.reply-text { color: #67c23a; font-weight: 500; }

/* 成为志愿者样式 */
.volunteer-section { background: #fff; padding: 30px; border-radius: 12px; }
.volunteer-banner { text-align: center; padding: 40px 20px; background: linear-gradient(135deg, #67c23a, #85ce61); color: #fff; border-radius: 12px; margin-bottom: 30px; }
.volunteer-banner h2 { margin: 0 0 10px; font-size: 28px; }
.volunteer-banner p { margin: 0; opacity: 0.9; }

.login-prompt { text-align: center; padding: 60px 20px; }
.login-prompt p { color: #909399; margin: 20px 0; }

.volunteer-status { text-align: center; padding: 60px 20px; }
.volunteer-status h3 { margin: 20px 0 10px; color: #303133; }
.volunteer-status p { color: #909399; margin-bottom: 20px; }
.volunteer-status.success { background: #f0f9eb; border-radius: 12px; }
.volunteer-status.pending { background: #fdf6ec; border-radius: 12px; }
.volunteer-status.rejected { background: #fef0f0; border-radius: 12px; }

/* 志愿者登录信息样式 */
.volunteer-login-info {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px 20px;
  margin: 20px auto;
  max-width: 360px;
  text-align: left;
}
.volunteer-login-info .info-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 12px;
  font-size: 14px;
}
.volunteer-login-info .info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}
.volunteer-login-info .info-item:last-child {
  border-bottom: none;
}
.volunteer-login-info .label {
  color: #909399;
  font-size: 13px;
  min-width: 70px;
}
.volunteer-login-info .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.volunteer-form-section { padding: 20px 0; }
.volunteer-form-section h3 { text-align: center; margin-bottom: 30px; color: #303133; }
</style>
