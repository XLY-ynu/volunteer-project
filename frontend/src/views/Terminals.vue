<template>
  <div>
    <div class="header">
      <h2>终端管理</h2>
      <el-form :inline="true" :model="form" @submit.prevent>
        <el-form-item label="终端代码">
          <el-input v-model="form.code" placeholder="device-001" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="大厅一号屏" />
        </el-form-item>
        <el-form-item label="分组">
          <el-input v-model="form.groupName" placeholder="大厅" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="register">注册/更新</el-button>
        </el-form-item>
        <el-form-item label="筛选分组">
          <el-input v-model="groupFilter" placeholder="分组" />
        </el-form-item>
        <el-form-item>
          <el-button @click="loadTerminals">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="12" class="status-row">
      <el-col :span="6">
        <el-card>
          <div class="stat-title">在线终端</div>
          <div class="stat-value success">{{ status.online }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-title">离线终端</div>
          <div class="stat-value danger">{{ status.offline }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-alert
      v-if="status.offline > 0"
      type="warning"
      :closable="false"
      title="以下终端离线，请检查网络/电源"
      style="margin: 12px 0"
    />
    <el-table v-if="status.offline > 0" :data="status.offlineTerminals" size="small" style="margin-bottom: 12px">
      <el-table-column prop="code" label="代码" width="140" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="lastHeartbeat" label="最后心跳" width="200" />
    </el-table>

    <el-table :data="terminals" style="width: 100%" @selection-change="onSelect">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="code" label="代码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="groupName" label="分组" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="lastHeartbeat" label="心跳" width="180" />
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button size="small" @click="viewHeartbeats(scope.row.id)">心跳</el-button>
          <el-button size="small" @click="viewBindings(scope.row.id)">绑定</el-button>
          <el-button size="small" type="primary" @click="openAttr(scope.row)">属性</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="bind">
      <el-select v-model="selectedPlaylist" placeholder="选择播放列表" style="width: 240px">
        <el-option v-for="p in playlists" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-date-picker
        v-model="startEnd"
        type="datetimerange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        style="width: 360px"
      />
      <el-button type="success" @click="bind">绑定播放列表</el-button>
    </div>

    <el-dialog v-model="heartbeatDialog" title="心跳记录" width="500px">
      <el-table :data="heartbeats">
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="时间" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="bindingDialog" title="绑定记录" width="560px">
      <el-table :data="bindings">
        <el-table-column prop="playlistId" label="播放列表ID" width="140" />
        <el-table-column prop="startTime" label="开始" />
        <el-table-column prop="endTime" label="结束" />
        <el-table-column prop="active" label="激活" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.active ? 'success' : 'info'">{{ scope.row.active ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="attrDialog" title="终端属性" width="520px">
      <el-input v-model="attrText" type="textarea" :rows="8" placeholder='{"brightness":80}' />
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
import {
  bindPlaylistToTerminals,
  fetchPlaylists,
  fetchTerminals,
  fetchTerminalHeartbeats,
  fetchTerminalPlaylists,
  fetchTerminalStatus,
  registerTerminal
} from '../api';

const terminals = ref<any[]>([]);
const playlists = ref<any[]>([]);
const selectedTerminalIds = ref<number[]>([]);
const selectedPlaylist = ref<number | null>(null);
const startEnd = ref<[string, string] | null>(null);
const groupFilter = ref<string>('');
const status = ref<{ online: number; offline: number; offlineTerminals: any[] }>({ online: 0, offline: 0, offlineTerminals: [] });

const form = ref({ code: '', name: '', groupName: '' });
const heartbeatDialog = ref(false);
const heartbeats = ref<any[]>([]);
const bindingDialog = ref(false);
const bindings = ref<any[]>([]);
const attrDialog = ref(false);
const attrText = ref('');
const currentTerminalId = ref<number | null>(null);

const loadTerminals = async () => {
  const resp = await fetchTerminals(1, 50, groupFilter.value || undefined);
  // @ts-ignore
  terminals.value = resp.data?.data?.records || [];
};

const loadPlaylists = async () => {
  const resp = await fetchPlaylists();
  // @ts-ignore
  playlists.value = resp.data?.data || [];
};

const loadStatus = async () => {
  const resp = await fetchTerminalStatus();
  // @ts-ignore
  status.value = resp.data?.data || { online: 0, offline: 0, offlineTerminals: [] };
};

const register = async () => {
  if (!form.value.code || !form.value.name) {
    ElMessage.warning('请输入代码和名称');
    return;
  }
  await registerTerminal(form.value);
  ElMessage.success('已注册/更新');
  form.value = { code: '', name: '', groupName: '' };
  loadTerminals();
};

const onSelect = (rows: any[]) => {
  selectedTerminalIds.value = rows.map((r) => r.id);
};

const bind = async () => {
  if (!selectedPlaylist.value) {
    ElMessage.warning('请选择播放列表');
    return;
  }
  if (selectedTerminalIds.value.length === 0) {
    ElMessage.warning('请选择终端');
    return;
  }
  const payload: any = { playlistId: selectedPlaylist.value, terminalIds: selectedTerminalIds.value };
  if (startEnd.value) {
    payload.startTime = startEnd.value[0];
    payload.endTime = startEnd.value[1];
  }
  await bindPlaylistToTerminals(payload);
  ElMessage.success('绑定成功');
};

const viewHeartbeats = async (id: number) => {
  const resp = await fetchTerminalHeartbeats(id, 1, 20);
  // @ts-ignore
  heartbeats.value = resp.data?.data?.records || [];
  heartbeatDialog.value = true;
};

const viewBindings = async (id: number) => {
  const resp = await fetchTerminalPlaylists(id);
  // @ts-ignore
  bindings.value = resp.data?.data || [];
  bindingDialog.value = true;
};

const openAttr = (row: any) => {
  currentTerminalId.value = row.id;
  attrText.value = row.attributes || '';
  attrDialog.value = true;
};

const saveAttr = async () => {
  if (currentTerminalId.value == null) return;
  await fetch('/api/terminals/' + currentTerminalId.value + '/attributes', {
    method: 'PUT',
    headers: { 'Content-Type': 'text/plain', Authorization: `Bearer ${localStorage.getItem('token') || ''}` },
    body: attrText.value
  });
  ElMessage.success('已保存属性');
  attrDialog.value = false;
  loadTerminals();
};

onMounted(() => {
  loadTerminals();
  loadPlaylists();
  loadStatus();
});
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.status-row {
  margin-bottom: 12px;
}
.stat-title {
  color: #909399;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
.stat-value.success {
  color: #67c23a;
}
.stat-value.danger {
  color: #f56c6c;
}
.bind {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
