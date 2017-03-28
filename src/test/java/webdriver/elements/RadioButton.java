package webdriver.elements;

import org.openqa.selenium.By;

/**
 * Class describing the Radiobutton
 */
public class RadioButton extends BaseElement {

	/**
	 * Constructor
	 * @param locator Locator
	 * @param name Name
	 */
	public RadioButton(final By locator, final String name) {
		super(locator, name);
	}

	/**
	 * Returns element type name
	 * @return Element type name
	 */
	protected String getElementType() {
		return getLoc("loc.radio");
	}

	/**
	 * Constructor
	 * @param locator Locator
	 */
	public RadioButton(final By locator) {
		super(locator);
	}
}
