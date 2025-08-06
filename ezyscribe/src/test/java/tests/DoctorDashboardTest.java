package tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.*;

import pages.DoctorDashboardPage;

import java.util.HashMap;
import java.util.Map;

public class DoctorDashboardTest {

    private WebDriver driver;
    private DoctorDashboardPage doctorPage;
    private boolean isLoggedIn = false;
    private static final Logger log = LogManager.getLogger(DoctorDashboardTest.class);

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        options.addArguments("disable-infobars");
        options.addArguments("--use-fake-ui-for-media-stream"); // Auto allow mic

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.media_stream_mic", 1); // Allow mic
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        doctorPage = new DoctorDashboardPage(driver);
    }

    @BeforeMethod
    public void ensureLoggedInAndRefresh() {
        if (!isLoggedIn) {
            doctorPage.loginAsDoctor("testprovider@gmail.com", "12345678");
            isLoggedIn = true;
        } else {
            doctorPage.refreshDashboard();
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void themeSwitch() {
        doctorPage.waitForDashboardToLoad();

        doctorPage.selectDarkMode();
        Assert.assertTrue(doctorPage.isDarkModeActive(), "❌ Dark mode not activated!");

        doctorPage.selectLightMode();
        Assert.assertTrue(doctorPage.isLightModeActive(), "❌ Light mode not activated!");
    }

    @Test(priority = 2)
    public void searchByFirstTaskId() throws InterruptedException {
        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        doctorPage.waitForDashboardToLoad();

        String taskId = doctorPage.searchSecondTaskIdInTableAndSearch();

        new Actions(driver).sendKeys(Keys.ENTER).perform();
        Thread.sleep(1000);

        doctorPage.assertFirstRowHasTaskId(taskId);
        Thread.sleep(1000);
    }

    @Test(priority = 3)
    public void testStatusFilter() {
        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        log.info("🔍 Starting Status Filter Test...");

        doctorPage.waitForDashboardToLoad();
        log.info("✅ Dashboard loaded.");

        log.info("📌 Applying first status filter...");
        doctorPage.applyFirstStatusFilterAndVerify();

        log.info("🧹 Attempting to clear status filter...");
        doctorPage.clearStatusFilter();
        log.info("✔️ Status Filter Test completed.");
    }

    @Test(priority = 4)
    public void testPriorityFilter() {
        log.info("🔍 Starting Priority Filter Test...");
        doctorPage.clickResetFiltersIfPresent();

        doctorPage.waitForDashboardToLoad();
        log.info("✅ Dashboard loaded.");

        log.info("📌 Applying 'Medium' priority filter...");
        doctorPage.applyPriorityFilterAndVerify();

        log.info("🧹 Attempting to clear priority filter...");
        doctorPage.clearPriorityFilter();
        log.info("✔️ Priority Filter Test completed.");
    }

    @Test(priority = 5)
    public void testToggleTaskIdColumnView() {
        doctorPage.waitForDashboardToLoad();
        doctorPage.toggleTaskIdColumnVisibility();
        doctorPage.assertTaskIdColumnHidden();
    }

    @Test(priority = 6)
    public void testTaskIdAscendingSort() throws InterruptedException {
        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        doctorPage.waitForDashboardToLoad();
        doctorPage.applyAscendingSortOnTaskId();
        Thread.sleep(1000);
        doctorPage.verifyTaskIdsInAscendingOrder();
    }

    @Test(priority = 7)
    public void verifyRecordingWorkflow_ReviewAfterRecordAgain() throws Exception {
        log.info("🎙️ TC15 - Record → Pause → Review → Record Again → Pause → Review → Upload");

        log.info("[Step 1] Start recording...");
        doctorPage.startRecording();
        Thread.sleep(2000);

        log.info("[Step 2] Pause recording...");
        doctorPage.pauseRecording();
        Assert.assertTrue(doctorPage.isReviewButtonVisible(), "❌ Review button should appear after pause.");

        log.info("[Step 3] Open Review popup...");
        doctorPage.openReviewPopup();
        Assert.assertTrue(doctorPage.isReviewPopupOpen(), "❌ Review popup should be open.");

        log.info("[Step 4] Click 'Record Again'...");
        doctorPage.recordAgain();

        log.info("[Step 5] Pause second recording...");
        doctorPage.pauseRecording();
        Assert.assertTrue(doctorPage.isReviewButtonVisible(), "❌ Review button should appear after second pause.");

        log.info("[Step 6] Open Review popup again...");
        doctorPage.openReviewPopup();
        Assert.assertTrue(doctorPage.isReviewPopupOpen(), "❌ Review popup should open after second recording.");

        log.info("[Step 7] Upload the recordings...");
        doctorPage.uploadRecordings();

        log.info("[Step 8] Verify task created...");
        Assert.assertTrue(doctorPage.verifyTaskCreated(), "❌ Task was not created after uploading.");
    }
}
