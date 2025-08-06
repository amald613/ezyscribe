package tests;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pages.ScribeDashboardPage;

public class ScribeDashboardTest {

    private WebDriver driver;
    private ScribeDashboardPage scribePage;
    private boolean isLoggedIn = false;

    @BeforeClass
    public void setup() {
        ChromeOptions options = new ChromeOptions();

        // Disable automation-disruptive features
        options.addArguments("--incognito");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-extensions");
        options.addArguments("disable-infobars");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
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
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(priority = 1)
    public void themeSwitch() {
        scribePage.waitForDashboardToLoad();

        // Switch to dark mode and verify
        scribePage.selectDarkMode();
        Assert.assertTrue(scribePage.isDarkModeActive(), "❌ Dark mode not activated!");

        // Switch to light mode and verify
        scribePage.selectLightMode();
        Assert.assertTrue(scribePage.isLightModeActive(), "❌ Light mode not activated!");
    }
    @Test(priority = 2)
    public void searchByFirstTaskId() throws InterruptedException {
    	  driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        scribePage.waitForDashboardToLoad();
        String taskId = scribePage.searchSecondTaskIdInTableAndSearch();
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.ENTER).perform();
        Thread.sleep(1000);
//        driver.navigate().refresh();
        scribePage.assertFirstRowHasTaskId(taskId);
        Thread.sleep(1000);
        
    }




    @Test(priority = 3)
    public void testStatusFilter() {
    	driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        System.out.println("🔍 Starting Status Filter Test...");
        

        scribePage.waitForDashboardToLoad();
        System.out.println("✅ Dashboard loaded.");

        // Apply first available status filter and verify
        System.out.println("📌 Applying first status filter...");
        scribePage.applyFirstStatusFilterAndVerify();

        // Clear filter and verify
        System.out.println("🧹 Attempting to clear status filter...");
//        scribePage.clearStatusFilter();
        System.out.println("✅ Status filter cleared.");

        System.out.println("✔️ Status Filter Test completed.");
    }

    @Test(priority = 4)
    public void testPriorityFilter() {
        System.out.println("🔍 Starting Priority Filter Test...");
        scribePage.clickResetFiltersIfPresent();

        scribePage.waitForDashboardToLoad();
        System.out.println("✅ Dashboard loaded.");

        // Apply "Medium" priority filter and verify
        System.out.println("📌 Applying 'Medium' priority filter...");
        scribePage.applyPriorityFilterAndVerify(); // ✅ corrected here

        // Clear filter and verify
        System.out.println("🧹 Attempting to clear priority filter...");
//        scribePage.clearPriorityFilter();
        System.out.println("✅ Priority filter cleared.");

        System.out.println("✔️ Priority Filter Test completed.");
    }


    @Test(priority = 5)
    public void testToggleTaskIdColumnView() {
        scribePage.waitForDashboardToLoad();
        scribePage.toggleTaskIdColumnVisibility();
        scribePage.assertTaskIdColumnHidden();
    }

    @Test(priority = 6)
    public void testTaskIdAscendingSort() throws InterruptedException {
    	driver.navigate().to("https://appv2.ezyscribe.com/tasks");
        scribePage.waitForDashboardToLoad();
//        scribePage.clickResetFiltersIfPresent();
        scribePage.applyAscendingSortOnTaskId();
        Thread.sleep(1000);
        scribePage.verifyTaskIdsInAscendingOrder();
    }



}

