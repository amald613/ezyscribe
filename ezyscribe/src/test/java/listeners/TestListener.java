package listeners;

import com.aventstack.extentreports.Status;
import org.testng.*;
import utils.ExtentReportManager;

public class TestListener implements ITestListener, ISuiteListener {

    // Called once before the entire test suite starts
    @Override
    public void onStart(ISuite suite) {
        ExtentReportManager.getExtent(); // Initialize Extent report
    }

    // Called once after the entire test suite finishes
    @Override
    public void onFinish(ISuite suite) {
        ExtentReportManager.flushReports(); // Flush report after all tests
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentReportManager.createTest(result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentReportManager.getTest().log(Status.PASS, "Test Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentReportManager.getTest().log(Status.FAIL, "Test Failed: " + result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentReportManager.getTest().log(Status.SKIP, "Test Skipped");
    }

    // Not needed anymore — we moved flush to ISuite's onFinish
    @Override
    public void onFinish(ITestContext context) { }

    @Override
    public void onStart(ITestContext context) { }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) { }
}
