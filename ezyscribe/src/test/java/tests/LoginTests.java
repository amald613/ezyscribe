package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pages.LoginPage;
import utils.DriverFactory;
import utils.ExcelUtils;

import java.time.Duration;

public class LoginTests extends DriverFactory {
    private static final Logger log = LogManager.getLogger(LoginTests.class);

    @DataProvider(name = "loginData")
    public Object[][] getLoginData() {
        log.info("Loading login test data from Excel...");
        return ExcelUtils.getLoginData("src/test/resources/LoginData.xlsx", "Sheet1");
    }

    @Test(dataProvider = "loginData", description = "TC01-TC24 - Login test for various valid/invalid credentials", priority = 1)
    public void loginTest(String tcid, String email, String password, String expectedResult) {
        log.info("Starting test case: " + tcid);
        LoginPage login = new LoginPage(driver);

        log.debug("Entering email: " + email);
        login.enterEmail(email);

        log.debug("Entering password.");
        login.enterPassword(password);

        log.info("Clicking submit...");
        login.clickSubmit();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        switch (expectedResult) {
            case "success_doctor":
            case "success_scribe":
                log.info("Expecting successful login...");
                wait.until(ExpectedConditions.urlContains("/tasks"));
                Assert.assertTrue(
                        driver.getCurrentUrl().contains("/tasks"),
                        "Login failed. URL: " + driver.getCurrentUrl()
                );
                log.info("Login successful for: " + email);
                break;

            case "error":
                log.info("Expecting error message for invalid credentials...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Sleep interrupted", e);
                }

                String errorMsg = login.getErrorMessage();
                log.debug("Received error message: " + errorMsg);

                Assert.assertTrue(
                        errorMsg.contains("Invalid email format") ||
                        errorMsg.contains("Password must be at least 8 characters.") ||
                        errorMsg.contains("Invalid email or password"),
                        "Expected error message not found. Got: " + errorMsg
                );
                break;

            default:
                log.error("Unknown expected result type: " + expectedResult);
                Assert.fail("Unknown expected result: " + expectedResult);
        }

        if (tcid.equalsIgnoreCase("TC15") || tcid.equalsIgnoreCase("TC23")) {
            try {
                log.info("Waiting to prevent rate-limiting...");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Sleep interrupted", e);
            }
        }

        log.info("Finished test case: " + tcid);
    }

    @Test(description = "TC09 - Verify 'Forgot Password' triggers email alert", priority = 2)
    public void forgotPasswordTest_TC09() {
        log.info("Starting TC09 - Forgot Password flow");

        LoginPage login = new LoginPage(driver);
        login.enterEmail("testprovider@gmail.com");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement forgotBtn = wait.until(ExpectedConditions.elementToBeClickable(login.getForgotPasswordButton()));

        forgotBtn.click();
        log.info("Clicked 'Forgot Password'");

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(text(),'Check your email for the reset password link!')]")
        ));

        Assert.assertTrue(alert.isDisplayed(), "Expected reset password alert was not shown.");
        log.info("Reset password alert displayed successfully");
    }

    @Test(description = "TC25 - Verify Epic Login Redirect", priority = 3)
    public void epicLoginRedirectTest() {
        log.info("Starting TC25 - Epic Login Redirect test");

        LoginPage login = new LoginPage(driver);
        login.clickEpicLoginAndWaitForRedirect();

        String url = driver.getCurrentUrl();
        log.debug("Redirected to URL: " + url);

        Assert.assertTrue(url.contains("fhir.epic.com"), "Epic login redirect failed. URL: " + url);
        log.info("Epic login redirect successful");
    }
}
