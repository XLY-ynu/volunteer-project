import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('portal_token') || '',
    username: localStorage.getItem('portal_username') || '',
    role: localStorage.getItem('portal_role') || ''
  }),
  actions: {
    setToken(token: string) { this.token = token; localStorage.setItem('portal_token', token); },
    setUsername(username: string) { this.username = username; localStorage.setItem('portal_username', username); },
    setRole(role: string) { this.role = role; localStorage.setItem('portal_role', role); },
    logout() {
      this.token = ''; this.username = ''; this.role = '';
      localStorage.removeItem('portal_token'); localStorage.removeItem('portal_username'); localStorage.removeItem('portal_role');
      localStorage.removeItem('portal_profile');
    }
  }
});
