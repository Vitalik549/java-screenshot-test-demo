package com.dreamteam;

import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Selenide.$;

public class ComputersPage {

    // general page object, containing all locators for MVP
    public static By
            computersCounter = By.cssSelector("#main>h1"),
            computersTable = By.cssSelector("table.computers"),
            pagesCounter = By.cssSelector("#pagination .current>a"),
            filterPanel = By.id("actions");

    public static ComputersPage open() {
        return Selenide.open("/computers", ComputersPage.class);
    }

    @Step
    public ComputersPage openFirstComputer(){
        $(computersTable).$("td a").click();
        return this;
    }

    @Step // this is from ComputerPage, to be refactored when corresponding PO created
    public ComputersPage deleteComputer(){
        $("[value='Delete this computer']").click();
        return this;
    }

    @Step
    public ComputersPage clickOnFilterInput(){
        $(filterPanel).$("input").click();
        return this;
    }
}
