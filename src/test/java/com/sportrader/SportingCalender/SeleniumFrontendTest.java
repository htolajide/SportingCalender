package com.sportrader.SportingCalender;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumFrontendTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8081";

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void navigateToHome() {
        driver.get(BASE_URL);
    }

    @Test
    @Order(1)
    @DisplayName("Should load home page with all required elements")
    void testPageLoad() {
        assertTrue(driver.getTitle().contains("Sportradar") || driver.getTitle().contains("Calendar"));
        
        WebElement navbar = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.className("navbar")));
        assertTrue(navbar.isDisplayed());
        
        WebElement heroSection = driver.findElement(By.className("bg-light"));
        assertTrue(heroSection.isDisplayed());
        
        WebElement eventCount = driver.findElement(By.id("eventCount"));
        assertTrue(eventCount.isDisplayed());
        
        WebElement filtersSection = driver.findElement(By.id("filterForm"));
        assertTrue(filtersSection.isDisplayed());
        
        WebElement eventsList = driver.findElement(By.id("eventsList"));
        assertTrue(eventsList.isDisplayed());
        
        WebElement addEventSection = driver.findElement(By.id("addEventSection"));
        assertTrue(addEventSection.isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("Should display events from database")
    void testEventsDisplay() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".event-card")));
        
        List<WebElement> eventCards = driver.findElements(By.cssSelector(".event-card"));
        assertTrue(eventCards.size() > 0, "Should have at least one event displayed");
        
        for (WebElement card : eventCards) {
            List<WebElement> teamBadges = card.findElements(By.className("team-badge"));
            assertTrue(teamBadges.size() >= 2, "Event should have home and away teams");
            
            assertTrue(card.findElements(By.className("competition-badge")).size() > 0);
            assertTrue(card.findElements(By.className("date-badge")).size() > 0);
            assertTrue(card.findElements(By.className("status-badge")).size() > 0);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Should filter events by status")
    void testFilterByStatus() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".event-card")));
        
        int initialCount = driver.findElements(By.cssSelector(".event-card")).size();
        
        WebElement statusFilter = driver.findElement(By.id("statusFilter"));
        Select select = new Select(statusFilter);
        select.selectByValue("scheduled");
        
        WebElement applyBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Apply')]")
            )
        );
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".event-card")));
        
        List<WebElement> filteredEvents = driver.findElements(By.cssSelector(".event-card"));
        assertTrue(filteredEvents.size() <= initialCount);
        
        select.selectByValue("");
        applyBtn.click();
    }

    @Test
    @Order(4)
    @DisplayName("Should search events by team name")
    void testSearchEvents() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput")));
        
        WebElement searchInput = driver.findElement(By.id("searchInput"));
        searchInput.clear();
        searchInput.sendKeys("Al");
        searchInput.sendKeys(Keys.ENTER);
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        
        List<WebElement> searchResults = driver.findElements(By.cssSelector(".event-card"));
        
        for (WebElement card : searchResults) {
            String cardText = card.getText().toLowerCase();
            assertTrue(cardText.contains("al"), "Search result should contain 'Al'");
        }
        
        searchInput.clear();
        searchInput.sendKeys(Keys.ENTER);
    }

    @Test
    @Order(5)
    @DisplayName("Should allow filling add event form")
    void testAddEventFormFields() {
        WebElement addEventSection = driver.findElement(By.id("addEventSection"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView(true);", addEventSection);
        
        WebElement form = driver.findElement(By.id("addEventForm"));
        assertTrue(form.isDisplayed());
        
        WebElement dateInput = driver.findElement(By.id("newEventDate"));
        assertTrue(dateInput.isDisplayed());

        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].value = '2024-12-15';",
            dateInput
        );

        assertEquals("2024-12-15", dateInput.getAttribute("value"));
        
        WebElement timeInput = driver.findElement(By.id("newEventTime"));
        assertTrue(timeInput.isDisplayed());
        timeInput.sendKeys("18:00");
        
        WebElement homeTeamSelect = driver.findElement(By.id("newHomeTeamSelect"));
        assertTrue(homeTeamSelect.isDisplayed());
        
        WebElement awayTeamSelect = driver.findElement(By.id("newAwayTeamSelect"));
        assertTrue(awayTeamSelect.isDisplayed());
        
        WebElement competitionInput = driver.findElement(By.id("newCompetition"));
        assertTrue(competitionInput.isDisplayed());
        
        WebElement stageSelect = driver.findElement(By.id("newStage"));
        assertTrue(stageSelect.isDisplayed());
        
        WebElement statusSelect = driver.findElement(By.id("newStatus"));
        assertTrue(statusSelect.isDisplayed());
        
        WebElement stadiumInput = driver.findElement(By.id("newStadium"));
        assertTrue(stadiumInput.isDisplayed());
    }

    @Test
    @Order(6)
    @DisplayName("Should sort events by different criteria")
    void testSortEvents() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("sortBy")));
        
        List<WebElement> initialEvents = driver.findElements(By.cssSelector(".event-card"));
        int initialCount = initialEvents.size();
        
        WebElement sortBySelect = driver.findElement(By.id("sortBy"));
        Select select = new Select(sortBySelect);
        select.selectByValue("competition");
        
        WebElement applyBtn = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(.,'Apply')]")
            )
        );
        
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".event-card")));
        
        List<WebElement> sortedEvents = driver.findElements(By.cssSelector(".event-card"));
        assertEquals(initialCount, sortedEvents.size(), "Event count should remain same after sorting");
        
        select.selectByValue("date");
        applyBtn.click();
    }

    @Test
    @Order(7)
    @DisplayName("Should load event to form when card is clicked")
    void testClickEventCard() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".event-card")));
        
        List<WebElement> eventCards = driver.findElements(By.cssSelector(".event-card"));
        if (eventCards.size() > 0) {
            WebElement firstCard = eventCards.get(0);
            firstCard.click();
            
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("submitBtnText"), "Update Event"));
            
            WebElement submitBtnText = driver.findElement(By.id("submitBtnText"));
            assertEquals("Update Event", submitBtnText.getText());
            
            WebElement cancelBtn = driver.findElement(By.id("cancelEditBtn"));
            if (cancelBtn.isDisplayed()) {
                cancelBtn.click();
            }
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should be responsive on different screen sizes")
    void testResponsiveDesign() {
        driver.manage().window().setSize(new Dimension(375, 667));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        
        WebElement navbar = driver.findElement(By.className("navbar"));
        assertTrue(navbar.isDisplayed());
        
        List<WebElement> eventCards = driver.findElements(By.cssSelector(".event-card"));
        assertTrue(eventCards.size() > 0);
        
        driver.manage().window().setSize(new Dimension(1920, 1080));
    }

    @Test
    @Order(9)
    @DisplayName("Should have working navigation links")
    void testNavigationLinks() {
        List<WebElement> navLinks = driver.findElements(By.cssSelector(".nav-link"));
        assertTrue(navLinks.size() >= 2, "Should have at least 2 navigation links");
        
        for (WebElement link : navLinks) {
            String href = link.getAttribute("href");
            String onclick = link.getAttribute("onclick");
            assertTrue(href != null || onclick != null, "Nav link should have href or onclick");
        }
    }

    @Test
    @Order(10)
    @DisplayName("Should validate required form fields")
    void testFormValidation() {
        WebElement addEventSection = driver.findElement(By.id("addEventSection"));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView(true);", addEventSection);
        
        WebElement submitBtn = driver.findElement(By.id("submitBtn"));
        
        WebElement dateInput = driver.findElement(By.id("newEventDate"));
        String required = dateInput.getAttribute("required");
        assertTrue(required != null, "Date field should be required");
        
        WebElement homeTeamSelect = driver.findElement(By.id("newHomeTeamSelect"));
        required = homeTeamSelect.getAttribute("required");
        assertTrue(required != null, "Home team should be required");
    }
}