package webdriver.elements;

import org.openqa.selenium.Point;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;


public class Image extends BaseElement {

	public Image(String url, String name) {
		super(url, name);
	}

	protected String getElementType() {
		return getLoc("loc.image");
	}



}
