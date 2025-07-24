package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

public class DriverFactory {
    protected WebDriver driver;

    // Set up ChromeDriver once before all tests in the class
    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    // Launch browser before each test method
    @BeforeMethod
    public void setup() {
    	
//    	 ChromeOptions options = new ChromeOptions();
//    	    options.addArguments("--headless=new"); // Use "--headless=new" for Chrome 109+
//    	    options.addArguments("--window-size=1920,1080"); // Optional but helps with element visibility
//    	    options.addArguments("--disable-gpu"); // Good practice for headless
//    	    options.addArguments("--no-sandbox");  // Useful for CI environments
//    	    options.addArguments("--disable-dev-shm-usage");
    	    
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://appv2.ezyscribe.com/auth/login");
    }

    // Close browser after each test method
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
