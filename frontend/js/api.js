/**
 * Retrivo: Smart Campus Lost & Found Matcher - API Client
 * Clean, lightweight vanilla JS HTTP client with JWT handling
 */

const API_BASE = window.location.origin.includes(':8080')
    ? '/api'
    : 'http://localhost:8080/api';

const API = {
    // ------------------------------------------------------------
    // TOKEN & SESSION MANAGEMENT
    // ------------------------------------------------------------
    getToken() {
        return localStorage.getItem('retrivo_token') || localStorage.getItem('findit_token');
    },

    getUser() {
        try {
            const u = localStorage.getItem('retrivo_user') || localStorage.getItem('findit_user');
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    setSession(token, user) {
        localStorage.setItem('retrivo_token', token);
        localStorage.setItem('retrivo_user', JSON.stringify(user));
        // Keep findit keys synced for safety
        localStorage.setItem('findit_token', token);
        localStorage.setItem('findit_user', JSON.stringify(user));
    },

    clearSession() {
        localStorage.removeItem('retrivo_token');
        localStorage.removeItem('retrivo_user');
        localStorage.removeItem('findit_token');
        localStorage.removeItem('findit_user');
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    isAdmin() {
        const u = this.getUser();
        return u && u.role === 'ADMIN';
    },

    // ------------------------------------------------------------
    // CORE FETCH WRAPPER
    // ------------------------------------------------------------
    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const res = await fetch(url, config);
            const json = await res.json().catch(() => null);

            if (!res.ok) {
                if (res.status === 401 && !endpoint.includes('/auth/login')) {
                    this.clearSession();
                    window.dispatchEvent(new CustomEvent('auth-state-changed'));
                }
                const errorMsg = json?.message || `Request failed with status ${res.status}`;
                throw new Error(errorMsg);
            }

            return json?.data !== undefined ? json.data : json;
        } catch (err) {
            console.error(`API Error [${endpoint}]:`, err);
            throw err;
        }
    },

    // ------------------------------------------------------------
    // AUTHENTICATION
    // ------------------------------------------------------------
    async register(data) {
        const res = await this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data)
        });
        if (res && res.token) {
            this.setSession(res.token, {
                id: res.id,
                fullName: res.fullName,
                email: res.email,
                role: res.role
            });
            window.dispatchEvent(new CustomEvent('auth-state-changed'));
        }
        return res;
    },

    async login(email, password) {
        const res = await this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        if (res && res.token) {
            this.setSession(res.token, {
                id: res.id,
                fullName: res.fullName,
                email: res.email,
                role: res.role
            });
            window.dispatchEvent(new CustomEvent('auth-state-changed'));
        }
        return res;
    },

    logout() {
        this.clearSession();
        window.dispatchEvent(new CustomEvent('auth-state-changed'));
    },

    async getMe() {
        return this.request('/auth/me');
    },

    // ------------------------------------------------------------
    // CATEGORIES
    // ------------------------------------------------------------
    async getCategories() {
        return this.request('/categories');
    },

    // ------------------------------------------------------------
    // ITEMS (LOST & FOUND)
    // ------------------------------------------------------------
    async browseItems(params = {}) {
        const q = new URLSearchParams();
        Object.entries(params).forEach(([k, v]) => {
            if (v !== undefined && v !== null && v !== '') {
                q.append(k, v);
            }
        });
        return this.request(`/items/browse?${q.toString()}`);
    },

    async getRecentItems() {
        return this.request('/items/recent');
    },

    async getMyItems() {
        return this.request('/items/my');
    },

    async getItemById(id) {
        return this.request(`/items/${id}`);
    },

    async createItem(itemData) {
        return this.request('/items', {
            method: 'POST',
            body: JSON.stringify(itemData)
        });
    },

    async updateItem(id, itemData) {
        return this.request(`/items/${id}`, {
            method: 'PUT',
            body: JSON.stringify(itemData)
        });
    },

    async deleteItem(id) {
        return this.request(`/items/${id}`, {
            method: 'DELETE'
        });
    },

    async uploadImage(file) {
        const formData = new FormData();
        formData.append('file', file);

        const url = `${API_BASE}/items/upload-image`;
        const headers = {};
        const token = this.getToken();
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const res = await fetch(url, {
            method: 'POST',
            headers,
            body: formData
        });

        const json = await res.json();
        if (!res.ok) throw new Error(json?.message || 'Failed to upload image');
        return json.data;
    },

    // ------------------------------------------------------------
    // SMART MATCHES
    // ------------------------------------------------------------
    async getMyMatches() {
        return this.request('/matches');
    },

    async getMatchesForItem(itemId) {
        return this.request(`/matches/item/${itemId}`);
    },

    async getAllMatches(minScore) {
        return this.request(`/matches/all${minScore ? `?minScore=${minScore}` : ''}`);
    },

    // ------------------------------------------------------------
    // CLAIMS & RECOVERY
    // ------------------------------------------------------------
    async submitClaim(matchId, message) {
        return this.request('/claims', {
            method: 'POST',
            body: JSON.stringify({ matchId, message })
        });
    },

    async getMyClaims() {
        return this.request('/claims/my');
    },

    async reviewClaim(claimId, status, reviewNote) {
        return this.request(`/claims/${claimId}/review`, {
            method: 'PUT',
            body: JSON.stringify({ status, reviewNote })
        });
    },

    async completeClaim(claimId) {
        return this.request(`/claims/${claimId}/complete`, {
            method: 'PUT'
        });
    },

    async getClaimById(claimId) {
        return this.request(`/claims/${claimId}`);
    },

    async getClaimMessages(claimId) {
        return this.request(`/claims/${claimId}/messages`);
    },

    async sendClaimMessage(claimId, content) {
        return this.request(`/claims/${claimId}/messages`, {
            method: 'POST',
            body: JSON.stringify({ content })
        });
    },

    // ------------------------------------------------------------
    // NOTIFICATIONS
    // ------------------------------------------------------------
    async getNotifications() {
        return this.request('/notifications');
    },

    async getUnreadCount() {
        return this.request('/notifications/unread-count');
    },

    async markNotificationRead(id) {
        return this.request(`/notifications/${id}/read`, { method: 'PUT' });
    },

    async markAllNotificationsRead() {
        return this.request('/notifications/read-all', { method: 'PUT' });
    },

    // ------------------------------------------------------------
    // ADMIN DASHBOARD & MODERATION
    // ------------------------------------------------------------
    async getAdminStats() {
        return this.request('/admin/stats');
    },

    async getAdminUsers() {
        return this.request('/admin/users');
    },

    async getAdminItems(page = 0, size = 20) {
        return this.request(`/admin/items?page=${page}&size=${size}`);
    },

    async adminUpdateItemStatus(id, status) {
        return this.request(`/admin/items/${id}/status?status=${status}`, { method: 'PUT' });
    },

    async adminDeleteItem(id) {
        return this.request(`/admin/items/${id}`, { method: 'DELETE' });
    },

    // ------------------------------------------------------------
    // PUBLIC STATS
    // ------------------------------------------------------------
    async getPublicStats() {
        return this.request('/stats/public');
    }
};

window.API = API;

