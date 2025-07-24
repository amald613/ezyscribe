package pages;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginPage {
    private WebDriver driver;
    private static final Logger log = LogManager.getLogger(LoginPage.class);

    // --- Locators ---
    private By emailInput = By.name("email");
    private By passwordInput = By.name("password");
    private By loginButton = By.xpath("//button[@type='submit']");
    private By errorMessage = By.cssSelector("[data-slot='form-message']");
    private By epicLoginButton = By.xpath("//button[.//img[contains(@alt, 'Epic Logo')]]");

    // --- Constructor ---
    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // --- Actions ---
    public void enterEmail(String email) {
        try {
            WebElement emailField = driver.findElement(emailInput);
            emailField.clear();
            emailField.sendKeys(email);
            log.info("Entered email: {}", email);
        } catch (Exception e) {
            log.error("Failed to enter email: {}", email, e);
        }
    }

    public void enterPassword(String password) {
        try {
            WebElement passwordField = driver.findElement(passwordInput);
            passwordField.clear();
            passwordField.sendKeys(password);
            log.info("Entered password.");
        } catch (Exception e) {
            log.error("Failed to enter password.", e);
        }
    }

    public void clickSubmit() {
        try {
            driver.findElement(loginButton).click();
            log.info("Clicked login button.");
        } catch (Exception e) {
            log.error("Failed to click login button.", e);
        }
    }

    public String getErrorMessage() {
        try {
            String msg = driver.findElement(errorMessage).getText();
            log.info("Captured error message: {}", msg);
            return msg;
        } catch (Exception e) {
            log.warn("No error message displayed.");
            return "";
        }
    }

    // --- Forgot Password Helpers ---
    public WebElement getForgotPasswordButton() {
        try {
            WebElement btn = driver.findElement(By.xpath("//button[normalize-space(text())='Forgot your password?']"));
            log.debug("Found 'Forgot Password' button.");
            return btn;
        } catch (Exception e) {
            log.error("Unable to locate 'Forgot Password' button.", e);
            throw e;
        }
    }

    // --- Epic Login ---
    public void clickEpicLoginAndWaitForRedirect() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(epicLoginButton));
            button.click();
            log.info("Clicked Epic login button.");
            wait.until(ExpectedConditions.urlContains("fhir.epic.com"));
            log.info("Redirected to Epic login URL.");
        } catch (Exception e) {
            log.error("Epic login redirect failed.", e);
            throw e;
        }
    }
}
