import http from './http';

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export const loginApi = (username: string, password: string) => {
  return http.post<{ data: LoginResponse }>('/auth/login', { username, password });
};

// 系统信息
export const fetchSystemInfo = () => http.get('/ops/system-info');
export const downloadBackup = () => http.get('/ops/backup', { responseType: 'blob' });

// 用户管理
export const fetchUsers = (page = 1, size = 10, keyword?: string) =>
  http.get('/users', { params: { page, size, keyword } });
export const createUser = (payload: { username: string; password: string; role: string; email?: string }) =>
  http.post('/users', payload);
export const updateUser = (id: number, payload: { username?: string; role?: string; email?: string; enabled?: boolean }) =>
  http.put(`/users/${id}`, payload);
export const deleteUser = (id: number) => http.delete(`/users/${id}`);
export const resetUserPassword = (id: number, newPassword: string) =>
  http.post(`/users/${id}/reset-password`, { newPassword });

// 操作日志
export const fetchOperationLogs = (page = 1, size = 20, username?: string, operation?: string, startTime?: string, endTime?: string) =>
  http.get('/ops/logs', { params: { page, size, username, operation, startTime, endTime } });
export const exportOperationLogs = (startTime?: string, endTime?: string) =>
  http.get('/ops/logs/export', { params: { startTime, endTime }, responseType: 'blob' });
