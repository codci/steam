package webdriver.elements;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebElement;

/**
 * Hidden element with overrided methods without waiting when element is present or exists on page
 */
public class Hidden extends BaseElement {

    public Hidden(final By locator, final String name) {
        super(locator, name);
    }

    public Hidden(By locatorStageA, By locatorStageB, String name) {
        super(locatorStageA, locatorStageB, name);
    }

    public Hidden(By locator) {
        super(locator);
    }

    protected String getElementType() {
        return getLoc("loc.hidden");
    }

    @Override
    public void setValueViaJs(final String value) {
        element = getHiddenElement();
        info(String.format(getLoc("loc.text.typing") + " '%1$s'", value));
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].value=\"\";", element);
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].value=\"" + value + "\";", element);
    }

    @Override
    public String getAttribute(String attr) {
        return getHiddenElement().getAttribute(attr);
    }

    @Override
    public void click() {
        clickViaJS();
    }

    @Override
    public void clickViaJS() {
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].click();", getHiddenElement());
        info(getLoc("loc.clicking"));
    }

    @Override
    public void clickViaJsAndWait() {
        clickViaJS();
        getBrowser().waitForPageToLoad();
    }

    /**
     * Get RemoteWebElement object of current current element
     * @return RemoteWebElement object of current current element
     */
    public RemoteWebElement getHiddenElement() {
        return (RemoteWebElement) getBrowser().getDriver().findElement(locator);
    }

}
