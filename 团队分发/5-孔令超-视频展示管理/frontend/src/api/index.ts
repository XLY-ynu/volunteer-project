import http from './http';

export const loginApi = (username: string, password: string) => {
  return http.post('/org/login', { username, password });
};

// 媒体资源管理
export const fetchMedia = (page = 1, size = 10) => http.get('/media', { params: { page, size } });
export const deleteMedia = (id: number) => http.delete(`/media/${id}`);
export const downloadMedia = (id: number) => http.get(`/media/${id}/download`, { responseType: 'blob' });
export const uploadMedia = (file: File, type?: string) => {
  const form = new FormData();
  form.append('file', file);
  if (type) form.append('type', type);
  return http.post('/media/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } });
};

// 播放列表管理
export const createPlaylist = (payload: any) => http.post('/playlists', payload);
export const updatePlaylist = (id: number, payload: any) => http.put(`/playlists/${id}`, payload);
export const deletePlaylist = (id: number) => http.delete(`/playlists/${id}`);
export const fetchPlaylists = () => http.get('/playlists');
export const fetchPlaylistItems = (playlistId: number) => http.get(`/playlists/${playlistId}/items`);
export const fetchPlaylistPreview = (playlistId: number) => http.get(`/playlists/${playlistId}/preview`);

export const fetchLayouts = () => http.get('/layouts');
export const fetchContent = (page = 1, size = 10, categoryId?: number, published?: boolean) =>
  http.get('/content', { params: { page, size, categoryId, published } });

// 志愿者端 - 活动参与
export const portalLogin = (payload: { phone: string; password: string }) =>
  http.post('/portal/auth/login', payload);
export const portalRegister = (payload: { name: string; phone: string; password: string; email?: string; organization?: string }) =>
  http.post('/portal/auth/register', payload);
export const fetchPortalMe = () => http.get('/portal/me');
export const updatePortalMe = (payload: { name?: string; email?: string; organization?: string }) =>
  http.put('/portal/me', payload);
export const fetchPortalStats = () => http.get('/portal/stats');
export const fetchPortalSignups = () => http.get('/portal/my-signups');
export const signupActivityPortal = (payload: { activityId: number }) => http.post('/portal/activities/signup', payload);
export const cancelActivitySignup = (activityId: number) => http.delete(`/portal/activities/signup/${activityId}`);
export const checkinActivityPublic = (payload: any) => http.post('/public/activities/checkin', payload);
export const portalCheckin = (code: string, token: string) =>
  http.post('/portal/checkin', { code }, { headers: { Authorization: `Bearer ${token}` } });

// 公开接口
export const getPublicActivities = (params?: { page?: number; size?: number; keyword?: string }) =>
  http.get('/public/activities', { params });
export const getPublicCategories = () => http.get('/public/categories');
export const getPublicContent = (params?: { categoryId?: number; page?: number; size?: number }) =>
  http.get('/public/content', { params });
export const getPublicMedia = (params?: { page?: number; size?: number; type?: string }) =>
  http.get('/public/media', { params });
