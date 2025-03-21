package com.eot.sample.android;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.MobileCapabilityType;

public class AppiumNativeAndroidParallelCalcTest {

    private final HashMap<String, AppiumDriver> drivers = new HashMap<>();

    @DataProvider(name = "device-provider", parallel = true)
    public Object[][] provide() {
        return new Object[][] {
            {"emulator-5554", 2, 5},
            {"emulator-5556", 3, 6}
        };
    }

    @BeforeMethod
    public void beforeMethod(Object[] testArgs) {
        String methodName = ((Method) testArgs[0]).getName();
        String udid = (String) testArgs[2];
        log(String.format("Running test '%s' on '%s'", methodName, udid));

        try {
            AppiumDriver driver = createAppiumDriver(new URL("http://127.0.0.1:4723/wd/hub"), udid);
            drivers.put(udid, driver);
            log(String.format("Created AppiumDriver for device %s", udid));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AppiumDriver", e);
        }
    }

    @AfterMethod
    public void afterMethod(Object[] testArgs) {
        String methodName = ((Method) testArgs[0]).getName();
        ITestResult result = ((ITestResult) testArgs[1]);
        String udid = (String) testArgs[2];
        Integer systemPort = (Integer) testArgs[3];

        AppiumDriver driver = drivers.get(udid);
        try {
            if (driver != null) {
                driver.quit();
            }
            log(String.format("Test '%s' result on %s: %s", methodName, udid, result.getStatus()));
        } catch (Exception e) {
            log("Exception while quitting driver: " + e.getMessage());
        }
    }

    @Test(dataProvider = "device-provider", threadPoolSize = 2)
    public void runTest(Method method, ITestResult testResult, String udid, int num1, int num2) {
        log(String.format("Running test on %s with numbers %d + %d", udid, num1, num2));
        AppiumDriver driver = drivers.get(udid);
        try {
            driver.findElement(By.id("digit_" + num1)).click();
            driver.findElement(By.id("op_add")).click();
            driver.findElement(By.id("digit_" + num2)).click();
            driver.findElement(By.id("eq")).click();  // assuming 'eq' is the ID for equals
        } catch (Exception e) {
            log("Test failed: " + e);
        }
    }

    private AppiumDriver createAppiumDriver(URL appiumServerUrl, String udid) {
        UiAutomator2Options capabilities = new UiAutomator2Options();
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android Emulator");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, "11");
        capabilities.setCapability(MobileCapabilityType.UDID, udid);
        capabilities.setCapability("app",
            new File("src/test/resources/sampleApps/AndroidCalculator.apk").getAbsolutePath());
        capabilities.setCapability(MobileCapabilityType.NO_RESET, false);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);

        return new AppiumDriver(appiumServerUrl, capabilities);
    }

    private void log(String message) {
        System.out.println(" ### " + new Date() + " ### " + message);
    }
}
