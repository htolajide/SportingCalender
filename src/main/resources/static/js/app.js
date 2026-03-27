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
    setTimeout(() => errorDiv.classList.add('d-none'), 6000);
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
    }, 4000);
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

function createEventCard(event) {
    const isPlayed = event.status === 'played';
    const scoreDisplay = event.result && isPlayed ? 
        `<div class="score-badge"><i class="bi bi-trophy-fill"></i> ${event.result.homeGoals} - ${event.result.awayGoals}</div>` : 
        `<span class="text-muted small"><i class="bi bi-clock"></i> ${formatTime(event.timeVenueUtc)} UTC</span>`;
    
    const homeTeam = event.homeTeam?.name || 'TBD';
    const awayTeam = event.awayTeam?.name || 'TBD';
    
    return `
        <div class="card event-card ${event.status} shadow-sm">
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
                                <span class="status-badge status-${event.status}">${event.status}</span>
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
            timeVenueUtc: document.getElementById('newEventTime').value + ':00',
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