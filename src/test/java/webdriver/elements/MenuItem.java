package webdriver.elements;

import org.openqa.selenium.By;

/**
 * The class that describes a elements item
 */
public class MenuItem extends BaseElement {

	protected static final String DELIMITER = " -> "; // разделитель пунктов
														// меню (используется
														// для логирования)
	public MenuItem(final By locator, final String[] names) {
		super(locator, getName(names));
	}

	protected String getElementType() {
		return getLoc("loc.menu");
	}

	/**
	 * The creation of the full name of the elements item (used for logging)
	 * 
	 * @param names
	 *            An array of names of individual items of a complex elements
	 */
	protected static String getName(final String[] names) {
		StringBuilder result = new StringBuilder(names[0]);
		for (int i = 1; i < names.length; i++) {
			result.append(DELIMITER);
			result.append(names[i]);
		}
		return result.toString();
	}
}
