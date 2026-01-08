<template>
  <div class="page-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div class="header-left">
          <h3>终端管理</h3>
          <span class="subtitle">管理和监控显示终端设备</span>
        </div>
        <div class="header-right">
          <el-tag :type="status.offline > 0 ? 'danger' : 'success'" size="large">
            在线 {{ status.online }} / 离线 {{ status.offline }}
          </el-tag>
          <el-button type="primary" @click="showRegisterDialog = true">
            <el-icon><Plus /></el-icon>注册终端
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 终端列表 -->
    <el-card class="content-card" shadow="never">
      <el-table :data="terminals" stripe>
        <el-table-column prop="code" label="代码" width="140" />
        <el-table-column prop="name" label="名称" min-width="120">
          <template #default="scope">{{ scope.row.name || '-' }}</template>
        </el-table-column>
        <el-table-column prop="groupName" label="分组" width="100">
          <template #default="scope">{{ scope.row.groupName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastHeartbeat" label="最后心跳" width="150">
          <template #default="scope">{{ formatTime(scope.row.lastHeartbeat) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="openBindDialog(scope.row)">绑定</el-button>
              <el-button size="small" @click="openAttr(scope.row)">设置</el-button>
              <el-button size="small" type="danger" @click="deleteTerminal(scope.row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 注册终端对话框 -->
    <el-dialog v-model="showRegisterDialog" title="注册终端" width="400px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="代码" required>
          <el-input v-model="form.code" placeholder="终端唯一标识" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="终端显示名称" />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="form.groupName" placeholder="如：一楼大厅" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegisterDialog = false">取消</el-button>
        <el-button type="primary" @click="register">确定</el-button>
      </template>
    </el-dialog>

    <!-- 绑定播放列表对话框 -->
    <el-dialog v-model="bindDialog" title="绑定播放列表" width="500px">
      <div class="bind-terminal-info">
        <el-icon><Monitor /></el-icon>
        <span>{{ currentTerminal?.name || currentTerminal?.code }}</span>
      </div>
      
      <el-form label-width="80px">
        <el-form-item label="播放列表">
          <el-select v-model="selectedPlaylist" placeholder="选择播放列表" style="width: 100%">
            <el-option v-for="p in playlists" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="播放时段">
          <el-date-picker v-model="startEnd" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" 
            start-placeholder="开始" end-placeholder="结束" style="width: 100%" />
        </el-form-item>
      </el-form>

      <div class="bind-history" v-if="bindings.length > 0">
        <div class="bind-history-title">已绑定记录</div>
        <el-table :data="bindings" size="small" max-height="200">
          <el-table-column label="播放列表" min-width="120">
            <template #default="scope">{{ getPlaylistName(scope.row.playlistId) }}</template>
          </el-table-column>
          <el-table-column label="开始时间" width="140">
            <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="140">
            <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="bindDialog = false">取消</el-button>
        <el-button type="primary" @click="bind" :disabled="!selectedPlaylist">绑定</el-button>
      </template>
    </el-dialog>

    <!-- 终端设置对话框 -->
    <el-dialog v-model="attrDialog" title="终端设置" width="450px">
      <div class="attr-terminal-info">
        <el-icon><Monitor /></el-icon>
        <span>{{ currentTerminal?.name || currentTerminal?.code }}</span>
      </div>
      
      <el-form label-width="80px">
        <el-form-item label="屏幕亮度">
          <div class="slider-row">
            <el-slider v-model="attrForm.brightness" :min="0" :max="100" :step="5" />
            <span class="slider-value">{{ attrForm.brightness }}%</span>
          </div>
        </el-form-item>
        <el-form-item label="音量">
          <div class="slider-row">
            <el-slider v-model="attrForm.volume" :min="0" :max="100" :step="5" />
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
          <el-select v-model="attrForm.resolution" style="width: 100%">
            <el-option label="1920×1080 (Full HD)" value="1920x1080" />
            <el-option label="3840×2160 (4K)" value="3840x2160" />
            <el-option label="1280×720 (HD)" value="1280x720" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="attrDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAttr">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { bindPlaylistToTerminals, fetchPlaylists, fetchTerminals, fetchTerminalPlaylists, fetchTerminalStatus, registerTerminal, deleteTerminal as deleteTerminalApi, updateTerminalAttr } from '../api';
import { Plus, Monitor } from '@element-plus/icons-vue';

const terminals = ref<any[]>([]);
const playlists = ref<any[]>([]);
const status = ref({ online: 0, offline: 0 });

const showRegisterDialog = ref(false);
const form = ref({ code: '', name: '', groupName: '' });

const bindDialog = ref(false);
const currentTerminal = ref<any>(null);
const selectedPlaylist = ref<number | null>(null);
const startEnd = ref<[string, string] | null>(null);
const bindings = ref<any[]>([]);

const attrDialog = ref(false);
const attrForm = ref({
  brightness: 80,
  volume: 50,
  orientation: 'landscape',
  resolution: '1920x1080'
});

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 100);
  terminals.value = resp.data?.data?.records || [];
};

const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  playlists.value = resp.data?.data || [];
};

const loadStatus = async () => {
  const resp = await fetchTerminalStatus();
  const data = resp.data?.data || {};
  status.value = { online: data.online || 0, offline: data.offline || 0 };
};

const formatTime = (t: string) => t ? t.replace('T', ' ').substring(0, 16) : '-';

const getPlaylistName = (id: number) => {
  const p = playlists.value.find(p => p.id === id);
  return p ? p.name : `列表${id}`;
};

const register = async () => {
  if (!form.value.code) {
    ElMessage.warning('请输入终端代码');
    return;
  }
  await registerTerminal(form.value);
  ElMessage.success('注册成功');
  showRegisterDialog.value = false;
  form.value = { code: '', name: '', groupName: '' };
  loadTerminals();
  loadStatus();
};

const openBindDialog = async (terminal: any) => {
  currentTerminal.value = terminal;
  selectedPlaylist.value = null;
  startEnd.value = null;
  const resp = await fetchTerminalPlaylists(terminal.id);
  bindings.value = resp.data?.data || [];
  bindDialog.value = true;
};

const bind = async () => {
  if (!selectedPlaylist.value || !currentTerminal.value) return;
  const payload: any = { 
    terminalIds: [currentTerminal.value.id], 
    playlistId: selectedPlaylist.value 
  };
  if (startEnd.value) {
    payload.startTime = startEnd.value[0];
    payload.endTime = startEnd.value[1];
  }
  await bindPlaylistToTerminals(payload);
  ElMessage.success('绑定成功');
  bindDialog.value = false;
};

const openAttr = (terminal: any) => {
  currentTerminal.value = terminal;
  const attr = terminal.attributes ? (typeof terminal.attributes === 'string' ? JSON.parse(terminal.attributes) : terminal.attributes) : {};
  attrForm.value = {
    brightness: attr.brightness ?? 80,
    volume: attr.volume ?? 50,
    orientation: attr.orientation ?? 'landscape',
    resolution: attr.resolution ?? '1920x1080'
  };
  attrDialog.value = true;
};

const saveAttr = async () => {
  if (!currentTerminal.value) return;
  await updateTerminalAttr(currentTerminal.value.id, attrForm.value);
  ElMessage.success('设置已保存');
  attrDialog.value = false;
  loadTerminals();
};

const deleteTerminal = async (id: number) => {
  await ElMessageBox.confirm('确定删除该终端？', '提示', { type: 'warning' });
  await deleteTerminalApi(id);
  ElMessage.success('已删除');
  loadTerminals();
  loadStatus();
};

onMounted(() => {
  loadTerminals();
  loadPlaylists();
  loadStatus();
});
</script>

<style scoped>
.page-container { display: flex; flex-direction: column; gap: 16px; }
.page-header, .content-card { border-radius: 12px; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0; font-size: 18px; }
.subtitle { font-size: 13px; color: #909399; margin-left: 12px; }
.header-right { display: flex; align-items: center; gap: 12px; }

.bind-terminal-info, .attr-terminal-info { 
  display: flex; align-items: center; gap: 8px; 
  padding: 12px 16px; background: #f5f7fa; border-radius: 8px; 
  margin-bottom: 20px; font-weight: 500; 
}

.bind-history { margin-top: 20px; }
.bind-history-title { font-size: 13px; color: #909399; margin-bottom: 8px; }

.slider-row { display: flex; align-items: center; gap: 16px; width: 100%; }
.slider-row .el-slider { flex: 1; }
.slider-value { min-width: 50px; text-align: right; color: #606266; }
</style>
