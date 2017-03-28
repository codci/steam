package webdriver;

import org.testng.annotations.Test;

/**
 * An abstract class that describes the basic test application contains
 * methods for logging and field test settings (options)
 */
public abstract class BaseTest extends BaseEntity implements IAnalys {


    /**
     * To override.
     */
    public abstract void runTest();

    /**
     * Test
     *
     * @throws Throwable Throwable
     */
    @Test
    public void xTest() throws Throwable {
        Class<? extends BaseTest> currentClass = this.getClass();
        checkJiraIssue(currentClass); //not an jira extended method
        checkJiraIssue(currentClass.getMethod("runTest", null)); //not an jira extended method
        try {
            processBugAnnotation();
            logger.logTestName(currentClass.getName());
            zephyrExecution();
            runTest();
            logger.logTestEnd(currentClass.getName());
            zephyrTestPass();
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            if (shouldAnalys()) {
                invokeAnalys(e, getBrowser().getDriver().getPageSource());
            } else {
                zephyrTestFail();
                logger.warn("");
                logger.warnRed(getLoc("loc.test.failed"));
                logger.warn("");
                throw e;
            }
        } finally {
            if (Browser.getAnalyzeTraffic()) {
                getBrowser().assertAnalyzedTrafficResponseCode();
                getBrowser().assertAnalyzedTrafficLoadTime();
                ProxyServ.saveToFile(this.getClass().getName());
            }
            makeScreen(currentClass);
        }
    }


    /**
     * Format logging
     *
     * @param message Message
     * @return Message
     */
    @Override
    protected String formatLogMsg(final String message) {
        return message;
    }

    @Override
    public boolean shouldAnalys() {
        return false;
    }

    @Override
    public void invokeAnalys(final Throwable exc, final String bodyText) {
        // not necessary yet
    }
}
