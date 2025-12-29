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

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">分组告警规则</div>
        <el-button size="small" type="primary" @click="openRuleDialog">新增规则</el-button>
      </div>
      <el-table :data="groupRules" size="small">
        <el-table-column prop="groupName" label="分组" width="140" />
        <el-table-column prop="offlineThreshold" label="离线阈值" width="120" />
        <el-table-column prop="notifyChannel" label="通知通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.notifyChannel)">
              {{ channelLabel(scope.row.notifyChannel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyTarget" label="通知目标" min-width="160">
          <template #default="scope">
            <span>{{ scope.row.notifyTarget || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="editRule(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeRule(scope.row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">分组告警概览</div>
        <el-button size="small" @click="loadGroupAlerts">刷新</el-button>
      </div>
      <el-table :data="groupAlerts" size="small">
        <el-table-column prop="groupName" label="分组" width="140" />
        <el-table-column prop="total" label="总数" width="80" />
        <el-table-column prop="offline" label="离线" width="80" />
        <el-table-column prop="ruleThreshold" label="阈值" width="80" />
        <el-table-column prop="notifyChannel" label="通知通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.notifyChannel)">
              {{ channelLabel(scope.row.notifyChannel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alert" label="告警" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.alert ? 'danger' : 'success'" size="small">
              {{ scope.row.alert ? '告警' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

  <el-card class="group-alert-card" shadow="never">
    <div class="group-head">
      <div class="group-title">告警通知通道</div>
      <el-button size="small" @click="loadGroupAlerts">刷新</el-button>
    </div>
    <el-empty v-if="activeAlerts.length === 0" description="暂无告警通知" />
    <el-table v-else :data="activeAlerts" size="small">
      <el-table-column prop="groupName" label="分组" width="160" />
      <el-table-column prop="offline" label="离线" width="80" />
      <el-table-column prop="ruleThreshold" label="阈值" width="80" />
      <el-table-column prop="notifyChannel" label="通道" width="120">
        <template #default="scope">
          <el-tag size="small" :type="channelTagType(scope.row.notifyChannel)">
            {{ channelLabel(scope.row.notifyChannel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="notifyTarget" label="目标" min-width="180" />
    </el-table>
  </el-card>

  <el-card class="group-alert-card" shadow="never">
    <div class="group-head">
      <div class="group-title">通知通道健康度</div>
      <el-button size="small" @click="loadNotificationHealth">刷新</el-button>
    </div>
    <el-empty v-if="notificationHealth.length === 0" description="暂无数据" />
    <el-table v-else :data="notificationHealth" size="small">
      <el-table-column prop="channel" label="通道" width="140" />
      <el-table-column prop="total" label="7日总量" width="100" />
      <el-table-column prop="success" label="成功" width="90" />
      <el-table-column prop="failed" label="失败" width="90" />
      <el-table-column prop="pending" label="排队/待发" width="110" />
      <el-table-column prop="successRate" label="成功率" width="120">
        <template #default="scope">
          <el-progress :percentage="Number((scope.row.successRate || '0').replace('%',''))" :text-inside="true" :stroke-width="16" />
        </template>
      </el-table-column>
    </el-table>
  </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">告警订阅</div>
        <el-button size="small" type="primary" @click="openSubscriptionDialog">新增订阅</el-button>
      </div>
      <el-table :data="alertSubscriptions" size="small">
        <el-table-column prop="groupName" label="分组" width="160">
          <template #default="scope">
            <span>{{ scope.row.groupName || '全部分组' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.channel)">
              {{ channelLabel(scope.row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" min-width="180">
          <template #default="scope">{{ scope.row.target || '-' }}</template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="editSubscription(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeSubscription(scope.row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">静默时间段</div>
        <el-button size="small" type="primary" @click="openSilenceDialog">新增静默</el-button>
      </div>
      <el-table :data="alertSilences" size="small">
        <el-table-column prop="groupName" label="分组" width="160">
          <template #default="scope">
            <span>{{ scope.row.groupName || '全部分组' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channel" label="通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.channel)">
              {{ channelLabel(scope.row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间段" min-width="220">
          <template #default="scope">
            {{ formatTime(scope.row.startTime) }} ~ {{ formatTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="editSilence(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeSilence(scope.row.id)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">离线趋势</div>
        <div class="group-actions">
          <el-select v-model="trendGroup" placeholder="全部分组" clearable size="small" style="width: 160px">
            <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
          </el-select>
          <el-input-number v-model="trendDays" :min="3" :max="30" size="small" />
          <el-button size="small" @click="loadOfflineTrend">刷新</el-button>
        </div>
      </div>
      <div v-if="offlineTrend.length" class="trend-chart">
        <div v-for="row in offlineTrend" :key="row.day" class="trend-bar">
          <div class="bar" :style="{ height: trendPercent(row) + '%' }"></div>
          <div class="trend-value">{{ row.offlineCount || 0 }}</div>
          <div class="trend-label">{{ formatDay(row.day) }}</div>
        </div>
      </div>
      <el-table :data="offlineTrend" size="small">
        <el-table-column prop="day" label="日期" width="140" />
        <el-table-column prop="offlineCount" label="离线次数" width="120" />
      </el-table>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">告警历史</div>
        <el-button size="small" @click="loadAlertHistory">刷新</el-button>
      </div>
      <el-table :data="alertHistory" size="small">
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="groupName" label="分组" width="140" />
        <el-table-column prop="offline" label="离线" width="80" />
        <el-table-column prop="ruleThreshold" label="阈值" width="80" />
        <el-table-column prop="channel" label="通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.channel)">
              {{ channelLabel(scope.row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" min-width="160" />
        <el-table-column prop="silenced" label="静默" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.silenced ? 'warning' : 'success'" size="small">
              {{ scope.row.silenced ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination layout="prev, pager, next" :total="historyTotal" :page-size="historySize" :current-page="historyPage" @current-change="onHistoryPage" />
      </div>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">通知日志</div>
        <el-button size="small" @click="loadNotificationLogs">刷新</el-button>
      </div>
      <el-table :data="notificationLogs" size="small">
        <el-table-column prop="createdAt" label="时间" width="170">
          <template #default="scope">{{ formatTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="channel" label="通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.channel)">
              {{ channelLabel(scope.row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" width="160" />
        <el-table-column prop="title" label="标题" min-width="140" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag size="small" :type="notificationStatusType(scope.row.status)">
              {{ scope.row.status || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="重试" width="100">
          <template #default="scope">
            <span>{{ (scope.row.retryCount ?? 0) + '/' + (scope.row.maxRetries ?? '-') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="180">
          <template #default="scope">
            <span>{{ scope.row.errorMessage || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nextRetryAt" label="下次重试" width="160">
          <template #default="scope">
            <span>{{ scope.row.nextRetryAt || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :disabled="scope.row.status !== 'failed' || (scope.row.retryCount ?? 0) >= (scope.row.maxRetries ?? 0)"
              @click="retryLog(scope.row.id)"
            >
              重试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination layout="prev, pager, next" :total="logTotal" :page-size="logSize" :current-page="logPage" @current-change="onLogPage" />
      </div>
    </el-card>

    <el-card class="group-alert-card" shadow="never">
      <div class="group-head">
        <div class="group-title">通知通道配置</div>
        <el-button size="small" @click="loadNotificationConfigs">刷新</el-button>
      </div>
      <el-table :data="notificationConfigs" size="small">
        <el-table-column prop="channel" label="通道" width="120">
          <template #default="scope">
            <el-tag size="small" :type="channelTagType(scope.row.channel)">
              {{ channelLabel(scope.row.channel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'" size="small">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配置摘要" min-width="220">
          <template #default="scope">
            <span>{{ configSummary(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button-group>
              <el-button size="small" @click="openConfig(scope.row)">编辑</el-button>
              <el-button size="small" @click="openTest(scope.row)">测试</el-button>
              <el-button size="small" @click="validateConfig(scope.row)">校验</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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

    <el-dialog v-model="ruleDialog" :title="ruleEditingId ? '编辑告警规则' : '新增告警规则'" width="420px">
      <el-form :model="ruleForm" label-width="90px">
        <el-form-item label="分组名称">
          <el-input v-model="ruleForm.groupName" placeholder="如: 一层大屏" />
        </el-form-item>
        <el-form-item label="离线阈值">
          <el-input-number v-model="ruleForm.offlineThreshold" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="通知通道">
          <el-select v-model="ruleForm.notifyChannel" placeholder="选择通道">
            <el-option v-for="c in channelOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知目标">
          <el-input v-model="ruleForm.notifyTarget" placeholder="手机号/邮箱/机器人Webhook" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subscriptionDialog" :title="subscriptionEditingId ? '编辑订阅' : '新增订阅'" width="420px">
      <el-form :model="subscriptionForm" label-width="90px">
        <el-form-item label="分组">
          <el-select v-model="subscriptionForm.groupName" placeholder="全部分组" clearable>
            <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知通道">
          <el-select v-model="subscriptionForm.channel" placeholder="选择通道">
            <el-option v-for="c in channelOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知目标">
          <el-input v-model="subscriptionForm.target" placeholder="手机号/邮箱/机器人Webhook" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="subscriptionForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subscriptionDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSubscription">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="silenceDialog" :title="silenceEditingId ? '编辑静默' : '新增静默'" width="460px">
      <el-form :model="silenceForm" label-width="90px">
        <el-form-item label="分组">
          <el-select v-model="silenceForm.groupName" placeholder="全部分组" clearable>
            <el-option v-for="g in groupOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知通道">
          <el-select v-model="silenceForm.channel" placeholder="选择通道">
            <el-option v-for="c in channelOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段">
          <el-date-picker
            v-model="silenceFormRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%"
            @change="onSilenceRangeChange"
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="silenceForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="silenceDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSilence">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="configDialog" :title="`配置${channelLabel(configForm.channel)}`" width="520px">
      <el-form :model="configForm" label-width="110px">
        <el-form-item label="启用">
          <el-switch v-model="configForm.enabled" />
        </el-form-item>
        <template v-if="configForm.channel === 'email'">
          <el-form-item label="SMTP Host">
            <el-input v-model="configForm.host" placeholder="smtp.example.com" />
          </el-form-item>
          <el-form-item label="SMTP Port">
            <el-input-number v-model="configForm.port" :min="1" :max="65535" />
          </el-form-item>
          <el-form-item label="账号">
            <el-input v-model="configForm.username" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="configForm.password" show-password />
          </el-form-item>
          <el-form-item label="发件人">
            <el-input v-model="configForm.from" placeholder="可选" />
          </el-form-item>
          <el-form-item label="TLS">
            <el-switch v-model="configForm.tls" />
          </el-form-item>
        </template>
        <template v-else-if="configForm.channel === 'sms'">
          <el-form-item label="接口地址">
            <el-input v-model="configForm.endpoint" placeholder="https://api.example.com/sms/send" />
          </el-form-item>
          <el-form-item label="Headers(JSON)">
            <el-input v-model="configForm.headers" type="textarea" :rows="3" placeholder='{"Authorization":"Bearer xxx"}' />
          </el-form-item>
          <el-form-item label="Payload模板">
            <el-input v-model="configForm.payloadTemplate" type="textarea" :rows="3" placeholder='{"phone":"{target}","content":"{content}"}' />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="Webhook">
            <el-input v-model="configForm.webhookUrl" placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=..." />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="configDialog = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="testDialog" title="发送测试通知" width="420px">
      <el-form :model="testForm" label-width="90px">
        <el-form-item label="通道">
          <el-input v-model="testForm.channel" disabled />
        </el-form-item>
        <el-form-item label="目标">
          <el-input v-model="testForm.target" placeholder="手机号/邮箱/留空(站内)" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="testForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="testForm.content" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="testDialog = false">取消</el-button>
        <el-button type="primary" @click="sendTest">发送</el-button>
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
import { onMounted, ref, computed } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import {
  bindPlaylistToTerminals,
  fetchPlaylists,
  fetchTerminals,
  fetchTerminalHeartbeats,
  fetchTerminalPlaylists,
  fetchTerminalStatus,
  registerTerminal,
  fetchActiveBroadcasts,
  fetchGroupRules,
  createGroupRule,
  updateGroupRule,
  deleteGroupRule,
  fetchGroupAlerts,
  fetchOfflineTrend,
  fetchAlertHistory,
  fetchNotificationLogs,
  fetchNotificationHealth,
  fetchNotificationConfigs,
  saveNotificationConfig,
  sendNotificationTest,
  validateNotificationConfig,
  retryNotificationLog,
  fetchAlertSubscriptions,
  createAlertSubscription,
  updateAlertSubscription,
  deleteAlertSubscription,
  fetchAlertSilences,
  createAlertSilence,
  updateAlertSilence,
  deleteAlertSilence
} from '../api';
import { Plus, Monitor, WarningFilled, InfoFilled, Link, Connection, Timer, List, Setting, Bell } from '@element-plus/icons-vue';

const terminals = ref<any[]>([]);
const playlists = ref<any[]>([]);
const selectedTerminalIds = ref<number[]>([]);
const selectedPlaylist = ref<number | null>(null);
const startEnd = ref<[string, string] | null>(null);
const groupFilter = ref('');
const status = ref<{ online: number; offline: number; offlineTerminals: any[] }>({ online: 0, offline: 0, offlineTerminals: [] });
const groupRules = ref<any[]>([]);
const groupAlerts = ref<any[]>([]);
const offlineTrend = ref<any[]>([]);
const trendGroup = ref('');
const trendDays = ref(7);
const ruleDialog = ref(false);
const ruleEditingId = ref<number | null>(null);
const ruleForm = ref({ groupName: '', offlineThreshold: 1, enabled: true, notifyChannel: 'web', notifyTarget: '' });
const subscriptionDialog = ref(false);
const subscriptionEditingId = ref<number | null>(null);
const subscriptionForm = ref({ groupName: '', channel: 'web', target: '', enabled: true });
const silenceDialog = ref(false);
const silenceEditingId = ref<number | null>(null);
const silenceForm = ref({ groupName: '', channel: 'web', startTime: '', endTime: '', enabled: true });
const silenceFormRange = ref<[string, string] | null>(null);
const alertSubscriptions = ref<any[]>([]);
const alertSilences = ref<any[]>([]);
const alertHistory = ref<any[]>([]);
const notificationLogs = ref<any[]>([]);
const notificationHealth = ref<any[]>([]);
const notificationConfigs = ref<any[]>([]);
const historyPage = ref(1);
const historySize = ref(10);
const historyTotal = ref(0);
const logPage = ref(1);
const logSize = ref(10);
const logTotal = ref(0);
const configDialog = ref(false);
const testDialog = ref(false);
const configForm = ref<any>({
  channel: 'sms',
  enabled: true,
  host: '',
  port: 25,
  username: '',
  password: '',
  from: '',
  tls: false,
  webhookUrl: '',
  endpoint: '',
  headers: '',
  payloadTemplate: ''
});
const testForm = ref({ channel: 'sms', target: '', title: '通知测试', content: '这是一条测试消息' });
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

const groupOptions = computed(() => {
  const set = new Set<string>();
  terminals.value.forEach((t) => {
    if (t.groupName) set.add(t.groupName);
  });
  groupRules.value.forEach((r) => {
    if (r.groupName) set.add(r.groupName);
  });
  return Array.from(set);
});

const channelOptions = [
  { label: '站内通知', value: 'web' },
  { label: '短信', value: 'sms' },
  { label: '邮件', value: 'email' },
  { label: '企业微信', value: 'wechat' },
  { label: '钉钉', value: 'dingtalk' }
];

const activeAlerts = computed(() => {
  return groupAlerts.value.filter((item) => item.alert);
});

const trendMax = computed(() => {
  return Math.max(1, ...offlineTrend.value.map((row: any) => Number(row.offlineCount || 0)));
});

const notifiedAlertKeys = new Set<string>();

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
const loadGroupRules = async () => {
  const resp = await fetchGroupRules();
  groupRules.value = resp.data?.data || [];
};
const loadGroupAlerts = async () => {
  const resp = await fetchGroupAlerts();
  const alerts = resp.data?.data || [];
  groupAlerts.value = alerts;
  notifyAlerts(alerts);
};
const loadOfflineTrend = async () => {
  const resp = await fetchOfflineTrend(trendDays.value, trendGroup.value || undefined);
  offlineTrend.value = resp.data?.data || [];
};
const loadAlertHistory = async () => {
  const resp = await fetchAlertHistory(historyPage.value, historySize.value);
  const data = resp.data?.data || {};
  alertHistory.value = data.records || [];
  historyTotal.value = data.total || 0;
};
const loadNotificationLogs = async () => {
  const resp = await fetchNotificationLogs(logPage.value, logSize.value);
  const data = resp.data?.data || {};
  notificationLogs.value = data.records || [];
  logTotal.value = data.total || 0;
};
const loadNotificationHealth = async () => {
  try {
    const resp = await fetchNotificationHealth();
    notificationHealth.value = resp.data?.data || [];
  } catch {
    notificationHealth.value = [];
  }
};
const loadNotificationConfigs = async () => {
  const resp = await fetchNotificationConfigs();
  const list = resp.data?.data || [];
  const mapped = list.map((item: any) => {
    let config = {};
    if (item.configJson) {
      try {
        config = JSON.parse(item.configJson);
      } catch (e) {
        config = {};
      }
    }
    return { ...item, config };
  });
  const existing = new Set(mapped.map((c: any) => c.channel));
  channelOptions.filter((c) => c.value !== 'web').forEach((c) => {
    if (!existing.has(c.value)) {
      mapped.push({ channel: c.value, enabled: false, config: {} });
    }
  });
  notificationConfigs.value = mapped;
};
const loadAlertSubscriptions = async () => {
  const resp = await fetchAlertSubscriptions();
  alertSubscriptions.value = resp.data?.data || [];
};
const loadAlertSilences = async () => {
  const resp = await fetchAlertSilences();
  alertSilences.value = resp.data?.data || [];
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

const onHistoryPage = (p: number) => {
  historyPage.value = p;
  loadAlertHistory();
};

const onLogPage = (p: number) => {
  logPage.value = p;
  loadNotificationLogs();
};

const channelLabel = (channel?: string) => {
  const target = channel || 'web';
  const found = channelOptions.find((c) => c.value === target);
  return found ? found.label : '站内通知';
};

const channelTagType = (channel?: string) => {
  const target = channel || 'web';
  if (target === 'sms') return 'warning';
  if (target === 'email') return 'success';
  if (target === 'wechat') return 'info';
  if (target === 'dingtalk') return 'danger';
  return 'info';
};

const notificationStatusType = (status?: string) => {
  if (status === 'sent') return 'success';
  if (status === 'failed') return 'danger';
  if (status === 'abandoned') return 'warning';
  return 'info';
};

const configSummary = (row: any) => {
  const cfg = row.config || {};
  if (row.channel === 'email') {
    const host = cfg.host || '-';
    const port = cfg.port ? `:${cfg.port}` : '';
    return `${host}${port}`;
  }
  if (row.channel === 'sms') {
    return cfg.endpoint || '-';
  }
  if (row.channel === 'wechat' || row.channel === 'dingtalk') {
    return cfg.webhookUrl || cfg.webhook || '-';
  }
  return '-';
};

const validateConfig = async (row: any) => {
  const resp = await validateNotificationConfig(row.channel, { config: row.config });
  const data = resp.data?.data;
  if (data?.valid) {
    ElMessage.success(data.message || '配置校验通过');
  } else {
    ElMessage.warning(data?.message || '配置校验未通过');
  }
};

const retryLog = async (id: number) => {
  await retryNotificationLog(id);
  ElMessage.success('已触发重试');
  loadNotificationLogs();
};

const openConfig = (row: any) => {
  configForm.value = {
    channel: row.channel,
    enabled: row.enabled !== false,
    host: row.config?.host || '',
    port: row.config?.port || 25,
    username: row.config?.username || '',
    password: row.config?.password || '',
    from: row.config?.from || '',
    tls: !!row.config?.tls,
    webhookUrl: row.config?.webhookUrl || row.config?.webhook || '',
    endpoint: row.config?.endpoint || '',
    headers: row.config?.headers ? JSON.stringify(row.config.headers, null, 2) : '',
    payloadTemplate: row.config?.payloadTemplate || ''
  };
  configDialog.value = true;
};

const saveConfig = async () => {
  const channel = configForm.value.channel;
  const config: any = {};
  if (channel === 'email') {
    config.host = configForm.value.host;
    config.port = configForm.value.port;
    config.username = configForm.value.username;
    config.password = configForm.value.password;
    config.from = configForm.value.from;
    config.tls = configForm.value.tls;
  } else if (channel === 'sms') {
    config.endpoint = configForm.value.endpoint;
    if (configForm.value.headers) {
      try {
        config.headers = JSON.parse(configForm.value.headers);
      } catch (e) {
        ElMessage.error('短信Headers不是合法JSON');
        return;
      }
    }
    if (configForm.value.payloadTemplate) {
      config.payloadTemplate = configForm.value.payloadTemplate;
    }
  } else if (channel === 'wechat' || channel === 'dingtalk') {
    config.webhookUrl = configForm.value.webhookUrl;
  }
  await saveNotificationConfig(channel, { enabled: configForm.value.enabled, config });
  ElMessage.success('配置已保存');
  configDialog.value = false;
  loadNotificationConfigs();
};

const openTest = (row: any) => {
  testForm.value = { channel: row.channel, target: '', title: '通知测试', content: '这是一条测试消息' };
  testDialog.value = true;
};

const sendTest = async () => {
  if (testForm.value.channel !== 'web' && !testForm.value.target) {
    ElMessage.warning('请填写测试目标');
    return;
  }
  await sendNotificationTest(testForm.value);
  ElMessage.success('测试消息已发送');
  testDialog.value = false;
  loadNotificationLogs();
};

const formatDay = (day: string) => {
  if (!day) return '-';
  return day.length > 5 ? day.slice(5) : day;
};

const trendPercent = (row: any) => {
  const count = Number(row.offlineCount || 0);
  return Math.round((count / trendMax.value) * 100);
};

const notifyAlerts = (alerts: any[]) => {
  const activeKeys = new Set<string>();
  alerts.forEach((item) => {
    const channel = item.notifyChannel || 'web';
    const key = `${item.groupName}-${channel}`;
    if (item.alert) {
      activeKeys.add(key);
      if (!notifiedAlertKeys.has(key) && channel === 'web') {
        ElNotification({
          title: '分组离线告警',
          message: `${item.groupName} 离线 ${item.offline}/${item.total}，超过阈值 ${item.ruleThreshold}`,
          type: 'warning'
        });
        notifiedAlertKeys.add(key);
      }
    }
  });
  Array.from(notifiedAlertKeys).forEach((key) => {
    if (!activeKeys.has(key)) {
      notifiedAlertKeys.delete(key);
    }
  });
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

const openRuleDialog = () => {
  ruleEditingId.value = null;
  ruleForm.value = { groupName: '', offlineThreshold: 1, enabled: true, notifyChannel: 'web', notifyTarget: '' };
  ruleDialog.value = true;
};

const editRule = (row: any) => {
  ruleEditingId.value = row.id;
  ruleForm.value = {
    groupName: row.groupName,
    offlineThreshold: row.offlineThreshold,
    enabled: row.enabled,
    notifyChannel: row.notifyChannel || 'web',
    notifyTarget: row.notifyTarget || ''
  };
  ruleDialog.value = true;
};

const saveRule = async () => {
  if (!ruleForm.value.groupName) {
    ElMessage.warning('请输入分组名称');
    return;
  }
  if (ruleForm.value.notifyChannel !== 'web' && !ruleForm.value.notifyTarget) {
    ElMessage.warning('请填写通知目标');
    return;
  }
  if (ruleEditingId.value) {
    await updateGroupRule(ruleEditingId.value, ruleForm.value);
  } else {
    await createGroupRule(ruleForm.value);
  }
  ElMessage.success('已保存');
  ruleDialog.value = false;
  loadGroupRules();
  loadGroupAlerts();
};

const removeRule = async (id: number) => {
  await deleteGroupRule(id);
  ElMessage.success('已删除');
  loadGroupRules();
  loadGroupAlerts();
};

const openSubscriptionDialog = () => {
  subscriptionEditingId.value = null;
  subscriptionForm.value = { groupName: '', channel: 'web', target: '', enabled: true };
  subscriptionDialog.value = true;
};

const editSubscription = (row: any) => {
  subscriptionEditingId.value = row.id;
  subscriptionForm.value = {
    groupName: row.groupName || '',
    channel: row.channel || 'web',
    target: row.target || '',
    enabled: row.enabled
  };
  subscriptionDialog.value = true;
};

const saveSubscription = async () => {
  if (subscriptionForm.value.channel !== 'web' && !subscriptionForm.value.target) {
    ElMessage.warning('请填写通知目标');
    return;
  }
  if (subscriptionEditingId.value) {
    await updateAlertSubscription(subscriptionEditingId.value, subscriptionForm.value);
  } else {
    await createAlertSubscription(subscriptionForm.value);
  }
  ElMessage.success('订阅已保存');
  subscriptionDialog.value = false;
  loadAlertSubscriptions();
};

const removeSubscription = async (id: number) => {
  await deleteAlertSubscription(id);
  ElMessage.success('订阅已删除');
  loadAlertSubscriptions();
};

const openSilenceDialog = () => {
  silenceEditingId.value = null;
  silenceForm.value = { groupName: '', channel: 'web', startTime: '', endTime: '', enabled: true };
  silenceFormRange.value = null;
  silenceDialog.value = true;
};

const editSilence = (row: any) => {
  silenceEditingId.value = row.id;
  silenceForm.value = {
    groupName: row.groupName || '',
    channel: row.channel || 'web',
    startTime: row.startTime || '',
    endTime: row.endTime || '',
    enabled: row.enabled
  };
  if (row.startTime && row.endTime) {
    silenceFormRange.value = [row.startTime, row.endTime];
  } else {
    silenceFormRange.value = null;
  }
  silenceDialog.value = true;
};

const onSilenceRangeChange = (val: [string, string] | null) => {
  if (!val) {
    silenceForm.value.startTime = '';
    silenceForm.value.endTime = '';
    return;
  }
  silenceForm.value.startTime = val[0];
  silenceForm.value.endTime = val[1];
};

const saveSilence = async () => {
  if (!silenceForm.value.startTime || !silenceForm.value.endTime) {
    ElMessage.warning('请填写静默时间段');
    return;
  }
  if (silenceEditingId.value) {
    await updateAlertSilence(silenceEditingId.value, silenceForm.value);
  } else {
    await createAlertSilence(silenceForm.value);
  }
  ElMessage.success('静默已保存');
  silenceDialog.value = false;
  loadAlertSilences();
};

const removeSilence = async (id: number) => {
  await deleteAlertSilence(id);
  ElMessage.success('静默已删除');
  loadAlertSilences();
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
onMounted(() => {
  loadTerminals();
  loadPlaylists();
  loadStatus();
  loadGroupRules();
  loadGroupAlerts();
  loadOfflineTrend();
  loadAlertSubscriptions();
  loadAlertSilences();
  loadAlertHistory();
  loadNotificationLogs();
  loadNotificationHealth();
  loadNotificationConfigs();
});
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
.group-alert-card { border-radius: 12px; }
.group-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.group-title { font-weight: 600; }
.group-actions { display: flex; align-items: center; gap: 8px; }
.pagination { margin-top: 8px; text-align: right; }
.trend-chart { display: flex; align-items: flex-end; gap: 12px; height: 140px; padding: 8px 4px; margin-bottom: 12px; }
.trend-bar { flex: 1; min-width: 24px; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 4px; }
.trend-bar .bar { width: 100%; background: linear-gradient(180deg, #409eff, #67c23a); border-radius: 6px 6px 0 0; min-height: 6px; }
.trend-label { font-size: 12px; color: #909399; }
.trend-value { font-size: 12px; color: #606266; }

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
