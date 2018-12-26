package com.dreamteam.screenshot;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.Color;
import ru.yandex.qatools.ashot.Screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.*;

public class ScreenshotUtils {

    /**
     * It's better to replace this with your own sleep implementation
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage fileToBufferedImage(File file) {
        BufferedImage fullImg = null;
        try {
            fullImg = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fullImg;
    }

    public static BufferedImage getSubImage(BufferedImage bufferedImage, Rectangle areaToCut) {
        return bufferedImage.getSubimage(areaToCut.x, areaToCut.y, areaToCut.width, areaToCut.height);
    }

    /**
     * Moves focus outside of the screen view, there's not guarantee that it will work in all browsers
     */
    public static void removeFocusFromPage() {
        actions().moveToElement($("body"), -1, -1).build().perform();
    }

    public static void removeFocusFromActiveElement() {
        sleep(200);
        executeJavaScript("document.activeElement.blur()");
        sleep(200); //let js remove focus on element before executing next test step
    }

    /**
     * @implNote removes transparency from element's background. Should be used when transparent element
     * overlaps another dynamic element, which potentially could cause diff on screenshots
     * <p>
     * Use this method directly on element with background transparency (not parent/child node)
     */
    public static void removeElementBackgroundTransparencyIfPresent(SelenideElement element) {
        int min = 0, max = 255;

        Color backgroundColor = Color.fromString(element.getCssValue("background-color"));
        int alpha = backgroundColor.getColor().getAlpha();

        // with 0 alpha element could have any color and setting it as background color
        // could cause unpredictable screenshot results

        // max alpha means that element's transparency is 0 so there's no reason to remove it
        if (alpha != min && alpha != max)
            setCustomBackgroundColorAsRgb(element, backgroundColor);
    }

    /**
     * @implNote Sets color as RGB, so A-channel (opacity) will be set to 1 by default
     * <p>
     * In case A-channel required - use color.asRgba() as parameter to js script
     */
    public static void setCustomBackgroundColorAsRgb(SelenideElement element, Color color) {
        if (color == null) return;
        String script = "return $(arguments[0]).css('background-color', arguments[1]).css('background-color')";
        executeJavaScript(script, element, color.asRgb());
    }

    public static BufferedImage markIgnoredAreasOnImage(Screenshot screenshot, java.awt.Color color) {
        Graphics g = screenshot.getImage().getGraphics();
        g.setColor(color);
        screenshot.getIgnoredAreas().forEach(area -> {
            g.fillRect(area.x, area.y, area.width, area.height);
        });
        g.dispose();
        return screenshot.getImage();
    }

    public static String getBrowserName() {
        // indicate the way you identify your browser name (system variable, selenide config, etc)
        // return Selenide.Configuration.browser;
        return ((RemoteWebDriver) WebDriverRunner.getWebDriver()).getCapabilities().getBrowserName().toLowerCase();
    }

    public static Screenshot getFileAsScreenshot(String path) {
        File expectedImageFile = new File(path);
        if (!expectedImageFile.exists())
            throw new RuntimeException("Expected image does not exist by path: " + expectedImageFile.getAbsolutePath());
        return new Screenshot(ScreenshotUtils.fileToBufferedImage(expectedImageFile));
    }
}