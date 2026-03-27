const API_URL = 'http://localhost:8081/api/events';

async function loadEvents() {
    const dateFilter = document.getElementById('dateFilter');
    let url = API_URL;
    
    if (dateFilter && dateFilter.value) {
        url += `/date?date=${dateFilter.value}`; // Requires backend endpoint update or filter in JS
        // For simplicity in Milestone 4, we will filter in JS to save backend time
    }

    try {
        const response = await fetch(API_URL);
        const events = await response.json();
        const container = document.getElementById('eventsList');
        container.innerHTML = '';

        events.forEach(event => {
            const card = `
                <div class="col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">${event.competition.name}</h5>
                            <h6 class="card-subtitle mb-2 text-muted">${event.stage.name} • ${event.dateVenue}</h6>
                            <p class="card-text">
                                <span class="team-badge">${event.homeTeam.name}</span> 
                                vs 
                                <span class="team-badge">${event.awayTeam.name}</span>
                            </p>
                            <p class="card-text"><small class="text-muted">Status: ${event.status}</small></p>
                            ${event.result ? `<p class="card-text"><strong>Score:</strong> ${event.result.homeGoals} - ${event.result.awayGoals}</p>` : ''}
                        </div>
                    </div>
                </div>
            `;
            container.innerHTML += card;
        });
    } catch (error) {
        console.error('Error loading events:', error);
    }
}

document.getElementById('addEventForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageDiv = document.getElementById('message');
    
    const payload = {
        dateVenue: document.getElementById('dateVenue').value,
        timeVenueUtc: document.getElementById('timeVenueUtc').value,
        status: document.getElementById('status').value,
        season: 2024,
        stadiumName: "Stadium",
        homeTeam: {
            name: document.getElementById('homeTeamName').value,
            slug: document.getElementById('homeTeamName').value.toLowerCase().replace(/\s/g, '-'),
            countryCode: "UNK"
        },
        awayTeam: {
            name: document.getElementById('awayTeamName').value,
            slug: document.getElementById('awayTeamName').value.toLowerCase().replace(/\s/g, '-'),
            countryCode: "UNK"
        },
        competition: {
            name: document.getElementById('competitionName').value,
            originId: "custom-competition"
        },
        stage: {
            name: document.getElementById('stageName').value,
            ordering: 1
        }
    };

    try {
        const response = await fetch(API_URL + '/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            messageDiv.innerHTML = '<div class="alert alert-success">Event created successfully!</div>';
            document.getElementById('addEventForm').reset();
        } else {
            messageDiv.innerHTML = '<div class="alert alert-danger">Failed to create event.</div>';
        }
    } catch (error) {
        messageDiv.innerHTML = '<div class="alert alert-danger">Error connecting to server.</div>';
    }
});