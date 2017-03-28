package webdriver;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import webdriver.Browser.Browsers;
import webdriver.annotations.Bug;
import webdriver.annotations.Jira;
import webdriver.stats.PageStats;
import webdriver.utils.ExcelUtils;
import webdriver.utils.HttpUtils;
import webdriver.utils.zephyrConnector.Reporter;
import webdriver.utils.zephyrConnector.Session;
import webdriver.utils.zephyrConnector.ZephyrJiraException;
import webdriver.utils.zephyrConnector.ZephyrJiraReporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import static org.testng.AssertJUnit.assertTrue;

/**
 * BaseEntity
 */
public abstract class BaseEntity {

    public static final String ZEPHYR_CONNECTION_KEY = "project.zephyrconnection";
    private String testCycle = null;
    private String testIssue = null;
    static final String PROPERTY_KEY_PREFIX = "org.uncommons.reportng.";
    static final String GLOBAL_LOGSTEP_PART = PROPERTY_KEY_PREFIX + "global-logstep-part";
    static final String LOG_PAGE_LOAD_TIME = PROPERTY_KEY_PREFIX + "logPageLoadTime";
    static final String SEP = File.separator;
    private Reporter reporter;
    private boolean newClass = false;
    protected static int stepNumber = 1;
    protected static Logger logger = Logger.getInstance();
    private static String PROPERTIES_FILE = "selenium.properties";
    protected static PropertiesResourceManager props = new PropertiesResourceManager(PROPERTIES_FILE);
    private static final String ROOT_DIR = System.getProperty("basedir", System.getProperty("user.dir"));
    private boolean shouldRewriteStatFile = true;

    private static final String PASS = "1";
    private static final String FAIL = "2";

    public Browser getBrowser() {
        return Browser.getInstance();
    }

    /**
     * Get locale
     *
     * @param key key
     * @return value
     */
    protected static String getLoc(final String key) {
        return Logger.getLoc(key);
    }

    // ==============================================================================================
    // Methods for logging

    /**
     * Format message.
     *
     * @param message message
     * @return null
     */
    protected abstract String formatLogMsg(String message);

    /**
     * Message for debugging.
     *
     * @param message Message
     */
    protected final void debug(final String message) {
        logger.debug(String.format("[%1$s] %2$s", this.getClass().getSimpleName(), formatLogMsg(message)));
    }

    /**
     * Informative message.
     *
     * @param message Message
     */
    protected void info(final String message) {
        logger.info(formatLogMsg(message));
    }

    /**
     * Warning.
     *
     * @param message Message
     */
    protected void warn(final String message) {
        logger.warn(formatLogMsg(message));
    }

    /**
     * Error message without stopping the test.
     *
     * @param message Message
     */
    protected void error(final String message) {
        logger.error(formatLogMsg(message));
    }

    /**
     * Fatal error message.
     *
     * @param message Message
     */
    protected void fatal(final String message) {
        logger.fatal(formatLogMsg(message));
        assertTrue(formatLogMsg(message), false);
    }

    /**
     * Logging a step number.
     *
     * @param step - step number
     */
    public static void logStep(final int step) {
        logger.step(step);
    }

    /**
     * Logging a several steps in a one action
     *
     * @param fromStep - the first step number to be logged
     * @param toStep   - the last step number to be logged
     */
    public void logStep(final int fromStep, final int toStep) {
        logger.step(fromStep, toStep);
    }

    // ==============================================================================================
    // Asserts

    /**
     * Universal method
     *
     * @param isTrue  Condition
     * @param passMsg Positive message
     * @param failMsg Negative message
     */
    public void doAssert(final Boolean isTrue, final String passMsg,
                         final String failMsg) {
        if (isTrue) {
            info(passMsg);
        } else {
            fatal(failMsg);
        }
    }

    /**
     * Assert Objects are Equal
     *
     * @param expected Expected Value
     * @param actual   Actual Value
     */
    public void assertEquals(final Object expected, final Object actual) {
        if (!expected.equals(actual)) {
            fatal("Expected value: '" + expected + "', but was: '" + actual
                    + "'");
        }
    }


    /**
     * Assert Objects are Equal, doesn't fail the test
     *
     * @param passMessage Pass Message
     * @param failMessage Fail Message
     * @param expected    Expected Value
     * @param actual      Actual Value
     */
    public void assertEqualsNoFail(final String passMessage,
                                   final String failMessage, final Object expected, final Object actual) {
        if (expected.equals(actual)) {
            info(passMessage);
        } else {
            error(failMessage);
            error("Expected value: '" + expected + "', but was: '" + actual
                    + "'");
        }
    }

    /**
     * Assert Objects are Equal
     *
     * @param message  Fail Message
     * @param expected Expected Value
     * @param actual   Actual Value
     */
    public void assertEquals(final String message, final Object expected,
                             final Object actual) {
        if (!expected.equals(actual)) {
            fatal(message);
        }
    }

    /**
     * Assert Objects are Equal
     *
     * @param passMessage Pass Message
     * @param message     Fail Message
     * @param expected    Expected Value
     * @param actual      Actual Value
     */
    public void assertEquals(final String passMessage, final String message, final Object expected,
                             final Object actual) {
        if (!expected.equals(actual)) {
            fatal(message);
        } else info(passMessage);
    }

    /**
     * Assert current page's URL.
     *
     * @param url expected URL
     */
    public void assertCurrentURL(String url) {
        String actualUrl = getBrowser().getLocation();
        assertEquals(url, actualUrl);
        info("Page has corret URL.");
    }

    // ==============================================================================================
    //

    /**
     * screenshot.
     *
     * @param name Name of class
     * @return String path to screen
     */
    protected String makeScreen(final Class<? extends BaseEntity> name) {
        return makeScreen(name, true);
    }


    // ==============================================================================================
    //

    /**
     * screenshot.
     *
     * @param name           Name of class
     * @param additionalInfo additionalInfo
     * @return String path to screen
     */
    protected String makeScreen(final Class<? extends BaseEntity> name, final boolean additionalInfo) {
        String fileName = name.getPackage().getName() + "." + name.getSimpleName();
        String pageSourcePath = String.format("surefire-reports" + File.separator + "html" +
                File.separator + "Screenshots/%1$s.txt", fileName);
        String screenshotPath = String.format("surefire-reports" + File.separator + "html" +
                File.separator + "Screenshots/%1$s.png", fileName);

        try {
            String pageSource = getBrowser().getDriver().getPageSource();
            FileUtils.writeStringToFile(new File(pageSourcePath), pageSource);
        } catch (Exception e1) {
            logger.debug(this, e1);
            warn("Failed to save page source");
        }
        try {
            File screen = ((TakesScreenshot) getBrowser().getDriver()).getScreenshotAs(OutputType.FILE);
            File addedNewFile = new File(screenshotPath);
            FileUtils.copyFile(screen, addedNewFile);
        } catch (Exception e) {
            logger.debug(this, e);
            warn("Failed to save screenshot");
        }

        if (additionalInfo) {
            String formattedName = String.format(
                    "<a href='Screenshots/%1$s.png'>ScreenShot</a>", fileName);
            String formattedNamePageSource = String.format(
                    "<a href='Screenshots/%1$s.txt'>Page Source</a>", fileName);
            logger.info(formattedName);
            logger.info(formattedNamePageSource);
            logger.printDots(formattedName.length());
        }

        return new File(screenshotPath).getAbsolutePath();
    }

    /**
     * killing process by Image name
     */
    public void checkAndKill() {
        logger.info("killing processes");
        try {
            String line;
            Process p = Runtime.getRuntime().exec(
                    String.format("taskkill /IM %1$s.exe /F",
                            Browser.currentBrowser.toString()));
            BufferedReader input = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            while ((line = input.readLine()) != null) {
                logger.info(line);
            }
            input.close();
        } catch (Exception err) {
            logger.info(this, err);
        }
    }

    /**
     * Only for IE
     */
    public void acceptUnsignedSertificate() {
        if (Browser.currentBrowser == Browsers.IEXPLORE) {
            getBrowser().navigate("javascript:document.getElementById('overridelink').click()");
        }
    }

    /**
     * Before Class method
     * Make a browser window
     * Create Zephyr test cycle
     */
    @BeforeClass
    public void before() {
        Browser browser = getBrowser();
        browser.windowMaximise();
        browser.navigate(browser.getStartBrowserURL());
        stepNumber = 1;
        if (Browser.getAnalyzeTraffic()) {
            browser.startAnalyzeTraffic();
        }
        if ("true".equalsIgnoreCase(System.getProperty(ZEPHYR_CONNECTION_KEY, "false"))) {
            try {
                reporter = new ZephyrJiraReporter();
                newClass = true;
                if (reporter.report()) {
                    reporter.createTestCycle();
                }
            } catch (Exception e) {
                logger.debug(this, e);
                logger.error(e.getMessage());
            }
        }

    }


    /**
     * Close browser after each test Class
     * And update Jira-Zephyr test cycle
     */
    @AfterClass
    public void after() {
        ProxyServ.stopProxyServer();
        if (getBrowser().isBrowserAlive()) {
            if ("true".equalsIgnoreCase(System.getProperty(ZEPHYR_CONNECTION_KEY, "false"))) {
                updateTestCycle();
            }
            getBrowser().exit();
        }
    }

    private void updateTestCycle() {
        try {
            if (reporter.report()) {
                reporter.updateTestCycle();
            }
        } catch (Exception e) {
            logger.debug(this, e);
            logger.error(e.getMessage());
        }
    }

    /**
     * Logging steps
     */
    protected void LogStep() {
        logStep(stepNumber++);
    }

    /**
     * Log message comes from \testcases\*.xlsx file.
     * Excel file xlsx or xls comes from system property "logexcelpart"
     * first(A) column used for row numbers,
     * second(B) used for test-cases messages.
     * If file for test is not exists, it would be created
     *
     * @deprecated As of release 1.4.1, replaced by {@link #logExcel(int step)} because of java conventions
     * If you want to make your LogSteps collapsing, just make
     * -Dorg.uncommons.reportng.global-logstep-part=your log step part, like "----=="
     */
    @Deprecated
    public void LogHeaderStep(int step) {
        logExcel(step);
    }

    /**
     * Log message comes from \testcases\*.xlsx file.
     * Excel file xlsx or xls comes from system property "logexcelpart"
     * first(A) column used for row numbers,
     * second(B) used for test-cases messages.
     * If file for test is not exists, it would be created
     *
     * @deprecated As of release 1.4.1, replaced by {@link #logExcel()} because of java conventions
     */
    @Deprecated
    public void LogHeaderStep() {
        logExcel(stepNumber++);
    }

    /**
     * Log message comes from \testcases\packagename*.xlsx file.
     * Excel file xlsx or xls comes from system property "logexcelpart"
     * first(A) column used for row numbers,
     * second(B) used for test-cases messages.
     * If file for test is not exists, it would be created
     */
    public void logExcel(int step) {
        Class<?> currentClass = this.getClass();
        String prePath = ROOT_DIR + "/src/test/resources/testcases";
        boolean folderCreated = new File(prePath + SEP + currentClass.getPackage()).mkdirs();
        if (folderCreated) {
            logger.info("Folder " + prePath + SEP + currentClass.getPackage() + " created");
        }
        File excelLogFile = new File(prePath + SEP + currentClass.getPackage() + SEP + currentClass.getSimpleName() + "." + System.getProperty("logexcelpart", "xlsx"));


        if (!excelLogFile.exists()) {
            ExcelUtils newFile;
            if ("xlsx".equals(System.getProperty("logexcelpart", "xlsx"))) {
                newFile = new ExcelUtils(ExcelUtils.FileFormat.NEW);
            } else {
                newFile = new ExcelUtils(ExcelUtils.FileFormat.OLD);
            }
            newFile.addSheet("Test");
            newFile.addCell("Test", "A1", "1 Fill this column with step numbers");
            newFile.addCell("Test", "B1", "Fill this column with your steps");
            newFile.save(excelLogFile.getAbsolutePath());
            logger.info(excelLogFile.getAbsolutePath() + " Created from default template");
        }

        ExcelUtils eu = new ExcelUtils(excelLogFile.getAbsolutePath());
        String stepMessage = eu.getCellValue(0, "B" + step);
        StringBuilder logstepPartStringBuilder = new StringBuilder(System.getProperty(GLOBAL_LOGSTEP_PART));
        logger.info(String.format("%1$s[ %2$s: %3$s ]%4$s", System.getProperty(GLOBAL_LOGSTEP_PART), step, stepMessage, logstepPartStringBuilder.reverse()));
        logger.info(stepMessage);
    }

    /**
     * Log message comes from \testcases\packagename*.xlsx file.
     * Excel file xlsx or xls comes from system property "logexcelpart"
     * first(A) column used for row numbers,
     * second(B) used for test-cases messages.
     * If file for test is not exists, it would be created
     */
    public void logExcel() {
        logExcel(stepNumber++);
    }

    /**
     * Logging steps with info
     */
    protected void LogStep(final String info) {
        logStep(stepNumber++);
        logger.info(String.format("----==[ %1$s ]==----", info));
    }

    /**
     * Change test status in zephyr to fail
     *
     * @throws Exception
     */
    public void zephyrTestFail() throws ZephyrJiraException {
        if ("true".equalsIgnoreCase(System.getProperty(ZEPHYR_CONNECTION_KEY, "false")) && reporter.report()) {
            reporter.updateExecutionRecord(FAIL);
        }
    }

    /**
     * Change zephyr test status to pass
     */
    public void zephyrTestPass() {
        if ("true".equalsIgnoreCase(System.getProperty(ZEPHYR_CONNECTION_KEY, "false"))) {
            try {
                if (reporter.report()) {
                    reporter.updateExecutionRecord(PASS);
                }
            } catch (Exception e) {
                logger.debug(this, e);
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Start zephyr test execution
     *
     * @throws Exception
     */
    public void zephyrExecution() throws ZephyrJiraException {
        if ("true".equalsIgnoreCase(System.getProperty(ZEPHYR_CONNECTION_KEY, "false")) && reporter.report()) {
            if (newClass) {
                beforeTC();
                reporter.manageTestCycle(testCycle);
                newClass = false;
            }
            reporter.createExecutionRecord(testIssue);
        }
    }

    /**
     * Save test name from class name
     */
    public void beforeTC() {
        testCycle = Session.props.getProperty("testcycle");
        testIssue = this.getClass().getSimpleName().replace("_", " ");
    }

    /**
     * Check if assigned jira issue is already fixed
     *
     * @param clazz
     */
    protected void checkJiraIssue(AnnotatedElement clazz) {
        Annotation[] annotations = clazz.getDeclaredAnnotations();
        if (annotations.length > 0) {
            analyzeJiraAnnotations(annotations);
        }
    }

    /**
     * Analyze Jira annotations*
     * @param annotations annotations
     * TODO Refactor is one class can contains more than one @Jira? If not - remove foreach and replace single Jira annotation processing. Also refactor referenced code
     */
    private void analyzeJiraAnnotations(Annotation[] annotations) {
        String url = "", issue = "", login = "", password = "";
        for (Annotation annotation : annotations) {
            if (annotation instanceof Jira) {
                try {
                    Jira jiraAnn = (Jira) annotation;
                    url = jiraAnn.url().isEmpty() ? Browser.getJiraUrl() : jiraAnn.url();
                    if (url.isEmpty()) {
                        throw new SkipException("Jira URL is not defined");
                    }
                    issue = jiraAnn.issue();
                    login = jiraAnn.login().isEmpty() ? Browser.getJiraLogin() : jiraAnn.login();
                    password = jiraAnn.password().isEmpty() ? Browser.getJiraPassword() : jiraAnn.password();
                    if (login.isEmpty() || password.isEmpty()) {
                        warn("Jira login or password is not defined");
                    }
                    HttpUtils http = new HttpUtils();
                    http.executeGetRequest(String.format("%1$srest/gadget/latest/login?os_captcha=&os_password=%3$s&os_username=%2$s", url, login, password), true);
                    http.executeGetRequest(String.format("%1$srest/api/latest/issue/%2$s", url, issue), true);
                    String json = http.getResponse();
                    JSONObject jsonObj = new JSONObject(json);
                    String status = (String) jsonObj.getJSONObject("fields").getJSONObject("status").get("name");
                    if (!("resolved".equalsIgnoreCase(status) || "closed".equalsIgnoreCase(status))) {
                        throw new SkipException(String.format("Jira issue %1$s has status %2$s", issue, status));
                    }
                } catch (SkipException e) {
                    logger.debug(this, e);
                    warn(e.getMessage());
                    throw e;
                } catch (Exception e) {
                    logger.debug(this, e);
                    warn("Jira checking failed : " + e.getMessage());
                    warn("URL: " + String.format("%1$srest/api/latest/issue/%2$s", url, issue));
                    warn("Login: " + login);
                }
            }
        }
    }

    /**
     * Process {@link Bug} annotation if test marked by it.
     * Link to bug will be generated to report like  <a href="http://jira.address.ru/browse/PRJ-123" target="_blank">PRJ-123: Button not clickable</a>.
     * Link will be generated if jira url is specified in selenium.properties, property jiraUrl.
     * Otherwise text from {@link Bug#title()} will be printed to report.
     * Text will be printed as warning.
     */
    protected void processBugAnnotation() {
        String message = "Тест отмечен дефектом: ";
        Bug bug = this.getClass().getAnnotation(Bug.class);
        if (bug != null && !bug.title().isEmpty()) {
            StringBuilder resultLine = new StringBuilder();
            String bugTitle = bug.title();
            String jiraUrl = props.getProperty("jiraUrl", "");
            resultLine.append(message);
            if (jiraUrl.isEmpty()) {
                resultLine.append(bugTitle);
            } else {
                resultLine.append("<a href=\"")
                        .append(jiraUrl)
                        .append("/browse/")
                        .append(CommonFunctions.regexGetMatch(bugTitle, "[a-zA-Zа-яА-Я]+-\\d+").toUpperCase())
                        .append("\" target=\"_blank\">")
                        .append(bugTitle)
                        .append("</a>");
            }
            logger.warn(resultLine.toString());
        }
    }

    /**
     * Is load page time logging activated?
     *
     * @return True - if so, false - otherwise
     */
    protected boolean shouldLogPageLoadTime() {
        return "true".equals(System.getProperty(LOG_PAGE_LOAD_TIME, "false"));
    }

    /**
     * Log page load time to excel file
     */
    protected void logPageLoadStat() {
        PageStats pageStats = getBrowser().getPageLoadTime();
        String logFileName = this.getClass().getName().replaceAll("\\$", ".");
        String statDirPath = ROOT_DIR + "/target/stats/";
        String logFilePath = statDirPath + logFileName + ".xlsx";
        File statDir = new File(statDirPath);
        File logFile = new File(logFilePath);
        boolean isHeaderNeeded = shouldRewriteStatFile;
        ExcelUtils logExcelFile;
        if (!statDir.exists()) {
            statDir.mkdir();
        }
        if (!logFile.exists()) {
            shouldRewriteStatFile = false;
            logExcelFile = new ExcelUtils(ExcelUtils.FileFormat.NEW);
            logExcelFile.save(logFilePath);
        } else if (shouldRewriteStatFile) {
            shouldRewriteStatFile = false;
            logFile.delete();
            logExcelFile = new ExcelUtils(ExcelUtils.FileFormat.NEW);
            logExcelFile.save(logFilePath);
        } else {
            logExcelFile = new ExcelUtils(logFilePath);
        }
        if (isHeaderNeeded) {
            logExcelFile = new ExcelUtils(logFilePath);
            logExcelFile.addSheet("Stats");
            logExcelFile.addCell(0, "A1", "URL");
            logExcelFile.addCell(0, "B1", "Redirect Time, s");
            logExcelFile.addCell(0, "C1", "AppCache Time, s");
            logExcelFile.addCell(0, "D1", "DNS Time, s");
            logExcelFile.addCell(0, "E1", "TCP time, s");
            logExcelFile.addCell(0, "F1", "Request Time, s");
            logExcelFile.addCell(0, "G1", "Response Time, s");
            logExcelFile.addCell(0, "H1", "Processing Time, s");
            logExcelFile.addCell(0, "I1", "OnLoad Time, s");
            logExcelFile.addCell(0, "J1", "Total Time, s");
        }
        int currentRow = logExcelFile.getRowsCount(0);
        logExcelFile.addCell(0, currentRow, 0, getBrowser().getLocation());
        logExcelFile.addCell(0, currentRow, 1, String.valueOf(pageStats.getRedirectTime()));
        logExcelFile.addCell(0, currentRow, 2, String.valueOf(pageStats.getAppCacheTime()));
        logExcelFile.addCell(0, currentRow, 3, String.valueOf(pageStats.getDnsTime()));
        logExcelFile.addCell(0, currentRow, 4, String.valueOf(pageStats.getTcpTime()));
        logExcelFile.addCell(0, currentRow, 5, String.valueOf(pageStats.getRequestTime()));
        logExcelFile.addCell(0, currentRow, 6, String.valueOf(pageStats.getResponseTime()));
        logExcelFile.addCell(0, currentRow, 7, String.valueOf(pageStats.getProcessingTime()));
        logExcelFile.addCell(0, currentRow, 8, String.valueOf(pageStats.getOnLoadTime()));
        logExcelFile.addCell(0, currentRow, 9, String.valueOf(pageStats.getTotalTime()));
        logExcelFile.save(logFilePath);
    }
}
