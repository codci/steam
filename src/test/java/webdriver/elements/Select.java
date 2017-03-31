package webdriver.elements;

import org.openqa.selenium.By;

public class Select extends BaseElement {

	public Select(final By locator, final String name) {
		super(locator, name);
	}

	public Select(String string, String name) {
		super(string, name);
	}



	public Select(By locator) {
		super(locator);
	}

	protected String getElementType() {
		return getLoc("loc.label");
	}

	/**
	 * Clear field and type text
	 * @param value text
	 */
	public void setValue(final String value) {
		waitForIsElementPresent();
		element.clear();
		waitForIsElementPresent();
		info(String.format(getLoc("loc.text.select") + " '%1$s'", value));
		element.sendKeys(value);
	}

}
