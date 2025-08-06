package pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ScribeDashboardPage {

    private WebDriver driver;
    private WebDriverWait wait;

    public ScribeDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Locators
    private By taskIdSearchBox = By.xpath("//input[@placeholder='Search task numbers...']");
    private By themeToggleButton = By.xpath("//button[@data-slot='dropdown-menu-trigger' and descendant::span[text()='Toggle theme']]");
    private By darkModeOption = By.xpath("//div[@role='menuitem' and normalize-space()='Dark']");
    private By lightModeOption = By.xpath("//div[@role='menuitem' and normalize-space()='Light']");
    private By anyMenuItem = By.xpath("//div[@role='menuitem' and (normalize-space()='Dark' or normalize-space()='Light')]");
    private By emailField = By.xpath("//input[@name='email']");
    private By passwordField = By.xpath("//input[@type='password']");
    private By loginButton = By.xpath("//button[@type='submit']");
    private By statusFilterButton = By.xpath("//button[@data-slot='popover-trigger' and contains(text(), 'Status')]");
    private By statusOptionsGroup = By.xpath("//div[@role='group']");
    private By firstStatusOption = By.xpath("(//div[@role='option' and @cmdk-item=''])[2]");
    private By tableRows = By.xpath("//table//tbody/tr");
    private By clearStatusFilterButton = By.xpath("//div[@role='button' and @aria-label='Clear Status filter']");
    private By clearPriorityFilterButton = By.xpath("//div[@role='button' and @aria-label='Clear Priority filter']");
    private By viewButton = By.xpath("//button[@aria-label='Toggle columns']");
    private By columnOptionsGroup = By.xpath("//div[@role='group']");
    private By firstViewOption = By.xpath("(//div[@role='option' and @cmdk-item=''])[1]");
    private By taskIdColumnHeader = By.xpath("//th[.='Task ID']");
    private By priorityFilterButton = By.xpath("//button[@data-slot='popover-trigger' and contains(text(), 'Priority')]");
    private By priorityOptionsGroup = By.xpath("//div[@role='group']");
    private By secondPriorityOption = By.xpath("(//div[@role='option' and @cmdk-item=''])[2]");
    private By taskIdCells = By.xpath("//table//tbody//tr/td[1]//a");
    private By resetFiltersButton = By.xpath("//button[@aria-label='Reset filters' and normalize-space(text())='Reset']");

    // ====================== CORE UTILITIES ===========================

    public void loginAsScribe(String email, String password) {
        driver.get("https://appv2.ezyscribe.com/auth/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(email);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    public void refreshDashboard() {
        driver.navigate().refresh();
        waitForDashboardToLoad();
    }

    public void waitForDashboardToLoad() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(taskIdSearchBox));
    }

    public void clickResetFiltersIfPresent() {
        try {
            List<WebElement> resetButtons = driver.findElements(resetFiltersButton);
            if (!resetButtons.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", resetButtons.get(0));
                System.out.println("🔁 Clicked Reset Filters.");
                Thread.sleep(1000);
                refreshDashboard();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Reset Filters failed: " + e.getMessage());
        }
    }

    // ====================== THEME TOGGLE ===========================

    public void safeClickThemeToggleWithRetry() {
        try {
            clickThemeToggle();
        } catch (RuntimeException e) {
            clickThemeToggle();
        }
    }

    public void clickThemeToggle() {
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(themeToggleButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", toggle);
        new Actions(driver).moveToElement(toggle).pause(200).click().perform();
        new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(ExpectedConditions.presenceOfElementLocated(anyMenuItem));
    }

    public boolean isDarkModeActive() {
        return driver.findElement(By.tagName("html")).getAttribute("class").contains("dark");
    }

    public boolean isLightModeActive() {
        return !isDarkModeActive();
    }

    public void selectDarkMode() {
        safeClickThemeToggleWithRetry();
        WebElement dark = wait.until(ExpectedConditions.elementToBeClickable(darkModeOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dark);
        wait.until(driver -> isDarkModeActive());
    }

    public void selectLightMode() {
        safeClickThemeToggleWithRetry();
        WebElement light = wait.until(ExpectedConditions.elementToBeClickable(lightModeOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", light);
        wait.until(driver -> isLightModeActive());
    }

   
 // ====================== SEARCH + ASSERT ===========================
    public String searchSecondTaskIdInTableAndSearch() {
        By secondTaskIdCell = By.xpath("(//td[@data-slot='table-cell']//a)[2]");
        String taskId = wait.until(ExpectedConditions.visibilityOfElementLocated(secondTaskIdCell)).getText().trim();

        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(taskIdSearchBox));
        searchInput.clear();
        searchInput.sendKeys(taskId);
        System.out.println("🔍 Searched for second Task ID: " + taskId);

        // Step 1: Wait 1 second after sending input
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        // Step 2: Refresh the page
        driver.navigate().refresh();

        // Step 3: Wait for dashboard to fully load
        waitForDashboardToLoad();

        // Step 4: Wait another 1 second after dashboard reload
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        return taskId;
    }



    public void assertFirstRowHasTaskId(String expectedTaskId) {
        By firstTaskIdCell = By.xpath("(//td[@data-slot='table-cell']//a)[1]");

        for (int i = 1; i <= 3; i++) {
            try {
                WebElement cell = wait.until(ExpectedConditions.visibilityOfElementLocated(firstTaskIdCell));
                String actual = cell.getText().trim();

                if (actual.equals(expectedTaskId)) {
                    System.out.println("✅ First row matches searched Task ID: " + actual);
                    return;
                } else {
                    System.out.println("⚠️ Attempt " + i + ": Mismatch - Found: " + actual + ", Expected: " + expectedTaskId);
                    Thread.sleep(500); // Let table reload if needed
                }
            } catch (StaleElementReferenceException e) {
                System.out.println("🔁 Retry due to stale element...");
            } catch (Exception e) {
                System.out.println("⚠️ Retry due to exception: " + e.getMessage());
            }
        }

        throw new AssertionError("❌ First row does not match expected Task ID after retries. Expected: " + expectedTaskId);
    }

    public void clearTaskIdSearchBoxWithWait() {
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(taskIdSearchBox));
        boolean cleared = false;

        for (int i = 0; i < 5; i++) {
            try {
                searchInput.clear();
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", searchInput);
                Thread.sleep(300);
                if (searchInput.getAttribute("value").isEmpty()) {
                    cleared = true;
                    break;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Search box clearing attempt failed: " + e.getMessage());
            }
        }

        if (!cleared) {
            throw new RuntimeException("❌ Could not clear Task ID search box after retries.");
        }

        // Wait for table to repopulate after clearing search
        By rowLocator = By.xpath("//table//tbody//tr");
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(rowLocator, 0));
        } catch (Exception e) {
            System.out.println("⚠️ Table did not reload after clearing search. Continuing anyway.");
        }

        System.out.println("🧹 Search box cleared and table loaded.");
    }


    // ====================== FILTERS ===========================

    public void applyFirstStatusFilterAndVerify() {
        clearTaskIdSearchBoxWithWait();
        wait.until(ExpectedConditions.elementToBeClickable(statusFilterButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(statusOptionsGroup));
        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(firstStatusOption));
        String selectedStatus = option.findElement(By.cssSelector("span.truncate")).getText().trim();
        option.click();
        By statusCell = By.xpath("(//table//tbody/tr)[1]/td[4]//span[contains(@class,'capitalize')]");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(statusCell, selectedStatus));
    }

    public void clearStatusFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(clearStatusFilterButton)).click();
    }

    public void applyPriorityFilterAndVerify() {
        clearTaskIdSearchBoxWithWait();
        wait.until(ExpectedConditions.elementToBeClickable(priorityFilterButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(priorityOptionsGroup));
        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(secondPriorityOption));
        String selectedPriority = option.findElement(By.cssSelector("span.truncate")).getText().trim();
        option.click();
        By priorityCell = By.xpath("(//table//tbody/tr)[1]/td[5]//span[contains(@class,'capitalize')]");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(priorityCell, selectedPriority));
    }

    public void clearPriorityFilter() {
        wait.until(ExpectedConditions.elementToBeClickable(clearPriorityFilterButton)).click();
    }

    // ====================== COLUMN VISIBILITY ===========================

    public void toggleTaskIdColumnVisibility() {
        wait.until(ExpectedConditions.elementToBeClickable(viewButton)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(columnOptionsGroup));
        WebElement firstOption = wait.until(ExpectedConditions.elementToBeClickable(firstViewOption));
        firstOption.click();
    }

    public void assertTaskIdColumnHidden() {
        List<WebElement> headers = driver.findElements(taskIdColumnHeader);
        if (!headers.isEmpty()) {
            throw new AssertionError("❌ 'Task ID' column should be hidden but is still visible.");
        }
    }

    // ====================== ASCENDING SORT ===========================
    public void ensureTaskIdColumnVisible() {
        List<WebElement> headers = driver.findElements(taskIdColumnHeader);
        if (headers.isEmpty()) {
            System.out.println("🔎 Task ID column hidden. Re-enabling it...");
            toggleTaskIdColumnVisibility();
            refreshDashboard();
            waitForDashboardToLoad();
        } else {
            System.out.println("✅ Task ID column already visible.");
        }
    }
    public void applyAscendingSortOnTaskId() {
        try {
            System.out.println("🟡 Applying ascending sort on Task ID...");

            clickResetFiltersIfPresent();
            ensureTaskIdColumnVisible();

            By sortButtonLocator = By.xpath("//button[contains(., 'Task #') and @aria-haspopup='menu']");
            By menuLocator = By.xpath("//div[@role='menu']");
            By ascOption = By.xpath("//div[@role='menuitemcheckbox' and contains(., 'Asc')]");
            By rowLocator = By.xpath("//table//tbody/tr");

            // Click the sort button
            WebElement sortButton = wait.until(ExpectedConditions.elementToBeClickable(sortButtonLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sortButton);
            Thread.sleep(300);

            int retries = 3;
            boolean menuAppeared = false;

            while (retries-- > 0) {
                try {
                    sortButton.click();
                    System.out.println("✅ Clicked Task # sort button");
                } catch (Exception e) {
                    System.out.println("⚠️ Fallback to JS click");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sortButton);
                }

                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(menuLocator));
                    menuAppeared = true;
                    break;
                } catch (Exception e) {
                    System.out.println("🔁 Dropdown not visible yet, retrying...");
                    sortButton = wait.until(ExpectedConditions.elementToBeClickable(sortButtonLocator));
                    Thread.sleep(500);
                }
            }

            if (!menuAppeared) {
                throw new RuntimeException("❌ Dropdown did not appear after retries");
            }

            System.out.println("📂 Dropdown is visible");

            // Click 'Ascending' option
            WebElement ascItem = null;
            List<WebElement> options = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ascOption));

            for (WebElement el : options) {
                try {
                    if (el.isDisplayed()) {
                        ascItem = el;
                        break;
                    }
                } catch (StaleElementReferenceException ignored) {}
            }

            if (ascItem == null) {
                throw new RuntimeException("❌ 'Asc' option not found in dropdown");
            }

            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ascItem);
                System.out.println("⬆️ Clicked 'Ascending' sort option");
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ Asc element stale, retrying...");
                WebElement retryAsc = wait.until(ExpectedConditions.elementToBeClickable(ascOption));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", retryAsc);
            }

            // Wait for rows to update
            boolean rowsUpdated = false;
            for (int i = 0; i < 5; i++) {
                List<WebElement> rows = driver.findElements(rowLocator);
                if (!rows.isEmpty()) {
                    System.out.println("✅ Rows visible after sorting. Count: " + rows.size());
                    rowsUpdated = true;
                    break;
                }
                Thread.sleep(1000);
            }

            if (!rowsUpdated) {
                throw new RuntimeException("❌ Table did not update after ascending sort.");
            }

            // ⏸️ Add short wait before verifying
            Thread.sleep(1000);

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to apply ascending sort: " + e.getMessage(), e);
        }
    }

    public void verifyTaskIdsInAscendingOrder() {
        By rowLocator = By.xpath("//table//tbody/tr");

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(rowLocator, 1));
                List<WebElement> rows = driver.findElements(rowLocator);
                System.out.println("🔍 Total table rows found: " + rows.size());

                List<Integer> ids = new ArrayList<>();
                for (WebElement row : rows) {
                    try {
                        WebElement link = row.findElement(By.xpath(".//a"));
                        String text = link.getText().replaceAll("[^0-9]", "").trim();
                        if (!text.isEmpty()) {
                            ids.add(Integer.parseInt(text));
                            System.out.println("➡️ Found Task ID: " + text);
                        }
                    } catch (StaleElementReferenceException | org.openqa.selenium.NoSuchElementException e) {
                        System.out.println("⚠️ Skipping stale/missing row: " + e.getMessage());
                    }
                }

                if (ids.size() < 2) {
                    System.out.println("⚠️ Not enough IDs found. Retry " + attempt);
                    Thread.sleep(1000); // Increase wait
                    continue;
                }

                List<Integer> sorted = new ArrayList<>(ids);
                Collections.sort(sorted);

                System.out.println("📋 Actual Order:   " + ids);
                System.out.println("📋 Expected Order: " + sorted);

                if (ids.equals(sorted)) {
                    System.out.println("✅ Task IDs are in correct ascending order.");
                    return; // success
                } else {
                    System.out.println("⚠️ Attempt " + attempt + ": IDs not yet sorted ascending");
                    Thread.sleep(1000); // Wait more before retrying
                }

            } catch (Exception e) {
                System.out.println("⚠️ Retry " + attempt + " due to: " + e.getMessage());
            }
        }

        throw new AssertionError("❌ Task IDs not in ascending order after retries.");
    }



}
