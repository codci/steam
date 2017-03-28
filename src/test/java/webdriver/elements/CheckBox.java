package webdriver.elements;

import java.util.Random;
import org.openqa.selenium.By;

/**
 * Class describing the checkbox
 */
public class CheckBox extends BaseElement {

	public CheckBox(final By locator, final String name) {
		super(locator, name);
	}

	protected String getElementType() {
		return getLoc("loc.checkbox");
	}

	public CheckBox(By locatorStageA, By locatorStageB, String name) {
		super(locatorStageA, locatorStageB, name);
	}

	public CheckBox(By locator) {
		super(locator);
	}

	public CheckBox(String stringLocator, String name) {
		super(stringLocator, name);
	}

	/**
	 * Checking the checkbox value when an invalid value test stops
	 * @param expectedState expected value
	 */
	public void assertState(boolean expectedState) {
		assertEquals(formatLogMsg(getLoc("loc.checkbox.wrong.value")), expectedState, isChecked());
	}

	/**
	 * set true
	 */
	public void check() {
		check(true);
	}

	/**
	 * Set value
	 * @param state value (true/false)
	 */
	private void check(boolean state) {
		waitAndAssertIsPresent();
		info(String.format(getLoc("loc.setting.value") + " '%1$s'", state));
		if (state && !element.isSelected()) {
			element.click();
		} else if (!state && element.isSelected()) {
			element.click();
		}

	}

	/**
	 * Set a random value
	 * @return set value
	 */
	public boolean checkRandom() {
		boolean state = new Random().nextBoolean();
		check(state);
		return state;
	}

	/**
	 * Get the value of the checkbox (true / false)
	 */
	public boolean isChecked() {
		waitAndAssertIsPresent();
		return element.isSelected();
	}

	/**
	 * reverse state
	 */
	public void toggle() {
		check(!isChecked());
	}

	/**
	 * Set the checkbox to false
	 */
	public void uncheck() {
		check(false);
	}
}
