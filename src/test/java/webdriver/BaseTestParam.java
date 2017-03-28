package webdriver;

/**
 * An abstract class that describes the basic test
 * application contains methods for logging and field test settings (options)
 */
public abstract class BaseTestParam extends BaseEntity implements IAnalys {

    /**
     * To override
     */
    public abstract void runTest();

    /**
     * Test
     *
     * @throws Throwable Throwable
     */
    public void xTest() throws Throwable {
        Class<? extends BaseTestParam> currentClass = this.getClass();
        checkJiraIssue(currentClass);
        checkJiraIssue(currentClass.getMethod("runTest", null));
        try {
            processBugAnnotation();
            logger.logTestName(currentClass.getName());
            runTest();
            logger.logTestEnd(currentClass.getName());
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            if (shouldAnalys()) {
                invokeAnalys(e, Browser.getDriver().getPageSource());
            } else {
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
     * formats LogMsg
     *
     * @param message Message
     * @return formatted message
     */
    protected String formatLogMsg(final String message) {
        return message;
    }

}
