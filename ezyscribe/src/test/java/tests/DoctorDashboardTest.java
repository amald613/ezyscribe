package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import pages.DoctorDashboardPage;
import pages.LoginPage;

import java.time.Duration;
import java.util.*;

public class DoctorDashboardTest {

    private WebDriver driver;
    private DoctorDashboardPage doctorDashboardPage;
    private WebDriverWait wait;
    private static final Logger log = LogManager.getLogger(DoctorDashboardTest.class);

    @BeforeClass
    public void setup() {
        log.info("Setting up Brave browser with mic permissions and logging in as Doctor");

        WebDriverManager.chromedriver().setup();

        // Step 1: Define ChromeOptions for Brave
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\Amaldev\\AppData\\Local\\BraveSoftware\\Brave-Browser\\Application\\brave.exe");

        // Step 2: Set permissions (auto-allow mic)
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.media_stream_mic", 1); // Allow microphone
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Step 3: Add browser-level arguments
        options.addArguments("--use-fake-ui-for-media-stream"); // Prevent mic popup
        options.addArguments("--disable-popup-blocking");
        options.addArguments("user-data-dir=C:\\Temp\\BraveProfile"); // Optional: clean profile

        // Step 4: Start WebDriver
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Step 5: Login
        driver.get("https://appv2.ezyscribe.com");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.enterEmail("testprovider@gmail.com");
        loginPage.enterPassword("12345678");
        loginPage.clickSubmit();

        doctorDashboardPage = new DoctorDashboardPage(driver);
        log.info("Login successful and browser configured for mic access");
    }


    @AfterMethod
    public void refreshPageAfterEachTest() {
        log.info("Refreshing page after test");
        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[.//span[text()='Language']]")));
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            log.info("Closing browser");
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void verifyLanguageToggle() {
        log.info("Executing TC07 - Language Toggle");
        By languageToggleBy = By.xpath("//button[.//span[text()='Language']]");
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(languageToggleBy));
        toggle.click();
        log.info("Language toggle dropdown clicked");
    }

    @Test(priority = 2)
    public void verifyDarkLightSystemModeSwitch() throws InterruptedException {
        log.info("Executing TC08 - Dark to Light Theme Toggle");
        doctorDashboardPage.selectDarkMode();
        log.info("Dark mode selected");
        driver.navigate().refresh();
        doctorDashboardPage.selectLightMode();
        log.info("Light mode selected");
        Thread.sleep(1000);
    }
    @Test(priority = 3)
    public void verifyFilterByTaskId() {
        log.info("Executing TC09 - Filter by Task ID (Dynamic)");

        // Step 1: Get the first visible task ID from the page
        String taskId = doctorDashboardPage.getFirstTaskId();
        log.info("Captured first Task ID from list: " + taskId);

        // Step 2: Enter it into the Task ID filter
        doctorDashboardPage.enterTaskIdFilter(taskId);
        log.info("Filtered with Task ID: " + taskId);

        // Step 3: Verify it is shown in the filtered results
        Assert.assertTrue(doctorDashboardPage.verifyTaskIdPresent(taskId), "Filtered task not present");
    }


    @Test(priority = 4)
    public void verifySortByTaskIdActuallySorts() {
        log.info("Executing TC10 - Sort by Task ID");
        doctorDashboardPage.sortByTaskId();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(1)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Task ID column is not sorted in ascending order.");
    }

    @Test(priority = 5)
    public void verifySortByPatientInitialsActuallySorts() {
        log.info("Executing TC11 - Sort by Patient Initials");
        doctorDashboardPage.sortByPatientInitials();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(2)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Patient Initials column is not sorted.");
    }

    @Test(priority = 6)
    public void verifySortByStatusActuallySorts() {
        log.info("Executing TC12 - Sort by Status");
        doctorDashboardPage.sortByStatus();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(3)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Status column is not sorted.");
    }

    @Test(priority = 7)
    public void verifySortByPriorityActuallySorts() {
        log.info("Executing TC13 - Sort by Priority");
        doctorDashboardPage.sortByPriority();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(4)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Priority column is not sorted.");
    }

    @Test(priority = 8)
    public void verifySortByUploadDateActuallySorts() {
        log.info("Executing TC14 - Sort by Upload Date");
        doctorDashboardPage.sortByUploadDate();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(5)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Upload Date column is not sorted.");
    }

    @Test(priority = 9)
    public void verifySortByUploadTimeActuallySorts() {
        log.info("Executing TC15 - Sort by Upload Time");
        doctorDashboardPage.sortByUploadTime();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(6)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Upload Time column is not sorted.");
    }

    @Test(priority = 10)
    public void verifySortByAudioDurationActuallySorts() {
        log.info("Executing TC16 - Sort by Audio Duration");
        doctorDashboardPage.sortByAudioDuration();
        List<String> after = doctorDashboardPage.getColumnValues(By.cssSelector("tbody tr td:nth-child(7)"));
        List<String> sorted = new ArrayList<>(after);
        Collections.sort(sorted);
        Assert.assertEquals(after, sorted, "Audio Duration column is not sorted.");
    }

    @Test(priority = 11)
    public void verifyFilterByStatusInProgress() {
        log.info("Executing TC17 - Filter by Status = In Progress");
        doctorDashboardPage.filterStatusInProgress();
        boolean allInProgress = doctorDashboardPage.allStatusesAreInProgress();
        Assert.assertTrue(allInProgress, "Not all rows are 'Inprogress'");
    }

    @Test(priority = 12)
    public void verifyFilterByPriorityMedium() {
        log.info("Executing TC18 - Filter by Priority = Medium");
        doctorDashboardPage.filterPriorityMedium();
        boolean allMedium = doctorDashboardPage.allPrioritiesAreMedium();
        Assert.assertTrue(allMedium, "Not all rows have priority 'Medium'");
    }

    @Test(priority = 13)
    public void verifyViewTaskHidesRow() {
        log.info("Executing TC19 - View Task & Hide");
        String taskId = doctorDashboardPage.getFirstTaskId();
        doctorDashboardPage.clickViewButton();
        doctorDashboardPage.clickFirstTaskInView();
        boolean stillPresent = doctorDashboardPage.isTaskIdPresentInList(taskId);
        Assert.assertFalse(stillPresent, "Task row with ID " + taskId + " still visible after view");
    }
    @Test(priority = 14)
    public void verifyRecordingWorkflow_ReviewAfterRecordAgain() throws Exception {
        log.info("Executing TC15 - Record → Pause → Review → Record Again → Pause → Review → Upload");

        // Step 1: Start Recording
        log.info("Step 1: Start recording...");
        doctorDashboardPage.startRecording();
        Thread.sleep(2000); // wait to simulate actual audio

        // Step 2: Click Pause (Pause becomes Review)
        log.info("Step 2: Click Pause (should switch to Review)");
        doctorDashboardPage.pauseRecording();
        Assert.assertTrue(doctorDashboardPage.isReviewButtonVisible(),
            "Review button should appear after pausing.");

        // Step 3: Open Review popup
        log.info("Step 3: Open review popup");
        doctorDashboardPage.openReviewPopup();
        Assert.assertTrue(doctorDashboardPage.isReviewPopupOpen(), "Review popup should be open");

        // Step 4: Click Record Again
        log.info("Step 4: Click 'Record Again'");
        doctorDashboardPage.recordAgain();
        
     
       

        // Step 5: Click Pause (Pause becomes Review)
        log.info("Step 2: Click Pause (should switch to Review)");
        doctorDashboardPage.pauseRecording();
        Assert.assertTrue(doctorDashboardPage.isReviewButtonVisible(),
            "Review button should appear after pausing.");

        // Step 6: Open Review popup
        log.info("Step 3: Open review popup");
        doctorDashboardPage.openReviewPopup();
        Assert.assertTrue(doctorDashboardPage.isReviewPopupOpen(), "Review popup should be open");
        
        // Step 7: Upload recordings
        log.info("Step 7: Upload the recording");
        doctorDashboardPage.uploadRecordings();

        // Step 8: Verify task created
        Assert.assertTrue(doctorDashboardPage.verifyTaskCreated(), "Task should be created after uploading");
    }


}

