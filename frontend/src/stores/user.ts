import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    setToken(token: string) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    setUsername(username: string) {
      this.username = username;
      localStorage.setItem('username', username);
    },
    setRole(role: string) {
      this.role = role;
      localStorage.setItem('role', role);
    },
    logout() {
      this.token = '';
      this.username = '';
      this.role = '';
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('role');
    }
  }
});
