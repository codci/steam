package webdriver;

import org.openqa.selenium.By;
import webdriver.elements.factory.FormFactory;

public class BaseAnnotatedForm extends BaseForm {
	public BaseAnnotatedForm(final By locator, final String formTitle) {
		super(locator,formTitle);
		FormFactory.initElements(this);
	}
} 