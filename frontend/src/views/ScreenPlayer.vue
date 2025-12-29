<template>
  <div class="screen-player">
    <div class="screen-toolbar">
      <div class="toolbar-left">
        <span class="screen-title">终端播放</span>
        <span class="screen-code">{{ terminalCode }}</span>
        <el-tag size="small" :type="playbackReady ? (offlineMode ? 'warning' : 'success') : 'info'">
          {{ playbackReady ? (offlineMode ? '离线回放' : '播放中') : '加载中' }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="loadPlayback">刷新</el-button>
        <el-button size="small" @click="togglePanel">{{ showPanel ? '隐藏队列' : '显示队列' }}</el-button>
      </div>
    </div>

    <div class="screen-canvas" v-if="areas.length">
      <div
        v-for="(area, idx) in areas"
        :key="idx"
        class="screen-area"
        :style="areaStyle(area)"
      >
        <div class="area-content" v-if="currentItem(idx)">
          <video
            v-if="currentItem(idx)?.type === 'video'"
            :key="itemKey(idx)"
            :src="currentItem(idx)?.url"
            autoplay
            muted
            playsinline
            loop
          ></video>
          <img
            v-else-if="currentItem(idx)?.type === 'image'"
            :key="itemKey(idx)"
            :src="currentItem(idx)?.url"
          />
          <div v-else class="content-slide">
            <div class="content-title">{{ currentItem(idx)?.title }}</div>
            <div class="content-summary">{{ currentItem(idx)?.summary || '内容展示' }}</div>
          </div>
        </div>
        <div v-else class="area-empty">暂无内容</div>
      </div>
    </div>
    <div v-else class="screen-empty">未配置布局或播放列表</div>

    <div class="queue-panel" v-if="showPanel">
      <div class="panel-section">
        <div class="panel-title">插播队列</div>
        <div v-if="!broadcastList.length" class="panel-empty">暂无插播任务</div>
        <div v-else class="queue-list">
          <div class="queue-item" v-for="(item, idx) in broadcastList" :key="item.id + '-' + idx">
            <div class="queue-index">{{ idx + 1 }}</div>
            <div class="queue-info">
              <div class="queue-name">{{ itemLabel(item) }}</div>
              <div class="queue-meta">
                {{ queueModeLabel(item) }} · 优先级 {{ item.priority || 0 }} · {{ item.duration || '-' }}s
              </div>
            </div>
            <el-tag size="small" type="danger" v-if="idx === broadcastIndex">播放中</el-tag>
          </div>
        </div>
      </div>
      <div class="panel-section">
        <div class="panel-title">区域级素材池</div>
        <div v-if="!areaQueues.length" class="panel-empty">暂无素材</div>
        <div v-else>
          <div class="pool" v-for="(pool, idx) in areaQueues" :key="'pool-' + idx">
            <div class="pool-head">
              <span>区域 {{ idx + 1 }}</span>
              <span class="pool-meta">{{ pool.length }} 条</span>
              <el-tag size="small" type="info">{{ areaModeLabel(idx) }}</el-tag>
            </div>
            <div class="pool-items">
              <div
                v-for="(item, pIdx) in pool.slice(0, 8)"
                :key="item.id + '-' + pIdx"
                class="pool-item"
                :class="{ active: pIdx === areaStates[idx]?.index }"
              >
                <span class="pool-name">{{ itemLabel(item) }}</span>
                <span class="pool-duration">{{ item.duration || '-' }}s</span>
              </div>
            </div>
            <div v-if="pool.length > 8" class="pool-more">+{{ pool.length - 8 }} 条</div>
          </div>
        </div>
      </div>
    </div>

    <div class="broadcast-overlay" v-if="broadcastItem">
      <div class="broadcast-badge">插播中</div>
      <video
        v-if="broadcastItem.type === 'video'"
        :key="broadcastItemKey"
        :src="broadcastItem.url"
        autoplay
        muted
        playsinline
        loop
      ></video>
      <img v-else-if="broadcastItem.type === 'image'" :src="broadcastItem.url" />
      <div v-else class="content-slide">
        <div class="content-title">{{ broadcastItem.title }}</div>
        <div class="content-summary">{{ broadcastItem.summary || '插播内容' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { fetchPlaybackPublic, fetchPublicBroadcasts, sendPublicHeartbeat, fetchPublicContentById } from '../api';

const route = useRoute();
const terminalCode = computed(() => String(route.query.code || 'public-screen'));
const showPanel = ref(route.query.debug === '1');
const areas = ref<any[]>([]);
const baseQueue = ref<any[]>([]);
const areaQueues = ref<any[][]>([]);
const areaStates = ref<{ index: number; startedAt: number }[]>([]);
const offlineMode = ref(false);
const playbackReady = ref(false);
const timer = ref<number | null>(null);
const broadcastTimer = ref<number | null>(null);
const heartbeatTimer = ref<number | null>(null);
const reloadTimer = ref<number | null>(null);
const broadcastList = ref<any[]>([]);
const broadcastIndex = ref(0);
const broadcastStartedAt = ref(Date.now());

const broadcastItem = computed(() => broadcastList.value[broadcastIndex.value] || null);
const broadcastItemKey = computed(() => `broadcast-${broadcastIndex.value}-${broadcastItem.value?.id || ''}`);

const areaStyle = (area: { x: number; y: number; w: number; h: number }) => ({
  left: area.x + '%',
  top: area.y + '%',
  width: area.w + '%',
  height: area.h + '%'
});

const togglePanel = () => {
  showPanel.value = !showPanel.value;
};

const itemLabel = (item: any) => item?.name || item?.title || item?.type || '素材';

const queueModeLabel = (item: any) => (item?.queueMode === 'interrupt' ? '插播优先' : '顺序排队');

const areaModeLabel = (idx: number) => {
  const area = areas.value[idx];
  if (!area) return '未知';
  return area.playMode === 'shared' ? '共享轮播' : '独立轮播';
};

const itemKey = (idx: number) => {
  const item = currentItem(idx);
  return `${idx}-${areaStates.value[idx]?.index || 0}-${item?.id || ''}`;
};

const currentItem = (idx: number) => {
  const state = areaStates.value[idx];
  const list = areaQueues.value[idx] || [];
  if (!state || !list.length) return null;
  return list[state.index] || null;
};

const buildQueue = async (playback: any) => {
  const assets = new Map<number, any>();
  (playback.mediaAssets || []).forEach((m: any) => assets.set(m.id, m));
  const items = playback.items || [];
  const list: any[] = [];
  for (const item of items) {
    if (item.mediaId) {
      const media = assets.get(item.mediaId);
      if (!media) continue;
      const type = media.type === 'video' ? 'video' : 'image';
      list.push({
        id: media.id,
        name: media.name,
        type,
        url: media.url,
        thumbUrl: media.thumbUrl,
        duration: item.displayDuration || media.durationSeconds || 10
      });
    } else if (item.contentId) {
      try {
        const resp = await fetchPublicContentById(item.contentId);
        const content = resp.data?.data;
        list.push({
          id: `content-${item.contentId}`,
          type: 'content',
          title: content?.title,
          summary: content?.summary,
          duration: item.displayDuration || 12
        });
      } catch (e) {
        // ignore
      }
    }
  }
  baseQueue.value = list;
  buildAreaQueues();
};

const buildAreaQueues = () => {
  const list = baseQueue.value || [];
  const count = areas.value.length || 1;
  areaQueues.value = areas.value.map((area, idx) => {
    const mode = area?.playMode || area?.mode || 'split';
    let subset = list;
    if (count > 1 && mode !== 'shared') {
      subset = list.filter((_, i) => i % count === idx);
      if (!subset.length) subset = list;
    }
    if (area?.shuffle) {
      subset = [...subset].sort(() => Math.random() - 0.5);
    }
    return subset;
  });
  areaStates.value = areas.value.map((_, idx) => ({
    index: list.length ? idx % list.length : 0,
    startedAt: Date.now()
  }));
};

const initAreas = (layoutJson?: string) => {
  if (layoutJson) {
    try {
      const parsed = JSON.parse(layoutJson);
      areas.value = parsed.areas || [{ x: 0, y: 0, w: 100, h: 100 }];
    } catch {
      areas.value = [{ x: 0, y: 0, w: 100, h: 100 }];
    }
  } else {
    areas.value = [{ x: 0, y: 0, w: 100, h: 100 }];
  }
  buildAreaQueues();
};

const tick = () => {
  if (!areaQueues.value.length) return;
  const now = Date.now();
  areaStates.value.forEach((state, idx) => {
    const list = areaQueues.value[idx] || [];
    if (!list.length) return;
    const item = list[state.index];
    const fallback = areas.value[idx]?.defaultDuration || 10;
    const duration = (item?.duration || fallback) * 1000;
    if (now - state.startedAt >= duration) {
      state.index = (state.index + 1) % list.length;
      state.startedAt = now;
    }
  });
};

const tickBroadcast = () => {
  if (!broadcastList.value.length) return;
  const current = broadcastList.value[broadcastIndex.value];
  const duration = (current?.duration || 10) * 1000;
  if (Date.now() - broadcastStartedAt.value >= duration) {
    broadcastIndex.value = (broadcastIndex.value + 1) % broadcastList.value.length;
    broadcastStartedAt.value = Date.now();
  }
};

const loadPlayback = async () => {
  playbackReady.value = false;
  try {
    const resp = await fetchPlaybackPublic(terminalCode.value);
    const list = resp.data?.data || [];
    const active = list[0];
    if (!active) {
      baseQueue.value = [];
      areaQueues.value = [];
      areas.value = [];
      return;
    }
    await buildQueue(active);
    initAreas(active.layout?.layoutJson);
    localStorage.setItem(`screen_cache_${terminalCode.value}`, JSON.stringify(active));
    offlineMode.value = false;
    playbackReady.value = true;
  } catch (e) {
    const cached = localStorage.getItem(`screen_cache_${terminalCode.value}`);
    if (cached) {
      const active = JSON.parse(cached);
      await buildQueue(active);
      initAreas(active.layout?.layoutJson);
      offlineMode.value = true;
      playbackReady.value = true;
    }
  }
};

const loadBroadcasts = async () => {
  try {
    const resp = await fetchPublicBroadcasts(terminalCode.value);
    const list = resp.data?.data || [];
    const mapped = list
      .map((item: any) => {
        const job = item.job || {};
        if (item.media) {
          return {
            id: item.media.id,
            type: item.media.type === 'video' ? 'video' : 'image',
            name: item.media.name,
            url: item.media.url,
            duration: item.media.durationSeconds || 10,
            priority: job.priority || 0,
            queueMode: job.queueMode || 'queue',
            startTime: job.startTime
          };
        }
        if (item.content) {
          return {
            id: `content-${item.content.id}`,
            type: 'content',
            title: item.content.title,
            summary: item.content.summary,
            duration: 12,
            priority: job.priority || 0,
            queueMode: job.queueMode || 'queue',
            startTime: job.startTime
          };
        }
        return null;
      })
      .filter(Boolean);
    broadcastList.value = mapped.sort((a: any, b: any) => {
      const wa = a.queueMode === 'interrupt' ? 0 : 1;
      const wb = b.queueMode === 'interrupt' ? 0 : 1;
      if (wa !== wb) return wa - wb;
      if (a.priority !== b.priority) return b.priority - a.priority;
      if (a.startTime && b.startTime) {
        return new Date(a.startTime).getTime() - new Date(b.startTime).getTime();
      }
      return 0;
    });
    if (broadcastList.value.length) {
      broadcastIndex.value = 0;
      broadcastStartedAt.value = Date.now();
    }
    localStorage.setItem(`screen_broadcast_cache_${terminalCode.value}`, JSON.stringify(broadcastList.value));
  } catch (e) {
    const cached = localStorage.getItem(`screen_broadcast_cache_${terminalCode.value}`);
    if (cached) {
      broadcastList.value = JSON.parse(cached);
      if (broadcastList.value.length) {
        broadcastIndex.value = 0;
        broadcastStartedAt.value = Date.now();
      }
    }
  }
};

const sendHeartbeat = () => {
  sendPublicHeartbeat({ code: terminalCode.value, status: offlineMode.value ? 'offline' : 'online' });
};

const startTimers = () => {
  if (timer.value) clearInterval(timer.value);
  timer.value = window.setInterval(tick, 1000);
  if (broadcastTimer.value) clearInterval(broadcastTimer.value);
  broadcastTimer.value = window.setInterval(tickBroadcast, 1000);
  if (heartbeatTimer.value) clearInterval(heartbeatTimer.value);
  heartbeatTimer.value = window.setInterval(sendHeartbeat, 60000);
  if (reloadTimer.value) clearInterval(reloadTimer.value);
  reloadTimer.value = window.setInterval(() => {
    loadPlayback();
    loadBroadcasts();
  }, 60000);
};

onMounted(async () => {
  await loadPlayback();
  await loadBroadcasts();
  sendHeartbeat();
  startTimers();
});

onBeforeUnmount(() => {
  if (timer.value) clearInterval(timer.value);
  if (broadcastTimer.value) clearInterval(broadcastTimer.value);
  if (heartbeatTimer.value) clearInterval(heartbeatTimer.value);
  if (reloadTimer.value) clearInterval(reloadTimer.value);
});
</script>

<style scoped>
.screen-player { position: relative; width: 100%; height: 100vh; background: #0f172a; color: #fff; overflow: hidden; }
.screen-toolbar { position: absolute; top: 16px; left: 16px; right: 16px; z-index: 5; display: flex; justify-content: space-between; align-items: center; background: rgba(15, 23, 42, 0.6); padding: 10px 16px; border-radius: 8px; }
.toolbar-left { display: flex; align-items: center; gap: 8px; }
.screen-title { font-weight: 600; }
.screen-code { font-size: 12px; color: #cbd5f5; }
.screen-canvas { position: absolute; inset: 0; }
.screen-area { position: absolute; padding: 6px; box-sizing: border-box; }
.area-content { width: 100%; height: 100%; background: #111827; border-radius: 8px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.area-content video, .area-content img { width: 100%; height: 100%; object-fit: cover; }
.area-empty { width: 100%; height: 100%; border: 1px dashed rgba(255,255,255,0.2); border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #94a3b8; }
.screen-empty { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; color: #94a3b8; }
.content-slide { padding: 16px; text-align: center; }
.content-title { font-size: 18px; font-weight: 600; margin-bottom: 6px; }
.content-summary { font-size: 12px; color: #cbd5f5; }
.broadcast-overlay { position: absolute; inset: 0; background: rgba(17, 24, 39, 0.88); display: flex; align-items: center; justify-content: center; z-index: 6; }
.broadcast-overlay video, .broadcast-overlay img { width: 90%; height: 90%; object-fit: contain; border-radius: 12px; }
.broadcast-badge { position: absolute; top: 20px; right: 20px; background: #ef4444; padding: 6px 12px; border-radius: 999px; font-size: 12px; }

.queue-panel { position: absolute; right: 16px; top: 64px; width: 320px; max-height: calc(100vh - 90px); background: rgba(15, 23, 42, 0.86); border-radius: 12px; padding: 12px; overflow: auto; z-index: 5; }
.panel-section { margin-bottom: 16px; }
.panel-title { font-weight: 600; margin-bottom: 8px; color: #e2e8f0; }
.panel-empty { color: #94a3b8; font-size: 12px; }
.queue-list { display: flex; flex-direction: column; gap: 8px; }
.queue-item { display: flex; align-items: center; gap: 8px; background: rgba(30, 41, 59, 0.6); padding: 8px; border-radius: 8px; }
.queue-index { width: 20px; height: 20px; border-radius: 50%; background: #334155; display: flex; align-items: center; justify-content: center; font-size: 12px; }
.queue-info { flex: 1; }
.queue-name { font-weight: 600; font-size: 13px; }
.queue-meta { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.pool { margin-bottom: 12px; }
.pool-head { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #cbd5f5; margin-bottom: 6px; }
.pool-meta { color: #94a3b8; }
.pool-items { display: flex; flex-direction: column; gap: 6px; }
.pool-item { display: flex; align-items: center; justify-content: space-between; background: rgba(30, 41, 59, 0.6); border-radius: 6px; padding: 6px 8px; font-size: 12px; }
.pool-item.active { background: rgba(59, 130, 246, 0.4); }
.pool-name { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pool-duration { color: #e2e8f0; }
.pool-more { font-size: 11px; color: #94a3b8; margin-top: 4px; text-align: right; }
</style>
