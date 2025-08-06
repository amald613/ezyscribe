package pages;


import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.*;
import org.openqa.selenium.support.ui.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DoctorDashboardPage {
    WebDriver driver;
    static WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(DoctorDashboardPage.class);

    public DoctorDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ============ Locators ============
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
    private By clearStatusFilterButton = By.xpath("//div[@role='button' and @aria-label='Clear Status filter']");
    private By clearPriorityFilterButton = By.xpath("//div[@role='button' and @aria-label='Clear Priority filter']");
    private By viewButton = By.xpath("//button[@aria-label='Toggle columns']");
    private By columnOptionsGroup = By.xpath("//div[@role='group']");
    private By firstViewOption = By.xpath("(//div[@role='option' and @cmdk-item=''])[1]");
    private By taskIdColumnHeader = By.xpath("//th[.='Task ID']");
    private By priorityFilterButton = By.xpath("//button[@data-slot='popover-trigger' and contains(text(), 'Priority')]");
    private By priorityOptionsGroup = By.xpath("//div[@role='group']");
    private By secondPriorityOption = By.xpath("(//div[@role='option' and @cmdk-item=''])[2]");
    private By resetFiltersButton = By.xpath("//button[@aria-label='Reset filters' and normalize-space(text())='Reset']");

    // ============ Login ============
    public void loginAsDoctor(String email, String password) {
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

    // ============ Theme Toggle ============
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
        clickThemeToggle();
        WebElement dark = wait.until(ExpectedConditions.elementToBeClickable(darkModeOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dark);
        wait.until(driver -> isDarkModeActive());
    }

    public void selectLightMode() {
        clickThemeToggle();
        WebElement light = wait.until(ExpectedConditions.elementToBeClickable(lightModeOption));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", light);
        wait.until(driver -> !isDarkModeActive());
    }

    // ============ Task ID Search ============
    public String searchSecondTaskIdInTableAndSearch() {
        By secondTaskIdCell = By.xpath("(//td[@data-slot='table-cell']//a)[2]");
        String taskId = wait.until(ExpectedConditions.visibilityOfElementLocated(secondTaskIdCell)).getText().trim();
        WebElement searchInput = wait.until(ExpectedConditions.elementToBeClickable(taskIdSearchBox));
        searchInput.clear();
        searchInput.sendKeys(taskId);
        System.out.println("🔍 Searched for second Task ID: " + taskId);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        refreshDashboard();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
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
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                System.out.println("🔁 Retry due to: " + e.getMessage());
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

        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//table//tbody//tr"), 0));
        } catch (Exception e) {
            System.out.println("⚠️ Table did not reload after clearing search.");
        }
        System.out.println("🧹 Search box cleared and table loaded.");
    }

    // ============ Filters ============
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

    // ============ Column Visibility ============
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

    // ============ Sorting ============
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

            WebElement sortButton = wait.until(ExpectedConditions.elementToBeClickable(sortButtonLocator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", sortButton);
            Thread.sleep(300);

            for (int i = 0; i < 3; i++) {
                try {
                    sortButton.click();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(menuLocator));
                    break;
                } catch (Exception e) {
                    System.out.println("🔁 Retry click: " + e.getMessage());
                    sortButton = wait.until(ExpectedConditions.elementToBeClickable(sortButtonLocator));
                    Thread.sleep(500);
                }
            }

            WebElement ascItem = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ascOption))
                    .stream().filter(WebElement::isDisplayed).findFirst()
                    .orElseThrow(() -> new RuntimeException("❌ 'Asc' option not found"));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ascItem);
            Thread.sleep(1000);

            for (int i = 0; i < 5; i++) {
                List<WebElement> rows = driver.findElements(rowLocator);
                if (!rows.isEmpty()) {
                    System.out.println("✅ Rows visible after sorting. Count: " + rows.size());
                    return;
                }
                Thread.sleep(1000);
            }

            throw new RuntimeException("❌ Table did not update after ascending sort.");
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
                List<Integer> ids = new ArrayList<>();
                for (WebElement row : rows) {
                    try {
                        WebElement link = row.findElement(By.xpath(".//a"));
                        String text = link.getText().replaceAll("[^0-9]", "").trim();
                        if (!text.isEmpty()) ids.add(Integer.parseInt(text));
                    } catch (Exception ignored) {}
                }

                if (ids.size() < 2) {
                    Thread.sleep(800);
                    continue;
                }

                List<Integer> sorted = new ArrayList<>(ids);
                Collections.sort(sorted);

                if (ids.equals(sorted)) {
                    System.out.println("✅ Task IDs are in correct ascending order.");
                    return;
                } else {
                    System.out.println("⚠️ Not sorted yet, retrying...");
                    Thread.sleep(800);
                }
            } catch (Exception e) {
                System.out.println("⚠️ Retry due to: " + e.getMessage());
            }
        }

        throw new AssertionError("❌ Task IDs not in ascending order after retries.");
    }

    // ========== RECORDING FUNCTIONALITY ==========

    @FindBy(xpath = "//button[contains(., 'Record')]")
    private static WebElement recordButton;

    @FindBy(xpath = "//button[.//canvas]")
    private WebElement stopButton;

    @FindBy(xpath = "//button[.//*[name()='svg' and @aria-hidden='true'][.//*[name()='rect'][@rx='1']]]")
    private static WebElement pauseButton;

    @FindBy(xpath = "//p[contains(@class, 'gap-5') and contains(@class, 'items-center')]")
    private static WebElement recordingTimer;

    @FindBy(xpath = "//button[contains(., 'Review')]")
    private static WebElement reviewButton;

    @FindBy(xpath = "//div[@role='dialog' and contains(., 'Review Recordings')]")
    private static WebElement reviewPopup;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(., 'Upload Recordings')]")
    private static WebElement uploadRecordingsButton;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(., 'Record Again')]")
    private static WebElement recordAgainButton;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@class, 'lucide-trash')]")
    private List<WebElement> deleteRecordingButtons;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@class, 'lucide-play')]")
    private List<WebElement> playRecordingButtons;

    @FindBy(xpath = "//div[contains(text(),'Audio uploaded and workflow started!')]")
    private static WebElement taskCreatedNotification;

    // === Recording Actions ===

    public void startRecording() {
        logger.info("Starting recording...");
        wait.until(ExpectedConditions.elementToBeClickable(recordButton)).click();
        wait.until(ExpectedConditions.visibilityOf(recordingTimer));
    }

    public void pauseRecording() {
        logger.info("Pausing recording...");
        try {
			wait.until(ExpectedConditions.elementToBeClickable(pauseButton)).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        wait.until(ExpectedConditions.visibilityOf(reviewButton));
    }

    public void stopRecording() {
        logger.info("Stopping recording...");
        wait.until(ExpectedConditions.elementToBeClickable(stopButton)).click();
    }

    public boolean isReviewButtonVisible() {
        try {
            try {
				return reviewButton.isDisplayed();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } catch (NoSuchElementException e) {
            return false;
        }
		return false;
    }

    public void openReviewPopup() {
        if (isElementDisplayed(reviewButton)) {
            reviewButton.click();
            wait.until(ExpectedConditions.visibilityOf(reviewPopup));
        }
    }

    public void uploadRecordings() {
        logger.info("Uploading recordings...");
        wait.until(ExpectedConditions.elementToBeClickable(uploadRecordingsButton)).click();
        wait.until(ExpectedConditions.invisibilityOf(reviewPopup));
    }

    public void recordAgain() {
        logger.info("Clicking 'Record Again'...");
        wait.until(ExpectedConditions.elementToBeClickable(recordAgainButton)).click();
        wait.until(ExpectedConditions.invisibilityOf(reviewPopup));
    }

    public void deleteRecording(int index) {
        if (index < deleteRecordingButtons.size()) {
            logger.info("Deleting recording at index {}", index);
            deleteRecordingButtons.get(index).click();
        }
    }

    public void playRecording(int index) {
        if (index < playRecordingButtons.size()) {
            logger.info("Playing recording at index {}", index);
            playRecordingButtons.get(index).click();
        }
    }

    public int getRecordingsCount() {
        return deleteRecordingButtons.size();
    }

    public boolean isReviewPopupOpen() {
        return isElementDisplayed(reviewPopup);
    }

    public boolean isRecordingAvailable() {
        return isElementDisplayed(recordButton);
    }

    private static boolean isElementDisplayed(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean verifyTaskCreated() {
        try {
            wait.until(ExpectedConditions.visibilityOf(taskCreatedNotification));
            logger.info("Task creation notification visible");
            return true;
        } catch (TimeoutException e) {
            logger.warn("Task creation notification not found");
            return false;
        }
    }
}