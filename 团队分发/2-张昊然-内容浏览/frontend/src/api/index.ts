import http from './http';

// 用户门户登录
export const userPortalLogin = (username: string, password: string) =>
  http.post('/user-portal/login', { username, password });

// 公开接口 - 内容浏览
export const getPublicCategories = () => http.get('/public/categories');
export const getPublicContent = (params?: { categoryId?: number; page?: number; size?: number; keyword?: string }) =>
  http.get('/public/content', { params });
export const getPublicContentById = (id: number) => http.get(`/public/content/${id}`);
export const getPublicMedia = (params?: { page?: number; size?: number; type?: string }) =>
  http.get('/public/media', { params });
export const getPublicActivities = (params?: { page?: number; size?: number; keyword?: string }) =>
  http.get('/public/activities', { params });
export const getPublicRecommendations = (parentId?: number, limit?: number, strategy?: string) =>
  http.get('/public/recommendations', { params: { parentId, limit, strategy } });

// 志愿者端 - 内容浏览和媒体观看
export const portalLogin = (payload: { phone: string; password: string }) =>
  http.post('/portal/auth/login', payload);
export const portalRegister = (payload: { name: string; phone: string; password: string; email?: string; organization?: string }) =>
  http.post('/portal/auth/register', payload);
export const fetchPortalMe = () => http.get('/portal/me');
export const fetchPortalStats = () => http.get('/portal/stats');
export const fetchPortalSignups = () => http.get('/portal/my-signups');
