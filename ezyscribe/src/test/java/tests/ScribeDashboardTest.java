package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import pages.ScribeDashboardPage;
import pages.LoginPage;

import java.time.Duration;
import java.util.*;

public class ScribeDashboardTest {

    private WebDriver driver;
    private ScribeDashboardPage scribeDashboardPage;
    private WebDriverWait wait;
    private static final Logger log = LogManager.getLogger(ScribeDashboardTest.class);

    @BeforeClass
    public void setup() {
        log.info("Setting up browser and logging in as Scribe");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\Amaldev\\AppData\\Local\\BraveSoftware\\Brave-Browser\\Application\\brave.exe");

        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("https://appv2.ezyscribe.com");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterEmail("testscribe@gmail.com");
        loginPage.enterPassword("12345678");
        loginPage.clickSubmit();

        scribeDashboardPage = new ScribeDashboardPage(driver);
        log.info("Login successful");
    }

    @AfterMethod
    public void refreshPageAfterEachTest() {
        log.info("Refreshing page after test");
        driver.navigate().refresh();
        // Wait for a common element that should be present after refresh
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Filter task by id']")));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            log.info("Closing browser");
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void verifyDarkLightSystemModeSwitch() throws InterruptedException {
        log.info("Executing TC01 - Dark to Light Theme Toggle");
        scribeDashboardPage.selectDarkMode();
        log.info("Dark mode selected");
        driver.navigate().refresh();
        scribeDashboardPage.selectLightMode();
        log.info("Light mode selected");
        Thread.sleep(1000);
    }

    @Test(priority = 2)
    public void verifyFilterByTaskId() {
        log.info("Executing TC09 - Filter by Task ID (Dynamic)");

        // Step 1: Get the first visible task ID from the page
        String taskId = scribeDashboardPage.getFirstTaskId();
        log.info("Captured first Task ID from list: " + taskId);

        // Step 2: Enter it into the Task ID filter
        scribeDashboardPage.enterTaskIdFilter(taskId);
        log.info("Filtered with Task ID: " + taskId);

        // Step 3: Verify it is shown in the filtered results
        Assert.assertTrue(scribeDashboardPage.verifyTaskIdPresent(taskId), "Filtered task not present");
    }


    @Test(priority = 3)
    public void verifySortByTaskIdActuallySorts() {
        log.info("Executing TC03 - Sort by Task ID");
        scribeDashboardPage.sortByTaskId();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(1)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Task ID column is not sorted in ascending order.");
    }

    @Test(priority = 4)
    public void verifySortByPatientInitialsActuallySorts() {
        log.info("Executing TC04 - Sort by Patient Initials");
        scribeDashboardPage.sortByPatientInitials();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(2)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Patient Initials column is not sorted.");
    }

    @Test(priority = 5)
    public void verifySortByStatusActuallySorts() {
        log.info("Executing TC05 - Sort by Status");
        scribeDashboardPage.sortByStatus();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(3)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Status column is not sorted.");
    }

    @Test(priority = 6)
    public void verifySortByPriorityActuallySorts() {
        log.info("Executing TC06 - Sort by Priority");
        scribeDashboardPage.sortByPriority();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(4)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Priority column is not sorted.");
    }

    @Test(priority = 7)
    public void verifySortByUploadDateActuallySorts() {
        log.info("Executing TC07 - Sort by Upload Date");
        scribeDashboardPage.sortByUploadDate();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(5)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Upload Date column is not sorted.");
    }

    @Test(priority = 8)
    public void verifySortByUploadTimeActuallySorts() {
        log.info("Executing TC08 - Sort by Upload Time");
        scribeDashboardPage.sortByUploadTime();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(6)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Upload Time column is not sorted.");
    }

    @Test(priority = 9)
    public void verifySortByAudioDurationActuallySorts() {
        log.info("Executing TC09 - Sort by Audio Duration");
        scribeDashboardPage.sortByAudioDuration();
        List<String> after = scribeDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(7)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Audio Duration column is not sorted.");
    }

    @Test(priority = 10)
    public void verifyFilterByStatusInProgress() {
        log.info("Executing TC10 - Filter by Status = In Progress");
        scribeDashboardPage.filterStatusInProgress();
        boolean allInProgress = scribeDashboardPage.allStatusesAreInProgress();
        Assert.assertTrue(allInProgress, "Not all rows are 'Inprogress'");
    }

    @Test(priority = 11)
    public void verifyFilterByPriorityMedium() {
        log.info("Executing TC11 - Filter by Priority = Medium");
        scribeDashboardPage.filterPriorityMedium();
        boolean allMedium = scribeDashboardPage.allPrioritiesAreMedium();
        Assert.assertTrue(allMedium, "Not all rows have priority 'Medium'");
    }

    @Test(priority = 12)
    public void verifyViewTaskHidesRow() {
        log.info("Executing TC12 - View Task & Hide");
        String taskId = scribeDashboardPage.getFirstTaskId();
        scribeDashboardPage.clickViewButton();
        scribeDashboardPage.clickFirstTaskInView();
        boolean stillPresent = scribeDashboardPage.isTaskIdPresentInList(taskId);
        Assert.assertFalse(stillPresent, "Task row with ID " + taskId + " still visible after view");
    }
}