package com.dreamteam;

import com.dreamteam.screenshot.ScreenshotUtils;
import com.dreamteam.screenshot.Screenshoter;
import io.qameta.allure.Attachment;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

public class AllureAttachmentHandler {

    @Attachment(value = "{0}", type = "image/png")
    public static byte[] attachImage(String attachName, BufferedImage bfi) {
        return bufferedImageToBytesAsPng(bfi);
    }

    public static byte[] bufferedImageToBytesAsPng(BufferedImage bfi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bfi, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static void attachImageWithMarkedIgnoredAreas(Screenshot screenshot, String msg) {
        Set<Coords> areas = screenshot.getIgnoredAreas();
        if (areas != null && !areas.isEmpty()) {
            attachImage(msg + " with ignored areas", ScreenshotUtils.markIgnoredAreasOnImage(screenshot, Color.MAGENTA));
        }
    }

    public static void attachImageToAllure(String attachName, Screenshot screenshot) {
        attachImage(attachName, screenshot.getImage());
        if (Screenshoter.attachImagesWithMarkedIgnoredArea)
            attachImageWithMarkedIgnoredAreas(screenshot, attachName);
    }
}
