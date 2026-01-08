import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('user_token') || '',
    username: localStorage.getItem('user_username') || '',
    role: localStorage.getItem('user_role') || ''
  }),
  actions: {
    setToken(token: string) { this.token = token; localStorage.setItem('user_token', token); },
    setUsername(username: string) { this.username = username; localStorage.setItem('user_username', username); },
    setRole(role: string) { this.role = role; localStorage.setItem('user_role', role); },
    logout() {
      this.token = ''; this.username = ''; this.role = '';
      localStorage.removeItem('user_token'); localStorage.removeItem('user_username'); localStorage.removeItem('user_role');
      localStorage.removeItem('user_profile');
    }
  }
});
