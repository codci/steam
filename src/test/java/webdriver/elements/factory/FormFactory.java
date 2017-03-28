package webdriver.elements.factory;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.testng.SkipException;
import webdriver.Logger;
import webdriver.annotations.UI;
import webdriver.elements.BaseElement;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Factory class to make using Page Objects simpler and easier.
 *
 * @see <a href="http://code.google.com/p/webdriver/wiki/PageObjects">Page Objects Wiki</a>
 */
public class FormFactory {
    
    private static final Logger logger = Logger.getInstance();

    private FormFactory() {
        // do not instantiate that class
    }

    /**
     * Instantiate an instance of the given class, and set a lazy proxy for each of the WebElement
     * and List<WebElement> fields that have been declared, assuming that the field name is also
     * the HTML element's "id" or "name". This means that for the class:
     * <p/>
     * <code>
     * public class Page {
     * private WebElement submit;
     * }
     * </code>
     * <p/>
     * there will be an element that can be located using the xpath expression "//*[@id='submit']" or
     * "//*[@name='submit']"
     * <p/>
     * By default, the element or the list is looked up each and every time a method is called upon it.
     * To change this behaviour, simply annotate the field with the {@link CacheLookup}.
     * To change how the element is located, use the {@link FindBy} annotation.
     * <p/>
     * This method will attempt to instantiate the class given to it, preferably using a constructor
     * which takes a WebDriver instance as its only argument or falling back on a no-arg constructor.
     * An exception will be thrown if the class cannot be instantiated.
     *
     * @param driver           The driver that will be used to look up the elements
     * @param pageClassToProxy A class which will be initialised.
     * @return An instantiated instance of the class with WebElement and List<WebElement> fields proxied
     * @see FindBy
     * @see CacheLookup
     */
    public static <T> T initElements(WebDriver driver, Class<T> pageClassToProxy) {
        T page = instantiatePage(driver, pageClassToProxy);
        initElements(page);
        return page;
    }

    /**
     * As
     * {@link org.openqa.selenium.support.PageFactory#initElements(org.openqa.selenium.WebDriver, Class)}
     * but will only replace the fields of an already instantiated Page Object.
     *
     * @param page The object with WebElement and List<WebElement> fields that should be proxied.
     */
    public static void initElements(Object page) {
        Field[] fields = page.getClass().getDeclaredFields();
        for (Field field : fields) {
            UI fieldInf = field.getAnnotation(UI.class);
            if (fieldInf != null) {
                field.setAccessible(true);
                if (BaseElement.class.isAssignableFrom(field.getType())) {
                    assignElements(page, field, fieldInf);
                }
            }
        }
    }

    private static void assignElements(Object page, Field field, UI fieldInf) {
        By locator = null;
        for (Method m : fieldInf.getClass().getDeclaredMethods()) {
            try {
                String returnValue = (String) m.invoke(fieldInf);
                Class params[] = new Class[1];
                params[0] = String.class;
                if (!returnValue.isEmpty()) {
                    Method meth = By.class.getDeclaredMethod(m.getName(), params[0]);
                    locator = (By) meth.invoke(null, returnValue);
                }
                if (locator != null) {
                    break;
                }
            } catch (Exception e) {
                logger.debug("FormFactory.initElements", e);
            }
        }

        String title = null;
        try {
            title = fieldInf.title();
        } catch (Exception e) {
            logger.debug("FormFactory.initElements", e);
        }
        try {
            Class[] argTypes = {By.class, String.class};
            Constructor constructor = field.getType().getDeclaredConstructor(argTypes);
            if (title == null || title.isEmpty()) {
                title = field.getName();
            }
            Object[] arguments = {locator, title};
            BaseElement element = (BaseElement) constructor.newInstance(arguments);
            field.set(page, element);
        } catch (Exception e) {
            logger.debug("FormFactory.initElements", e);
        }
    }

    /**
     * Similar to the other "initElements" methods, but takes an {@link ElementLocatorFactory} which
     * is used for providing the mechanism for fniding elements. If the ElementLocatorFactory returns
     * null then the field won't be decorated.
     *
     * @param factory The factory to use
     * @param page    The object to decorate the fields of
     */
    public static void initElements(final ElementLocatorFactory factory, Object page) {
        initElements(new UIFieldDecorator(factory), page);
    }

    /**
     * Similar to the other "initElements" methods, but takes an {@link FieldDecorator} which is used
     * for decorating each of the fields.
     *
     * @param decorator the decorator to use
     * @param page      The object to decorate the fields of
     */
    public static void initElements(FieldDecorator decorator, Object page) {
        Class<?> proxyIn = page.getClass();
        while (proxyIn != Object.class) {
            proxyFields(decorator, page, proxyIn);
            proxyIn = proxyIn.getSuperclass();
        }
    }

    private static void proxyFields(FieldDecorator decorator, Object page, Class<?> proxyIn) {
        Field[] fields = proxyIn.getDeclaredFields();
        for (Field field : fields) {
            Object value = decorator.decorate(page.getClass().getClassLoader(), field);
            if (value != null) {
                try {
                    field.setAccessible(true);
                    field.set(page, value);
                } catch (IllegalAccessException e) {
                    logger.debug("FromFactory.proxyFields", e);
                    throw new SkipException("Failed to assign fields", e);
                }
            }
        }
    }

    private static <T> T instantiatePage(WebDriver driver, Class<T> pageClassToProxy) {
        T result = getPageProxyFromConstructor(driver, pageClassToProxy);
        if (result == null) {
            result = getPageProxyNewInstance(pageClassToProxy);
        }
        if (result == null) {
            throw new SkipException("Failed to instantiate page");
        }
        return result;
    }

    private static <T> T getPageProxyFromConstructor(WebDriver driver, Class<T> pageClassToProxy) {
        try {
            Constructor<T> constructor = pageClassToProxy.getConstructor(WebDriver.class);
            return constructor.newInstance(driver);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.debug("FormFactory.getPageProxyFromConstructor", e);
            return null;
        }
    }

    private static <T> T getPageProxyNewInstance(Class<T> pageClassToProxy) {
        try {
            return pageClassToProxy.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.debug("FormFactory.getPageProxyNewInstance", e);
            return null;
        }
    }
}
