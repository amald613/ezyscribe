package tests;

import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.ScribeDashboardPage;
import utils.ExtentReportManager;
import java.util.HashMap;
import java.util.Map;

public class ScribeDashboardTest {

    private WebDriver driver;
    private ScribeDashboardPage scribePage;
    private boolean isLoggedIn = false;
    private static ExtentTest test;

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();

        // ✅ Disable Chrome popups and automation-disruptive features
        options.addArguments("--incognito");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        options.addArguments("--use-fake-ui-for-media-stream"); // Optional mic access
        options.addArguments("disable-infobars");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.default_content_setting_values.media_stream_mic", 1); // Auto allow mic
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        scribePage = new ScribeDashboardPage(driver);
    }

    @BeforeMethod
    public void ensureLoggedInAndRefresh() {
        if (!isLoggedIn) {
            scribePage.loginAsScribe("testscribe@gmail.com", "12345678");
            isLoggedIn = true;
        } else {
            scribePage.refreshDashboard();
        }
    }

    @AfterClass
    public void teardown() {
        ExtentReportManager.flushReports();
        if (driver != null) {
            driver.quit();
        }
    }

    // ========================= TEST CASES ==============================

    @Test(priority = 1)
    public void themeSwitch() {
        test = ExtentReportManager.createTest("Theme Toggle Test");

        scribePage.waitForDashboardToLoad();

        scribePage.selectDarkMode();
        Assert.assertTrue(scribePage.isDarkModeActive(), "❌ Dark mode not activated!");
        test.pass("✅ Dark mode verified.");

        scribePage.selectLightMode();
        Assert.assertTrue(scribePage.isLightModeActive(), "❌ Light mode not activated!");
        test.pass("✅ Light mode verified.");
    }

    @Test(priority = 2)
    public void searchByFirstTaskId() throws InterruptedException {
        test = ExtentReportManager.createTest("Task ID Search Test");

        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        scribePage.waitForDashboardToLoad();

        String taskId = scribePage.searchSecondTaskIdInTableAndSearch();
        test.info("🔍 Task ID used for search: " + taskId);

        new Actions(driver).sendKeys(Keys.ENTER).perform();
        Thread.sleep(1000);

        scribePage.assertFirstRowHasTaskId(taskId);
        test.pass("✅ Searched Task ID found at top.");
    }

    @Test(priority = 3)
    public void testStatusFilter() {
        test = ExtentReportManager.createTest("Status Filter Test");

        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        scribePage.waitForDashboardToLoad();

        test.info("📌 Applying status filter...");
        scribePage.applyFirstStatusFilterAndVerify();
        test.pass("✅ Status filter applied and verified.");

        test.info("🧹 (Optional) Clearing status filter...");
        // scribePage.clearStatusFilter(); // Optional
    }

    @Test(priority = 4)
    public void testPriorityFilter() {
        test = ExtentReportManager.createTest("Priority Filter Test");

        scribePage.clickResetFiltersIfPresent();
        scribePage.waitForDashboardToLoad();

        test.info("📌 Applying 'Medium' priority filter...");
        scribePage.applyPriorityFilterAndVerify();
        test.pass("✅ Priority filter applied and verified.");
    }

    @Test(priority = 5)
    public void testToggleTaskIdColumnView() {
        test = ExtentReportManager.createTest("Toggle Task ID Column Test");

        scribePage.waitForDashboardToLoad();
        scribePage.toggleTaskIdColumnVisibility();
        scribePage.assertTaskIdColumnHidden();
        test.pass("✅ Task ID column hidden successfully.");
    }

    @Test(priority = 6)
    public void testTaskIdAscendingSort() throws InterruptedException {
        test = ExtentReportManager.createTest("Ascending Sort by Task ID");

        driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        scribePage.waitForDashboardToLoad();

        scribePage.applyAscendingSortOnTaskId();
        Thread.sleep(1000);

        scribePage.verifyTaskIdsInAscendingOrder();
        test.pass("✅ Task IDs are sorted in ascending order.");
    }
}
