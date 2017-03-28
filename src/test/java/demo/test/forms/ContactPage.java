package demo.test.forms;

import org.openqa.selenium.By;
import webdriver.BaseForm;
import webdriver.Browser;

public class ContactPage extends BaseForm{

    public ContactPage() {
        super(By.className("map-frame"), "Контакты");
    }

    public void assertCity(String city) {
        String title = Browser.getDriver().getTitle();
        assertEquals(String.format("Город %s присутствует в заголовке", city),
                String.format("Город %s отсутствует в заголовке. Текущий заголовок: %s", city, title),
                true, title.contains(city));
    }
}
