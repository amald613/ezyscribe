package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.*;
import org.openqa.selenium.support.ui.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorDashboardPage {
    WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(DoctorDashboardPage.class);

    public DoctorDashboardPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // ========== THEME TOGGLE ==========
    @FindBy(xpath = "(//button[@data-slot='dropdown-menu-trigger'])[4]")
    WebElement themeToggleButton;

    @FindBy(xpath = "//div[@role='menuitem' and normalize-space()='Dark']")
    WebElement darkModeOption;

    @FindBy(xpath = "//div[@role='menuitem' and normalize-space()='Light']")
    WebElement lightModeOption;

    public void selectDarkMode() {
        logger.info("Selecting Dark Mode");
        wait.until(ExpectedConditions.elementToBeClickable(themeToggleButton)).click();
        wait.until(ExpectedConditions.visibilityOf(darkModeOption));
        wait.until(ExpectedConditions.elementToBeClickable(darkModeOption)).click();
        logger.info("Dark Mode selected");
    }

    public void selectLightMode() {
        logger.info("Selecting Light Mode");
        wait.until(ExpectedConditions.elementToBeClickable(themeToggleButton)).click();
        wait.until(ExpectedConditions.visibilityOf(lightModeOption));
        wait.until(ExpectedConditions.elementToBeClickable(lightModeOption)).click();
        logger.info("Light Mode selected");
    }

    // ========== LANGUAGE TOGGLE ==========
    private final By languageToggleLocator = By.xpath("//button[.//span[text()='Language']]");

    public void toggleLanguage() {
        logger.info("Toggling Language");
        wait.until(ExpectedConditions.elementToBeClickable(languageToggleLocator)).click();
    }

    // ========== TASK ID FILTER ==========
    @FindBy(xpath = "//input[@placeholder='Filter task by id']")
    WebElement taskIdFilterInput;

    public void enterTaskIdFilter(String taskId) {
        logger.info("Entering Task ID filter: {}", taskId);
        wait.until(ExpectedConditions.visibilityOf(taskIdFilterInput)).clear();
        taskIdFilterInput.sendKeys(taskId);
        taskIdFilterInput.sendKeys(Keys.ENTER);
    }

    public boolean verifyTaskIdPresent(String taskId) {
        try {
            By taskLocator = By.xpath("//td[contains(text(),'" + taskId + "')]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(taskLocator));
            logger.info("Task ID {} found", taskId);
            return true;
        } catch (TimeoutException e) {
            logger.warn("Task ID {} not found", taskId);
            return false;
        }
    }

    // ========== SORTING ==========
    private final By taskIdSortBtn = By.xpath("//th[contains(., 'Task ID')]/button");
    private final By patientInitialsSortBtn = By.xpath("//th[contains(., 'Patient Initials')]/button");
    private final By statusSortBtn = By.xpath("//th[contains(., 'Status')]/button");
    private final By prioritySortBtn = By.xpath("//th[contains(., 'Priority')]/button");
    private final By uploadDateSortBtn = By.xpath("//th[contains(., 'Provider Upload Date')]/button");
    private final By uploadTimeSortBtn = By.xpath("//th[contains(., 'Provider Upload Time')]/button");
    private final By audioDurationSortBtn = By.xpath("//th[contains(., 'Audio duration')]/button");

    public void sortBy(By columnSortBtn) {
        logger.info("Sorting by column");
        wait.until(ExpectedConditions.elementToBeClickable(columnSortBtn)).click();
    }

    public void sortByTaskId()           { sortBy(taskIdSortBtn); }
    public void sortByPatientInitials()  { sortBy(patientInitialsSortBtn); }
    public void sortByStatus()           { sortBy(statusSortBtn); }
    public void sortByPriority()         { sortBy(prioritySortBtn); }
    public void sortByUploadDate()       { sortBy(uploadDateSortBtn); }
    public void sortByUploadTime()       { sortBy(uploadTimeSortBtn); }
    public void sortByAudioDuration()    { sortBy(audioDurationSortBtn); }

    public List<String> getColumnValues(By columnSelector) {
        logger.debug("Getting column values");
        List<WebElement> elements = driver.findElements(columnSelector);
        return elements.stream().map(e -> e.getText().trim()).collect(Collectors.toList());
    }

    // ========== STATUS FILTER ==========
    private final By statusFilterBtn = By.xpath("//button[@data-slot=\"popover-trigger\" and normalize-space()=\"Status\"]");
    private final By inProgressOption = By.xpath("//div[@cmdk-item='' and normalize-space()='In Progress']");
    private final By statusColumnCells = By.cssSelector("tbody tr td:nth-child(3)");

    public void filterStatusInProgress() {
        logger.info("Filtering status: In Progress");
        wait.until(ExpectedConditions.elementToBeClickable(statusFilterBtn)).click();
        wait.until(ExpectedConditions.elementToBeClickable(inProgressOption)).click();

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(statusColumnCells),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'No data')]"))
            ));
        } catch (TimeoutException e) {
            logger.error("No data or status rows not loaded");
            throw new AssertionError("Neither rows nor 'No data' message appeared after filtering.");
        }
    }

    public boolean allStatusesAreInProgress() {
        List<WebElement> statusCells = driver.findElements(statusColumnCells);
        if (statusCells.isEmpty()) {
            logger.warn("No rows found. Likely 'No data' displayed.");
            return false;
        }

        for (WebElement cell : statusCells) {
            String status = cell.getText().trim();
            if (!status.equalsIgnoreCase("Inprogress")) {
                logger.error("Unexpected status: {}", status);
                return false;
            }
        }
        return true;
    }

    // ========== PRIORITY FILTER ==========
    private final By priorityFilterBtn = By.xpath("//button[@data-slot='popover-trigger' and normalize-space()='Priority']");
    private final By mediumPriorityOption = By.xpath("//div[@cmdk-item='' and normalize-space()='Medium']");
    private final By priorityColumnCells = By.cssSelector("tbody tr td:nth-child(4)");

    public void filterPriorityMedium() {
        logger.info("Filtering priority: Medium");
        wait.until(ExpectedConditions.elementToBeClickable(priorityFilterBtn)).click();
        wait.until(ExpectedConditions.elementToBeClickable(mediumPriorityOption)).click();

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(priorityColumnCells),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'No data')]"))
            ));
        } catch (TimeoutException e) {
            logger.error("No data or priority rows not loaded");
            throw new AssertionError("Neither rows nor 'No data' message appeared after filtering by Medium priority.");
        }
    }

    public boolean allPrioritiesAreMedium() {
        List<WebElement> priorityCells = driver.findElements(priorityColumnCells);
        if (priorityCells.isEmpty()) {
            logger.warn("No rows found. Likely 'No data' displayed.");
            return false;
        }

        for (WebElement cell : priorityCells) {
            String priority = cell.getText().trim();
            if (!priority.equalsIgnoreCase("Medium")) {
                logger.error("Unexpected priority: {}", priority);
                return false;
            }
        }
        return true;
    }

    // ========== VIEW & VERIFY TASK HIDE ==========
    public String getFirstTaskId() {
        By firstTaskIdCell = By.cssSelector("tbody tr td:nth-child(1)");
        String taskId = wait.until(ExpectedConditions.visibilityOfElementLocated(firstTaskIdCell)).getText().trim();
        logger.info("First task ID: {}", taskId);
        return taskId;
    }

    public void clickViewButton() {
        By viewBtn = By.xpath("//button[normalize-space()='View']");
        logger.info("Clicking View button");
        wait.until(ExpectedConditions.elementToBeClickable(viewBtn)).click();
    }

    public void clickFirstTaskInView() {
        By firstTaskInView = By.xpath("(//div[@role='menuitemcheckbox' and normalize-space()='taskNo'])[1]");
        logger.info("Clicking first task in View dropdown");
        wait.until(ExpectedConditions.elementToBeClickable(firstTaskInView)).click();
    }

    public boolean isTaskIdPresentInList(String taskId) {
        logger.info("Verifying if Task ID {} is present in list", taskId);
        List<WebElement> taskIds = driver.findElements(By.cssSelector("tbody tr td:nth-child(1)"));
        for (WebElement idCell : taskIds) {
            if (idCell.getText().trim().equals(taskId)) {
                return true;
            }
        }
        logger.warn("Task ID {} not found in list", taskId);
        return false;
    }
 // ========== RECORDING FUNCTIONALITY ==========

    @FindBy(xpath = "//button[contains(., 'Record')]")
    private WebElement recordButton;

    @FindBy(xpath = "//button[.//canvas]")
    private WebElement stopButton;

    @FindBy(xpath = "//button[.//*[name()='svg' and @aria-hidden='true'][.//*[name()='rect'][@rx='1']]]")
    private WebElement pauseButton;

    @FindBy(xpath = "//p[contains(@class, 'gap-5') and contains(@class, 'items-center')]")
    private WebElement recordingTimer;

    @FindBy(xpath = "//button[contains(., 'Review')]")
    private WebElement reviewButton;

    @FindBy(xpath = "//div[@role='dialog' and contains(., 'Review Recordings')]")
    private WebElement reviewPopup;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(., 'Upload Recordings')]")
    private WebElement uploadRecordingsButton;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(., 'Record Again')]")
    private WebElement recordAgainButton;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@class, 'lucide-trash')]")
    private List<WebElement> deleteRecordingButtons;

    @FindBy(xpath = "//div[@role='dialog']//button[contains(@class, 'lucide-play')]")
    private List<WebElement> playRecordingButtons;

    @FindBy(xpath = "//div[contains(text(),'Audio uploaded and workflow started!')]")
    private WebElement taskCreatedNotification;

    // === Recording Actions ===

    public void startRecording() {
        logger.info("Starting recording...");
        wait.until(ExpectedConditions.elementToBeClickable(recordButton)).click();
        wait.until(ExpectedConditions.visibilityOf(recordingTimer));
    }

    public void pauseRecording() {
        logger.info("Pausing recording...");
        wait.until(ExpectedConditions.elementToBeClickable(pauseButton)).click();
        wait.until(ExpectedConditions.visibilityOf(reviewButton));
    }

    public void stopRecording() {
        logger.info("Stopping recording...");
        wait.until(ExpectedConditions.elementToBeClickable(stopButton)).click();
    }

    public boolean isReviewButtonVisible() {
        try {
            return reviewButton.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
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

    private boolean isElementDisplayed(WebElement element) {
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
