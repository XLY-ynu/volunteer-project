import http from './http';

// 志愿者门户认证
export const portalLogin = (payload: { phone: string; password: string }) =>
  http.post('/portal/auth/login', payload);
export const portalRegister = (payload: { name: string; phone: string; password: string; email?: string; organization?: string }) =>
  http.post('/portal/auth/register', payload);
export const portalCheckPhone = (phone: string) =>
  http.get('/portal/auth/check-phone', { params: { phone } });

// 志愿者个人信息
export const fetchPortalMe = () => http.get('/portal/me');
export const updatePortalMe = (payload: { name?: string; email?: string; organization?: string }) =>
  http.put('/portal/me', payload);
export const fetchPortalStats = () => http.get('/portal/stats');

// 活动相关
export const fetchActivitiesPublic = (page = 1, size = 10, keyword?: string) =>
  http.get('/public/activities', { params: { page, size, keyword } });
export const fetchPortalSignups = () => http.get('/portal/my-signups');
export const signupActivityPortal = (payload: { activityId: number }) => http.post('/portal/activities/signup', payload);
export const cancelActivitySignup = (activityId: number) => http.delete(`/portal/activities/signup/${activityId}`);

// 签到
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

// 普通用户端 - 加入志愿者组织
export const getPublicOrgs = () => http.get('/public/orgs');
export const applyToOrg = (payload: { orgId: number; name: string; phone: string; email?: string; reason?: string }) =>
  http.post('/public/org-applications', payload);
export const getMyOrgApplications = (phone: string) =>
  http.get('/public/org-applications/my', { params: { phone } });

// 普通用户端 - 成为志愿者
export const registerVolunteerPublic = (payload: { name: string; phone: string; email?: string; organization?: string }) =>
  http.post('/public/volunteer/register', payload);
