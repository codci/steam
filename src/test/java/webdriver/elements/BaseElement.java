package webdriver.elements;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.*;
import org.sikuli.api.ScreenRegion;
import webdriver.BaseEntity;
import webdriver.Browser;
import webdriver.CommonFunctions;
import webdriver.Logger;
import webdriver.waitings.SmartWait;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class, describing base element.
 */
public abstract class BaseElement extends BaseEntity {

    private static final String LINK = "link=";
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String FILE = "file:///";
    private static final String RESOURCE = "resource:///";
    private static final String ID = "id=";
    private static final String CSS = "css=";
    private static final int TIMEOUT_IS_PRESENT = 5;
    private static final int TIMEOUT_WAIT_0 = 0;
    private static final int SHIFT_5 = 5;
    private static final int SHIFT_0 = 0;

    /**
     * @uml.property name="name"
     */
    protected String name;
    /**
     * @uml.property name="locator"
     * @uml.associationEnd multiplicity="(1 1)"
     */

    protected String id;

    protected By locator;
    /**
     * @uml.property name="element"
     * @uml.associationEnd
     */
    protected RemoteWebElement element;

    /**
     * @uml.property name="element"
     * @uml.associationEnd
     */
    protected ScreenRegion sikuliScreenRegion;
    protected URL sikuliLocator;
    protected String similarity = Browser.getSimilarityForSikuli();

    /**
     * The simple constructor, name will be extracted
     *
     * @param loc By Locator
     */
    protected BaseElement(final By loc) {
        locator = loc;
    }

    /**
     * The main constructor
     *
     * @param loc    By Locator
     * @param nameOf Output in logs
     */
    protected BaseElement(final By loc, final String nameOf) {
        locator = loc;
        name = nameOf;
    }

    /**
     * using different locators for different stages DON'T FORGET TO INIT
     *
     * @param locatorStageA First locator
     * @param locatorStageB Second locator
     * @param nameOfElement Name
     */
    protected BaseElement(final By locatorStageA, final By locatorStageB, final String nameOfElement) {
        locator = (Browser.stageController.getStage() == webdriver.stage.Stages.STAGE_A) ? locatorStageA
                : locatorStageB;
        name = nameOfElement + " " + Browser.stageController.getStage();
    }

    /**
     * easy adapting from Selenium RC locators. CSS, ID, LINK
     *
     * @param stringLocator String locator
     * @param nameOfElement Name
     */
    protected BaseElement(String stringLocator, final String nameOfElement) {
        String clearLoc = null;
        if (stringLocator.contains(CSS)) {
            clearLoc = stringLocator.replaceFirst(CSS, "");
            locator = By.cssSelector(clearLoc);
            name = nameOfElement;
        } else if (stringLocator.contains(ID)) {
            clearLoc = stringLocator.replaceFirst(ID, "");
            locator = By.id(clearLoc);
            name = nameOfElement;
        } else if (stringLocator.contains(LINK)) {
            clearLoc = stringLocator.replaceFirst(LINK, "");
            locator = By.linkText(clearLoc);
            name = nameOfElement;
        } else if (stringLocator.contains(RESOURCE) || stringLocator.contains(FILE) || stringLocator.contains(HTTP)
                || stringLocator.contains(HTTPS)) {
            sikuliLocator = getSikuliLocator(stringLocator);
            name = nameOfElement;
        } else {
            logger.fatal("UNKNOWN LOCATOR's TYPE. Change to 'By'");
        }
    }

    /**
     * @return RemoteWebElement
     * @uml.property name="element"
     */
    public RemoteWebElement getElement() {
        waitForIsElementPresent();
        return element;
    }

    /**
     * @param elementToSet RemoteWebElement
     * @uml.property name="element"
     */
    public void setElement(final RemoteWebElement elementToSet) {
        element = elementToSet;
    }

    /**
     * Verify that the drop-down element is minimized (performed by a class member)
     *
     * @return true if collapsed
     */
    public boolean isCollapsed() {
        waitAndAssertIsPresent();
        String elementClass = element.getAttribute("class");
        return elementClass.toLowerCase().contains("collapse");
    }

    /**
     * Check that the element is enabled (performed by a class member)
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        waitAndAssertIsPresent();
        String elementClass = element.getAttribute("class");
        return element.isEnabled() && (!elementClass.toLowerCase().contains("disabled"));
    }

    /**
     * Check that the item is checked (performed by a class member)
     *
     * @return true if checked
     */
    public boolean isChecked() {
        waitAndAssertIsPresent();
        String elementClass = element.getAttribute("class");
        return !elementClass.toLowerCase().contains("unchecked");
    }

    /**
     * Set similarity
     *
     * @param similarity similarity
     * @return BaseElement
     */
    public BaseElement setSimilar(final String similarity) {
        this.similarity = similarity;
        return this;
    }

    /**
     * Get similarity
     *
     * @return String
     */
    public String getSimilarity() {
        return similarity;
    }

    /**
     * Get locator for sikuli
     *
     * @param stringLocator path or url
     * @return URL
     */
    public URL getSikuliLocator(String stringLocator) {
        String result = stringLocator;
        String fullPath = ClassLoader.getSystemResource(result.replaceAll(RESOURCE, "")).getPath().substring(1);
        if (stringLocator.contains(RESOURCE)) {
            result = FILE + fullPath;
        }
        try {
            return new URL(result);
        } catch (MalformedURLException e) {
            logger.debug(this, e);
        }
        return null;
    }

    /**
     * @return Locator
     * @uml.property name="locator"
     */
    public By getLocator() {
        return locator;
    }

    /**
     * @return Name of element
     * @uml.property name="name"
     */
    public String getName() {
        try {
            if (name == null) {
                name = getBrowser().getDriver().findElement(locator).getText();
            }
        } catch (Exception e) {
            logger.debug(this, e);
            name = "unknown";
        }
        return name;
    }

    /**
     * The implementation of an abstract method for logging of BaseEntity
     *
     * @param message Message to display in the log
     * @return Formatted message (containing the name and type of item)
     */
    protected String formatLogMsg(final String message) {
        return String.format("%1$s '%2$s' %3$s %4$s", getElementType(), name, Logger.LOG_DELIMITER, message);
    }

    /**
     * The method returns the element type (used for logging)
     *
     * @return Type of element
     * @uml.property name="elementType"
     */
    protected abstract String getElementType();

    /**
     * Send keys.
     */
    public void sendKeys(Keys key) {
        waitForIsElementPresent();
        assertIsPresent();
        getBrowser().getDriver().findElement(locator).sendKeys(key);
    }










    /**
     * Click via Action.
     */
    public void clickViaAction() {
        waitForIsElementPresent();
        info(getLoc("loc.clicking"));
        Actions action = new Actions(getBrowser().getDriver());
        action.click(getElement());
        action.perform();
    }

    /**
     * Click via JS.
     */
    public void clickViaJS() {
        waitForIsElementPresent();
        assertIsPresent();
        info(getLoc("loc.clicking"));
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].click();", getElement());
    }

    /**
     * Wait for the item and click on it
     */
    public void waitAndClick() {
        waitForElementClickable();
        info(getLoc("loc.clicking"));
        click();
    }

    /**
     * ext click through .sendKeys("\n");
     */
    public void clickExt() {
        info(getLoc("loc.clicking.extended"));
        waitForIsElementPresent();
        if (getBrowser().getDriver() instanceof JavascriptExecutor) {
            ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].style.border='3px solid red'", element);
        }
        getBrowser().getDriver().findElement(locator).sendKeys("\n");
    }

    /**
     * Move mouse to this element.
     */
    public void moveMouseToElement() {
        waitForIsElementPresent();
        Actions action = new Actions(getBrowser().getDriver());
        action.moveToElement(getElement());
        action.perform();
    }

    /**
     * Wait for element is present.
     */
    public void waitForIsElementPresent() {
        if (sikuliLocator != null) {
            isPresent();
            return;
        }

        isPresent(Integer.valueOf(getBrowser().getTimeoutForCondition()));
        // troubleshooting if element is not found
        boolean isVisible;
        try {
            isVisible = element.isDisplayed();
        } catch (Exception | AssertionError ex) {
            logger.debug(this, ex);
            isVisible = false;
        }
        Assert.assertTrue(formatLogMsg(getLoc("loc.is.absent")), isVisible);
    }

    /**
     * Wait until element is clickable.
     */
    public void waitForElementClickable() {
        new WebDriverWait(getBrowser().getDriver(), Long.parseLong(getBrowser().getTimeoutForCondition())).until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Performing troubleshooting via changing active locator, output log and report.
     */
    private void performTroubleShooting() {
        try {
            info("---------------- Troubleshooting starting --------------------");
            int length = decrementLocator(locator).toString().length();
            for (int i = 0; i < length; i++) {
                decrementLocator(locator);
                Boolean result = isPresent(TIMEOUT_WAIT_0);
                info("Re-try with locator: \t" + locator.toString() + String.format(new String(new char[i]).replace('\0', ' ') + " :%s", result ? "FOUND!" : "NOT FOUND"));
                if (result) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.debug(this, e);
            warn(e.getMessage());
        } finally {
            info("---------------- Troubleshooting finished --------------------");
        }
    }

    /**
     * Decrement type of locator (troubleshooting).
     *
     * @param locator
     * @return By
     */
    private By decrementLocator(By locator) {
        for (Field field : locator.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String strLocator = (String) field.get(locator);
                field.set(locator, strLocator.substring(0, strLocator.length() - 1));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.debug(this, e);
                warn(e.getMessage());
            }
        }
        return locator;
    }

    /**
     * Does an element exist in html (no check for display)
     */
    public final boolean exists() {
        getBrowser().getDriver().manage().timeouts().implicitlyWait(TIMEOUT_WAIT_0, TimeUnit.SECONDS);
        boolean result = !getBrowser().getDriver().findElements(locator).isEmpty();
        element = (RemoteWebElement) getBrowser().getDriver().findElement(locator);
        getBrowser().getDriver().manage().timeouts().implicitlyWait(Integer.valueOf(getBrowser().getTimeoutForCondition()), TimeUnit.SECONDS);
        return result;
    }

    /**
     * Click on the item.
     */
    public void click() {
        waitForIsElementPresent();
        info(getLoc("loc.clicking"));
        getBrowser().getDriver().getMouse().mouseMove(element.getCoordinates());
        if (getBrowser().getDriver() instanceof JavascriptExecutor) {
            ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].style.border='3px solid red'", element);
        }
        element.click();
    }

    /**
     * Click on an item and wait for the page is loaded
     */
    public void clickAndWait() {
        click();
        getBrowser().waitForPageToLoad();
    }

    /**
     * Click on an item ext click through .sendKeys("\n") and wait for the page is loaded.
     */
    public void clickExtAndWait() {
        clickExt();
        getBrowser().waitForPageToLoad();
    }

    /**
     * Click on an item js click and wait for the page is loaded.
     */
    public void clickViaJsAndWait() {
        clickViaJS();
        getBrowser().waitForPageToLoad();
    }

    /**
     * Click and look forward to the emergence of a new window.
     */
    public void clickAndWaitForNewWindow() {
        Set<String> set = getBrowser().getDriver().getWindowHandles();
        int count = getBrowser().getDriver().getWindowHandles().size();
        click();
        info(getLoc("loc.select.next.window"));
        getBrowser().waitForNewWindow(count);
        Iterator<String> cIter = getBrowser().getDriver().getWindowHandles().iterator();
        String handle = null;
        while (cIter.hasNext()) {
            String temp = cIter.next();
            if (!set.contains(temp)) {
                handle = temp;
                break;
            }
        }
        getBrowser().getDriver().switchTo().window(handle);
        getBrowser().windowMaximise();
    }

    /**
     * Click and look forward to closing the current window
     */
    public void clickAndWaitForCloseWindow() {
        int count = getBrowser().getDriver().getWindowHandles().size();
        click();
        info(getLoc("loc.select.previous.window"));
        getBrowser().waitForNewWindow(count - 2);
        getBrowser().getDriver().switchTo().window((String) getBrowser().getDriver().getWindowHandles().toArray()[count - 2]);
    }

    /**
     * Get the item text (inner text).
     *
     * @return Text of element
     */
    public String getText() {
        waitForIsElementPresent();
        return element.getText();
    }

    /**
     * Check for an element on the page If the element is absent, then the test is aborted
     */
    public void assertIsPresent() {
        if (!isPresent()) {
            fatal(getLoc("loc.is.absent"));
        }
    }

    /**
     * Check for an element on the page If the element is present, then the test is aborted
     */
    public void assertIsAbsent() {
        if (!isAbsent()) {
            fatal(getLoc("loc.is.present"));
        }
    }

    /**
     * Wait for the item and abort the test, if the item does not appear.
     */
    public void waitAndAssertIsPresent() {
        waitForIsElementPresent();
    }

    /**
     * Check for is element present on the page.
     *
     * @return Is element present
     */
    public boolean isPresent() {
        return isPresent(Long.parseLong(Browser.getTimeoutForCondition()));
    }

    public boolean isAbsent() {
        return isAbsent(Long.parseLong(Browser.getTimeoutForCondition()));
    }

    /**
     * Check for is element present on the page.
     *
     * @return Is element present
     */
    private boolean isPresent(long timeout) {

        ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                try {
                    List<WebElement> elems = driver.findElements(locator);
                    for (WebElement elem : elems) {
                        if (elem.isDisplayed()) {
                            element = (RemoteWebElement) elem;
                            return true;
                        }
                    }
                    return false;
                } catch (Exception | AssertionError e) {
                    logger.debug(this, e);
                    return false;
                }
            }
        };
        return SmartWait.waitForTrue(condition, timeout);
    }

    private boolean isAbsent(long timeout) {
        ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                try {
                    List<WebElement> elems = driver.findElements(locator);
                    return elems.size() == 0;
                } catch (Exception | AssertionError e) {
                    logger.debug(this, e);
                    return false;
                }
            }
        };
        return SmartWait.waitForTrue(condition, timeout);
    }


    /**
     * Double click on the item. Waiting for the end of renderiga
     */
    public void doubleClick() {
        waitForIsElementPresent();
        info(getLoc("loc.clicking.double"));
        getBrowser().getDriver().getMouse().mouseMove(element.getCoordinates());
        Actions builder = new Actions(getBrowser().getDriver());
        Action dClick = builder.doubleClick(element).build();
        dClick.perform();

    }

    /**
     * another implementation of waiting.
     */
    public void waitForExists() {
        ExpectedCondition condition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                Boolean isExist = !driver.findElements(locator).isEmpty();
                if (isExist) {
                    element = (RemoteWebElement) driver.findElement(locator);
                }
                return isExist;
            }
        };
        SmartWait.waitFor(condition);
    }

    /**
     * another implementation of waiting for not exists.
     */
    public void waitForDoesNotExist() {
        ExpectedCondition condition = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(final WebDriver driver) {
                return driver.findElements(locator).isEmpty();
            }
        };
        SmartWait.waitFor(condition);
    }

    /**
     * Gets attribute value of the element.
     *
     * @param attr Attribute name
     * @return Attribute value
     */
    public String getAttribute(final String attr) {
        waitForIsElementPresent();
        return getBrowser().getDriver().findElement(locator).getAttribute(attr);
    }

    /**
     * set value via javascript <b>document.getElementById('%1$s').value='%2$s' </b>
     *
     * @param elementId Element Id
     * @param value     Value
     */
    public void setValueViaJS(final String elementId, final String value) {
        try {
            ((JavascriptExecutor) getBrowser().getDriver()).executeScript(String.format(
                    "document.getElementById('%1$s').value='%2$s'", elementId, value), element);
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            Logger.getInstance().warn(e.getMessage());
        }
    }

    /**
     * set innerHtml via javascript <b>arguments[0].innerHTML='%1$s' </b>.
     *
     * @param value value
     */
    public void setInnerHtml(final String value) {
        waitAndAssertIsPresent();
        element.click();
        info("Ввод текста '" + value + "'");
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].innerHTML=\"\";", element);
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].innerHTML=\"" + value + "\";", element);
    }

    /**
     * Check if element is "disabled" or "readonly".
     */
    public void assertDisabled() {
        waitAndAssertIsPresent();
        String[] attributes = new String[]{"disabled", "readonly", "readOnly"};
        String disabled;
        for (int i = 0; i < attributes.length; i++) {
            disabled = element.getAttribute(attributes[i]);
            if ("".equals(disabled) || "disabled".equals(disabled) || "true".equals(disabled)
                    || "readonly".equals(disabled)) {
                return;
            }
        }
        fatal(getElementType() + " " + name + getLoc("loc.is.disabled"));
    }

    /**
     * Set value via javascript <b>arguments[0].value='%1$s' </b>.
     *
     * @param value value
     */
    public void setValueViaJs(final String value) {
        waitAndAssertIsPresent();
        element.click();
        info(String.format(getLoc("loc.text.typing") + " '%1$s'", value));
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].value=\"\";", element);
        ((JavascriptExecutor) getBrowser().getDriver()).executeScript("arguments[0].value=\"" + value + "\";", element);
    }

    /**
     * Click if present.
     */
    public void clickIfPresent() {
        if (isPresent()) {
            click();
        }
    }

    /**
     * Right Click.
     */
    public void clickRight() {
        waitForIsElementPresent();
        info("Clicking Right");
        getBrowser().getDriver().getMouse().mouseMove(element.getCoordinates());
        getBrowser().getDriver().getMouse().contextClick(element.getCoordinates());
    }

    /**
     * Focuses the element.
     */
    public void focus() {
        getBrowser().getDriver().getMouse().mouseMove(element.getCoordinates());
    }

    /**
     * Get Sikuli Driver.
     *
     * @return SikuliDriver
     */
    public ScreenRegion getSikuliRegion() {
        return sikuliScreenRegion;
    }


}
