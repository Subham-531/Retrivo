/**
 * Retrivo: Smart Campus Lost & Found Matcher - Main Application Controller
 * Handles client-side view routing, state management, form submissions, and Chart.js analytics
 */

const App = {
    state: {
        currentView: 'home',
        categories: [],
        currentUser: null,
        browseParams: {
            page: 0,
            size: 12,
            reportType: '',
            categoryId: '',
            colour: '',
            location: '',
            status: '',
            query: '',
            sortBy: 'createdAt',
            sortDir: 'desc'
        },
        adminCharts: {
            trends: null,
            category: null
        }
    },

    async init() {
        this.initTheme();
        this.setupEventListeners();
        await this.loadCategories();
        this.updateAuthStateUI();
        this.handleRoute();
        this.initMouseDepthController();
        this.initScrollReveal();

        // Check notifications if authenticated
        if (API.isAuthenticated()) {
            this.refreshUnreadNotifications();
        }
    },

    initTheme() {
        const saved = localStorage.getItem('retrivo_theme');
        const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const theme = saved || (systemPrefersDark ? 'dark' : 'light');
        this.setTheme(theme, false);

        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (!localStorage.getItem('retrivo_theme')) {
                    this.setTheme(e.matches ? 'dark' : 'light', true);
                }
            });
        }
    },

    setTheme(theme, notify = false) {
        document.documentElement.setAttribute('data-theme', theme);
        const btn = document.getElementById('themeToggleBtn');
        if (btn) {
            const isDark = theme === 'dark';
            btn.setAttribute('title', isDark ? 'Switch to Light Paper Mode' : 'Switch to Dark Archive Mode');
            btn.setAttribute('aria-label', isDark ? 'Switch to Light Paper Mode' : 'Switch to Dark Archive Mode');
        }

        // Re-render admin charts if admin view is active
        if (this.state.currentView === 'admin' && this.state.adminCharts && (this.state.adminCharts.trends || this.state.adminCharts.category)) {
            API.getAdminStats().then(stats => this.renderAdminCharts(stats)).catch(() => {});
        }

        if (notify) {
            this.showToast(`Switched to ${theme === 'dark' ? 'Dark Archive' : 'Light Paper'} mode`, 'info');
        }
    },

    toggleTheme() {
        const current = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
        const next = current === 'dark' ? 'light' : 'dark';
        localStorage.setItem('retrivo_theme', next);
        this.setTheme(next, true);
    },

    setupEventListeners() {
        // Navigation links
        document.querySelectorAll('[data-nav]').forEach(el => {
            el.addEventListener('click', (e) => {
                e.preventDefault();
                const view = el.getAttribute('data-nav');
                this.navigate(view);
            });
        });

        // Listen for auth state changes
        window.addEventListener('auth-state-changed', () => {
            this.updateAuthStateUI();
            this.handleRoute();
        });

        // Close dropdowns on outside click
        document.addEventListener('click', (e) => {
            const dropdown = document.getElementById('notifDropdown');
            const bellBtn = document.getElementById('notifBellBtn');
            if (dropdown && bellBtn && !bellBtn.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        // Window resize / popstate
        window.addEventListener('popstate', () => this.handleRoute());
    },

    navigate(view, params = {}) {
        window.history.pushState({ view, params }, '', `#${view}`);
        this.renderView(view, params);
    },

    handleRoute() {
        const hash = window.location.hash.replace('#', '') || 'home';
        this.renderView(hash);
    },

    async renderView(viewName, params = {}) {
        this.state.currentView = viewName;

        // Hide all views
        document.querySelectorAll('.view-section').forEach(sec => sec.classList.remove('active'));

        // Highlight active nav link
        document.querySelectorAll('.nav-item-btn').forEach(btn => {
            if (btn.getAttribute('data-nav') === viewName) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        const target = document.getElementById(`view-${viewName}`) || document.getElementById('view-home');
        if (target) target.classList.add('active');

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });

        // View specific initializers
        switch (viewName) {
            case 'home':
                await this.initHomeView();
                break;
            case 'browse':
                await this.initBrowseView(params);
                break;
            case 'dashboard':
                await this.initDashboardView();
                break;
            case 'report':
                this.initReportView(params);
                break;
            case 'matches':
                await this.initMatchesView();
                break;
            case 'claims':
                await this.initClaimsView();
                break;
            case 'admin':
                await this.initAdminView();
                break;
            default:
                await this.initHomeView();
        }

        if (window.lucide) window.lucide.createIcons();
    },

    updateAuthStateUI() {
        const user = API.getUser();
        this.state.currentUser = user;

        const authActions = document.getElementById('navAuthActions');
        const userProfile = document.getElementById('navUserProfile');
        const adminNavBtn = document.getElementById('navBtnAdmin');
        const dashboardNavBtn = document.getElementById('navBtnDashboard');

        if (user) {
            if (authActions) authActions.style.display = 'none';
            if (userProfile) {
                userProfile.style.display = 'flex';
                document.getElementById('navUserName').textContent = user.fullName;
                document.getElementById('navUserRole').textContent = user.role;
                document.getElementById('navUserInitial').textContent = user.fullName.charAt(0).toUpperCase();
            }
            if (dashboardNavBtn) dashboardNavBtn.style.display = 'flex';
            if (adminNavBtn) adminNavBtn.style.display = (user.role === 'ADMIN') ? 'flex' : 'none';
        } else {
            if (authActions) authActions.style.display = 'flex';
            if (userProfile) userProfile.style.display = 'none';
            if (dashboardNavBtn) dashboardNavBtn.style.display = 'none';
            if (adminNavBtn) adminNavBtn.style.display = 'none';
        }
    },

    async loadCategories() {
        try {
            this.state.categories = await API.getCategories();
            this.populateCategoryDropdowns();
        } catch (e) {
            console.error('Failed to load categories', e);
        }
    },

    populateCategoryDropdowns() {
        const selects = ['filterCategory', 'reportCategory'];
        selects.forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            const currentVal = el.value;
            let options = '<option value="">All Categories</option>';
            if (id === 'reportCategory') options = '<option value="" disabled selected>Select Category *</option>';
            this.state.categories.forEach(cat => {
                options += `<option value="${cat.id}">${this.escapeHtml(cat.name)}</option>`;
            });
            el.innerHTML = options;
            if (currentVal) el.value = currentVal;
        });
    },

    // ============================================================
    // VIEW 1: HOME (LANDING PAGE)
    // ============================================================
    async initHomeView() {
        try {
            // Load live statistics
            const stats = await API.getPublicStats();
            document.getElementById('statTotalReports').textContent = stats.totalReports || 0;
            document.getElementById('statTotalRecovered').textContent = stats.totalRecovered || 0;
            document.getElementById('statTotalMatches').textContent = stats.totalMatches || 0;
            document.getElementById('statTotalStudents').textContent = stats.totalStudents || 0;

            // Load recent reports
            const recent = await API.getRecentItems();
            const container = document.getElementById('recentItemsGrid');
            if (container) {
                if (!recent || recent.length === 0) {
                    container.innerHTML = '<div class="empty-state"><p class="empty-desc">No recent reports found.</p></div>';
                } else {
                    container.innerHTML = recent.map(item => this.createItemCardHtml(item)).join('');
                }
            }
        } catch (e) {
            console.error('Error loading home view', e);
        }
    },

    // ============================================================
    // VIEW 2: BROWSE & SEARCH ITEMS
    // ============================================================
    async initBrowseView(params = {}) {
        if (params.reportType !== undefined) {
            this.state.browseParams.reportType = params.reportType;
            this.setBrowseTabActive(params.reportType);
        }
        if (params.query !== undefined) {
            this.state.browseParams.query = params.query;
            const searchInput = document.getElementById('browseSearchInput');
            if (searchInput) searchInput.value = params.query;
        }

        await this.fetchAndRenderBrowseItems();
    },

    async fetchAndRenderBrowseItems() {
        const grid = document.getElementById('browseItemsGrid');
        const pagination = document.getElementById('browsePagination');
        if (!grid) return;

        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem;"><p>Loading items...</p></div>';

        try {
            const pageData = await API.browseItems(this.state.browseParams);
            const items = pageData.content || [];

            if (items.length === 0) {
                grid.innerHTML = `
                    <div class="empty-state" style="grid-column: 1/-1;">
                        <div class="empty-icon"><i data-lucide="inbox"></i></div>
                        <h3 class="empty-title">No Matching Reports Found</h3>
                        <p class="empty-desc">Try adjusting your filters, searching with different keywords, or check back later.</p>
                        <button class="btn btn-outline" onclick="App.resetBrowseFilters()">Reset All Filters</button>
                    </div>
                `;
            } else {
                grid.innerHTML = items.map(item => this.createItemCardHtml(item)).join('');
            }

            // Render Pagination
            if (pagination && pageData.totalPages > 1) {
                let pagesHtml = '';
                for (let i = 0; i < pageData.totalPages; i++) {
                    const activeClass = i === pageData.number ? 'btn-primary' : 'btn-outline';
                    pagesHtml += `<button class="btn btn-sm ${activeClass}" onclick="App.goToBrowsePage(${i})">${i + 1}</button>`;
                }
                pagination.innerHTML = pagesHtml;
            } else if (pagination) {
                pagination.innerHTML = '';
            }

            if (window.lucide) window.lucide.createIcons();
        } catch (e) {
            grid.innerHTML = `<div class="empty-state" style="grid-column: 1/-1;"><p class="empty-desc">Error loading items: ${e.message}</p></div>`;
        }
    },

    setBrowseTabActive(type) {
        document.querySelectorAll('#browseTabs .tab-btn').forEach(btn => {
            const tabType = btn.getAttribute('data-tab');
            if (tabType === type) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });
    },

    goToBrowsePage(pageIndex) {
        this.state.browseParams.page = pageIndex;
        this.fetchAndRenderBrowseItems();
    },

    resetBrowseFilters() {
        this.state.browseParams = {
            page: 0,
            size: 12,
            reportType: '',
            categoryId: '',
            colour: '',
            location: '',
            status: '',
            query: '',
            sortBy: 'createdAt',
            sortDir: 'desc'
        };
        const searchInput = document.getElementById('browseSearchInput');
        if (searchInput) searchInput.value = '';
        const catSelect = document.getElementById('filterCategory');
        if (catSelect) catSelect.value = '';
        const colourSelect = document.getElementById('filterColour');
        if (colourSelect) colourSelect.value = '';
        const statusSelect = document.getElementById('filterStatus');
        if (statusSelect) statusSelect.value = '';
        this.setBrowseTabActive('');
        this.fetchAndRenderBrowseItems();
    },

    // ============================================================
    // VIEW 3: STUDENT DASHBOARD
    // ============================================================
    setTxt(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val != null ? val : '';
    },

    async initDashboardView() {
        if (!API.isAuthenticated()) {
            this.showToast('Please log in to view your dashboard', 'info');
            this.openLoginModal();
            return;
        }

        const user = API.getUser();
        this.setTxt('dashStudentName', user ? user.fullName : 'Student');

        try {
            // Load user's reported items
            const myItems = await API.getMyItems();
            const activeLost = myItems.filter(i => i.reportType === 'LOST' && i.status === 'ACTIVE').length;
            const activeFound = myItems.filter(i => i.reportType === 'FOUND' && i.status === 'ACTIVE').length;
            const recovered = myItems.filter(i => i.status === 'RETURNED').length;

            this.setTxt('dashCountLost', activeLost);
            this.setTxt('dashCountFound', activeFound);
            this.setTxt('dashCountRecovered', recovered);

            // Load user matches count
            const matches = await API.getMyMatches();
            this.setTxt('dashCountMatches', matches.length);

            // Load user claims count
            const claims = await API.getMyClaims();
            const pendingClaims = claims.filter(c => c.status === 'PENDING').length;
            this.setTxt('dashCountClaims', pendingClaims);

            // Render My Reports Table
            const container = document.getElementById('dashMyReportsList');
            if (container) {
                if (myItems.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-icon"><i data-lucide="plus-circle"></i></div>
                            <h4 class="empty-title">You Haven't Reported Any Items Yet</h4>
                            <p class="empty-desc">Did you misplace something or discover an unattended belonging?</p>
                            <button class="btn btn-primary" onclick="App.navigate('report', { type: 'LOST' })">Report Lost Item</button>
                        </div>
                    `;
                } else {
                    container.innerHTML = `
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Item Name</th>
                                        <th>Category</th>
                                        <th>Type</th>
                                        <th>Location</th>
                                        <th>Date</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${myItems.map(item => `
                                        <tr>
                                            <td><strong>${this.escapeHtml(item.itemName)}</strong></td>
                                            <td>${this.escapeHtml(item.categoryName || 'General')}</td>
                                            <td><span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span></td>
                                            <td>${this.escapeHtml(item.location)}</td>
                                            <td>${item.eventDate}</td>
                                            <td><span class="badge badge-${item.status.toLowerCase()}">${item.status}</span></td>
                                            <td>
                                                <div style="display: flex; gap: 0.4rem;">
                                                    <button class="btn btn-sm btn-outline" onclick="App.viewItemDetails(${item.id})">View</button>
                                                    <button class="btn btn-sm btn-subtle" onclick="App.openMatchesForItem(${item.id})">Matches</button>
                                                    <button class="btn btn-sm btn-danger" onclick="App.deleteItemPrompt(${item.id})">Delete</button>
                                                </div>
                                            </td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    `;
                }
            }

            if (window.lucide) window.lucide.createIcons();
        } catch (e) {
            console.error('Error initializing dashboard', e);
        }
    },

    // ============================================================
    // VIEW 4: REPORT ITEM (LOST OR FOUND)
    // ============================================================
    initReportView(params = {}) {
        if (!API.isAuthenticated()) {
            this.showToast('Please log in to report an item', 'info');
            this.openLoginModal();
            return;
        }

        const type = params.type || 'LOST';
        this.setReportType(type);
    },

    setReportType(type) {
        document.querySelectorAll('#reportTypeTabs .tab-btn').forEach(btn => {
            if (btn.getAttribute('data-type') === type) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        const identifyingGroup = document.getElementById('identifyingDetailsGroup');
        const formTitle = document.getElementById('reportFormTitle');
        const formSubtitle = document.getElementById('reportFormSubtitle');
        const submitBtn = document.getElementById('reportSubmitBtn');

        if (type === 'LOST') {
            if (identifyingGroup) identifyingGroup.style.display = 'block';
            if (formTitle) formTitle.textContent = 'Report Lost Belonging';
            if (formSubtitle) formSubtitle.textContent = 'Provide accurate details to help our smart matching engine connect you with found items.';
            if (submitBtn) submitBtn.textContent = 'Publish Lost Report';
        } else {
            if (identifyingGroup) identifyingGroup.style.display = 'none';
            if (formTitle) formTitle.textContent = 'Report Found Item';
            if (formSubtitle) formSubtitle.textContent = 'Help a fellow student recover their misplaced belonging safely.';
            if (submitBtn) submitBtn.textContent = 'Publish Found Report';
        }

        document.getElementById('reportTypeInput').value = type;
    },

    async handleReportFormSubmit(e) {
        e.preventDefault();
        if (!API.isAuthenticated()) {
            this.openLoginModal();
            return;
        }

        const reportType = document.getElementById('reportTypeInput').value;
        const itemName = document.getElementById('reportItemName').value.trim();
        const categoryId = parseInt(document.getElementById('reportCategory').value, 10);
        const colour = document.getElementById('reportColour').value.trim();
        const brand = document.getElementById('reportBrand').value.trim();
        const location = document.getElementById('reportLocation').value.trim();
        const eventDate = document.getElementById('reportDate').value;
        const description = document.getElementById('reportDescription').value.trim();
        const identifyingDetails = document.getElementById('reportIdentifying').value.trim();
        const imageFile = document.getElementById('reportImageFile').files[0];

        if (!itemName || !categoryId || !colour || !location || !eventDate || !description) {
            this.showToast('Please fill in all required fields marked with *', 'error');
            return;
        }

        const submitBtn = document.getElementById('reportSubmitBtn');
        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Submitting report...';

        try {
            let uploadedImageName = null;
            if (imageFile) {
                submitBtn.textContent = 'Uploading picture...';
                uploadedImageName = await API.uploadImage(imageFile);
            }

            const payload = {
                reportType,
                itemName,
                categoryId,
                colour,
                brand: brand || null,
                location,
                eventDate,
                description,
                identifyingDetails: (reportType === 'LOST' && identifyingDetails) ? identifyingDetails : null,
                imagePath: uploadedImageName
            };

            const created = await API.createItem(payload);
            this.showToast('Report created successfully! Matching engine executed.', 'success');
            document.getElementById('reportItemForm').reset();
            this.navigate('matches');
        } catch (err) {
            this.showToast(err.message, 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    },

    // ============================================================
    // VIEW 5: SMART MATCHES
    // ============================================================
    async initMatchesView() {
        if (!API.isAuthenticated()) {
            this.showToast('Please log in to view smart matches', 'info');
            this.openLoginModal();
            return;
        }

        const container = document.getElementById('smartMatchesList');
        if (!container) return;
        container.innerHTML = '<div style="text-align: center; padding: 3rem;"><p>Calculating compatibility matches...</p></div>';

        try {
            const matches = await API.getMyMatches();
            if (matches.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon"><i data-lucide="sparkles"></i></div>
                        <h3 class="empty-title">No Active Matches Found Yet</h3>
                        <p class="empty-desc">Our rule-based engine actively compares reports 24/7. As soon as a compatible lost or found item is reported, you will be notified!</p>
                        <button class="btn btn-outline" onclick="App.navigate('browse')">Browse All Campus Items</button>
                    </div>
                `;
            } else {
                container.innerHTML = matches.map(match => this.createMatchCardHtml(match)).join('');
            }

            if (window.lucide) window.lucide.createIcons();
        } catch (e) {
            container.innerHTML = `<div class="empty-state"><p class="empty-desc">Error loading matches: ${e.message}</p></div>`;
        }
    },

    createMatchCardHtml(match) {
        const lost = match.lostItem || {};
        const found = match.foundItem || {};
        const score = match.compatibilityScore || 0;
        const reasons = match.matchingReasons || [];
        const isResolved = match.status === 'RESOLVED';
        const isClaimed = match.status === 'CLAIMED';

        const lostImgUrl = this.getItemImageUrl(lost.imagePath);
        const foundImgUrl = this.getItemImageUrl(found.imagePath);

        let badgeClass = 'badge-low';
        if (score >= 80) badgeClass = 'badge-strong';
        else if (score >= 50) badgeClass = 'badge-possible';

        return `
            <div class="match-card">
                <div class="match-card-grid">
                    <!-- Lost Item Column (Oxblood Ribbon) -->
                    <div class="match-item-pane match-item-pane-lost">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                            <span class="badge badge-lost">Lost Report</span>
                            <span style="font-family: var(--font-mono); font-size: 0.75rem; color: var(--sepia-500);">${lost.eventDate || ''}</span>
                        </div>
                        ${lostImgUrl ? `
                            <div class="match-item-thumb">
                                <img src="${lostImgUrl}" alt="${this.escapeHtml(lost.itemName || '')}" loading="lazy" onerror="this.parentElement.style.display='none'">
                            </div>
                        ` : ''}
                        <h4 style="font-family: var(--font-display); font-size: 1.05rem; font-weight: 700; color: var(--ink-900); margin-bottom: 0.4rem;">${this.escapeHtml(lost.itemName || 'Unnamed Item')}</h4>
                        <p style="font-size: 0.85rem; color: var(--ink-500); margin-bottom: 0.6rem; line-height: 1.4;">${this.escapeHtml(lost.description || '')}</p>
                        <div style="font-family: var(--font-mono); font-size: 0.775rem; color: var(--ink-500); display: flex; flex-direction: column; gap: 0.25rem;">
                            <span><strong>Location:</strong> ${this.escapeHtml(lost.location || 'Unknown')}</span>
                            <span><strong>Colour:</strong> ${this.escapeHtml(lost.colour || 'N/A')}</span>
                            <span><strong>Reported by:</strong> ${this.escapeHtml(lost.userName || 'Student')}</span>
                        </div>
                    </div>

                    <!-- Compatibility Score Center (Frosted Glass Dial) -->
                    <div class="match-score-center">
                        <div class="score-circle">
                            <span class="score-number">${score}</span>
                            <span class="score-percent">%</span>
                        </div>
                        <span class="badge ${badgeClass}">${match.compatibilityLabel}</span>
                        <span style="font-family: var(--font-mono); font-size: 0.7rem; color: var(--sepia-500);">${match.status}</span>
                    </div>

                    <!-- Found Item Column (Moss Green Ribbon) -->
                    <div class="match-item-pane match-item-pane-found">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                            <span class="badge badge-found">Found Report</span>
                            <span style="font-family: var(--font-mono); font-size: 0.75rem; color: var(--sepia-500);">${found.eventDate || ''}</span>
                        </div>
                        ${foundImgUrl ? `
                            <div class="match-item-thumb">
                                <img src="${foundImgUrl}" alt="${this.escapeHtml(found.itemName || '')}" loading="lazy" onerror="this.parentElement.style.display='none'">
                            </div>
                        ` : ''}
                        <h4 style="font-family: var(--font-display); font-size: 1.05rem; font-weight: 700; color: var(--ink-900); margin-bottom: 0.4rem;">${this.escapeHtml(found.itemName || 'Unnamed Item')}</h4>
                        <p style="font-size: 0.85rem; color: var(--ink-500); margin-bottom: 0.6rem; line-height: 1.4;">${this.escapeHtml(found.description || '')}</p>
                        <div style="font-family: var(--font-mono); font-size: 0.775rem; color: var(--ink-500); display: flex; flex-direction: column; gap: 0.25rem;">
                            <span><strong>Location:</strong> ${this.escapeHtml(found.location || 'Unknown')}</span>
                            <span><strong>Colour:</strong> ${this.escapeHtml(found.colour || 'N/A')}</span>
                            <span><strong>Found by:</strong> ${this.escapeHtml(found.userName || 'Finder')}</span>
                        </div>
                    </div>
                </div>

                <!-- Match Breakdown Reasons -->
                <div class="match-reasons-list">
                    <div style="font-family: var(--font-ui); font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: var(--sepia-500); margin-bottom: 6px;">
                        Algorithmic Scoring Breakdown (5-Point Engine):
                    </div>
                    ${reasons.map(r => `<div class="reason-chip"><i data-lucide="check" style="width: 14px; height: 14px; color: var(--moss-500);"></i> ${this.escapeHtml(r)}</div>`).join('')}
                </div>

                <!-- Claim Action Bar -->
                <div style="margin-top: 16px; display: flex; justify-content: flex-end; align-items: center; gap: 12px; flex-wrap: wrap;">
                    ${isResolved ? '<span class="badge badge-returned"><i data-lucide="check-check" style="width: 14px;"></i> Recovered & Returned</span>' :
                      isClaimed ? `
                        <span class="badge badge-pending">Claim Active</span>
                        <button class="btn btn-outline btn-sm" onclick="App.navigate('claims')">
                            <i data-lucide="message-square" style="width: 14px;"></i> View Claim &amp; Chat
                        </button>
                      ` :
                      `<button class="btn btn-primary btn-sm" onclick="App.openClaimModal(${match.id}, '${this.escapeHtml(found.itemName || 'Item')}')"><i data-lucide="shield-check" style="width: 14px;"></i> Claim This Item</button>`}
                </div>
            </div>
        `;
    },

    // ============================================================
    // VIEW 6: CLAIMS & RECOVERY
    // ============================================================
    async initClaimsView() {
        if (!API.isAuthenticated()) {
            this.showToast('Please log in to manage claims', 'info');
            this.openLoginModal();
            return;
        }

        const container = document.getElementById('claimsListContainer');
        if (!container) return;

        container.innerHTML = '<div style="text-align: center; padding: 3rem;"><p>Loading claims...</p></div>';

        try {
            const claims = await API.getMyClaims();
            const user = API.getUser();

            if (claims.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon"><i data-lucide="file-check-2"></i></div>
                        <h3 class="empty-title">No Ownership Claims Found</h3>
                        <p class="empty-desc">When you submit a claim on a matched found item, or when someone claims an item you found, details appear here.</p>
                        <button class="btn btn-primary" onclick="App.navigate('matches')">Explore Match Suggestions</button>
                    </div>
                `;
            } else {
                container.innerHTML = claims.map(c => this.createClaimCardHtml(c, user)).join('');
            }

            if (window.lucide) window.lucide.createIcons();
        } catch (e) {
            container.innerHTML = `<div class="empty-state"><p class="empty-desc">Error loading claims: ${e.message}</p></div>`;
        }
    },

    createClaimCardHtml(claim, currentUser) {
        const found = claim.foundItem || {};
        const lost = claim.lostItem || {};
        const isFinder = found.userId === currentUser.id;
        const isClaimant = claim.claimantId === currentUser.id;
        const isAdmin = currentUser.role === 'ADMIN';

        let statusBadgeClass = 'badge-pending';
        let statusLabel = claim.status;
        if (claim.status === 'APPROVED') {
            statusBadgeClass = 'badge-returned';
            statusLabel = 'APPROVED · AWAITING HANDOVER';
        } else if (claim.status === 'REJECTED') {
            statusBadgeClass = 'badge-lost';
            statusLabel = 'DECLINED';
        } else if (claim.status === 'COMPLETED') {
            statusBadgeClass = 'badge-active';
            statusLabel = 'HANDOVER COMPLETED';
        } else {
            statusBadgeClass = 'badge-pending';
            statusLabel = 'UNDER REVIEW';
        }

        return `
            <div class="match-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <div>
                        <span style="font-family: var(--font-mono); font-size: 12px; color: var(--sepia-500);">Claim #${claim.id} · ${new Date(claim.createdAt).toLocaleDateString()}</span>
                        <h4 style="font-family: var(--font-display); font-size: 20px; font-weight: 700; color: var(--ink-900); margin-top: 4px;">
                            Claim for "${this.escapeHtml(found.itemName || 'Item')}"
                        </h4>
                    </div>
                    <span class="badge ${statusBadgeClass}">${statusLabel}</span>
                </div>

                <div style="background: var(--paper-100); border: 1px solid var(--paper-400); padding: 16px; border-radius: 4px; margin-bottom: 16px; box-shadow: var(--shadow-paper);">
                    <div style="font-family: var(--font-ui); font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; color: var(--ink-700); margin-bottom: 4px;">
                        Claimant's Ownership Verification Statement:
                    </div>
                    <p style="font-family: var(--font-body); color: var(--ink-900); font-size: 15px; line-height: 22px;">${this.escapeHtml(claim.message)}</p>
                    <div style="margin-top: 8px; font-family: var(--font-mono); font-size: 12px; color: var(--ink-300);">
                        Submitted by: <strong>${this.escapeHtml(claim.claimantName)}</strong> (${this.escapeHtml(claim.claimantEmail)})
                    </div>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                    <div style="font-family: var(--font-mono); font-size: 12px; color: var(--ink-500);">
                        Finder: <strong>${this.escapeHtml(found.userName || 'Campus Member')}</strong>
                    </div>

                    <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
                        <button class="btn btn-outline btn-sm" onclick="App.openClaimChat(${claim.id})" style="display: inline-flex; align-items: center; gap: 6px;">
                            <i data-lucide="message-square" style="width: 14px; height: 14px;"></i> Direct Chat
                        </button>

                        ${(isFinder || isAdmin) && claim.status === 'PENDING' ? `
                            <button class="btn btn-teal btn-sm" onclick="App.reviewClaimPrompt(${claim.id}, 'APPROVED')"><i data-lucide="check"></i> Approve Claim</button>
                            <button class="btn btn-danger btn-sm" onclick="App.reviewClaimPrompt(${claim.id}, 'REJECTED')"><i data-lucide="x"></i> Decline</button>
                        ` : ''}

                        ${claim.status === 'APPROVED' && (isFinder || isClaimant || isAdmin) ? `
                            <button class="btn btn-primary btn-sm" onclick="App.markClaimCompleted(${claim.id})"><i data-lucide="check-check"></i> Confirm Physical Handover</button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    },

    async reviewClaimPrompt(claimId, decision) {
        let note = '';
        if (decision === 'REJECTED') {
            note = prompt('Please specify reason for declining claim:') || '';
            if (!note) return;
        } else {
            if (!confirm('Are you sure you want to APPROVE this claim? The claimant will be authorized to coordinate physical handover at the Property Office.')) return;
        }

        try {
            await API.reviewClaim(claimId, decision, note);
            this.showToast(`Claim has been ${decision === 'APPROVED' ? 'approved' : 'declined'} successfully!`, 'success');
            await this.initClaimsView();
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async markClaimCompleted(claimId) {
        if (!confirm('Confirm that the belonging was physically handed over to the verified owner?')) return;
        try {
            await API.completeClaim(claimId);
            this.showToast('Item status marked as COMPLETED / RECOVERED!', 'success');
            await this.initClaimsView();
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // ============================================================
    // VIEW 7: ADMIN DASHBOARD
    // ============================================================
    async initAdminView() {
        if (!API.isAuthenticated() || !API.isAdmin()) {
            this.showToast('Access restricted: Administrators only', 'error');
            this.navigate('home');
            return;
        }

        try {
            const stats = await API.getAdminStats();
            this.setTxt('adminStatUsers', stats.totalUsers || 0);
            this.setTxt('adminStatLost', stats.totalLost || 0);
            this.setTxt('adminStatFound', stats.totalFound || 0);
            this.setTxt('adminStatMatches', stats.totalMatches || 0);
            this.setTxt('adminStatRecovered', stats.totalRecovered || 0);
            this.setTxt('adminStatPendingClaims', stats.pendingClaims || 0);
            this.setTxt('adminStatRecoveryRate', `${stats.recoveryRatePercentage || 0}%`);

            // Render Charts using Chart.js
            this.renderAdminCharts(stats);

            // Load moderation table
            await this.loadAdminItems();

            // Load users list
            await this.loadAdminUsers();
        } catch (e) {
            console.error('Error loading admin dashboard', e);
        }
    },

    renderAdminCharts(stats) {
        if (!window.Chart) return;

        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const textColor = isDark ? '#F7F2E8' : '#1A1612';

        // 1. Monthly Trends Chart
        const trendsCanvas = document.getElementById('chartMonthlyTrends');
        if (trendsCanvas) {
            if (this.state.adminCharts.trends) this.state.adminCharts.trends.destroy();
            const months = (stats.monthlyTrends || []).map(t => t.month);
            const reportedData = (stats.monthlyTrends || []).map(t => t.reported);
            const recoveredData = (stats.monthlyTrends || []).map(t => t.recovered);

            this.state.adminCharts.trends = new Chart(trendsCanvas, {
                type: 'line',
                data: {
                    labels: months.length > 0 ? months : ['2026-10'],
                    datasets: [
                        {
                            label: 'Total Reports',
                            data: reportedData.length > 0 ? reportedData : [12],
                            borderColor: '#6B2C2C',
                            backgroundColor: 'rgba(107, 44, 44, 0.1)',
                            fill: true,
                            tension: 0.3
                        },
                        {
                            label: 'Recovered Items',
                            data: recoveredData.length > 0 ? recoveredData : [2],
                            borderColor: '#5B6E3A',
                            backgroundColor: 'rgba(91, 110, 58, 0.1)',
                            fill: true,
                            tension: 0.3
                        }
                    ]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                font: { family: "'Newsreader', Georgia, serif", size: 13 },
                                color: textColor
                            }
                        }
                    }
                }
            });
        }

        // 2. Category Distribution Doughnut Chart
        const catCanvas = document.getElementById('chartCategoryDist');
        if (catCanvas) {
            if (this.state.adminCharts.category) this.state.adminCharts.category.destroy();
            const catMap = stats.categoryDistribution || {};
            const labels = Object.keys(catMap);
            const data = Object.values(catMap);

            this.state.adminCharts.category = new Chart(catCanvas, {
                type: 'doughnut',
                data: {
                    labels: labels.length > 0 ? labels : ['Electronics', 'Wallets', 'Books', 'Bottles', 'Keys'],
                    datasets: [{
                        data: data.length > 0 ? data : [5, 2, 2, 2, 1],
                        backgroundColor: [
                            '#6B2C2C', '#5B6E3A', '#B87333', '#8B6F47', '#2E2620', '#A85959', '#8A9D5F', '#D9A86C'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                font: { family: "'Newsreader', Georgia, serif", size: 12 },
                                color: textColor
                            }
                        }
                    }
                }
            });
        }
    },

    async loadAdminItems() {
        const tbody = document.getElementById('adminItemsTableBody');
        if (!tbody) return;
        try {
            const pageData = await API.getAdminItems(0, 30);
            const items = pageData.content || [];
            tbody.innerHTML = items.map(item => `
                <tr>
                    <td>#${item.id}</td>
                    <td><strong>${this.escapeHtml(item.itemName)}</strong></td>
                    <td><span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span></td>
                    <td>${this.escapeHtml(item.categoryName || 'General')}</td>
                    <td>${this.escapeHtml(item.userName || 'Student')}</td>
                    <td>
                        <select class="form-control" style="padding: 2px 6px; font-size: 0.775rem;" onchange="App.adminChangeStatus(${item.id}, this.value)">
                            <option value="ACTIVE" ${item.status === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                            <option value="CLAIM_PENDING" ${item.status === 'CLAIM_PENDING' ? 'selected' : ''}>CLAIM_PENDING</option>
                            <option value="RETURNED" ${item.status === 'RETURNED' ? 'selected' : ''}>RETURNED</option>
                            <option value="CLOSED" ${item.status === 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                        </select>
                    </td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="App.adminDeleteItemConfirm(${item.id})">Delete</button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            console.error('Failed to load admin items', e);
        }
    },

    async loadAdminUsers() {
        const tbody = document.getElementById('adminUsersTableBody');
        if (!tbody) return;
        try {
            const users = await API.getAdminUsers();
            tbody.innerHTML = users.map(u => `
                <tr>
                    <td>#${u.id}</td>
                    <td><strong>${this.escapeHtml(u.fullName)}</strong></td>
                    <td>${this.escapeHtml(u.email)}</td>
                    <td><span class="badge ${u.role === 'ADMIN' ? 'badge-returned' : 'badge-found'}">${u.role}</span></td>
                    <td>${new Date(u.createdAt).toLocaleDateString()}</td>
                </tr>
            `).join('');
        } catch (e) {
            console.error('Failed to load admin users', e);
        }
    },

    async adminChangeStatus(itemId, newStatus) {
        try {
            await API.adminUpdateItemStatus(itemId, newStatus);
            this.showToast(`Item #${itemId} status updated to ${newStatus}`, 'success');
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    async adminDeleteItemConfirm(itemId) {
        if (!confirm(`Are you sure you want to permanently delete Item #${itemId}?`)) return;
        try {
            await API.adminDeleteItem(itemId);
            this.showToast(`Item #${itemId} deleted`, 'success');
            await this.loadAdminItems();
        } catch (e) {
            this.showToast(e.message, 'error');
        }
    },

    // ============================================================
    // MODALS: ITEM DETAILS, CLAIMS, AUTH
    // ============================================================
    async viewItemDetails(id) {
        try {
            const item = await API.getItemById(id);
            const modalBody = document.getElementById('itemDetailModalBody');
            if (!modalBody) return;

            const imgUrl = this.getItemImageUrl(item.imagePath);

            modalBody.innerHTML = `
                <div style="display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap;">
                    <span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span>
                    <span class="badge badge-${item.status.toLowerCase()}">${item.status}</span>
                    <span class="badge badge-active">${this.escapeHtml(item.categoryName || 'General')}</span>
                </div>

                ${imgUrl ? `
                    <div class="item-detail-photo">
                        <img src="${imgUrl}" alt="${this.escapeHtml(item.itemName)}" onerror="this.parentElement.style.display='none'">
                        <span class="item-detail-photo-tag">EVIDENCE PHOTO &bull; ${item.reportType} #${String(item.id).padStart(2, '0')}</span>
                    </div>
                ` : ''}

                <h3 style="font-family: var(--font-display); font-size: 22px; font-weight: 800; margin-bottom: 8px; color: var(--ink-900);">${this.escapeHtml(item.itemName)}</h3>
                <p style="font-family: var(--font-body); color: var(--ink-500); margin-bottom: 20px; font-size: 15px; line-height: 22px;">${this.escapeHtml(item.description)}</p>

                <div style="background: var(--paper-200); border: 1px solid var(--paper-400); padding: 16px; border-radius: 4px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-family: var(--font-mono); font-size: 13px; margin-bottom: 20px; box-shadow: var(--shadow-paper);">
                    <div><strong style="color: var(--ink-700);">Colour:</strong> ${this.escapeHtml(item.colour)}</div>
                    <div><strong style="color: var(--ink-700);">Brand:</strong> ${this.escapeHtml(item.brand || 'N/A')}</div>
                    <div><strong style="color: var(--ink-700);">Location:</strong> ${this.escapeHtml(item.location)}</div>
                    <div><strong style="color: var(--ink-700);">Event Date:</strong> ${item.eventDate}</div>
                    <div><strong style="color: var(--ink-700);">Reporter:</strong> ${this.escapeHtml(item.userName || 'Student')}</div>
                    <div><strong style="color: var(--ink-700);">Contact:</strong> ${this.escapeHtml(item.userEmail || 'Protected')}</div>
                </div>

                ${item.identifyingDetails ? `
                    <div style="background: var(--paper-100); border: 1.5px solid var(--brass-500); padding: 16px; border-radius: 4px; margin-bottom: 20px; box-shadow: var(--shadow-inset-down);">
                        <strong style="display: flex; align-items: center; gap: 6px; color: var(--brass-500); font-family: var(--font-ui); font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;">
                            <i data-lucide="shield-check" style="width: 15px; height: 15px;"></i> Private Ownership Detail (Authorised View):
                        </strong>
                        <p style="font-family: var(--font-body); color: var(--ink-700); margin-top: 6px; font-size: 14px;">${this.escapeHtml(item.identifyingDetails)}</p>
                    </div>
                ` : ''}

                <div style="display: flex; justify-content: flex-end; gap: 12px;">
                    <button class="btn btn-outline" onclick="App.closeModal('itemDetailModal')">Close</button>
                    <button class="btn btn-primary" onclick="App.closeModal('itemDetailModal'); App.openMatchesForItem(${item.id})">
                        <i data-lucide="sparkles" style="width: 14px;"></i> Find Matches
                    </button>
                </div>
            `;

            this.openModal('itemDetailModal');
            if (window.lucide) window.lucide.createIcons();
        } catch (e) {
            this.showToast('Could not load item details: ' + e.message, 'error');
        }
    },

    openMatchesForItem(itemId) {
        this.closeModal('itemDetailModal');
        this.navigate('matches');
    },

    deleteItemPrompt(id) {
        if (!confirm('Are you sure you want to delete this report? This action cannot be undone.')) return;
        API.deleteItem(id)
            .then(() => {
                this.showToast('Report deleted successfully', 'success');
                this.initDashboardView();
            })
            .catch(err => this.showToast(err.message, 'error'));
    },

    openClaimModal(matchId, itemName) {
        document.getElementById('claimMatchId').value = matchId;
        document.getElementById('claimItemTitle').textContent = itemName;
        document.getElementById('claimMessageInput').value = '';
        this.openModal('claimModal');
    },

    async handleClaimSubmit(e) {
        e.preventDefault();
        const matchId = document.getElementById('claimMatchId').value;
        const message = document.getElementById('claimMessageInput').value.trim();

        if (!message) {
            this.showToast('Please provide proof of ownership', 'error');
            return;
        }

        try {
            const claim = await API.submitClaim(matchId, message);
            this.showToast('Ownership claim submitted! Opening direct chat with the finder...', 'success');
            this.closeModal('claimModal');
            if (claim && claim.id) {
                this.openClaimChat(claim.id);
            } else {
                this.navigate('claims');
            }
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    // ============================================================
    // NOTIFICATIONS
    // ============================================================
    async refreshUnreadNotifications() {
        try {
            const count = await API.getUnreadCount();
            const badge = document.getElementById('bellBadge');
            if (badge) {
                if (count > 0) {
                    badge.textContent = count > 9 ? '9+' : count;
                    badge.style.display = 'flex';
                } else {
                    badge.style.display = 'none';
                }
            }
        } catch (ignored) {}
    },

    async toggleNotificationsDropdown() {
        const dropdown = document.getElementById('notifDropdown');
        if (!dropdown) return;
        dropdown.classList.toggle('show');

        if (dropdown.classList.contains('show')) {
            const list = document.getElementById('notifDropdownList');
            list.innerHTML = '<div style="padding: 1rem; text-align: center; color: var(--text-muted);">Loading notifications...</div>';

            try {
                const notifications = await API.getNotifications();
                if (notifications.length === 0) {
                    list.innerHTML = '<div style="padding: 2rem 1rem; text-align: center; color: var(--text-muted); font-size: 0.85rem;">No notifications right now</div>';
                } else {
                    list.innerHTML = notifications.slice(0, 10).map(n => `
                        <div class="notif-item ${!n.isRead ? 'unread' : ''}" onclick="App.handleNotificationClick(${n.id}, '${n.link || ''}')">
                            <div class="notif-item-title">${this.escapeHtml(n.title)}</div>
                            <div class="notif-item-msg">${this.escapeHtml(n.message)}</div>
                            <div class="notif-item-time">${new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
                        </div>
                    `).join('');
                }
            } catch (e) {
                list.innerHTML = `<div style="padding: 1rem; color: var(--danger);">Failed to load notifications</div>`;
            }
        }
    },

    async handleNotificationClick(id, link) {
        try {
            await API.markNotificationRead(id);
            this.refreshUnreadNotifications();
            document.getElementById('notifDropdown').classList.remove('show');
            if (link) {
                const route = link.replace('/', '');
                this.navigate(route);
            }
        } catch (e) {}
    },

    async markAllNotificationsRead() {
        try {
            await API.markAllNotificationsRead();
            this.refreshUnreadNotifications();
            this.toggleNotificationsDropdown();
            this.showToast('All notifications marked as read', 'info');
        } catch (e) {}
    },

    // ============================================================
    // AUTH MODALS
    // ============================================================
    openLoginModal() {
        this.closeModal('registerModal');
        this.openModal('loginModal');
    },

    openRegisterModal() {
        this.closeModal('loginModal');
        this.openModal('registerModal');
    },

    async handleLoginSubmit(e) {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value.trim();
        const pass = document.getElementById('loginPassword').value;

        try {
            await API.login(email, pass);
            this.showToast('Welcome back! Logged in successfully.', 'success');
            this.closeModal('loginModal');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    async handleRegisterSubmit(e) {
        e.preventDefault();
        const fullName = document.getElementById('regFullName').value.trim();
        const email = document.getElementById('regEmail').value.trim();
        const phone = document.getElementById('regPhone').value.trim();
        const password = document.getElementById('regPassword').value;

        try {
            await API.register({ fullName, email, phone, password });
            this.showToast('Account registered successfully! Welcome to Retrivo.', 'success');
            this.closeModal('registerModal');
        } catch (err) {
            this.showToast(err.message, 'error');
        }
    },

    handleLogout() {
        API.logout();
        this.showToast('You have been logged out.', 'info');
        this.navigate('home');
    },

    fillDemoLogin(email, password) {
        document.getElementById('loginEmail').value = email;
        document.getElementById('loginPassword').value = password;
    },

    // ============================================================
    // UI HELPERS (TOAST, MODALS, CARDS)
    // ============================================================
    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const iconName = type === 'error' ? 'alert-triangle' : (type === 'success' ? 'check-check' : 'bell');
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <i data-lucide="${iconName}" style="width: 18px; height: 18px; flex-shrink: 0; color: ${type === 'error' ? 'var(--rust-500)' : (type === 'success' ? 'var(--moss-500)' : 'var(--brass-500)')};"></i>
            <span>${this.escapeHtml(message)}</span>
        `;
        container.appendChild(toast);
        if (window.lucide) window.lucide.createIcons();

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    openModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.classList.add('show');
    },

    closeModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.classList.remove('show');
    },

    getItemImageUrl(imagePath) {
        if (!imagePath) return null;
        const clean = String(imagePath).trim();
        if (!clean) return null;
        if (clean.startsWith('http://') || clean.startsWith('https://') || clean.startsWith('data:')) {
            return clean;
        }
        if (clean.startsWith('/uploads/')) {
            return clean;
        }
        if (clean.startsWith('uploads/')) {
            return '/' + clean;
        }
        return '/uploads/' + clean;
    },

    createItemCardHtml(item) {
        const idStr = String(item.id).padStart(2, '0');
        const imgUrl = this.getItemImageUrl(item.imagePath);

        return `
            <div class="item-card" onclick="App.viewItemDetails(${item.id})">
                <div class="item-card-image-wrap">
                    ${imgUrl ? `
                        <img src="${imgUrl}" alt="${this.escapeHtml(item.itemName)}" class="item-card-img" loading="lazy" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="item-card-placeholder-box" style="display: none;">
                            <i data-lucide="${item.reportType === 'LOST' ? 'search' : 'gift'}" style="width: 38px; height: 38px; opacity: 0.35;"></i>
                        </div>
                    ` : `
                        <div class="item-card-placeholder-box">
                            <i data-lucide="${item.reportType === 'LOST' ? 'search' : 'gift'}" style="width: 38px; height: 38px; opacity: 0.35;"></i>
                        </div>
                    `}
                    <div class="item-card-floating-badges">
                        <span class="badge ${item.reportType === 'LOST' ? 'badge-lost' : 'badge-found'}">${item.reportType}</span>
                        <span class="badge badge-${item.status.toLowerCase()}">${item.status}</span>
                    </div>
                </div>

                <div class="item-card-body">
                    <div class="item-category-label">${this.escapeHtml(item.categoryName || 'General')}</div>
                    <h3 class="item-card-title">${this.escapeHtml(item.itemName)}</h3>
                    <p class="item-card-desc">${this.escapeHtml(item.description)}</p>

                    <div class="item-card-meta">
                        <div class="meta-row"><i data-lucide="map-pin"></i> <span>${this.escapeHtml(item.location)}</span></div>
                        <div class="meta-row"><i data-lucide="calendar"></i> <span>${item.eventDate}</span></div>
                    </div>

                    <div class="item-card-footer">
                        <span class="item-ref-badge">#${idStr}</span>
                        <span class="item-card-action">View Details &rarr;</span>
                    </div>
                </div>
            </div>
        `;
    },

    initMouseDepthController() {
        // Disabled for calm, stable, uncluttered UI performance
    },

    initScrollReveal() {
        if (!('IntersectionObserver' in window)) {
            document.querySelectorAll('.reveal-on-scroll').forEach(el => el.classList.add('revealed'));
            return;
        }

        const observer = new IntersectionObserver((entries, obs) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('revealed');
                    obs.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -30px 0px'
        });

        document.querySelectorAll('.reveal-on-scroll').forEach(el => observer.observe(el));
    },

    // ============================================================
    // DIRECT IN-APP CLAIM CHAT
    // ============================================================
    activeChatClaimId: null,
    chatPollTimer: null,
    activeChatClaim: null,

    async openClaimChat(claimId) {
        if (!API.isAuthenticated()) {
            this.showToast('Please log in to participate in claim chat', 'info');
            this.openLoginModal();
            return;
        }

        this.activeChatClaimId = claimId;
        const modal = document.getElementById('claimChatModal');
        const stream = document.getElementById('chatMessagesStream');
        const badge = document.getElementById('chatClaimStatusBadge');
        const subtitle = document.getElementById('chatSubtitle');
        const input = document.getElementById('chatMessageInput');

        if (!modal || !stream) return;

        // Reset view
        stream.innerHTML = '<div style="text-align: center; padding: 2.5rem; color: var(--ink-500);"><p>Connecting to secure dispatch channel...</p></div>';
        if (input) input.value = '';

        this.openModal('claimChatModal');

        try {
            const claim = await API.getClaimById(claimId);
            this.activeChatClaim = claim;
            const currentUser = API.getUser();
            const found = claim.foundItem || {};
            const isClaimant = claim.claimantId === currentUser.id;
            const counterpartName = isClaimant ? (found.userName || 'Finder') : (claim.claimantName || 'Claimant');
            const counterpartRole = isClaimant ? 'Finder' : 'Claimant';

            if (badge) {
                badge.textContent = `CLAIM #${claim.id} · ${claim.status}`;
                badge.className = `badge ${claim.status === 'APPROVED' ? 'badge-returned' : claim.status === 'COMPLETED' ? 'badge-active' : 'badge-pending'}`;
            }

            if (subtitle) {
                subtitle.innerHTML = `Chatting with: <strong style="color: var(--ink-900);">${this.escapeHtml(counterpartName)}</strong> (${counterpartRole}) &bull; Case: <em>"${this.escapeHtml(found.itemName || 'Item')}"</em>`;
            }

            await this.loadChatMessages(claimId, true);
            this.startChatPolling(claimId);
        } catch (err) {
            stream.innerHTML = `<div class="chat-empty-state"><p class="empty-desc" style="color: var(--stamp-500);">Could not load chat: ${this.escapeHtml(err.message)}</p></div>`;
            this.showToast('Chat access error: ' + err.message, 'error');
        }
    },

    closeClaimChat() {
        this.stopChatPolling();
        this.activeChatClaimId = null;
        this.activeChatClaim = null;
        this.closeModal('claimChatModal');
    },

    startChatPolling(claimId) {
        this.stopChatPolling();
        this.chatPollTimer = setInterval(() => {
            if (this.activeChatClaimId === claimId) {
                this.loadChatMessages(claimId, false);
            } else {
                this.stopChatPolling();
            }
        }, 3000); // 3 second live polling
    },

    stopChatPolling() {
        if (this.chatPollTimer) {
            clearInterval(this.chatPollTimer);
            this.chatPollTimer = null;
        }
    },

    async loadChatMessages(claimId, shouldScrollToBottom = false) {
        try {
            const messages = await API.getClaimMessages(claimId);
            const currentUser = API.getUser();
            const stream = document.getElementById('chatMessagesStream');
            if (!stream) return;

            const isScrolledToBottom = stream.scrollHeight - stream.clientHeight <= stream.scrollTop + 50;

            if (messages.length === 0) {
                stream.innerHTML = `
                    <div class="chat-empty-state">
                        <div class="chat-empty-icon"><i data-lucide="message-square-dashed"></i></div>
                        <h4 style="font-family: var(--font-display); font-size: 16px; font-weight: 700; color: var(--ink-900); margin-bottom: 4px;">No messages yet</h4>
                        <p style="font-size: 13px; color: var(--ink-500); max-width: 320px;">Use the input box below or choose a quick prompt to begin coordinating recovery.</p>
                    </div>
                `;
            } else {
                stream.innerHTML = messages.map(m => {
                    const isMine = currentUser && m.senderId === currentUser.id;
                    const timeStr = m.createdAt ? new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
                    const dateStr = m.createdAt ? new Date(m.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric' }) : '';

                    return `
                        <div class="chat-msg-row ${isMine ? 'mine' : 'theirs'}">
                            <div class="chat-bubble-meta">
                                <span class="chat-bubble-sender">${isMine ? 'You' : this.escapeHtml(m.senderName)}</span>
                                <span class="chat-bubble-role-badge">${this.escapeHtml(m.senderRole)}</span>
                                <span class="chat-bubble-time">${dateStr} ${timeStr}</span>
                            </div>
                            <div class="chat-bubble">
                                <div class="chat-bubble-text">${this.escapeHtml(m.content)}</div>
                            </div>
                        </div>
                    `;
                }).join('');
            }

            if (window.lucide) window.lucide.createIcons();

            if (shouldScrollToBottom || isScrolledToBottom) {
                stream.scrollTop = stream.scrollHeight;
            }
        } catch (err) {
            console.error('Error fetching chat messages:', err);
        }
    },

    async handleSendChatMessage(e) {
        e.preventDefault();
        const input = document.getElementById('chatMessageInput');
        if (!input) return;
        const text = input.value.trim();
        if (!text) return;

        const claimId = this.activeChatClaimId;
        if (!claimId) return;

        input.value = '';
        input.focus();

        try {
            await API.sendClaimMessage(claimId, text);
            await this.loadChatMessages(claimId, true);
        } catch (err) {
            this.showToast('Could not send message: ' + err.message, 'error');
            input.value = text;
        }
    },

    applyQuickPrompt(promptText) {
        const input = document.getElementById('chatMessageInput');
        if (input) {
            input.value = promptText;
            input.focus();
        }
    },

    escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
};

window.App = App;

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

