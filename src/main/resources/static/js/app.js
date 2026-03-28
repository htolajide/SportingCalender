const API_BASE = 'http://localhost:8081/api/events';

// ===== Utility Functions =====

function updateTime() {
    const now = new Date();
    document.getElementById('currentTime').textContent = 
        now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

function showLoading(show) {
    document.getElementById('loadingSpinner').style.display = show ? 'block' : 'none';
    document.getElementById('eventsList').style.display = show ? 'none' : 'grid';
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
        if (container.querySelector('.alert')) {
            container.querySelector('.alert').classList.remove('show');
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

function updateEventCount(count) {
    document.getElementById('eventCountNum').textContent = count;
}

function scrollToSection(sectionId) {
    document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth' });
}

// ✅ NEW (handles both formats):
function formatTimeForApi(timeValue) {
    // HTML time input returns "HH:mm"
    // If already has seconds, return as-is; otherwise append ":00"
    if (!timeValue) return '00:00:00';
    if (timeValue.split(':').length === 3) return timeValue; // Already HH:mm:ss
    return timeValue + ':00'; // Convert HH:mm to HH:mm:ss
}

// ===== Data Loading =====

async function loadEvents(filters = {}) {
    showLoading(true);
    document.getElementById('noEvents').classList.add('d-none');
    
    try {
        let url = API_BASE + '/filter?sortBy=' + (filters.sortBy || 'date');
        if (filters.date) url += `&date=${filters.date}`;
        if (filters.competition) url += `&competition=${encodeURIComponent(filters.competition)}`;
        if (filters.status) url += `&status=${filters.status}`;
        
        const response = await fetch(url);
        if (!response.ok) throw new Error('Failed to fetch events');
        
        const events = await response.json();
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
        const [compResponse, statusResponse] = await Promise.all([
            fetch(API_BASE + '/competitions'),
            fetch(API_BASE + '/statuses')
        ]);
        
        const competitions = await compResponse.json();
        const select = document.getElementById('competitionFilter');
        
        // Clear existing options except "All"
        select.innerHTML = '<option value="">All Competitions</option>';
        competitions.forEach(comp => {
            const option = document.createElement('option');
            option.value = comp;
            option.textContent = comp;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading filter options:', error);
    }
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

// Update createEventCard to add click handlers
function createEventCard(event) {
    const isPlayed = event.status === 'played';
    const scoreDisplay = event.result && isPlayed ? 
        `<div class="score-badge cursor-pointer" onclick="loadResultToForm(events[${events.indexOf(event)}])">
            <i class="bi bi-trophy-fill"></i> ${event.result.homeGoals} - ${event.result.awayGoals}
            <i class="bi bi-pencil-fill ms-2 small"></i>
        </div>` : 
        `<span class="text-muted small cursor-pointer" onclick="loadResultToForm(events[${events.indexOf(event)}])">
            <i class="bi bi-clock"></i> ${formatTime(event.timeVenueUtc)} UTC
            <i class="bi bi-pencil ms-2 small"></i>
        </span>`;
    
    const homeTeam = event.homeTeam?.name || 'TBD';
    const awayTeam = event.awayTeam?.name || 'TBD';
    
    return `
        <div class="card event-card ${event.status} shadow-sm cursor-pointer" 
             onclick="loadEventToForm(events[${events.indexOf(event)}])" style="cursor: pointer;">
            <div class="card-body p-3">
                <div class="row align-items-center g-3">
                    
                    <!-- Left: Date & Competition -->
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
                    
                    <!-- Center: Teams (Full Width) -->
                    <div class="col-lg-6">
                        <div class="d-flex align-items-center justify-content-center gap-3 flex-wrap">
                            <span class="team-badge flex-grow-1 text-center" style="min-width: 150px;">
                                <i class="bi bi-shield-fill"></i>
                                <span class="text-truncate">${homeTeam}</span>
                            </span>
                            <span class="text-primary fw-bold">VS</span>
                            <span class="team-badge flex-grow-1 text-center" style="min-width: 150px;">
                                <i class="bi bi-shield-fill"></i>
                                <span class="text-truncate">${awayTeam}</span>
                            </span>
                        </div>
                        <div class="text-center mt-2">
                            ${scoreDisplay}
                        </div>
                    </div>
                    
                    <!-- Right: Stage, Status, Stadium -->
                    <div class="col-lg-4">
                        <div class="d-flex flex-column gap-2 align-items-lg-end">
                            <div class="d-flex gap-2 flex-wrap justify-content-lg-end">
                                <span class="status-badge status-${event.status} cursor-pointer" 
                                      onclick="event.stopPropagation(); loadResultToForm(events[${events.indexOf(event)}])">
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

// ===== Filter & Search =====

function getFilterValues() {
    return {
        date: document.getElementById('dateFilter').value,
        competition: document.getElementById('competitionFilter').value,
        status: document.getElementById('statusFilter').value,
        sortBy: document.getElementById('sortBy').value
    };
}

function applyFilters() {
    const filters = getFilterValues();
    loadEvents(filters);
}

function resetFilters() {
    document.getElementById('filterForm').reset();
    document.getElementById('searchInput').value = '';
    loadEvents();
}

async function searchEvents() {
    const team = document.getElementById('searchInput').value.trim();
    if (!team) {
        loadEvents();
        return;
    }
    
    showLoading(true);
    try {
        const response = await fetch(`${API_BASE}/search?team=${encodeURIComponent(team)}`);
        const events = await response.json();
        displayEvents(events);
        updateEventCount(events.length);
    } catch (error) {
        showError('Search failed. Please try again.');
    } finally {
        showLoading(false);
    }
}

// ===== Add Event Form =====

document.getElementById('addEventForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const loadingBtn = document.getElementById('submitBtnLoading');
    
    // Show loading state
    submitBtn.classList.add('d-none');
    loadingBtn.classList.remove('d-none');
    
    try {
        const payload = {
            dateVenue: document.getElementById('newEventDate').value,
            timeVenueUtc: formatTimeForApi(document.getElementById('newEventTime').value),
            status: document.getElementById('newStatus').value,
            season: 2024,
            stadiumName: document.getElementById('newStadium').value || null,
            homeTeam: {
                name: document.getElementById('newHomeTeam').value.trim(),
                slug: document.getElementById('newHomeTeam').value.toLowerCase().replace(/\s+/g, '-'),
                countryCode: 'UNK'
            },
            awayTeam: {
                name: document.getElementById('newAwayTeam').value.trim(),
                slug: document.getElementById('newAwayTeam').value.toLowerCase().replace(/\s+/g, '-'),
                countryCode: 'UNK'
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
        
        const response = await fetch(API_BASE + '/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            const newEvent = await response.json();
            showFormMessage('✅ Event created successfully! Refreshing list...', 'success');
            
            // Reset form
            document.getElementById('addEventForm').reset();
            
            // Refresh events list with current filters
            await loadEvents(getFilterValues());
            
            // Scroll to events section to show new event
            setTimeout(() => scrollToSection('eventsSection'), 1500);
        } else {
            throw new Error('Server returned error');
        }
    } catch (error) {
        console.error('Error creating event:', error);
        showFormMessage('❌ Failed to create event. Please try again.', 'danger');
    } finally {
        // Restore button state
        submitBtn.classList.remove('d-none');
        loadingBtn.classList.add('d-none');
    }
});

// ===== Initialize =====
document.addEventListener('DOMContentLoaded', function() {
    // Load initial data
    loadEvents();
    loadFilterOptions();
    updateTime();
    
    // Update time every minute
    setInterval(updateTime, 60000);
    
    // Auto-apply filters on change for better UX
    ['dateFilter', 'competitionFilter', 'statusFilter', 'sortBy'].forEach(id => {
        document.getElementById(id)?.addEventListener('change', applyFilters);
    });
    
    // Enter key support for search
    document.getElementById('searchInput')?.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchEvents();
    });
});

// ===== Add these new functions =====

// Track editing state
let editingEventId = null;
let editingEventType = 'create'; // 'create' or 'update' or 'result'

// Click event card to populate form
function loadEventToForm(event) {
    editingEventId = event.id;
    editingEventType = 'update';
    
    // Scroll to form
    document.getElementById('addEventSection').scrollIntoView({ behavior: 'smooth' });
    
    // Populate form fields
    // ✅ Fix: Extract only HH:mm for HTML time input
    const timeValue = event.timeVenueUtc;
    if (timeValue) {
        // If time has seconds (HH:mm:ss), extract just HH:mm
        const timeParts = timeValue.split(':');
        const timeForInput = timeParts.length >= 2 ? `${timeParts[0]}:${timeParts[1]}` : timeValue;
        document.getElementById('newEventTime').value = timeForInput;
    }
    document.getElementById('editingEventId').value = event.id;
    document.getElementById('editingEventType').value = 'update';
    document.getElementById('submitBtnText').textContent = 'Update Event';
    document.getElementById('newEventDate').value = event.dateVenue;
    document.getElementById('newHomeTeam').value = event.homeTeam?.name || '';
    document.getElementById('newAwayTeam').value = event.awayTeam?.name || '';
    document.getElementById('newCompetition').value = event.competition?.name || '';
    document.getElementById('newStage').value = event.stage?.name || 'GROUP STAGE';
    document.getElementById('newStatus').value = event.status || 'scheduled';
    document.getElementById('newStadium').value = event.stadiumName || '';
    
    // Show result section if event is played or has result
    const resultSection = document.getElementById('resultSection');
    if (event.status === 'played' || event.result) {
        resultSection.classList.remove('d-none');
        document.getElementById('resultHomeGoals').value = event.result?.homeGoals || 0;
        document.getElementById('resultAwayGoals').value = event.result?.awayGoals || 0;
        document.getElementById('resultWinner').value = event.result?.winner || 'draw';
    } else {
        resultSection.classList.add('d-none');
    }
    
    // Show message
    showFormMessage('📝 Editing event. Make changes and click Update.', 'info');
    
    // Change submit button
    document.getElementById('submitBtnText').textContent = 'Update Event';
}

// Click status badge to update result
function loadResultToForm(event) {
    editingEventId = event.id;
    editingEventType = 'result';
    
    // Scroll to form
    document.getElementById('addEventSection').scrollIntoView({ behavior: 'smooth' });
    
    // Populate basic info
    document.getElementById('editingEventId').value = event.id;
    document.getElementById('editingEventType').value = 'result';
    document.getElementById('submitBtnText').textContent = 'Update Result';
    document.getElementById('newHomeTeam').value = event.homeTeam?.name || '';
    document.getElementById('newAwayTeam').value = event.awayTeam?.name || '';
    document.getElementById('newHomeTeam').disabled = true;
    document.getElementById('newAwayTeam').disabled = true;
    
    // Show result section
    const resultSection = document.getElementById('resultSection');
    resultSection.classList.remove('d-none');
    document.getElementById('resultHomeGoals').value = event.result?.homeGoals || 0;
    document.getElementById('resultAwayGoals').value = event.result?.awayGoals || 0;
    
    // Determine winner
    const homeGoals = event.result?.homeGoals || 0;
    const awayGoals = event.result?.awayGoals || 0;
    if (homeGoals > awayGoals) {
        document.getElementById('resultWinner').value = 'home';
    } else if (awayGoals > homeGoals) {
        document.getElementById('resultWinner').value = 'away';
    } else {
        document.getElementById('resultWinner').value = 'draw';
    }
    
    // Disable non-result fields
    ['newEventDate', 'newEventTime', 'newCompetition', 'newStage', 'newStatus', 'newStadium'].forEach(id => {
        document.getElementById(id).disabled = true;
    });
    
    showFormMessage('⚽ Update match result below', 'info');
    document.getElementById('submitBtnText').textContent = 'Update Result';
}

// Reset form to create mode
function resetFormToCreate() {
    editingEventId = null;
    editingEventType = 'create';
    document.getElementById('editingEventId').value = '';
    document.getElementById('editingEventType').value = 'create';
    document.getElementById('submitBtnText').textContent = 'Create';
    document.getElementById('addEventForm').reset();
    document.getElementById('resultSection').classList.add('d-none');
    document.getElementById('formMessage').innerHTML = '';
    
    // Re-enable all fields
    ['newHomeTeam', 'newAwayTeam', 'newEventDate', 'newEventTime', 
     'newCompetition', 'newStage', 'newStatus', 'newStadium'].forEach(id => {
        document.getElementById(id).disabled = false;
    });
}

// Update form submit handler
document.getElementById('addEventForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const submitBtn = document.getElementById('submitBtn');
    const loadingBtn = document.getElementById('submitBtnLoading');
    
    // Show loading state
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
            payload = {
                dateVenue: document.getElementById('newEventDate').value,
                timeVenueUtc: formatTimeForApi(document.getElementById('newEventTime').value),
                status: document.getElementById('newStatus').value,
                season: 2024,
                stadiumName: document.getElementById('newStadium').value || null,
                homeTeam: {
                    name: document.getElementById('newHomeTeam').value.trim(),
                    slug: document.getElementById('newHomeTeam').value.toLowerCase().replace(/\s+/g, '-'),
                    countryCode: 'UNK'
                },
                awayTeam: {
                    name: document.getElementById('newAwayTeam').value.trim(),
                    slug: document.getElementById('newAwayTeam').value.toLowerCase().replace(/\s+/g, '-'),
                    countryCode: 'UNK'
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
            payload = {
                dateVenue: document.getElementById('newEventDate').value,
                timeVenueUtc: formatTimeForApi(document.getElementById('newEventTime').value),
                status: document.getElementById('newStatus').value,
                season: 2024,
                stadiumName: document.getElementById('newStadium').value || null,
                homeTeam: {
                    name: document.getElementById('newHomeTeam').value.trim(),
                    slug: document.getElementById('newHomeTeam').value.toLowerCase().replace(/\s+/g, '-'),
                    countryCode: 'UNK'
                },
                awayTeam: {
                    name: document.getElementById('newAwayTeam').value.trim(),
                    slug: document.getElementById('newAwayTeam').value.toLowerCase().replace(/\s+/g, '-'),
                    countryCode: 'UNK'
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
            
            // Reset form
            resetFormToCreate();
            
            // Refresh events list
            await loadEvents(getFilterValues());
            
            // Scroll to events section
            setTimeout(() => scrollToSection('eventsSection'), 1000);
        } else {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'Server error');
        }
    } catch (error) {
        console.error('Error:', error);
        showFormMessage('❌ Failed to save. Please try again.', 'danger');
    } finally {
        // Restore button state
        submitBtn.classList.remove('d-none');
        loadingBtn.classList.add('d-none');
    }
});

// Add cancel button for editing
function addCancelButton() {
    const formContainer = document.querySelector('#addEventForm .mb-3:last-of-type');
    if (formContainer && !document.getElementById('cancelBtn')) {
        const cancelBtn = document.createElement('button');
        cancelBtn.id = 'cancelBtn';
        cancelBtn.type = 'button';
        cancelBtn.className = 'btn btn-outline-secondary btn-sm w-100 mt-2';
        cancelBtn.textContent = 'Cancel Edit';
        cancelBtn.onclick = resetFormToCreate;
        formContainer.parentElement.appendChild(cancelBtn);
    }
}

// Show/hide result section based on status
document.getElementById('newStatus')?.addEventListener('change', (e) => {
    const resultSection = document.getElementById('resultSection');
    if (e.target.value === 'played') {
        resultSection.classList.remove('d-none');
    } else {
        resultSection.classList.add('d-none');
    }
});

// Store events globally for click handlers
let events = [];

// Update loadEvents to store events globally
async function loadEvents(filters = {}) {
    showLoading(true);
    document.getElementById('noEvents').classList.add('d-none');
    
    try {
        let url = API_BASE + '/filter?sortBy=' + (filters.sortBy || 'date');
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