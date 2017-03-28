package webdriver.elements;

import org.openqa.selenium.By;

/**
 * The class that describes the link
 */
public class Link extends BaseElement {

	/**
	 * Simplified constructor (the name is derived from the locator links)
	 * 
	 * @param locator
	 *            Locator links
	 */

	public Link(final By locator, final String name) {
		super(locator, name);
	}

	public Link(By locatorStageA, By locatorStageB, String name) {
		super(locatorStageA, locatorStageB, name);
	}

	public Link(By locator) {
		super(locator);
	}

	public Link(String string, String name) {
		super(string, name);
	}

	protected String getElementType() {
		return getLoc("loc.link");
	}
	
	public String getHref(){
		return getElement().getAttribute("href");
	}
}
