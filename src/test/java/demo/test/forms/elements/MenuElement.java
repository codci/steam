package demo.test.forms.elements;

import org.openqa.selenium.By;
import webdriver.BaseForm;
import webdriver.elements.Label;

public class MenuElement extends BaseForm{

    private String topLevelItemLocator = "//ul[contains(@class, 'top-menu')]//li[contains(@class, 'top')]//span[text()='%s']";
    private String secondLevelItemLocator = "//ul[contains(@class, 'menu')]/li[contains(@class, 'leaf')]//a[text()='%s']";

    public enum TopLevelMenuItem {
        WHAT_ME_DOING("Что мы делаем"),
        COMPANY("Компания"),
        PRESS_CENTER("Пресс-центр"),
        CAREER("Карьера"),
        CONTACTS("Контакты");

        private String name;

        TopLevelMenuItem(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public MenuElement() {
        super(By.xpath("//ul[contains(@class, 'top-menu')]"), "Меню");
    }

    public void navigate(TopLevelMenuItem topLevelMenuItem, String subItem) {
        new Label(By.xpath(String.format(topLevelItemLocator, topLevelMenuItem.getName())), topLevelMenuItem.getName()).moveMouseToElement();
        new Label(By.xpath(String.format(secondLevelItemLocator, subItem)), subItem).click();
    }
}
