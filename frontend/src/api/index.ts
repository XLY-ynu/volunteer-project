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

export const createPlaylist = (payload: any) => http.post('/playlists', payload);
export const fetchPlaylists = () => http.get('/playlists');
export const fetchPlaylistItems = (playlistId: number) => http.get(`/playlists/${playlistId}/items`);

export const fetchTerminals = (page = 1, size = 10, groupName?: string) =>
  http.get('/terminals', { params: { page, size, groupName } });
export const registerTerminal = (payload: any) => http.post('/terminals', payload);
export const bindPlaylistToTerminals = (payload: { terminalIds: number[]; playlistId: number; startTime?: string; endTime?: string }) =>
  http.post('/terminals/bind-playlist', payload);
