import http from './http';

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export const loginApi = (username: string, password: string) => {
  return http.post<{ data: LoginResponse }>('/auth/login', { username, password });
};

export const fetchCategories = () => {
  return http.get('/categories');
};

export const fetchPublicCategories = (parentId?: number) => {
  return http.get('/public/categories', { params: { parentId } });
};

export const createCategory = (payload: { name: string; code: string; parentId?: number; sortOrder?: number }) => {
  return http.post('/categories', payload);
};

export const deleteCategory = (id: number) => http.delete(`/categories/${id}`);

export const fetchMedia = (page = 1, size = 10) => {
  return http.get('/media', { params: { page, size } });
};

export const uploadMedia = (file: File, type?: string) => {
  const form = new FormData();
  form.append('file', file);
  if (type) form.append('type', type);
  return http.post('/media/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } });
};
export const uploadMediaThumb = (id: number, file: File) => {
  const form = new FormData();
  form.append('file', file);
  return http.post(`/media/${id}/thumb`, form, { headers: { 'Content-Type': 'multipart/form-data' } });
};

export const deleteMedia = (id: number) => http.delete(`/media/${id}`);
export const downloadMedia = (id: number) => http.get(`/media/${id}/download`, { responseType: 'blob' });

export const createPlaylist = (payload: any) => http.post('/playlists', payload);
export const updatePlaylist = (id: number, payload: any) => http.put(`/playlists/${id}`, payload);
export const deletePlaylist = (id: number) => http.delete(`/playlists/${id}`);
export const fetchPlaylists = () => http.get('/playlists');
export const fetchPlaylistItems = (playlistId: number) => http.get(`/playlists/${playlistId}/items`);
export const fetchPlaylistPreview = (playlistId: number) => http.get(`/playlists/${playlistId}/preview`);

export const fetchTerminals = (page = 1, size = 10, groupName?: string) =>
  http.get('/terminals', { params: { page, size, groupName } });
export const registerTerminal = (payload: any) => http.post('/terminals', payload);
export const bindPlaylistToTerminals = (payload: { terminalIds: number[]; playlistId: number; startTime?: string; endTime?: string }) =>
  http.post('/terminals/bind-playlist', payload);
export const fetchTerminalHeartbeats = (id: number, page = 1, size = 20) =>
  http.get(`/terminals/${id}/heartbeats`, { params: { page, size } });
export const fetchTerminalPlaylists = (id: number) => http.get(`/terminals/${id}/playlists`);

export const fetchContent = (page = 1, size = 10, categoryId?: number, published?: boolean, keyword?: string) =>
  http.get('/content', { params: { page, size, categoryId, published, keyword } });
export const createContent = (payload: any) => http.post('/content', payload);
export const updateContent = (id: number, payload: any) => http.put(`/content/${id}`, payload);
export const deleteContent = (id: number) => http.delete(`/content/${id}`);
export const fetchContentById = (id: number) => http.get(`/content/${id}`);
export const updateContentFlags = (id: number, payload: { headline?: boolean; recommended?: boolean }) =>
  http.put(`/content/${id}/flags`, payload);
export const updateContentOrder = (items: { id: number; sortOrder: number }[]) =>
  http.put('/content/reorder', { items });
export const updateContentWeights = (items: { id: number; recommendWeight: number }[]) =>
  http.put('/content/weights', { items });
export const fetchContentConfig = () => http.get('/content/config');
export const updateContentConfig = (payload: { recommendIntervalSec?: number; recommendCount?: number; recommendStrategy?: string; previewIntervalSec?: number }) =>
  http.put('/content/config', payload);
export const fetchPublicContentConfig = () => http.get('/public/content-config');
export const fetchRecommendedContent = () => http.get('/content/recommended');
export const fetchHeadlineContent = () => http.get('/content/headlines');
export const fetchPublicContent = (page = 1, size = 10, categoryId?: number, keyword?: string) =>
  http.get('/public/content', { params: { page, size, categoryId, keyword } });
export const fetchPublicContentById = (id: number) => http.get(`/public/content/${id}`);
export const fetchPublicRecommendations = (parentId?: number, limit?: number, strategy?: string) =>
  http.get('/public/recommendations', { params: { parentId, limit, strategy } });

export const fetchLayouts = () => http.get('/layouts');
export const createLayout = (payload: any) => http.post('/layouts', payload);
export const updateLayout = (id: number, payload: any) => http.put(`/layouts/${id}`, payload);
export const deleteLayout = (id: number) => http.delete(`/layouts/${id}`);
export const fetchLayout = (id: number) => http.get(`/layouts/${id}`);

export const fetchBroadcasts = (page = 1, size = 10, targetGroup?: string, targetTerminalCode?: string) =>
  http.get('/broadcasts', { params: { page, size, targetGroup, targetTerminalCode } });
export const createBroadcast = (payload: any) => http.post('/broadcasts', payload);
export const deleteBroadcast = (id: number) => http.delete(`/broadcasts/${id}`);
export const fetchBroadcastStatusCount = () => http.get('/broadcasts/status-count');
export const fetchActiveBroadcasts = (terminalCode: string, groupName?: string, page = 1, size = 10) =>
  http.get('/broadcasts/active', { params: { terminalCode, groupName, page, size } });

export const fetchSystemInfo = () => http.get('/ops/system-info');
export const downloadBackup = () => http.get('/ops/backup', { responseType: 'blob' });

export const fetchActivitiesPublic = (page = 1, size = 10, keyword?: string) =>
  http.get('/public/activities', { params: { page, size, keyword } });
export const signupActivityPublic = (payload: any) => http.post('/public/activities/signup-public', payload);
export const checkinActivityPublic = (payload: any) => http.post('/public/activities/checkin', payload);
export const fetchActivityStats = (id: number) => http.get(`/activities/${id}/stats`);
export const fetchVolunteerSignups = (phone: string) => http.get('/public/volunteer/signups', { params: { phone } });
export const registerVolunteerPublic = (payload: any) => http.post('/public/volunteer/register', payload);
export const fetchPlaybackPublic = (terminalCode: string) => http.get('/public/playback', { params: { terminalCode } });
export const fetchPublicTerminals = (groupName?: string) => http.get('/public/terminals', { params: { groupName } });

export const portalRegister = (payload: { name: string; phone: string; password: string; email?: string; organization?: string }) =>
  http.post('/portal/auth/register', payload);
export const portalLogin = (payload: { phone: string; password: string }) =>
  http.post('/portal/auth/login', payload);
export const portalCheckPhone = (phone: string) =>
  http.get('/portal/auth/check-phone', { params: { phone } });
export const portalResetPassword = (payload: { phone: string; newPassword: string }) =>
  http.post('/portal/auth/reset-password', payload);
export const portalChangePassword = (payload: { oldPassword: string; newPassword: string }) =>
  http.post('/portal/auth/change-password', payload);
export const fetchPortalMe = () => http.get('/portal/me');
export const updatePortalMe = (payload: { name?: string; email?: string; organization?: string }) =>
  http.put('/portal/me', payload);
export const fetchPortalSignups = () => http.get('/portal/my-signups');
export const signupActivityPortal = (payload: { activityId: number }) => http.post('/portal/activities/signup', payload);
export const fetchPortalAuditLogs = () => http.get('/portal/audit-logs');
export const fetchPortalStats = () => http.get('/portal/stats');
export const fetchPortalReminderSettings = () => http.get('/portal/reminder-settings');
export const updatePortalReminderSettings = (payload: { signupReminder?: boolean; checkinReminder?: boolean; channel?: string; reminderMinutes?: number }) =>
  http.put('/portal/reminder-settings', payload);
export const fetchPortalReminders = () => http.get('/portal/reminders');
export const fetchPortalReminderLogs = () => http.get('/portal/reminder-logs');
export const downloadPortalReminderLogs = () => http.get('/portal/reminder-logs/export', { responseType: 'blob' });
export const fetchPortalMessages = (page = 1, size = 10, type?: string, read?: string, status?: string) =>
  http.get('/portal/messages', { params: { page, size, type, read, status } });
export const markPortalMessagesRead = (payload: { keys?: string[]; readAll?: boolean }) =>
  http.post('/portal/messages/read', payload);

export const fetchGroupRules = () => http.get('/monitor/group-rules');
export const createGroupRule = (payload: { groupName: string; offlineThreshold?: number; enabled?: boolean; notifyChannel?: string; notifyTarget?: string }) =>
  http.post('/monitor/group-rules', payload);
export const updateGroupRule = (id: number, payload: { groupName?: string; offlineThreshold?: number; enabled?: boolean; notifyChannel?: string; notifyTarget?: string }) =>
  http.put(`/monitor/group-rules/${id}`, payload);
export const deleteGroupRule = (id: number) => http.delete(`/monitor/group-rules/${id}`);
export const fetchGroupAlerts = () => http.get('/monitor/group-alerts');
export const fetchOfflineTrend = (days = 7, groupName?: string) => http.get('/monitor/offline-trend', { params: { days, groupName } });
export const fetchAlertHistory = (page = 1, size = 20, groupName?: string) =>
  http.get('/monitor/alert-history', { params: { page, size, groupName } });
export const fetchNotificationLogs = (page = 1, size = 20, channel?: string) =>
  http.get('/monitor/notification-logs', { params: { page, size, channel } });
export const fetchNotificationConfigs = () => http.get('/monitor/notification-configs');
export const fetchNotificationConfig = (channel: string) => http.get(`/monitor/notification-configs/${channel}`);
export const saveNotificationConfig = (channel: string, payload: { enabled?: boolean; config?: Record<string, any> }) =>
  http.put(`/monitor/notification-configs/${channel}`, payload);
export const validateNotificationConfig = (channel: string, payload?: { config?: Record<string, any> }) =>
  http.post(`/monitor/notification-configs/${channel}/validate`, payload || {});
export const sendNotificationTest = (payload: { channel: string; target?: string; title?: string; content?: string }) =>
  http.post('/monitor/notification-test', payload);
export const retryNotificationLog = (id: number) => http.post(`/monitor/notification-logs/${id}/retry`);
export const fetchReminderLogs = (page = 1, size = 20, activityId?: number, status?: string, type?: string) =>
  http.get('/monitor/reminder-logs', { params: { page, size, activityId, status, type } });
export const fetchAlertSubscriptions = () => http.get('/monitor/alert-subscriptions');
export const createAlertSubscription = (payload: { groupName?: string; channel?: string; target?: string; enabled?: boolean }) =>
  http.post('/monitor/alert-subscriptions', payload);
export const updateAlertSubscription = (id: number, payload: { groupName?: string; channel?: string; target?: string; enabled?: boolean }) =>
  http.put(`/monitor/alert-subscriptions/${id}`, payload);
export const deleteAlertSubscription = (id: number) => http.delete(`/monitor/alert-subscriptions/${id}`);
export const fetchAlertSilences = () => http.get('/monitor/alert-silences');
export const createAlertSilence = (payload: { groupName?: string; channel?: string; startTime?: string; endTime?: string; enabled?: boolean }) =>
  http.post('/monitor/alert-silences', payload);
export const updateAlertSilence = (id: number, payload: { groupName?: string; channel?: string; startTime?: string; endTime?: string; enabled?: boolean }) =>
  http.put(`/monitor/alert-silences/${id}`, payload);
export const deleteAlertSilence = (id: number) => http.delete(`/monitor/alert-silences/${id}`);

export const fetchSummary = () => http.get('/monitor/summary');
export const fetchTerminalStatus = () => http.get('/monitor/terminal-status');

export const fetchPublicBroadcasts = (terminalCode: string) =>
  http.get('/public/broadcasts/active', { params: { terminalCode } });
export const sendPublicHeartbeat = (payload: { code: string; status: string }) =>
  http.post('/public/heartbeat', payload);
