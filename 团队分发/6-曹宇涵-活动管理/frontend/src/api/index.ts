import http from './http';

export const loginApi = (username: string, password: string) => {
  return http.post('/org/login', { username, password });
};

// 活动管理
export const fetchActivities = (page = 1, size = 10, keyword?: string) =>
  http.get('/activities', { params: { page, size, keyword } });
export const createActivity = (payload: any) => http.post('/activities', payload);
export const updateActivity = (id: number, payload: any) => http.put(`/activities/${id}`, payload);
export const deleteActivity = (id: number) => http.delete(`/activities/${id}`);
export const fetchActivityStats = (id: number) => http.get(`/activities/${id}/stats`);
export const fetchActivitySignups = (id: number, page = 1, size = 20) =>
  http.get(`/activities/${id}/signups`, { params: { page, size } });

// 求助处理
export const fetchHelpRequests = (page = 1, size = 10, status?: string) =>
  http.get('/org/help-requests', { params: { page, size, status } });
export const replyHelpRequest = (id: number, payload: { reply: string; status: string }) =>
  http.post(`/org/help-requests/${id}/reply`, payload);
