package demo.test.forms;

import demo.test.forms.elements.MenuElement;
import org.openqa.selenium.By;
import webdriver.BaseForm;

public class MainPage extends BaseForm{

    private MenuElement menuElement = new MenuElement();


    public MenuElement getMenu() {
        return menuElement;
    }

    public MainPage() {
        super(By.id("block-views-index-bonnet"), "Главная страница");
    }

}
