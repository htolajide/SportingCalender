const API_BASE = 'http://localhost:8081/api/events';

// ===== Global State =====
let events = [];
let availableTeams = [];
let editingEventId = null;
let editingEventType = 'create'; // 'create' | 'update' | 'result'

// ===== Utility Functions =====

function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'block' : 'none';
    document.getElementById('eventsList').style.display = show ? 'none' : 'flex';
}

function showError(message) {
    const errorDiv = document.getElementById('errorMessage');
    document.getElementById('errorText').textContent = message;
    errorDiv.classList.remove('d-none');
    setTimeout(() => errorDiv.classList.add('d-none'), 10000);
}

function showFormMessage(message, type = 'success') {
    const container = document.getElementById('formMessage');
    container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show mb-0 py-2 small" role="alert">
            ${message}
            <button type="button" class="btn-close btn-close-sm" onclick="this.parentElement.parentElement.innerHTML=''"></button>
        </div>
    `;
    setTimeout(() => {
        const alert = container.querySelector('.alert');
        if (alert) {
            alert.classList.remove('show');
            setTimeout(() => container.innerHTML = '', 300);
        }
    }, 6000);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('en-US', { 
        weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' 
    });
}

function formatTime(timeString) {
    return timeString?.substring(0, 5) || 'TBD';
}

function formatTimeForApi(timeValue) {
    if (!timeValue) return '00:00:00';
    const parts = timeValue.split(':');
    return parts.length === 3 ? timeValue : timeValue + ':00';
}

function determineWinner(homeGoals, awayGoals) {
    if (homeGoals > awayGoals) return 'home';
    if (awayGoals > homeGoals) return 'away';
    return 'draw';
}

function updateEventCount(count) {
    document.getElementById('eventCountNum').textContent = count;
}

function scrollToSection(sectionId) {
    document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
}

// ===== Submit Button Management =====
function updateSubmitButton() {
    const btnText = document.getElementById('submitBtnText');
    if (!btnText) return;
    
    if (editingEventType === 'result') {
        btnText.textContent = 'Update Result';
    } else if (editingEventType === 'update') {
        btnText.textContent = 'Update Event';
    } else {
        btnText.textContent = 'Create';
    }
}

// ===== Reset Form =====
function resetFormToCreate() {
    editingEventId = null;
    editingEventType = 'create';
    
    document.getElementById('editingEventId').value = '';
    document.getElementById('editingEventType').value = 'create';
    
    document.getElementById('addEventForm').reset();
    document.getElementById('resultSection').classList.add('d-none');
    document.getElementById('formMessage').innerHTML = '';
    
    // Re-enable all fields
    ['newEventDate', 'newEventTime', 'newCompetition', 'newStage', 
     'newStatus', 'newStadium', 'newHomeTeamSelect', 'newAwayTeamSelect']
        .forEach(id => {
            const el = document.getElementById(id);
            if (el) el.disabled = false;
        });
    
    updateSubmitButton();
}

// ===== Data Loading =====

async function loadEvents(filters = {}) {
    showLoading(true);
    document.getElementById('noEvents').classList.add('d-none');
    
    try {
        let url = `${API_BASE}/filter?sortBy=${filters.sortBy || 'date'}`;
        if (filters.date) url += `&date=${filters.date}`;
        if (filters.competition) url += `&competition=${encodeURIComponent(filters.competition)}`;
        if (filters.status) url += `&status=${filters.status}`;
        
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch events');
        
        events = await response.json();
        displayEvents(events);
        updateEventCount(events.length);
        return events;
    } catch (error) {
        console.error('Error loading events:', error);
        showError('Could not load events. Please check your connection.');
        return [];
    } finally {
        showLoading(false);
    }
}

async function loadFilterOptions() {
    try {
        const [compRes, statusRes] = await Promise.all([
            fetch(`${API_BASE}/competitions`),
            fetch(`${API_BASE}/statuses`)
        ]);
        
        const competitions = await compRes.json();
        const select = document.getElementById('competitionFilter');
        select.innerHTML = '<option value="">All Competitions</option>';
        
        competitions.forEach(comp => {
            const opt = document.createElement('option');
            opt.value = comp;
            opt.textContent = comp;
            select.appendChild(opt);
        });
    } catch (error) {
        console.error('Error loading filter options:', error);
    }
}

async function loadTeamsForDropdown() {
    try {
        const response = await fetch(`${API_BASE}/teams`);
        availableTeams = await response.json();
        populateTeamDropdowns();
    } catch (error) {
        console.error('Error loading teams:', error);
    }
}

function populateTeamDropdowns() {
    const homeSelect = document.getElementById('newHomeTeamSelect');
    const awaySelect = document.getElementById('newAwayTeamSelect');
    if (!homeSelect || !awaySelect) return;
    
    const defaultOpt = '<option value="">-- Select Team --</option>';
    const options = availableTeams.map(t => 
        `<option value="${t.id}" data-slug="${t.slug}" data-country="${t.countryCode || ''}">
            ${t.name}${t.countryCode ? ` (${t.countryCode})` : ''}
        </option>`
    ).join('');
    
    homeSelect.innerHTML = defaultOpt + options;
    awaySelect.innerHTML = defaultOpt + options;
    
    // Simple type-ahead filter
    [homeSelect, awaySelect].forEach(select => {
        select.addEventListener('input', function(e) {
            const term = e.target.value.toLowerCase();
            Array.from(select.querySelectorAll('option')).forEach(opt => {
                opt.style.display = opt.textContent.toLowerCase().includes(term) ? '' : 'none';
            });
        });
    });
}

// ===== Display Functions =====

function displayEvents(events) {
    const container = document.getElementById('eventsList');
    if (!events || events.length === 0) {
        container.innerHTML = '';
        document.getElementById('noEvents').classList.remove('d-none');
        return;
    }
    document.getElementById('noEvents').classList.add('d-none');
    container.innerHTML = events.map(event => createEventCard(event)).join('');
}

function createEventCard(event) {
    const isPlayed = event.status === 'played';
    const scoreDisplay = event.result && isPlayed ? 
        `<div class="score-badge cursor-pointer" onclick="event.stopPropagation(); loadResultToForm(${event.id})">
            <i class="bi bi-trophy-fill"></i> ${event.result.homeGoals} - ${event.result.awayGoals}
            <i class="bi bi-pencil-fill ms-2 small"></i>
        </div>` : 
        `<span class="text-muted small cursor-pointer" onclick="event.stopPropagation(); loadResultToForm(${event.id})">
            <i class="bi bi-clock"></i> ${formatTime(event.timeVenueUtc)} UTC
            <i class="bi bi-pencil ms-2 small"></i>
        </span>`;
    
    const homeTeam = event.homeTeam?.name || 'TBD';
    const awayTeam = event.awayTeam?.name || 'TBD';
    
    return `
        <div class="card event-card ${event.status} shadow-sm cursor-pointer" 
             onclick="loadEventToForm(${event.id})">
            <div class="card-body p-3">
                <div class="row align-items-center g-3">
                    <div class="col-lg-2">
                        <div class="d-flex flex-column gap-2">
                            <span class="date-badge justify-content-center">
                                <i class="bi bi-calendar3"></i> ${formatDate(event.dateVenue)}
                            </span>
                            <span class="competition-badge justify-content-center">
                                <i class="bi bi-trophy"></i> ${event.competition?.name || 'Unknown'}
                            </span>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <div class="d-flex align-items-center justify-content-center gap-3 flex-wrap">
                            <span class="team-badge flex-grow-1 text-center" style="min-width:150px">
                                <i class="bi bi-shield-fill"></i><span class="text-truncate">${homeTeam}</span>
                            </span>
                            <span class="text-primary fw-bold">VS</span>
                            <span class="team-badge flex-grow-1 text-center" style="min-width:150px">
                                <i class="bi bi-shield-fill"></i><span class="text-truncate">${awayTeam}</span>
                            </span>
                        </div>
                        <div class="text-center mt-2">${scoreDisplay}</div>
                    </div>
                    <div class="col-lg-4">
                        <div class="d-flex flex-column gap-2 align-items-lg-end">
                            <div class="d-flex gap-2 flex-wrap justify-content-lg-end">
                                <span class="status-badge status-${event.status} cursor-pointer" 
                                      onclick="event.stopPropagation(); loadResultToForm(${event.id})">
                                    ${event.status} <i class="bi bi-pencil ms-1"></i>
                                </span>
                                <span class="date-badge">
                                    <i class="bi bi-flag"></i> ${event.stage?.name || 'Unknown'}
                                </span>
                            </div>
                            ${event.stadiumName ? 
                                `<small class="text-muted"><i class="bi bi-geo-alt"></i> ${event.stadiumName}</small>` : 
                                '<small class="text-muted">Venue TBD</small>'}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// ===== Event Loading for Edit/Result =====

function loadEventToForm(eventId) {
    const event = events.find(e => e.id === eventId);
    if (!event) return;
    
    editingEventId = event.id;
    editingEventType = 'update';
    updateSubmitButton();
    
    scrollToSection('addEventSection');
    
    // Populate form
    document.getElementById('editingEventId').value = event.id;
    document.getElementById('editingEventType').value = 'update';
    document.getElementById('newEventDate').value = event.dateVenue;
    
    const timeParts = (event.timeVenueUtc || '00:00:00').split(':');
    document.getElementById('newEventTime').value = `${timeParts[0]}:${timeParts[1]}`;
    
    // Populate team selects
    const homeSelect = document.getElementById('newHomeTeamSelect');
    const awaySelect = document.getElementById('newAwayTeamSelect');
    
    if (homeSelect && event.homeTeam?.id) {
        homeSelect.value = event.homeTeam.id;
    }
    if (awaySelect && event.awayTeam?.id) {
        awaySelect.value = event.awayTeam.id;
    }
    
    document.getElementById('newCompetition').value = event.competition?.name || '';
    document.getElementById('newStage').value = event.stage?.name || 'GROUP STAGE';
    document.getElementById('newStatus').value = event.status || 'scheduled';
    document.getElementById('newStadium').value = event.stadiumName || '';
    
    // Handle result section
    const resultSection = document.getElementById('resultSection');
    if (event.status === 'played' || event.result) {
        resultSection.classList.remove('d-none');
        document.getElementById('resultHomeGoals').value = event.result?.homeGoals || 0;
        document.getElementById('resultAwayGoals').value = event.result?.awayGoals || 0;
        document.getElementById('resultWinner').value = event.result?.winner || determineWinner(
            event.result?.homeGoals || 0, 
            event.result?.awayGoals || 0
        );
    } else {
        resultSection.classList.add('d-none');
    }
    
    // Enable all fields for full edit
    ['newEventDate', 'newEventTime', 'newCompetition', 'newStage', 
     'newStatus', 'newStadium', 'newHomeTeamSelect', 'newAwayTeamSelect']
        .forEach(id => {
            const el = document.getElementById(id);
            if (el) el.disabled = false;
        });
    
    showFormMessage('📝 Editing event. Make changes and click Update.', 'info');
}

function loadResultToForm(eventId) {
    const event = events.find(e => e.id === eventId);
    if (!event) return;
    
    editingEventId = event.id;
    editingEventType = 'result';
    updateSubmitButton();
    
    scrollToSection('addEventSection');
    
    document.getElementById('editingEventId').value = event.id;
    document.getElementById('editingEventType').value = 'result';
    
    // Show team names (read-only)
    const homeSelect = document.getElementById('newHomeTeamSelect');
    const awaySelect = document.getElementById('newAwayTeamSelect');
    
    if (homeSelect && event.homeTeam?.id) homeSelect.value = event.homeTeam.id;
    if (awaySelect && event.awayTeam?.id) awaySelect.value = event.awayTeam.id;
    
    // Disable non-result fields
    ['newEventDate', 'newEventTime', 'newCompetition', 'newStage', 
     'newStatus', 'newStadium', 'newHomeTeamSelect', 'newAwayTeamSelect']
        .forEach(id => {
            const el = document.getElementById(id);
            if (el) el.disabled = true;
        });
    
    // Show and populate result section
    const resultSection = document.getElementById('resultSection');
    resultSection.classList.remove('d-none');
    
    document.getElementById('resultHomeGoals').value = event.result?.homeGoals || 0;
    document.getElementById('resultAwayGoals').value = event.result?.awayGoals || 0;
    document.getElementById('resultWinner').value = event.result?.winner || 
        determineWinner(event.result?.homeGoals || 0, event.result?.awayGoals || 0);
    
    showFormMessage('⚽ Update match result below', 'info');
}

// Auto-update winner when goals change
function setupResultWinnerAutoUpdate() {
    ['resultHomeGoals', 'resultAwayGoals'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('change', function() {
                const home = parseInt(document.getElementById('resultHomeGoals').value) || 0;
                const away = parseInt(document.getElementById('resultAwayGoals').value) || 0;
                document.getElementById('resultWinner').value = determineWinner(home, away);
            });
        }
    });
}

// ===== Filter & Search =====

function getFilterValues() {
    return {
        date: document.getElementById('dateFilter')?.value,
        competition: document.getElementById('competitionFilter')?.value,
        status: document.getElementById('statusFilter')?.value,
        sortBy: document.getElementById('sortBy')?.value || 'date'
    };
}

function applyFilters() {
    loadEvents(getFilterValues());
}

function resetFilters() {
    document.getElementById('filterForm')?.reset();
    const searchInput = document.getElementById('searchInput');
    if (searchInput) searchInput.value = '';
    loadEvents();
}

async function searchEvents() {
    const team = document.getElementById('searchInput')?.value.trim();
    if (!team) { loadEvents(); return; }
    
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/search?team=${encodeURIComponent(team)}`);
        const results = await response.json();
        displayEvents(results);
        updateEventCount(results.length);
    } catch (error) {
        showError('Search failed. Please try again.');
    } finally {
        showLoading(false);
    }
}

// ===== Form Submission =====

async function submitForm(e) {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const loadingBtn = document.getElementById('submitBtnLoading');
    
    submitBtn.classList.add('d-none');
    loadingBtn.classList.remove('d-none');
    
    try {
        const editingId = document.getElementById('editingEventId').value;
        const editingType = document.getElementById('editingEventType').value;
        
        let url = API_BASE;
        let method = 'POST';
        let payload = {};
        
        if (editingType === 'result' && editingId) {
            // Update result only
            url = `${API_BASE}/${editingId}/result`;
            method = 'PUT';
            payload = {
                homeGoals: parseInt(document.getElementById('resultHomeGoals').value) || 0,
                awayGoals: parseInt(document.getElementById('resultAwayGoals').value) || 0,
                winner: document.getElementById('resultWinner').value
            };
        } else if (editingId) {
            // Update full event
            url = `${API_BASE}/${editingId}`;
            method = 'PUT';
            
            const homeSelect = document.getElementById('newHomeTeamSelect');
            const awaySelect = document.getElementById('newAwayTeamSelect');
            
            payload = {
                dateVenue: document.getElementById('newEventDate').value,
                timeVenueUtc: formatTimeForApi(document.getElementById('newEventTime').value),
                status: document.getElementById('newStatus').value,
                season: 2024,
                stadiumName: document.getElementById('newStadium').value || null,
                homeTeam: {
                    id: parseInt(homeSelect?.value) || null,
                    name: homeSelect?.options[homeSelect?.selectedIndex]?.text.split('(')[0].trim() || '',
                    slug: homeSelect?.options[homeSelect?.selectedIndex]?.dataset.slug || '',
                    countryCode: homeSelect?.options[homeSelect?.selectedIndex]?.dataset.country || 'UNK'
                },
                awayTeam: {
                    id: parseInt(awaySelect?.value) || null,
                    name: awaySelect?.options[awaySelect?.selectedIndex]?.text.split('(')[0].trim() || '',
                    slug: awaySelect?.options[awaySelect?.selectedIndex]?.dataset.slug || '',
                    countryCode: awaySelect?.options[awaySelect?.selectedIndex]?.dataset.country || 'UNK'
                },
                competition: {
                    name: document.getElementById('newCompetition').value.trim(),
                    originId: 'user-added-' + Date.now()
                },
                stage: {
                    name: document.getElementById('newStage').value,
                    ordering: 1
                }
            };
        } else {
            // Create new event
            url = `${API_BASE}/create`;
            method = 'POST';
            
            const homeSelect = document.getElementById('newHomeTeamSelect');
            const awaySelect = document.getElementById('newAwayTeamSelect');
            
            payload = {
                dateVenue: document.getElementById('newEventDate').value,
                timeVenueUtc: formatTimeForApi(document.getElementById('newEventTime').value),
                status: document.getElementById('newStatus').value,
                season: 2024,
                stadiumName: document.getElementById('newStadium').value || null,
                homeTeam: {
                    id: parseInt(homeSelect?.value) || null,
                    name: homeSelect?.options[homeSelect?.selectedIndex]?.text.split('(')[0].trim() || '',
                    slug: homeSelect?.options[homeSelect?.selectedIndex]?.dataset.slug || '',
                    countryCode: homeSelect?.options[homeSelect?.selectedIndex]?.dataset.country || 'UNK'
                },
                awayTeam: {
                    id: parseInt(awaySelect?.value) || null,
                    name: awaySelect?.options[awaySelect?.selectedIndex]?.text.split('(')[0].trim() || '',
                    slug: awaySelect?.options[awaySelect?.selectedIndex]?.dataset.slug || '',
                    countryCode: awaySelect?.options[awaySelect?.selectedIndex]?.dataset.country || 'UNK'
                },
                competition: {
                    name: document.getElementById('newCompetition').value.trim(),
                    originId: 'user-added-' + Date.now()
                },
                stage: {
                    name: document.getElementById('newStage').value,
                    ordering: 1
                }
            };
        }
        
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            const action = editingType === 'result' ? 'Result updated' : 
                          editingId ? 'Event updated' : 'Event created';
            showFormMessage(`✅ ${action} successfully!`, 'success');
            resetFormToCreate();
            await loadEvents(getFilterValues());
            setTimeout(() => scrollToSection('eventsSection'), 1000);
        } else {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'Server error');
        }
    } catch (error) {
        console.error('Error:', error);
        showFormMessage('❌ Failed to save. Please try again.', 'danger');
    } finally {
        submitBtn.classList.remove('d-none');
        loadingBtn.classList.add('d-none');
    }
}

// ===== Initialize =====

document.addEventListener('DOMContentLoaded', function() {
    // Load data
    loadEvents();
    loadFilterOptions();
    loadTeamsForDropdown();
    updateTime();
    setupResultWinnerAutoUpdate();
    
    // Filter auto-apply
    ['dateFilter', 'competitionFilter', 'statusFilter', 'sortBy'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener('change', applyFilters);
    });
    
    // Search on Enter
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') searchEvents();
        });
    }
    
    // Form submit
    const form = document.getElementById('addEventForm');
    if (form) {
        form.addEventListener('submit', submitForm);
    }
    
    // Status change shows/hides result section
    const statusSelect = document.getElementById('newStatus');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            const resultSection = document.getElementById('resultSection');
            if (this.value === 'played') {
                resultSection.classList.remove('d-none');
            } else if (editingEventType !== 'result') {
                resultSection.classList.add('d-none');
            }
        });
    }
    
    // Cancel button
    const cancelBtn = document.getElementById('cancelEditBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', resetFormToCreate);
    }
});