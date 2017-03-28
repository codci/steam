package webdriver;

import com.opera.core.systems.OperaDriver;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.selendroid.SelendroidCapabilities;
import io.selendroid.SelendroidDriver;
import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;

import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import webdriver.Browser.Browsers;

import javax.naming.NamingException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import static webdriver.Logger.getLoc;

/**
 * The class-initializer-based browser string parameter.
 */
final public class BrowserFactory {

    private static final String LOCALHOST = "localhost";
    private static String androidHost = System.getProperty("androidHost", LOCALHOST);
    private static String androidPort = System.getProperty("androidPort", "6789");
    private static String androidEmuSid = System.getProperty("androidSerial", "emulator-5554");
    private static String androidBrowserName = System.getProperty("androidBrowserName", "android");
    private static final Logger logger = Logger.getInstance();
    private static final String CLS_NAME = BrowserFactory.class.getName();

    private BrowserFactory() {
        // do not instantiate BrowserFactory class
    }

    /**
     * Setting up Driver
     *
     * @param type Browser type
     * @return RemoteWebDriver
     */
    public static RemoteWebDriver setUp(final Browsers type) {
        Proxy proxy = null;
        if (Browser.getAnalyzeTraffic()) {
            //browsermob proxy
            //-----------------------------------------------------------
            //captures the mouse movements and navigations
            ProxyServ.getProxyServer().setCaptureHeaders(true);
            ProxyServ.getProxyServer().setCaptureContent(true);
            // get the Selenium proxy object
            proxy = null;
            try {
                proxy = ProxyServ.getProxyServer().seleniumProxy();
            } catch (Exception e) {
                logger.debug("BrowserFactory.setUp", e);
            }
        }
        return getWebDriver(type, proxy);
    }

    /**
     * Setting up Driver
     *
     * @param type Browser type
     * @return RemoteWebDriver
     * @throws NamingException NamingException
     */
    public static RemoteWebDriver setUp(final String type) throws NamingException {
        for (Browsers t : Browsers.values()) {
            if (t.toString().equalsIgnoreCase(type)) {
                return setUp(t);
            }
        }
        throw new NamingException(getLoc("loc.browser.name.wrong") + ":\nchrome\nfirefox\niexplore\nopera\nsafari");
    }
    
    //////////////////
    // Private methods
    //////////////////

    private static RemoteWebDriver getWebDriver(final Browsers type, Proxy proxy) {
        DesiredCapabilities capabilitiesProxy = new DesiredCapabilities();
        if (proxy != null) {
            capabilitiesProxy.setCapability(CapabilityType.PROXY, proxy);
        }
        switch (type) {
            case CHROME:
                return getChromeDriver(proxy);
            case FIREFOX:
                return getFirefoxDriver(capabilitiesProxy);
            case IEXPLORE:
                return getIEDriver(proxy);
            case OPERA:
                return getOperaDriver(capabilitiesProxy);
            case SAFARI:
                return getSafariDriver(capabilitiesProxy);
            case SELENDROID:
                return getSelendroidDriver();
            /**
             * Appium Android browser for testing with Android
             */
            case APPIUM_ANDROID:
                return getAppiumDriver();
            /**
             * Appium iOS Safari browser for testing with iOS Simulator
             */
            case IOS:
                return getIosDriver();
            default:
                return null;
        }
    }
    
    private static RemoteWebDriver getFirefoxDriver(DesiredCapabilities capabilities) {
        DesiredCapabilities caps =  (capabilities != null) ? capabilities : new DesiredCapabilities();
        FirefoxProfile ffProfile = new FirefoxProfile();
        ffProfile.setPreference("plugin.state.flash", (Browser.isWithoutFlash()) ? 1 : 0);
        if (Browser.getDetectJsErrors()) {
            try {
                JavaScriptError.addExtension(ffProfile);
            } catch (IOException e) {
                logger.debug(CLS_NAME + ".getFirefoxDriver", e);
                logger.warn("Error during initializing of FF (JavaScriptError) webdriver");
            }
        }
        return new FirefoxDriver(new FirefoxBinary(), ffProfile, caps);
    }
    
    private static RemoteWebDriver getChromeDriver(Proxy proxy) {
        String platform = System.getProperty("os.name").toLowerCase();
        URL myTestURL = null;
        File myFile = null;
        if (platform.contains("win")) {
            myTestURL = ClassLoader.getSystemResource("chromedriver.exe");
        } else if (platform.contains("mac")) {
            myTestURL = ClassLoader.getSystemResource("chromedriver");
        } else {
            logger.fatal(String.format("Unsupported platform: %1$s for chrome browser %n", platform));
        }

        ChromeOptions options = null;
        if (Browser.getDetectJsErrors()) {
            options = new ChromeOptions();
            options.addExtensions(new File(ClassLoader.getSystemResource("Chrome_JSErrorCollector.crx").getPath()));
        }
        DesiredCapabilities cp1 = DesiredCapabilities.chrome();
        cp1.setCapability("chrome.switches", Arrays.asList("--disable-popup-blocking"));
        try {
            myFile = new File(myTestURL.toURI());
        } catch (URISyntaxException e1) {
            logger.debug(CLS_NAME + ".getChromeDriver", e1);
        }
        System.setProperty("webdriver.chrome.driver", myFile.getAbsolutePath());
        if (Browser.getAnalyzeTraffic()) {
            cp1.setCapability(CapabilityType.PROXY, proxy);
        }
        if (options != null) {
            cp1.setCapability(ChromeOptions.CAPABILITY, options);
        }
        RemoteWebDriver driver = new ChromeDriver(cp1);
        driver.manage().window().maximize();
        return driver;
    }
    
    private static RemoteWebDriver getIEDriver(Proxy proxy) {
        File myFile = null;
        if (Browser.getIeLocalRun()) {
            DesiredCapabilities cp = DesiredCapabilities.internetExplorer();
            cp.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
            URL myTestURL2 = ClassLoader.getSystemResource("IEDriverServer.exe");
            try {
                myFile = new File(myTestURL2.toURI());
            } catch (URISyntaxException e1) {
                logger.debug(CLS_NAME + ".getIEDriver", e1);
            }
            System.setProperty("webdriver.ie.driver", myFile.getAbsolutePath());
            if (Browser.getAnalyzeTraffic()) {
                cp.setCapability(CapabilityType.PROXY, proxy);
            }
            return new InternetExplorerDriver(cp);
            // better to avoid
        } else {
            // now remote connection will be refused, so use selenium server instead
            return new InternetExplorerDriver();
        }
    }
    
    private static RemoteWebDriver getOperaDriver(DesiredCapabilities capabilities) {
        if (capabilities != null) {
            return new OperaDriver(capabilities);
        }
        return new OperaDriver();
    }
    
    private static RemoteWebDriver getSafariDriver(DesiredCapabilities capabilities) {
        if (capabilities != null) {
            return new SafariDriver(capabilities);
        }
        return new SafariDriver();
    }
    
    private static RemoteWebDriver getSelendroidDriver() {
        SelendroidCapabilities sc = new SelendroidCapabilities();
        sc.setBrowserName(androidBrowserName);
        sc.setSerial(androidEmuSid);
        sc.setPlatform(Platform.ANDROID);
        try {
            return new SelendroidDriver(new URL(String.format("http://%1$s:%2$s/wd/hub", androidHost, androidPort)), sc);
        } catch (Exception e) {
            logger.info(CLS_NAME + ".getSelendroidDriver", e);
            return null;
        }
    }

    private static RemoteWebDriver getAppiumDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Appium");
        caps.setCapability(MobileCapabilityType.BROWSER_NAME, androidBrowserName);
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName", "Motorola Razr"));
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
        caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, System.getProperty("androidVersion", "4.4"));
        caps.setCapability("name", "SMART Android Test");
        try {
            return new AndroidDriver(new URL(String.format("http://%1$s:4723/wd/hub", System.getProperty("appiumURL", LOCALHOST))), caps);
        } catch (Exception e) {
            logger.info(CLS_NAME + ".getAppiumDriver", e);
            return null;
        }
    }

    private static RemoteWebDriver getIosDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "Appium");
        caps.setCapability(MobileCapabilityType.BROWSER_NAME, "Safari");
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName", "iPhone 5"));
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
        caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, System.getProperty("iosVersion", "8.1"));
        caps.setCapability("name", "SMART iOS Test");
        caps.setCapability("nativeWebTap", "true");
        try {
            return new IOSDriver(new URL(String.format("http://%1$s:4723/wd/hub", System.getProperty("appiumURL", LOCALHOST))), caps);
        } catch (Exception e) {
            logger.info(CLS_NAME + ".getIosDriver", e);
            return null;
        }
    }
}
