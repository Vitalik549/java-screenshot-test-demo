package com.dreamteam;

import com.codeborne.selenide.Configuration;
import com.dreamteam.screenshot.Screenshoter;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import java.awt.*;
import java.io.File;

import static com.dreamteam.AllureAttachmentHandler.attachImage;
import static com.dreamteam.AllureAttachmentHandler.attachImageToAllure;
import static com.dreamteam.screenshot.ScreenshotUtils.getBrowserName;
import static com.dreamteam.screenshot.ScreenshotUtils.getFileAsScreenshot;
import static io.qameta.allure.Allure.addAttachment;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class BaseScreenshotTest {

    protected Screenshoter defaultScreenshoter;

    @BeforeSuite
    public void config() {
        // scale for Retina display. For non-retina displays remove next line, so default scale 1 will be set
        Screenshoter.scaling = 2;
        Screenshoter.attachImagesWithMarkedIgnoredArea = true;
        Screenshoter.attachExpectedImageWhenTestPassed = true;

        Configuration.browser = "chrome";
        Configuration.browserSize = "1200x800";
        Configuration.baseUrl = "http://computer-database.herokuapp.com";
        defaultScreenshoter = new Screenshoter();
    }

    /**
     * For test: "src/test/java/com/dreamteam/screenshot/table/ComputersTests/checkFullPageWithIgnoringElements
     * screenshot should be located by path:
     * "src/test/resources/screenshots/table/ComputersTests/chrome/checkFullPageWithIgnoringElements.png"
     * with path pattern:
     * "SCREENSHOT_TESTS_DIR/packageName/browserName/testMethodName"
     * "SCREENSHOTS_DIR/packageName/browserName/screenshotName"
     *
     * @param packageName = "table/ComputersTests"
     * - path to test method relative in SCREENSHOT_TESTS_DIR
     * - same hierarchy should be used to place screenshot in SCREENSHOTS_DIR folder
     * @param browserName = chrome
     * - screenshots will be different for other browsers, so for each browser a separate package is created
     * @param testMethodName = checkFullPageWithIgnoringElements
     * - method name is class
     * @param screenshotName = checkFullPageWithIgnoringElements
     * - name of screenshot file, should be equal to corresponding "{testMethodName}.png"
     */

    private static final String ASSERTION_MESSAGE =
            "Actual and Expected screenshots do not match!\n"
                    + "If changes in UI are expected and valid - please replace base image located:\n"
                    + "%s\n\n"
                    + "with actual screenshot attachment: %s (to download make 'Right click' > 'Save image as')\n\n."
                    + "IMPORTANT! Do NOT change screenshot file name or extension.\n";

    private static final Color IGNORED_COLOR = Color.MAGENTA;

    protected void compareWithExpectedImage(Screenshot actualScreen) {
        // get expected screen
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String testName = caller.getMethodName() + ".png";
        String packageName = getPackageName(caller);
        String path = String.join(File.separator, Screenshoter.SCREENSHOTS_DIR, packageName, getBrowserName(), testName);

        Screenshot expectedScreen = getFileAsScreenshot(path);
        expectedScreen.setIgnoredAreas(actualScreen.getIgnoredAreas());

        // compare screens
        ImageDiff diff = new ImageDiffer()
                .withIgnoredColor(IGNORED_COLOR)
                .makeDiff(expectedScreen, actualScreen);

        // attach results to allure
        attachImageToAllure("actual", actualScreen);

        boolean hasDiff = diff.hasDiff();

        if (Screenshoter.attachExpectedImageWhenTestPassed || hasDiff)
            attachImageToAllure("expected", expectedScreen);

        if (hasDiff) {
            attachImage("diff", diff.getMarkedImage());
            addAttachment("Difference pixels", valueOf(diff.getDiffSize()));
        }
        Assert.assertFalse(hasDiff, String.format(ASSERTION_MESSAGE, path, testName));
    }

    private String getPackageName(StackTraceElement stackTraceElement) {
        String packageName = substringAfterLast(stackTraceElement.getClassName(), Screenshoter.SCREENSHOT_TESTS_DIR);
        return StringUtils.removeEnd(removeStart(packageName, "."), ".").replaceAll("\\.", "/");
    }
}
