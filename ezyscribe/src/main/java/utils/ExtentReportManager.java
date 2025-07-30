package utils;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtentReportManager {
    private static ExtentReports extent;
    private static ExtentTest test;

    // Use timestamped file name to avoid overwriting in Jenkins
    private static final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    private static final String reportPath = "test-output/ExtentReport_" + timestamp + ".html";

    public static ExtentReports createInstance() {
        ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath);
        reporter.config().setDocumentTitle("EzyScribe Automation Report");
        reporter.config().setReportName("UI Test Results");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Amal");
        return extent;
    }

    public static ExtentReports getExtent() {
        if (extent == null)
            extent = createInstance();
        return extent;
    }

    public static void flushReports() {
        if (extent != null)
            extent.flush();
    }

    public static ExtentTest createTest(String testName) {
        test = getExtent().createTest(testName);
        return test;
    }

    public static ExtentTest getTest() {
        return test;
    }
}
