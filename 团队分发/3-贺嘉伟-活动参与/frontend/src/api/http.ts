import axios from 'axios';

const http = axios.create({ baseURL: '/api' });

http.interceptors.request.use((config) => {
  const url = config.url || '';
  const tokenKey = url.startsWith('/portal') ? 'portal_token' : 'token';
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
      }
    }
    return Promise.reject(error);
  }
);

export default http;
