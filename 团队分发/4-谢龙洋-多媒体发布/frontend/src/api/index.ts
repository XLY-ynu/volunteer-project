import http from './http';

export const loginApi = (username: string, password: string) => {
  return http.post('/org/login', { username, password });
};

// 广播推送
export const fetchBroadcasts = (page = 1, size = 10, targetGroup?: string, targetTerminalCode?: string) =>
  http.get('/broadcasts', { params: { page, size, targetGroup, targetTerminalCode } });
export const createBroadcast = (payload: any) => http.post('/broadcasts', payload);
export const deleteBroadcast = (id: number) => http.delete(`/broadcasts/${id}`);
export const fetchBroadcastStatusCount = () => http.get('/broadcasts/status-count');

// 终端管理
export const fetchTerminals = (page = 1, size = 10, groupName?: string) =>
  http.get('/terminals', { params: { page, size, groupName } });
export const registerTerminal = (payload: any) => http.post('/terminals', payload);
export const deleteTerminal = (id: number) => http.delete(`/terminals/${id}`);
export const updateTerminalAttr = (id: number, payload: any) => http.put(`/terminals/${id}/attributes`, payload);
export const bindPlaylistToTerminals = (payload: { terminalIds: number[]; playlistId: number }) =>
  http.post('/terminals/bind-playlist', payload);
export const fetchTerminalHeartbeats = (id: number, page = 1, size = 20) =>
  http.get(`/terminals/${id}/heartbeats`, { params: { page, size } });
export const fetchTerminalPlaylists = (id: number) => http.get(`/terminals/${id}/playlists`);

export const fetchPlaylists = () => http.get('/playlists');
export const fetchLayouts = () => http.get('/layouts');

// 布局模板
export const fetchLayoutTemplates = () => http.get('/layout-templates');
export const createLayoutTemplate = (payload: any) => http.post('/layout-templates', payload);
export const updateLayoutTemplate = (id: number, payload: any) => http.put(`/layout-templates/${id}`, payload);
export const deleteLayoutTemplate = (id: number) => http.delete(`/layout-templates/${id}`);

// 普通用户端 - 发布求助
export const getPublicOrgs = () => http.get('/public/orgs');
export const submitHelpRequest = (payload: { orgId: number; title: string; content: string; contactName: string; contactPhone: string; address?: string }) =>
  http.post('/public/help-requests', payload);
export const getMyHelpRequests = (phone: string) =>
  http.get('/public/help-requests/my', { params: { phone } });
