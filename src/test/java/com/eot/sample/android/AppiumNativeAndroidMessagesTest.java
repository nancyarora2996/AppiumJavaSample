package com.eot.sample.android;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.eot.utils.Wait;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.remote.MobileCapabilityType;

public class AppiumNativeAndroidMessagesTest {

    private AppiumDriver driver;

    @BeforeMethod
    public void beforeMethod(Method method) {
        String methodName = method.getName();
        String udid = "emulator-5554";
        log(String.format("Running test '%s' on '%s'", methodName, udid));
        driver = createAppiumDriver(udid);
        handleUpgradePopup();
    }

    private void handleUpgradePopup() {
        Wait.waitFor(1);
        try {
            WebElement upgradeAppNotificationElement = driver.findElement(By.id("android:id/button1"));
            if (upgradeAppNotificationElement != null) {
                upgradeAppNotificationElement.click();
                Wait.waitFor(1);
            }
        } catch (NoSuchElementException ignored) {}

        try {
            WebElement gotItElement = driver.findElement(By.id("com.android2.calculator3:id/cling_dismiss"));
            if (gotItElement != null) {
                gotItElement.click();
                Wait.waitFor(1);
            }
        } catch (NoSuchElementException ignored) {}
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (driver != null) {
            log("Closing the driver");
            driver.quit();
        }
    }

    @Test
    public void runMessagesTest() {
        int p1 = 3;
        int p2 = 5;
        driver.findElement(By.id("digit" + p1)).click();
        driver.findElement(By.id("plus")).click();
        driver.findElement(By.id("digit" + p2)).click();
        driver.findElement(By.id("equal")).click();
    }

    private void log(String message) {
        System.out.println(" ### " + new Date() + " ### " + message);
    }

    private AppiumDriver createAppiumDriver(String udid) {
        log(String.format("Creating AppiumDriver for device UDID - '%s'", udid));

        UiAutomator2Options capabilities = new UiAutomator2Options();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
        capabilities.setCapability("autoGrantPermissions", true);
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, true);

        // Provide path to APK
        capabilities.setCapability("app",
                new File("./src/test/resources/sampleApps/AndroidCalculator.apk").getAbsolutePath());

        // Connect to manually running Appium server
        URL appiumServerUrl = null;
        try {
            appiumServerUrl = new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }

        AppiumDriver appiumDriver = new AppiumDriver(appiumServerUrl, capabilities);
        log(String.format("Created AppiumDriver connected to %s", appiumServerUrl));
        return appiumDriver;
    }
}
