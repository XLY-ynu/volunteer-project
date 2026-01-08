import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('org_token') || '',
    username: localStorage.getItem('org_username') || '',
    role: localStorage.getItem('org_role') || ''
  }),
  actions: {
    setToken(token: string) { this.token = token; localStorage.setItem('org_token', token); },
    setUsername(username: string) { this.username = username; localStorage.setItem('org_username', username); },
    setRole(role: string) { this.role = role; localStorage.setItem('org_role', role); },
    logout() {
      this.token = ''; this.username = ''; this.role = '';
      localStorage.removeItem('org_token'); localStorage.removeItem('org_username'); localStorage.removeItem('org_role');
    }
  }
});
