<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>终端管理</h3>
          <span class="subtitle">管理和监控显示终端设备</span>
        </div>
        <el-button type="primary" @click="showRegisterDialog = true">
          <el-icon><Plus /></el-icon>
          注册终端
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="16" class="status-row">
      <el-col :xs="12" :sm="6">
        <el-card class="status-card online" shadow="hover">
          <div class="status-icon"><el-icon><Monitor /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ status.online }}</div>
            <div class="status-label">在线终端</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="status-card offline" shadow="hover">
          <div class="status-icon"><el-icon><WarningFilled /></el-icon></div>
          <div class="status-info">
            <div class="status-value">{{ status.offline }}</div>
            <div class="status-label">离线终端</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert v-if="status.offline > 0" type="warning" show-icon :closable="false" class="offline-alert">
      <template #title>{{ status.offline }} 台终端离线</template>
      <div class="offline-list">
        <el-tag v-for="t in status.offlineTerminals" :key="t.id" type="danger" size="small">{{ t.name || t.code }}</el-tag>
      </div>
    </el-alert>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="分组">
          <el-input v-model="groupFilter" placeholder="分组名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTerminals">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 绑定播放列表区域 -->
    <el-card class="bind-card" shadow="never">
      <div class="bind-header">
        <div class="bind-title">
          <el-icon><Link /></el-icon>
          <span>绑定播放列表</span>
        </div>
        <el-tag v-if="selectedTerminalIds.length > 0" type="success">
          已选 {{ selectedTerminalIds.length }} 台终端
        </el-tag>
        <el-tag v-else type="info">请在下方表格勾选终端</el-tag>
      </div>
      <div class="bind-form">
        <el-form :inline="true">
          <el-form-item label="播放列表">
            <el-select v-model="selectedPlaylist" placeholder="选择播放列表" style="width: 200px" clearable>
              <el-option v-for="p in playlists" :key="p.id" :label="p.name" :value="p.id">
                <span>{{ p.name }}</span>
                <span class="playlist-item-count">{{ p.itemCount || 0 }}项</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="播放时段">
            <el-date-picker 
              v-model="startEnd" 
              type="datetimerange" 
              value-format="YYYY-MM-DD HH:mm:ss" 
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 360px" 
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="bind" :disabled="selectedTerminalIds.length === 0 || !selectedPlaylist">
              <el-icon><Connection /></el-icon>
              绑定到选中终端
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="content-card" shadow="never">
      <el-table :data="terminals" stripe @selection-change="onSelect">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="code" label="代码" width="140" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="groupName" label="分组" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.groupName" size="small">{{ scope.row.groupName }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="170">
          <template #default="scope">
            <span class="heartbeat-time">{{ formatTime(scope.row.lastHeartbeat) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="viewHeartbeats(scope.row.id)" title="心跳记录">
                <el-icon><Timer /></el-icon>
              </el-button>
              <el-button size="small" @click="viewBindings(scope.row.id)" title="绑定记录">
                <el-icon><List /></el-icon>
              </el-button>
              <el-button size="small" type="danger" @click="viewBroadcasts(scope.row)" title="当前插播">
                <el-icon><Bell /></el-icon>
              </el-button>
              <el-button size="small" type="primary" @click="openAttr(scope.row)" title="终端属性">
                <el-icon><Setting /></el-icon>
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showRegisterDialog" title="注册终端" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分组"><el-input v-model="form.groupName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="register">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="heartbeatDialog" title="心跳记录" width="500px">
      <el-table :data="heartbeats">
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getHeartbeatStatus(scope.row.createdAt) === 'online' ? 'success' : 'danger'" size="small">
              {{ getHeartbeatStatus(scope.row.createdAt) === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="bindingDialog" title="绑定记录" width="560px">
      <el-table :data="bindings">
        <el-table-column prop="playlistId" label="列表ID" width="100" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="endTime" label="结束" />
        <el-table-column prop="active" label="激活" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.active ? 'success' : 'info'" size="small">{{ scope.row.active ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="broadcastDialog" title="当前插播" width="600px">
      <el-alert v-if="currentBroadcasts.length > 0" type="warning" :closable="false" class="broadcast-alert">
        该终端有 {{ currentBroadcasts.length }} 个正在进行或待执行的插播
      </el-alert>
      <el-empty v-if="currentBroadcasts.length === 0" description="该终端当前没有插播" />
      <el-table v-else :data="currentBroadcasts" size="small">
        <el-table-column prop="title" label="标题" min-width="120" />
        <el-table-column label="内容" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.mediaId" type="success" size="small">媒体#{{ scope.row.mediaId }}</el-tag>
            <el-tag v-else-if="scope.row.contentId" type="primary" size="small">内容#{{ scope.row.contentId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间范围" min-width="160">
          <template #default="scope">
            <div class="broadcast-time">
              <span>{{ formatTime(scope.row.startTime) || '立即' }}</span>
              <span class="time-sep">~</span>
              <span>{{ formatTime(scope.row.endTime) || '永久' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="getBroadcastStatusType(scope.row)" size="small">
              {{ getBroadcastStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="attrDialog" title="终端属性" width="560px">
      <div class="attr-terminal-info">
        <el-icon><Monitor /></el-icon>
        <span>{{ currentTerminal?.name || currentTerminal?.code }}</span>
      </div>
      
      <el-tabs v-model="attrTab" class="attr-tabs">
        <el-tab-pane label="显示设置" name="display">
          <el-form label-width="100px" class="attr-form">
            <el-form-item label="屏幕亮度">
              <div class="slider-row">
                <el-slider v-model="attrForm.brightness" :min="0" :max="100" :step="5" show-stops />
                <span class="slider-value">{{ attrForm.brightness }}%</span>
              </div>
            </el-form-item>
            <el-form-item label="音量">
              <div class="slider-row">
                <el-slider v-model="attrForm.volume" :min="0" :max="100" :step="5" show-stops />
                <span class="slider-value">{{ attrForm.volume }}%</span>
              </div>
            </el-form-item>
            <el-form-item label="屏幕方向">
              <el-radio-group v-model="attrForm.orientation">
                <el-radio-button label="landscape">横屏</el-radio-button>
                <el-radio-button label="portrait">竖屏</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="分辨率">
              <el-select v-model="attrForm.resolution" placeholder="选择分辨率" style="width: 100%">
                <el-option label="1920×1080 (Full HD)" value="1920x1080" />
                <el-option label="3840×2160 (4K)" value="3840x2160" />
                <el-option label="1280×720 (HD)" value="1280x720" />
                <el-option label="1080×1920 (竖屏Full HD)" value="1080x1920" />
                <el-option label="自动" value="auto" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="播放设置" name="playback">
          <el-form label-width="100px" class="attr-form">
            <el-form-item label="自动播放">
              <el-switch v-model="attrForm.autoPlay" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-form-item label="循环播放">
              <el-switch v-model="attrForm.loop" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-form-item label="默认时长">
              <el-input-number v-model="attrForm.defaultDuration" :min="5" :max="300" :step="5" />
              <span class="input-suffix">秒（图片/内容）</span>
            </el-form-item>
            <el-form-item label="过渡效果">
              <el-select v-model="attrForm.transition" placeholder="选择过渡效果" style="width: 100%">
                <el-option label="无" value="none" />
                <el-option label="淡入淡出" value="fade" />
                <el-option label="滑动" value="slide" />
                <el-option label="缩放" value="zoom" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="网络设置" name="network">
          <el-form label-width="100px" class="attr-form">
            <el-form-item label="心跳间隔">
              <el-input-number v-model="attrForm.heartbeatInterval" :min="10" :max="300" :step="10" />
              <span class="input-suffix">秒</span>
            </el-form-item>
            <el-form-item label="离线缓存">
              <el-switch v-model="attrForm.offlineCache" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-form-item label="自动更新">
              <el-switch v-model="attrForm.autoUpdate" active-text="开启" inactive-text="关闭" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="高级" name="advanced">
          <el-form label-width="100px" class="attr-form">
            <el-form-item label="备注">
              <el-input v-model="attrForm.remark" type="textarea" :rows="2" placeholder="终端备注信息" />
            </el-form-item>
            <el-form-item label="位置">
              <el-input v-model="attrForm.location" placeholder="如：一楼大厅左侧" />
            </el-form-item>
            <el-form-item label="原始JSON">
              <el-input v-model="attrRawJson" type="textarea" :rows="4" placeholder='{"key":"value"}' />
              <div class="json-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>直接编辑JSON会覆盖上方设置</span>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <el-button @click="attrDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAttr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { bindPlaylistToTerminals, fetchPlaylists, fetchTerminals, fetchTerminalHeartbeats, fetchTerminalPlaylists, fetchTerminalStatus, registerTerminal, fetchActiveBroadcasts } from '../api';
import { Plus, Monitor, WarningFilled, InfoFilled, Link, Connection, Timer, List, Setting, Bell } from '@element-plus/icons-vue';

const terminals = ref<any[]>([]);
const playlists = ref<any[]>([]);
const selectedTerminalIds = ref<number[]>([]);
const selectedPlaylist = ref<number | null>(null);
const startEnd = ref<[string, string] | null>(null);
const groupFilter = ref('');
const status = ref<{ online: number; offline: number; offlineTerminals: any[] }>({ online: 0, offline: 0, offlineTerminals: [] });
const showRegisterDialog = ref(false);
const form = ref({ code: '', name: '', groupName: '' });
const heartbeatDialog = ref(false);
const heartbeats = ref<any[]>([]);
const bindingDialog = ref(false);
const bindings = ref<any[]>([]);
const broadcastDialog = ref(false);
const currentBroadcasts = ref<any[]>([]);
const attrDialog = ref(false);
const attrTab = ref('display');
const attrRawJson = ref('');
const currentTerminalId = ref<number | null>(null);
const currentTerminal = ref<any>(null);

const attrForm = ref({
  brightness: 80,
  volume: 50,
  orientation: 'landscape',
  resolution: '1920x1080',
  autoPlay: true,
  loop: true,
  defaultDuration: 15,
  transition: 'fade',
  heartbeatInterval: 60,
  offlineCache: true,
  autoUpdate: true,
  remark: '',
  location: ''
});

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 50, groupFilter.value || undefined);
  terminals.value = resp.data?.data?.records || [];
};
const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  playlists.value = resp.data?.data || [];
};
const loadStatus = async () => {
  const resp = await fetchTerminalStatus();
  status.value = resp.data?.data || { online: 0, offline: 0, offlineTerminals: [] };
};
const register = async () => {
  if (!form.value.code || !form.value.name) { ElMessage.warning('请输入代码和名称'); return; }
  await registerTerminal(form.value);
  ElMessage.success('已注册');
  form.value = { code: '', name: '', groupName: '' };
  showRegisterDialog.value = false;
  loadTerminals(); loadStatus();
};
const onSelect = (rows: any[]) => { selectedTerminalIds.value = rows.map(r => r.id); };

const formatTime = (t: string) => {
  if (!t) return '-';
  return t.replace('T', ' ').substring(0, 19);
};

// 根据心跳时间判断当时的状态（5分钟内为在线，与后端保持一致）
const getHeartbeatStatus = (createdAt: string) => {
  if (!createdAt) return 'offline';
  const heartbeatTime = new Date(createdAt.replace(' ', 'T'));
  const now = new Date();
  const diffSeconds = (now.getTime() - heartbeatTime.getTime()) / 1000;
  return diffSeconds <= 300 ? 'online' : 'offline';
};

const bind = async () => {
  if (!selectedPlaylist.value) { ElMessage.warning('请选择播放列表'); return; }
  const payload: any = { playlistId: selectedPlaylist.value, terminalIds: selectedTerminalIds.value };
  if (startEnd.value) { payload.startTime = startEnd.value[0]; payload.endTime = startEnd.value[1]; }
  await bindPlaylistToTerminals(payload);
  ElMessage.success('绑定成功');
};
const viewHeartbeats = async (id: number) => {
  const resp = await fetchTerminalHeartbeats(id, 1, 20);
  heartbeats.value = resp.data?.data?.records || [];
  heartbeatDialog.value = true;
};
const viewBindings = async (id: number) => {
  const resp = await fetchTerminalPlaylists(id);
  bindings.value = resp.data?.data || [];
  bindingDialog.value = true;
};

const viewBroadcasts = async (row: any) => {
  const resp = await fetchActiveBroadcasts(row.code, row.groupName);
  currentBroadcasts.value = resp.data?.data?.records || [];
  broadcastDialog.value = true;
};

// 根据时间动态计算插播状态，与插播管理页面保持一致
const calculateBroadcastStatus = (row: any): string => {
  const now = new Date();
  const start = row.startTime ? new Date(row.startTime) : null;
  const end = row.endTime ? new Date(row.endTime) : null;
  
  // 已结束
  if (end && end < now) {
    return 'completed';
  }
  // 未开始
  if (start && start > now) {
    return 'pending';
  }
  // 进行中
  return 'active';
};

const getBroadcastStatusType = (row: any) => {
  const status = calculateBroadcastStatus(row);
  switch (status) {
    case 'active': return 'danger';
    case 'pending': return 'warning';
    case 'completed': return 'info';
    default: return 'info';
  }
};

const getBroadcastStatusText = (row: any) => {
  const status = calculateBroadcastStatus(row);
  switch (status) {
    case 'active': return '进行中';
    case 'pending': return '待执行';
    case 'completed': return '已完成';
    default: return status;
  }
};

const openAttr = (row: any) => {
  currentTerminalId.value = row.id;
  currentTerminal.value = row;
  attrTab.value = 'display';
  
  // 解析现有属性
  let attrs: any = {};
  try {
    if (row.attributes) {
      attrs = JSON.parse(row.attributes);
    }
  } catch (e) {
    attrs = {};
  }
  
  // 填充表单
  attrForm.value = {
    brightness: attrs.brightness ?? 80,
    volume: attrs.volume ?? 50,
    orientation: attrs.orientation ?? 'landscape',
    resolution: attrs.resolution ?? '1920x1080',
    autoPlay: attrs.autoPlay ?? true,
    loop: attrs.loop ?? true,
    defaultDuration: attrs.defaultDuration ?? 15,
    transition: attrs.transition ?? 'fade',
    heartbeatInterval: attrs.heartbeatInterval ?? 60,
    offlineCache: attrs.offlineCache ?? true,
    autoUpdate: attrs.autoUpdate ?? true,
    remark: attrs.remark ?? '',
    location: attrs.location ?? ''
  };
  
  attrRawJson.value = row.attributes || '';
  attrDialog.value = true;
};

const saveAttr = async () => {
  if (currentTerminalId.value == null) return;
  
  let jsonStr = '';
  
  // 如果高级tab的JSON被修改了，优先使用它
  if (attrTab.value === 'advanced' && attrRawJson.value.trim()) {
    try {
      JSON.parse(attrRawJson.value); // 验证JSON格式
      jsonStr = attrRawJson.value;
    } catch (e) {
      ElMessage.error('JSON格式不正确');
      return;
    }
  } else {
    // 使用表单数据构建JSON
    const attrs = { ...attrForm.value };
    jsonStr = JSON.stringify(attrs);
  }
  
  await fetch('/api/terminals/' + currentTerminalId.value + '/attributes', {
    method: 'PUT',
    headers: { 'Content-Type': 'text/plain', Authorization: `Bearer ${localStorage.getItem('token') || ''}` },
    body: jsonStr
  });
  ElMessage.success('属性已保存');
  attrDialog.value = false;
  loadTerminals();
};
onMounted(() => { loadTerminals(); loadPlaylists(); loadStatus(); });
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; }
.status-row { margin-bottom: 0; }
.status-card { border-radius: 12px; }
.status-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.status-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; }
.status-card.online .status-icon { background: linear-gradient(135deg, #67c23a, #85ce61); }
.status-card.offline .status-icon { background: linear-gradient(135deg, #f56c6c, #f78989); }
.status-value { font-size: 28px; font-weight: 700; }
.status-label { font-size: 14px; color: #909399; }
.offline-alert { border-radius: 12px; }
.offline-list { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px; }
.filter-card, .content-card, .bind-card { border-radius: 12px; }

/* 绑定区域样式 */
.bind-card { background: linear-gradient(135deg, #f0f9eb, #e1f3d8); border: 1px solid #c2e7b0; }
.bind-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.bind-title { display: flex; align-items: center; gap: 8px; font-weight: 600; font-size: 15px; color: #67c23a; }
.bind-title .el-icon { font-size: 18px; }
.bind-form :deep(.el-form-item) { margin-bottom: 0; margin-right: 16px; }
.bind-form :deep(.el-form-item__label) { color: #606266; }
.playlist-item-count { float: right; color: #909399; font-size: 12px; }

/* 表格样式 */
.text-muted { color: #c0c4cc; }
.heartbeat-time { font-size: 13px; color: #606266; }

/* 终端属性弹窗样式 */
.attr-terminal-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #409eff15, #67c23a15);
  border-radius: 8px;
  margin-bottom: 16px;
  font-weight: 500;
  color: #303133;
}
.attr-terminal-info .el-icon { font-size: 20px; color: #409eff; }

.attr-tabs :deep(.el-tabs__header) { margin-bottom: 16px; }
.attr-tabs :deep(.el-tabs__item) { font-size: 14px; }

.attr-form { padding: 0 8px; }
.attr-form .el-form-item { margin-bottom: 20px; }

.slider-row {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}
.slider-row .el-slider { flex: 1; }
.slider-value {
  min-width: 50px;
  text-align: right;
  font-weight: 500;
  color: #409eff;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 13px;
}

.json-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #e6a23c;
}
.json-tip .el-icon { font-size: 14px; }

/* 插播对话框样式 */
.broadcast-alert { margin-bottom: 16px; }
.broadcast-time { display: flex; align-items: center; gap: 4px; font-size: 12px; }
.time-sep { color: #c0c4cc; }
</style>
