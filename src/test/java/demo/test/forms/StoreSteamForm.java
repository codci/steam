package demo.test.forms;

import org.openqa.selenium.By;
import org.testng.Assert;
import webdriver.BaseForm;
import webdriver.elements.Button;
import webdriver.elements.Label;
import webdriver.elements.TextBox;

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
        btnDiscount.click();
        browser.waitForPageToLoad();
    }
}
