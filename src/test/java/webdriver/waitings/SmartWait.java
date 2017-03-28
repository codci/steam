package webdriver.waitings;

import com.google.common.base.Function;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import webdriver.Browser;
import webdriver.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by p.ordenko on 22.05.2015, 20:00.
 */
final public class SmartWait {

    private static final Logger logger = Logger.getInstance();

    /**
     * Wait for some object from condition with default timeout (property "defaultConditionTimeout" from
     * selenium.properties). Wait until it's not false or null.
     * @param condition Condition for waiting {@link ExpectedCondition}
     * @param <T> Object for waiting
     * @return Object from condition
     */
    public static <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, Long.parseLong(Browser.getTimeoutForCondition()));
    }

    /**
     * Wait for some object from condition with timeout. Wait until it's not false or null.
     * @param condition Condition for waiting {@link ExpectedCondition}
     * @param timeOutInSeconds Timeout in seconds
     * @param <T> Object for waiting
     * @return Object from condition
     */
    public static <T> T waitFor(ExpectedCondition<T> condition, long timeOutInSeconds) {
        Browser.getDriver().manage().timeouts().implicitlyWait(0L, TimeUnit.MILLISECONDS);
        Wait<WebDriver> wait = new FluentWait<>((WebDriver) Browser.getDriver())
                .withTimeout(timeOutInSeconds, TimeUnit.SECONDS)
                .pollingEvery(300, TimeUnit.MILLISECONDS);
        try {
            return wait.until(condition);
        } catch (Exception | AssertionError e) {
            logger.debug("SmartWait.waitFor", e);
        } finally {
            Browser.getDriver().manage().timeouts().implicitlyWait(Long.parseLong(Browser.getTimeoutForCondition()), TimeUnit.SECONDS);
        }
        return null;
    }

    /**
     * Wait for condition and return true if waiting successful or false - otherwise.
     * Default timeout (property "defaultConditionTimeout" from selenium.properties) is using.
     * @param condition Condition for waiting {@link ExpectedCondition}
     * @return True if waiting successful or false - otherwise.
     */
    public static boolean waitForTrue(ExpectedCondition<Boolean> condition) {
        try {
            return Boolean.class.cast(waitFor(condition));
        } catch (Exception e) {
            logger.debug("SmartWait.waitForTrue", e);
            return false;
        }
    }


    /**
     * Wait for condition and return true if waiting successful or false - otherwise.
     * @param condition Condition for waiting {@link ExpectedCondition}
     * @param timeOutInSeconds Time out in seconds for waiting
     * @return True if waiting successful or false - otherwise.
     */
    public static boolean waitForTrue(ExpectedCondition<Boolean> condition, long timeOutInSeconds) {
        try {
            return Boolean.class.cast(waitFor(condition, timeOutInSeconds));
        } catch (Exception e) {
            logger.debug("SmartWait.waitForTrue", e);
            return false;
        }
    }

    /**
     * For waiting without WebDriver: Wait for function will be true or return some except false.
     * @param condition Function for waiting {@link Function}
     * @param waitWith Object who will helping to wait (which will be passed to {@link Function#apply(Object)})
     * @param timeOutInSeconds Time-out in seconds
     * @param <F> Type of waitWith param
     * @param <T> Type of object which is waiting
     * @return Object which waiting for or null - is exceptions occured
     */
    public static <F, T> T waitFor(Function<F, T> condition, F waitWith, long timeOutInSeconds) {
        Wait<F> wait = new FluentWait<>(waitWith)
                .withTimeout(timeOutInSeconds, TimeUnit.SECONDS)
                .pollingEvery(300, TimeUnit.MILLISECONDS);
        try {
            return wait.until(condition);
        } catch (Exception | AssertionError e) {
            logger.debug("SmartWait.waitFor", e);
        }
        return null;
    }

    /**
     * For waiting without WebDriver: Wait for function will be true or return some except false.
     * Default timeout (property "defaultConditionTimeout" from selenium.properties) is using.
     * @param condition Function for waiting {@link Function}
     * @param waitWith Object who will helping to wait (which will be passed to {@link Function#apply(Object)})
     * @param <F> Type of waitWith param
     * @param <T> Type of object which is waiting
     * @return Object which waiting for or null - is exceptions occured
     */
    public static<F, T> T waitFor(Function<F, T> condition, F waitWith) {
        return waitFor(condition, waitWith, Long.parseLong(Browser.getTimeoutForCondition()));
    }
}
