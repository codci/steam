package webdriver;

import net.jsourcerer.webdriver.jserrorcollector.JavaScriptError;
import net.sourceforge.htmlunit.corejs.javascript.JavaScriptException;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import webdriver.elements.BaseElement;
import webdriver.stage.StageController;
import webdriver.stats.PageStats;
import webdriver.utils.HttpUtils;
import webdriver.waitings.SmartWait;

import javax.naming.NamingException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static webdriver.Logger.getLoc;

/**
 * The main class to access the browser, which extends the capabilities of the standard Webdriver
 */
public final class Browser {

	private static final long SLEEP_SWITCH = 30000L;
	private static final long SLEEP_THREAD_SLEEP = 250;
	private static final long IMPLICITY_WAIT = 10;
	private static final String DEFAULT_CONDITION_TIMEOUT = "defaultConditionTimeout";
	private static final String DEFAULT_PAGE_LOAD_TIMEOUT = "defaultPageLoadTimeout";
	private static final String DEFAULT_SIKULI_TIMEOUT = "defaultSikuliTimeout";
	private static final String DEFAULT_SIKULI_SIMILARITY = "defaultSikuliSimilarity";
	private static final String URL_LOGIN_PAGE = "urlLoginPage";
	private static final String DETECT_JS_ERRORS = "detectJsErrors";
	private static final String TROUBLE_SHOOTING = "troubleShooting";
	private static final String ANALYZE_TRAFFIC = "analyzeTraffic";
	private static final String ANALYZE_TIME_LIMIT_LOADING_RESOURCE = "analizeTimeLimitLoadingResource";
	private static final String JIRA_URL = "jiraUrl";
	private static final String JIRA_LOGIN = "jiraLogin";
	private static final String JIRA_PASSWORD = "jiraPassword";
	private static final String IE_LOCAL_RUN = "localrun";
	private static final String WITHOUT_FLASH = "withoutFlash";
    private static final Logger logger = Logger.getInstance();

	// имя файла с настройками Selenium
	/**
	 * speedValue=100 defaultPageLoadTimeout=60 defaultConditionTimeout=180
	 * urlLoginPage=http://opensesame:%23pears0n@dev.pearsoninnovationlabs.com/ #overrided in
	 * Browser.initProperties-default=firefox; #if null - use argument 'browser' to JVM; if other value (without '*'),
	 * #will use it as browserStartCommand #Usage: #firefox #iexplore #chrome #null browser=iexplore
	 */
	static final String PROPERTIES_FILE = "selenium.properties";
	private static final String BROWSER_BY_DEFAULT = "firefox";
	private static final String BROWSER_PROP = "browser";
	private static final String STAGE = "stage";
	private static final String STAGE_A = "stage_A";

	// browsers
	private static Browser instance;
	private static ThreadLocal<RemoteWebDriver> driverHolder;
	private static final PropertiesResourceManager props = new PropertiesResourceManager(PROPERTIES_FILE);

	// поля класса
	private static String browserURL;
	private static String timeoutForPageLoad;
	private static String timeoutForCondition;
	private static String timeoutForSikuli;
	private static String similarityForSikuli;
	private static String detectJsErrors;
	private static String troubleShooting;
	private static String analyzeTraffic;
	private static String jiraUrl;
	private static String analizeTimeLimitLoadingResource;
	private static String jiraLogin;
	private static String jiraPassword;
	private static String ieLocalRun;
	private static String withoutFlash;

	public static final Browsers currentBrowser =
            Browsers.valueOf(System.getProperty(BROWSER_PROP, props.getProperty(BROWSER_PROP, BROWSER_BY_DEFAULT)).toUpperCase());

	public static final String stageProperty = System.getProperty(STAGE, STAGE_A);
	/**
	 * needed if using two stages
	 */
	public static final StageController stageController = new StageController();
	private static HttpUtils httpUtils;
    static boolean useCommonDriver = false;

    /**
     * get RemoteWebDriver
     * @return driver
     */
    static RemoteWebDriver commonDriverHolder = null;

	/**
	 * Private constructor (singleton pattern)
	 */
	private Browser() {
		Logger.getInstance().info(String.format(getLoc("loc.browser.ready"), currentBrowser.toString()));
	}

	/**
	 * Checks is Browser slive
	 * @return true\false
	 */
	public boolean isBrowserAlive() {
		return driverHolder.get() != null;
	}

	/**
	 * TimeoutForSikuli
	 * @return getTimeoutForSikuli
	 */
	public static String getTimeoutForSikuli() {
		return timeoutForSikuli;
	}

	/**
	 * DetectJSErrors
	 * @return getDetectJsErrors
	 */
	public static Boolean getDetectJsErrors() {
		return "true".equalsIgnoreCase(detectJsErrors);
	}

	/**
	 * DetectJSErrors
	 * @return getTroubleShooting
	 */
	public static Boolean getTroubleShooting() {
		return "true".equalsIgnoreCase(troubleShooting);
	}
	
	/**
	 * getTroubleShooting
	 * @return getTroubleShooting
	 */
	public static Boolean getAnalyzeTraffic() {
		return "true".equalsIgnoreCase(analyzeTraffic);
	}
	
	/**
	 * Get Jira Url
	 * @return getJiraUrl
	 */
	public static String getJiraUrl() {
		return jiraUrl;
	}
	
	/**
	 * Get Jira Url
	 * @return getJiraUrl
	 */
	public static String getJiraLogin() {
		return jiraLogin;
	}
	
	/**
	 * Get Jira Url
	 * @return getJiraUrl
	 */
	public static String getJiraPassword() {
		return jiraPassword;
	}
	
	/**
	 * SimilarityForSikuli
	 * @return SimilarityForSikuli
	 */
	public static String getSimilarityForSikuli() {
		return similarityForSikuli;
	}

	/**
	 * Gets instance of Browser
	 * @return browser instance
	 */
	synchronized public static Browser getInstance() {
		if (instance == null) {
			initProperties();
			instance = new Browser();
		}
		return instance;
	}

	/**
	 * The implementation of the browser is closed
	 * <p>
	 * see {@link BaseEntity#checkAndKill()} all browser processes will be killed
	 * <p>
	 * void after test
	 */
	public void exit() {
		try {
			getDriver().quit();
			Logger.getInstance().info(getLoc("loc.browser.driver.qiut"));
		} catch (Exception e) {
			logger.info(this, e);
		} finally {
			driverHolder.set(null);
		}
	}

	/**
	 * gets TimeoutForCondition
	 * @return timeoutForCondition
	 */
	public static String getTimeoutForCondition() {
		return timeoutForCondition;
	}

	/**
	 * gets TimeoutForPageLoad
	 * @return timeoutForPageLoad
	 */
	public String getTimeoutForPageLoad() {
		return timeoutForPageLoad;
	}

	/**
	 * gets StartBrowserURL
	 * @return browserURL
	 */
	public String getStartBrowserURL() {
		return browserURL;
	}

	/**
	 * init
	 */
	private static void initProperties() {
		timeoutForPageLoad = props.getProperty(DEFAULT_PAGE_LOAD_TIMEOUT);
		timeoutForCondition = props.getProperty(DEFAULT_CONDITION_TIMEOUT);
		timeoutForSikuli = props.getProperty(DEFAULT_SIKULI_TIMEOUT, "10000");
		similarityForSikuli = props.getProperty(DEFAULT_SIKULI_SIMILARITY, "0.9");
		PropertiesResourceManager propUrls = new PropertiesResourceManager("stage.properties");
		String choosenStage = propUrls.getProperty(stageProperty);
		browserURL = String.format(propUrls.getProperty(URL_LOGIN_PAGE), choosenStage);
		detectJsErrors = props.getProperty(DETECT_JS_ERRORS, "false");
		troubleShooting = props.getProperty(TROUBLE_SHOOTING, "false");
		analyzeTraffic = props.getProperty(ANALYZE_TRAFFIC, "false");
		analizeTimeLimitLoadingResource = props.getProperty(ANALYZE_TIME_LIMIT_LOADING_RESOURCE, "2000");
		jiraUrl = props.getProperty(JIRA_URL, "");
		jiraLogin = props.getProperty(JIRA_LOGIN, "");
		jiraPassword = props.getProperty(JIRA_PASSWORD, "");
		ieLocalRun = props.getProperty(IE_LOCAL_RUN, "");
		withoutFlash = props.getProperty(WITHOUT_FLASH, "false");
		// setting to stage controller
		stageController.setStage(webdriver.stage.Stages.valueOf(stageProperty.toUpperCase().trim()));

		//init
		driverHolder = new ThreadLocal<RemoteWebDriver>(){
			@Override
			protected RemoteWebDriver initialValue() {
				return Browser.getNewDriver();
			}
		};
	}

	private static RemoteWebDriver getNewDriver() {
		try {
			RemoteWebDriver driver = BrowserFactory.setUp(currentBrowser.toString());
			driver.manage().timeouts().implicitlyWait(IMPLICITY_WAIT, TimeUnit.SECONDS);
			Logger.getInstance().info(getLoc("loc.browser.constructed"));
			return driver;
		} catch (NamingException e) {
			logger.debug("Browser.getNewDriver", e);
		}
		return null;
	}

	/**
	 * wait the download page (on Javascript readyState)
	 */
	public void waitForPageToLoad() {
		logger.info("Waiting for page to load");
        ExpectedCondition<Boolean> condition = new ExpectedCondition<Boolean>() {
            public Boolean apply(final WebDriver d) {
                if (!(d instanceof JavascriptExecutor)) {
                    return true;
                }
                Object result = ((JavascriptExecutor) d)
                        .executeScript("return document['readyState'] ? 'complete' == document.readyState : true");
                return result != null && result instanceof Boolean && (Boolean) result;
            }
        };
        boolean isLoaded = SmartWait.waitForTrue(condition, Long.parseLong(getTimeoutForPageLoad()));
        if (!isLoaded) {
            logger.warn(getLoc("loc.browser.page.timeout"));
        }
	}

	/**
	 * waiting, while number of open windows will be more than previous
	 * @param prevWndCount - number of previous
	 */
	public void waitForNewWindow(final int prevWndCount) {
        ExpectedCondition<Boolean> condition =  new ExpectedCondition<Boolean>() {
            public Boolean apply(final WebDriver d) {
                return d.getWindowHandles().size() > prevWndCount;
            }
        };
        boolean isSuccessWait = SmartWait.waitForTrue(condition);
        if (!isSuccessWait) {
            Assert.assertTrue(getLoc("loc.browser.newwindow.notappear"), false);
        }
	}

	/**
	 * Select the previous window (the list of handlers)
	 */
	public void selectPreviousWindow() {
		Object[] handles = getDriver().getWindowHandles().toArray();
		String handle = getDriver().getWindowHandle();
		Assert.assertTrue(getLoc("loc.browser.windows.count.small"), handles.length > 1);
		for (int i = 1; i < handles.length; i++) {
			if (handles[i].equals(handle)) {
				getDriver().switchTo().window((String) handles[i - 1]);
				return;
			}
		}
		getDriver().switchTo().window((String) handles[handles.length - 2]);

	}

	/**
	 * Select the first window (the list of handlers)
	 */
	public void selectFirstWindow() {
		Object[] handles = getDriver().getWindowHandles().toArray();
		getDriver().switchTo().window((String) handles[0]);

	}

	/**
	 * We expect the emergence of the new window and select it
	 */
	public void selectNewWindow() {
		Object[] handles = getDriver().getWindowHandles().toArray();
		waitForNewWindow(handles.length);
		handles = getDriver().getWindowHandles().toArray();
		getDriver().switchTo().window((String) handles[handles.length - 1]);

	}

	/**
	 * Select the last window (the list of handlers)
	 */
	public void selectLastWindow() {
		Object[] handles = getDriver().getWindowHandles().toArray();
		getDriver().switchTo().window((String) handles[handles.length - 1]);

	}

	/**
	 * Go to the home page
	 */
	public void openStartPage() {
		getDriver().navigate().to(props.getProperty(getStartBrowserURL()));
	}

	/**
	 * maximizes the window
	 * <p>
	 * works on IE7, IE8, IE9, FF 3.6
	 */
	public void windowMaximise() {
		try {
			getDriver().executeScript("if (window.screen) {window.moveTo(0, 0);window.resizeTo(window.screen.availWidth,window.screen.availHeight);};");
			getDriver().manage().window().maximize();
		} catch (Exception e) {
            logger.debug(e);
			//A lot of browsers crash here
		}

	}

	/**
	 * Navgates to the Url
	 * @param url Url
	 */
	public void navigate(final String url) {
		getDriver().navigate().to(url);
	}

	/**
	 * Refresh page.
	 */
	public void refresh() {
		getDriver().navigate().refresh();
		Logger.getInstance().info("Page was refreshed.");
	}

	public static RemoteWebDriver getDriver() {
		if(useCommonDriver && commonDriverHolder!= null){
			return commonDriverHolder; 
		}
		if(driverHolder.get()==null){
			driverHolder.set(getNewDriver());
		}
		commonDriverHolder = driverHolder.get();
        return commonDriverHolder;
	}

	/**
	 * Open new window by hot keys Ctrl + N, webdriver switch focus on it.
	 */
	public void openNewWindow() {
		getDriver().findElement(By.xpath("//body")).sendKeys(Keys.CONTROL, "n");
		Object[] headers = getDriver().getWindowHandles().toArray();
		getDriver().switchTo().window(headers[headers.length - 1].toString());
	}

	/**
	 * click and switch to new window. (works on IE also)
	 * @param element element
	 */
	public void clickAndSwitchToNewWindow(final BaseElement element) {
		Set<String> existingHandles = getDriver().getWindowHandles();
		element.click();

		String foundHandle = null;
		long endTime = System.currentTimeMillis() + SLEEP_SWITCH;
		while (foundHandle == null && System.currentTimeMillis() < endTime) {
			Set<String> currentHandles = getDriver().getWindowHandles();
			foundHandle = getNewWindowHandle(existingHandles, currentHandles);
			if (foundHandle == null) {
				try {
					Thread.sleep(SLEEP_THREAD_SLEEP);
				} catch (InterruptedException e) {
                    logger.debug(this, e);
					logger.fatal("new window not found");
				}
			}
		}

		if (foundHandle != null) {
			getDriver().switchTo().window(foundHandle);
		} else {
			logger.fatal("new window not found");
		}
	}

    private String getNewWindowHandle(Set<String> previousHandles, Set<String> currentHandles) {
        if (currentHandles.size() != previousHandles.size()) {
            for (String currentHandle : currentHandles) {
                if (!previousHandles.contains(currentHandle)) {
                    logger.info("new window was found");
                    return currentHandle;
                }
            }
        }
        return null;
    }

	/**
	 * Trigger
	 * @param script script
	 * @param element element
	 */
	public void trigger(final String script, final WebElement element) {
		((JavascriptExecutor) getDriver()).executeScript(script, element);
	}

	/**
	 * Executes a script
	 * @note Really should only be used when the web driver is sucking at exposing functionality natively
	 * @param script The script to execute
	 * @return Object
	 */
	public Object trigger(final String script) {
		return ((JavascriptExecutor) getDriver()).executeScript(script);
	}

	/**
	 * Opens a new tab for the given URL (doesn't work on IE)
	 * @param url The URL to
	 */
	public void openTab(final String url) {
		String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
		Object element = trigger(String.format(script, url));
		if (element instanceof WebElement) {
			WebElement anchor = (WebElement) element;
			anchor.click();
			trigger("var a=arguments[0];a.parentNode.removeChild(a);", anchor);
		} else {
			throw new JavaScriptException(element, "Unable to open tab", 1);
		}
	}

	/**
	 * Gets current URL
	 * @return current URL
	 */
	public String getLocation() {
		return getDriver().getCurrentUrl();
	}

	/**
	 * Executes Java Scipt
	 * @param script Java Script
	 */
	public void jsExecute(final String script) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript(script);
	}

	/**
	 * Browsers enumeration
	 */
	public enum Browsers {
		/**
		 * @uml.property name="fIREFOX"
		 * @uml.associationEnd
		 */
		FIREFOX("firefox"), /**
		 * @uml.property name="iEXPLORE"
		 * @uml.associationEnd
		 */
		IEXPLORE("iexplore"), /**
		 * @uml.property name="cHROME"
		 * @uml.associationEnd
		 */
		CHROME("chrome"), /** 
		 * @uml.property name="oPERA"
		 * @uml.associationEnd
		 */
		OPERA("opera"), /** 
		 * @uml.property name="sAFARI"
		 * @uml.associationEnd
		 */
		 SAFARI("safari"), /**
         * @uml.property name="aNDROID"
         * @uml.associationEnd
         */
        SELENDROID("selendroid"),
        /**
         * @uml.property name="Appium_Android"
         * @uml.associationEnd
         */
        APPIUM_ANDROID("appium_android"),
        /**
         * @uml.property name="IOS"
         * @uml.associationEnd
         */
        IOS("ios");
		
		private String value;

		/**
		 * Constructor
		 * @param values Value
		 */
		Browsers(final String values) {
			value = values;
		}

		/**
		 * Returns string value
		 * @return String value
		 */
        @Override
		public String toString() {
			return value;
		}
	}

	/**
	 * Assert Js errors are absent
	 */
	public void assertNoJsErrors() {
		if (!getDetectJsErrors()) {
			Logger.getInstance().warn("Configuration error!");
			Logger.getInstance().fatal("Property 'detectJsErrors' is equals to 'false'");
		}
		List<JavaScriptError> jsErrors = JavaScriptError.readErrors(getDriver()); 
		Logger.getInstance().info("Javascript errors absence assertion:"); 
		Boolean isFailed = printJsErrors(jsErrors);	
		if (isFailed) {
			Logger.getInstance().fatal("Javascript error on the page!");
		}
	}
	
	/**
	 * Verify Js errors are absent
	 */
	public void verifyNoJsErrors() {
		if (!getDetectJsErrors()) {
			Logger.getInstance().warn("Configuration error!");
			Logger.getInstance().warn("Property 'detectJsErrors' is equals to 'false'");
		}
		List<JavaScriptError> jsErrors = JavaScriptError.readErrors(getDriver()); 
		Logger.getInstance().info("Javascript errors absence verification:"); 
		Boolean isFailed = printJsErrors(jsErrors);	
		if (isFailed) {
			Logger.getInstance().warn("Javascript error on the page!");
		}
	}
	

	/**
	 * Print Js error list
	 * @param jsErrors
	 * @return
	 */
	private Boolean printJsErrors(List<JavaScriptError> jsErrors) {
		if (!jsErrors.isEmpty()) {
			for(int i = 0; i < jsErrors.size(); i++) { 
				Logger.getInstance().warn("--------------------- Error information -------------------");
				Logger.getInstance().warn("| Error message:\t" + ((JavaScriptError) jsErrors.get(i)).getErrorMessage()); 
				Logger.getInstance().warn("| line number:\t" + ((JavaScriptError) jsErrors.get(i)).getLineNumber()); 
				Logger.getInstance().warn("| Source name:\t" + ((JavaScriptError) jsErrors.get(i)).getSourceName()); 
				Logger.getInstance().warn("-------------------------------------------------------------");
				Logger.getInstance().info("");
				}
			return true;
		}
		return false;
	}

	/**
	 * Set cookies into a webdriver instanse.
	 * @param cookies list
	 */
	public void setCookies(List<Cookie> cookies) {
		for (Cookie cookie : cookies) {
			getDriver().manage().addCookie(cookie);
		}
	}

	/**
	 * Get All cookies collection.
	 * @return
	 */
	public Set<Cookie> getCookies() {
		return getDriver().manage().getCookies();
	}

	/**
	 * Get http utils instance.
	 * @return
	 */
	public HttpUtils getHttpUtils(String url) {
		if (httpUtils == null){
			httpUtils = new HttpUtils(url);
		}
		httpUtils.setCookies(getCookies());
		return httpUtils;
	}

	/**
	 * Execute Get request
	 * @param url
	 */
	public void executeGetRequest(String url) {
		getHttpUtils().executeGetRequest(url);
		setCookies(httpUtils.getCookies());
	}

	/**
	 * Execute Post request
	 * @param url
	 * @param parameters params in format user=admin&pass=123
	 */
	public void executePostRequest(String url,String parameters) {
		getHttpUtils().executePostRequest(url,parameters);
		setCookies(httpUtils.getCookies());
	}

	public static boolean getIeLocalRun() {
		return "true".equalsIgnoreCase(ieLocalRun);
	}

	/**
	 * Get Http util instanse
	 * @return
	 */
	public HttpUtils getHttpUtils() {
		return getHttpUtils(null);
	}
	
	/**
	 * Create new Har file and begin to listen the traffic
	 */
	public void startAnalyzeTraffic(){
		ProxyServ.startAnalyze();
	}

	/**
	 * Analyze Har file that was previously created and stop anylize
	 * @param harName name of file to output
	 */
	public void assertAnalyzedTraffic(String harName) {
		ProxyServ.assertAnalyzedTraffic(harName);
	}

	/**
	 * Assert response code are less then 400
	 */
	public void assertAnalyzedTrafficResponseCode() {
		ProxyServ.assertAnalyzedTrafficResponseCode();
	}

	/**
	 * Assert load time is less then X
	 * @param timeLimitInMiliSeconds
	 */
	public void assertAnalyzedTrafficLoadTime(long timeLimitInMiliSeconds) {
		ProxyServ.assertAnalyzedTrafficLoadTime(timeLimitInMiliSeconds);
	}

	/**
	 * Assert load time is less then X
	 */
	public void assertAnalyzedTrafficLoadTime() {
		ProxyServ.assertAnalyzedTrafficLoadTime(getAnalizeTimeLimitLoadingResource());
	}
	
	/**
	 * Analize Time Limit Loading Resource
	 * @return long
	 */
	private long getAnalizeTimeLimitLoadingResource() {
		return Long.parseLong(analizeTimeLimitLoadingResource);
	}

	/**
	 * Assert first buffer response time
	 * @param timeLimitInMiliSeconds
	 */
	public void assertAnalyzedTrafficFirstBufferResponse(int timeLimitInMiliSeconds) {
		ProxyServ.assertAnalyzedTrafficFirstBufferResponse(timeLimitInMiliSeconds);
	}

    public static boolean isWithoutFlash() {
        return "true".equalsIgnoreCase(withoutFlash);
    }

    /**
     * Get time of page load and put it to object
     * @return {@link PageStats} object with timing measurement
     */
    public PageStats getPageLoadTime() {
        PageStats pageStats = new PageStats();
        pageStats.setRedirectTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.redirectEnd - window.performance.timing.redirectStart"
                        ).toString()) / 1000
        );
        pageStats.setAppCacheTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.domainLookupStart - window.performance.timing.fetchStart"
                        ).toString()) / 1000
        );
        pageStats.setDnsTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.domainLookupEnd - window.performance.timing.domainLookupStart"
                        ).toString()) / 1000
        );
        pageStats.setTcpTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.connectEnd - window.performance.timing.connectStart"
                        ).toString()) / 1000
        );
        pageStats.setRequestTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.responseStart - window.performance.timing.requestStart"
                        ).toString()) / 1000
        );
        pageStats.setResponseTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.responseEnd - window.performance.timing.responseStart"
                        ).toString()) / 1000
        );
        pageStats.setProcessingTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.domComplete - window.performance.timing.domLoading"
                        ).toString()) / 1000
        );
        pageStats.setOnLoadTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.loadEventEnd - window.performance.timing.loadEventStart"
                        ).toString()) / 1000
        );
        pageStats.setTotalTime(Double.valueOf(this
                        .getDriver()
                        .executeScript(
                                "return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart"
                        ).toString()) / 1000
        );
        return pageStats;
    }

	public static void useCommonDriver(boolean use) {
		useCommonDriver = use;
	}

	public static void setTimeoutForCondition(String timeoutForCondition) {
		Browser.timeoutForCondition=timeoutForCondition;		
	}
}
