package com.dreamteam.screenshot.table;

import com.dreamteam.BaseScreenshotTest;
import com.dreamteam.ComputersPage;
import com.dreamteam.screenshot.Screenshoter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.dreamteam.ComputersPage.*;

@Test
public class ComputersTests extends BaseScreenshotTest {

    private ComputersPage computersPage;

    @BeforeClass
    public void removeFirstComputer() {
        // to make some diff on site
        computersPage = open()
                .openFirstComputer()
                .deleteComputer();
    }

    @BeforeMethod
    public void openGooglePage() {
        computersPage = open();
    }

    @Test(description = "Check full page with ignoring elements")
    public void checkFullPageWithIgnoringElements() {
        Screenshoter screenshoter = new Screenshoter()
                .withIgnoredElements(computersCounter, pagesCounter, computersTable);

        compareWithExpectedImage(screenshoter.getPageScreenshot());
    }

    @Test(description = "Check full page without ignoring elements | Should fail")
    public void checkFullPageWithoutIgnoringElements() {
        compareWithExpectedImage(defaultScreenshoter.getPageScreenshot());
    }

    @Test(description = "Check filter panel with removing focus from active input")
    public void checkActiveInput() {
        Screenshoter screenshoter = new Screenshoter().withRemoveFocusFromActiveElement(true);

        // to simulate active input
        computersPage.clickOnFilterInput();

        compareWithExpectedImage(screenshoter.getScreenshotOf(filterPanel));
    }

    @Test(description = "Check filter panel without removing focus from active input | Should fail")
    public void checkActiveInputWithoutRemovingFocus() {
        // to simulate active input
        computersPage.clickOnFilterInput();

        compareWithExpectedImage(defaultScreenshoter.getScreenshotOf(filterPanel));
    }
}
