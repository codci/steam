package demo.test.forms.elements;

import org.openqa.selenium.By;
import webdriver.BaseForm;
import webdriver.elements.Button;
import webdriver.elements.Label;

public class ContinueInEnglishPopUp extends BaseForm{

    private Label lblBlackBackground = new Label(By.xpath("//*[contains(@class, 'mybox-overlay')]"), "Модальный фон");

    private String continueInEnglishButtonLocator = "//*[contains(@class, 'in-english-wrapper')]//*[contains(@class, 'butn')][text()='%s']";

    public ContinueInEnglishPopUp() {
        super(By.className("in-english"), "Continue in English");
    }

    public enum ContinueInEnglishValues {
        YES("Yes"),
        NO("Нет");

        private String value;

        ContinueInEnglishValues(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public void chooseContinueInEnglish(ContinueInEnglishValues value) {
        new Button(By.xpath(String.format(continueInEnglishButtonLocator, value.getValue())), value.getValue()).click();
    }

    @Override
    public void assertIsClosed() {
        lblBlackBackground.assertIsAbsent();
    }
}
