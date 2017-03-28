package webdriver.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * Class describing the combobox (dropdown list)
 */
public class ComboBox extends BaseElement {

	public ComboBox(final By locator, final String name) {
		super(locator, name);
	}
	
	public ComboBox(By locatorStageA, By locatorStageB, String name) {
		super(locatorStageA, locatorStageB, name);
	}

	public ComboBox(By locator) {
		super(locator);
	}

	public ComboBox(final String locator, final String name) {
		super(locator, name);
	}
	protected String getElementType() {
		return getLoc("loc.combobox");
	}

	/**
	 * Check availability and get a combo box selected value (label)
	 */
	public String getSelectedLabel() {
		assertIsPresent();
		return new Select(element).getFirstSelectedOption().getText();
	}

	/**
	 * Choose from a list
	 * 
	 * @param optionLocator
	 *            Locator value, which is necessary to choose
	 */
	public void select(String optionLocator) {
		selectByText(optionLocator);
	}
	
	/**Select by index
	 * @param index number of selected option
	 */
	public void selectByIndex(int index){
	    waitAndAssertIsPresent();
		info(String.format(getLoc("loc.selecting.value")+ " '%1$s'", index));
		new Select(element).selectByIndex(index);
	}
	
	/**Select by visible text
	 * @param value
	 */
	public void selectByText(String value){
	    waitAndAssertIsPresent();
		info(String.format(getLoc("loc.selecting.value")+ " '%1$s'", value));
		new Select(element).selectByVisibleText(value);
	}

	/**Select by value
	 * @param value argument value
	 */
	public void selectByValue(String value){
	    waitAndAssertIsPresent();
		info(String.format(getLoc("loc.selecting.value")+" '%1$s'", value));
		new Select(element).selectByValue(value);
	}
	
	/**Get selected value
	 * @return
	 */
	public String getSelectedText(){
		waitAndAssertIsPresent();
		return (new Select(element)).getFirstSelectedOption().getText();
	}
}
