package demo.test.forms;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import webdriver.BaseForm;
import webdriver.Logger;
import webdriver.elements.BaseElement;
import webdriver.elements.Button;
import webdriver.elements.Label;

import java.util.List;


/**
 * Created by d.sokol on 29.03.2017.
 */
public class StoreSteamForm extends BaseForm {

    private Label lbLogo = new Label(By.xpath("//*[@id='logo_holder']"),"Steam logo");
    private Button tabGame = new Button(By.xpath("//*[@id='genre_tab']"),"Game tab");
    private Button btnAction = new Button (By.xpath("//*[@id='genre_flyout']/div/a[6]"),"Action button");

    private Button btnDiscount = new Button (By.xpath("//*[@id='tab_select_Discounts']"),"Discount button");
    private Label lblSale = new Label(By.xpath(".//*[@id='DiscountsRows']/a[1]/div[2]/div[1]"),"Sale");
    private Label GameRow = new Label(By.xpath(".//*[@id='DiscountsRows']/a"),"Game from table");
    private String locGameRow = ".//*[@id='DiscountsRows']/a";
    private String loclblSal = ".//*[@id='DiscountsRows']/a[%s]/div[2]/div[1]";

    public StoreSteamForm() {
        super(By.id("store_nav_search_term"), "Steam Store");
    }

    public void assertLogo(){
        assert(lbLogo.isPresent());
    }

    public void selectCategoryGameAction() {
        tabGame.moveToElement();
        btnAction.click();
        browser.waitForPageToLoad();
    }

    public void selectDiscountTab(){
        btnDiscount.click();
        browser.waitForPageToLoad();
    }

    public void selectGameByMaxSale(){

        browser.waitForPageToLoad();
        List<WebElement> games_list = BaseElement.getListOfElements(locGameRow);
        assert games_list != null;

        int maxSale = 0;
        Label maxSaleLbl = null;

        //for (WebElement gameRow : games_list) {
        for (int i=1; i <= games_list.size(); i++){
            Logger.getInstance().info(getLoc(String.format(loclblSal, i)));
            Label lblSale2 = new Label(By.xpath(String.format(loclblSal, i)), "game of sale");
            int val = Integer.parseInt(lblSale2.getText().replaceAll("\\D+",""));
            Logger.getInstance().info(getLoc(String.format("sale is: %s", val)));
            if(val > maxSale){
                maxSale = val;
                maxSaleLbl = lblSale2;
            }
        }

        if (maxSaleLbl != null) {
            Logger.getInstance().info(getLoc(String.format("%s", maxSale)));
            maxSaleLbl.click();
        }
    }
}
