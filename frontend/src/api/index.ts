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

export const createCategory = (payload: { name: string; code: string; parentId?: number; sortOrder?: number }) => {
  return http.post('/categories', payload);
};

export const fetchMedia = (page = 1, size = 10) => {
  return http.get('/media', { params: { page, size } });
};

export const uploadMedia = (file: File, type?: string) => {
  const form = new FormData();
  form.append('file', file);
  if (type) form.append('type', type);
  return http.post('/media/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } });
};

export const deleteMedia = (id: number) => http.delete(`/media/${id}`);
export const downloadMedia = (id: number) => http.get(`/media/${id}/download`, { responseType: 'blob' });

export const createPlaylist = (payload: any) => http.post('/playlists', payload);
export const updatePlaylist = (id: number, payload: any) => http.put(`/playlists/${id}`, payload);
export const deletePlaylist = (id: number) => http.delete(`/playlists/${id}`);
export const fetchPlaylists = () => http.get('/playlists');
export const fetchPlaylistItems = (playlistId: number) => http.get(`/playlists/${playlistId}/items`);

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

export const fetchLayouts = () => http.get('/layouts');
export const createLayout = (payload: any) => http.post('/layouts', payload);
export const fetchLayout = (id: number) => http.get(`/layouts/${id}`);

export const fetchBroadcasts = (page = 1, size = 10, targetGroup?: string, targetTerminalCode?: string) =>
  http.get('/broadcasts', { params: { page, size, targetGroup, targetTerminalCode } });
export const createBroadcast = (payload: any) => http.post('/broadcasts', payload);
export const fetchActiveBroadcasts = (terminalCode: string, page = 1, size = 10) =>
  http.get('/broadcasts/active', { params: { terminalCode, page, size } });

export const fetchSystemInfo = () => http.get('/ops/system-info');
export const downloadBackup = () => http.get('/ops/backup', { responseType: 'blob' });

export const fetchActivitiesPublic = (page = 1, size = 10, keyword?: string) =>
  http.get('/public/activities', { params: { page, size, keyword } });
export const signupActivityPublic = (payload: any) => http.post('/public/activities/signup-public', payload);

export const fetchSummary = () => http.get('/monitor/summary');
