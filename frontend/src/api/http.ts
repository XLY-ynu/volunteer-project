import axios from 'axios';

const http = axios.create({
  baseURL: '/api'
});

// 根据请求URL获取对应的token key
const getTokenKey = (url: string): string => {
  if (url.startsWith('/portal')) return 'portal_token';
  if (url.startsWith('/org')) return 'org_token';
  if (url.startsWith('/user-portal')) return 'userToken';
  return 'admin_token'; // 默认管理员端
};

http.interceptors.request.use((config) => {
  const url = config.url || '';
  const tokenKey = getTokenKey(url);
  const token = localStorage.getItem(tokenKey);
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (resp) => resp,
  (error) => {
    if (error.response && error.response.status === 401) {
      const url = error.config?.url || '';
      if (url.startsWith('/portal')) {
        localStorage.removeItem('portal_token');
        localStorage.removeItem('portal_profile');
      } else if (url.startsWith('/org')) {
        localStorage.removeItem('org_token');
        localStorage.removeItem('org_username');
        localStorage.removeItem('org_role');
        window.location.href = '/org/login';
      } else if (url.startsWith('/user-portal')) {
        localStorage.removeItem('userToken');
      } else {
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_username');
        localStorage.removeItem('admin_role');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default http;
