# Sportradar Coding Academy - Backend Exercise

## Overview
A sports event calendar built with Java Spring Boot, MySQL, and Docker. It displays events, allows adding new events, and imports initial data from JSON.

## Features
- **Database**: MySQL running in Docker (No local installation required).
- **Backend**: Spring Boot 3, JPA, Hibernate.
- **Frontend**: HTML5, Bootstrap 5, Vanilla JS.
- **Data Import**: Auto-imports `DATA.json` on startup.
- **Efficiency**: Uses `@EntityGraph` to avoid N+1 query problems.
- **Normalization**: Database schema follows 3rd Normal Form (3NF).

## Setup & Run
# 1. Start the MySQL database container (Runs on port 3307)
docker-compose up -d

# 2. Wait 30 seconds for database initialization
# Verify container is running:
docker ps

# Install dependencies using maven dependency manager:
mvn clean install

# 4. Run the Spring Boot application (Runs on port 8081)
mvn spring-boot:run

# 5. Open browser to:
# http://localhost:8081/index.html
## 🖥 Frontend (Single Page Application)

### Single Page: `index.html`
- **URL:** `http://localhost:8081/index.html`
- **Layout:** Two-column responsive design
  - **Left (Main):** Event list with filters, sorting, and search
  - **Right (Sidebar):** Sticky "Add New Event" form
- **Features:**
  - ✅ Events load automatically on page load
  - ✅ Filter by Date, Competition, Status
  - ✅ Sort by Date, Competition, or Status
  - ✅ Search by team name (real-time)
  - ✅ Add new events without page reload (AJAX)
  - ✅ Success/error feedback messages
  - ✅ Smooth scrolling navigation
  - ✅ Responsive design (mobile-friendly)

### No Page Reloads
All interactions use JavaScript fetch API for a seamless SPA experience.

# 🧪 Testing
## Junit 5 and Mockito Backend Tests
- EventService and EventController were tested with Junit5 and Mockito
- Test libraries
### Run the test with 'mvn clean test' command
## Selenium Frontend Tests

- I've included comprehensive Selenium tests to verify frontend functionality:

# Run Selenium tests (requires app running on localhost:8081)
# Make sure your app is running first
mvn spring-boot:run

# In another terminal, run Selenium tests
mvn test -Dtest=SeleniumFrontendTest