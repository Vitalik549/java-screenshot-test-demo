package com.dreamteam.screenshot;

import com.codeborne.selenide.SelenideElement;
import com.google.common.collect.Sets;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.support.Color;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.dreamteam.screenshot.ScreenshotUtils.*;

public class Screenshoter {

    // configs related to screenshot tests
    public static final String SCREENSHOT_TESTS_DIR = "screenshot";
    public static final String SCREENSHOTS_DIR = String.join(File.separator, "src", "test", "resources", "screenshots");
    public static boolean attachImagesWithMarkedIgnoredArea = false;
    public static boolean attachExpectedImageWhenTestPassed = false;

    // Screenshoter configs
    public static int scaling = 1;

    private boolean removeFocusFromPage = false; // it's better to set this to true, when there are tooltips allover your site
    private boolean removeFocusFromActiveElement = false;
    private boolean removeBackgroundTransparency = false;
    private Color customBackgroundColor;

    // internal variables
    private SelenideElement element;
    private Rectangle areaToCut;
    private AShot aShot = new AShot()
            .shootingStrategy(ShootingStrategies.scaling(scaling))
            .coordsProvider(new WebDriverCoordsProvider());

    // methods
    public Screenshot getScreenshotOf(SelenideElement element) {
        return getImage(element);
    }

    public Screenshot getScreenshotOf(By locator) {
        return getImage($(locator));
    }

    public Screenshot getPageScreenshot() {
        return getImage(null);
    }

    /**
     * @implNote removes transparency from element's background. Should be used when transparent element
     * overlaps another dynamic element, which potentially could cause diff on screenshots
     * <p>
     * Use this method directly on element with background transparency (not parent/child node)
     */
    public Screenshoter withRemoveBackgroundOpacity(boolean removeBackgroundOpacity) {
        this.removeBackgroundTransparency = removeBackgroundOpacity;
        return this;
    }

    /**
     * Removes focus from ::active element, so active element css highlighting and input cursor will disappear
     * <p>
     * Another option is to disable cursor blinking by configuring your system, for example for mac:
     * https://superuser.com/questions/466660/how-to-disable-blinking-caret-when-editing-text-in-os-x
     */
    public Screenshoter withRemoveFocusFromActiveElement(boolean removeFocusFromActiveElement) {
        this.removeFocusFromActiveElement = removeFocusFromActiveElement;
        return this;
    }

    public Screenshoter withRemoveFocusFromPage(boolean removeFocusFromPage) {
        this.removeFocusFromPage = removeFocusFromPage;
        return this;
    }

    /**
     * When element has 0 opacity - usually Black color is set for background color.
     * So when transparency is removed, background could be changed to Black (or any other color).
     * <p>
     * For such cases use this method to indicate custom background color!
     */
    public Screenshoter withBackgroundColor(Color customBackgroundColor) {
        this.customBackgroundColor = customBackgroundColor;
        return this;
    }

    public Screenshoter withAreaToCut(int x, int y, int width, int height) {
        return withAreaToCut(new Rectangle(x, y, height, width));
    }

    public Screenshoter withAreaToCut(Rectangle rectangle) {
        areaToCut = rectangle;
        return this;
    }

    public Screenshoter withIgnoredElements(By... locators) {
        aShot.ignoredElements(Sets.newHashSet(locators));
        return this;
    }

    public Screenshoter addIgnoredElement(By... locators) {
        Stream.of(locators).forEach(locator -> aShot.addIgnoredElement(locator));
        return this;
    }

    /**
     * @return the screenshot of provided element or whole page if passed parameter is <code>null</code>
     */
    private Screenshot getImage(SelenideElement element) {
        this.element = element;

        prepareView();

        Screenshot actualScreen = element == null
                ? aShot.takeScreenshot(getWebDriver())
                : aShot.takeScreenshot(getWebDriver(), element);

        return afterProcess(actualScreen);
    }

    private Screenshot afterProcess(Screenshot screenshot) {
        if (areaToCut != null) {
            BufferedImage subImage = getSubImage(screenshot.getImage(), areaToCut);
            screenshot.setImage(subImage);
        }
        return screenshot;
    }

    private void prepareView() {
        if (removeFocusFromPage) removeFocusFromPage();
        if (removeFocusFromActiveElement) removeFocusFromActiveElement();

        if (customBackgroundColor == null && removeBackgroundTransparency) {
            removeElementBackgroundTransparencyIfPresent(element);
        } else {
            setCustomBackgroundColorAsRgb(element, customBackgroundColor);
        }
    }
}