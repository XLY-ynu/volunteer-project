import http from './http';

export const loginApi = (username: string, password: string) => {
  return http.post('/org/login', { username, password });
};

// 分类管理
export const fetchCategories = () => http.get('/categories');
export const createCategory = (payload: { name: string; code: string; parentId?: number; sortOrder?: number }) =>
  http.post('/categories', payload);
export const deleteCategory = (id: number) => http.delete(`/categories/${id}`);

// 内容管理
export const fetchContent = (page = 1, size = 10, categoryId?: number, published?: boolean, keyword?: string) =>
  http.get('/content', { params: { page, size, categoryId, published, keyword } });
export const createContent = (payload: any) => http.post('/content', payload);
export const updateContent = (id: number, payload: any) => http.put(`/content/${id}`, payload);
export const deleteContent = (id: number) => http.delete(`/content/${id}`);
export const fetchContentById = (id: number) => http.get(`/content/${id}`);

// 志愿者管理
export const fetchOrgVolunteers = (page = 1, size = 10, status?: string) =>
  http.get('/org/volunteers', { params: { page, size, status } });
export const auditVolunteer = (memberId: number, action: 'approve' | 'reject') =>
  http.post(`/org/volunteers/${memberId}/audit`, null, { params: { action } });

// 仪表盘
export const fetchOrgDashboard = () => http.get('/org/dashboard');
