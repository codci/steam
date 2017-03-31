package demo.test.forms;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import webdriver.BaseForm;
import webdriver.Logger;
import webdriver.elements.BaseElement;
import webdriver.elements.Button;
import webdriver.elements.Label;
import webdriver.elements.Select;

import java.util.List;
import java.util.Objects;


/**
 * Created by d.sokol on 29.03.2017.
 */
public class StoreSteamGamePageForm extends BaseForm {

    private Select selectorAgeDay = new Select(By.xpath(".//*[@id='agecheck_form']/select[1]"),"age Day");
    private Select selectorAgeMonth = new Select(By.xpath(".//*[@id='agecheck_form']/select[2]"),"age Month");
    private Select selectorAgeYear = new Select(By.xpath(".//*[@id='ageYear']"),"age Year");
    private Button btnAcceptAge = new Button(By.xpath(".//*[@id='agecheck_form']/a/span"),"Accept age button");

    private Label lblGameSale = new Label(By.xpath(".//*[@id='game_area_purchase']/div[1]/div/div[2]/div/div[1]/div[1]"),"Game sale");
    private Label lblGamePrice = new Label(By.xpath(".//*[@id='game_area_purchase']/div[1]/div/div[2]/div[1]/div[1]"),"Game price");
    private Label lblGamePriceWithSale = new Label(By.xpath(".//*[@id='game_area_purchase']/div[1]/div/div[2]/div[1]/div[1]/div[2]/div[1]"),"Game price with sale");
    private Label lblGamePriceAfterSale = new Label(By.xpath(".//*[@id='game_area_purchase']/div[1]/div/div[2]/div[1]/div[1]/div[2]/div[2]"),"Game price with sale");

    public StoreSteamGamePageForm() {
        super(By.id("game_highlights"), "Steam Store");
    }

    public void setAgeDate(String day, String month, String year) {
        selectorAgeDay.setValue(day);
        selectorAgeMonth.setValue(month);
        selectorAgeYear.setValue(year);
        btnAcceptAge.click();
    }

    public void assertSaleValue(String exp_sale){

        String actual_sale = lblGameSale.getText().replaceAll("\\D+","");
        assertEquals("assert sale value", exp_sale, actual_sale);
    }

    public void assertPriceValue(String exp_price){

        String actual_price = lblGamePriceWithSale.getText().replaceAll("\\D+","");
        assertEquals("assert price value", exp_price, actual_price);
    }

}
