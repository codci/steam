package webdriver.listeners;

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.OutputType;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import webdriver.*;
import webdriver.annotations.Author;
import webdriver.annotations.DevTime;
import webdriver.utils.rest.RequestMethod;
import webdriver.utils.rest.RestClient;
import webdriver.utils.rest.RestParamList;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by p.ordenko on 15.05.2015, 17:45.
 */
public class ReportingApiListener extends BaseEntity implements ITestListener {

    private static final String API_URL = props.getProperty("urApiUrl");
    private static final boolean IS_ENABLED = "true".equalsIgnoreCase(props.getProperty("urEnabled")) && System.getProperty("basedir") != null;
    private static final Logger LOGGER = Logger.getInstance();
    private static final String SID = CommonFunctions.getTimestamp();
    private static final String PROJECT_NAME = System.getProperty("org.uncommons.reportng.title", "Unknown");
    private static final Base64 BASE_64 = new Base64();
    private long tmpTestId = -1L;
    private RestParamList params = new RestParamList();

    @Override
    protected String formatLogMsg(String message) {
        return "";
    }

    private enum ApiMethod {
        TEST_PUT("/api/test/put", RequestMethod.POST),
        TEST_LOGS("/api/test/put/log", RequestMethod.POST),
        TEST_ATTACHMENTS("/api/test/put/attachment", RequestMethod.POST),
        TEST_AUTHOR("/api/test/update/author", RequestMethod.POST),
        TEST_DEV_INFO("/api/test/update/devInfo", RequestMethod.POST),
        TEST_UPDATE("/api/test/update", RequestMethod.POST);

        private String apiMethodPath;
        private transient RequestMethod requestMethod;

        ApiMethod(String apiMethodPath, RequestMethod requestMethod) {
            this.apiMethodPath = apiMethodPath;
            this.requestMethod = requestMethod;
        }

        public String getApiMethodPath() {
            return apiMethodPath;
        }

        public RequestMethod getRequestMethod() {
            return requestMethod;
        }

        public String getApiMethodUrl() {
            return API_URL + apiMethodPath;
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeObject(apiMethodPath);
            stream.writeObject(requestMethod);
        }

        private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
            apiMethodPath = (String) stream.readObject();
            requestMethod = (RequestMethod) stream.readObject();
        }
    }

    private enum Status {
        PASSED, FAILED, SKIPPED
    }

    @Override
    public synchronized void onTestStart(ITestResult result) {
        postTestInfo(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        updateStatusAndLogs(Status.PASSED, result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        updateStatusAndLogs(Status.FAILED, result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        updateStatusAndLogs(Status.SKIPPED, result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // not necessary now
    }

    @Override
    public void onStart(ITestContext context) {
        // not necessary now
    }

    @Override
    public void onFinish(ITestContext context) {
        // not necessary now
    }

    //////////////////
    // Private methods
    //////////////////

    private RestClient newRestClient(ApiMethod method, RestParamList params) {
        return new RestClient(method.getApiMethodUrl(), method.getRequestMethod(), params);
    }

    @SuppressWarnings("all")
    private void updateStatusAndLogs(Status status, ITestResult result) {
        if (IS_ENABLED && tmpTestId > 0) {
            // Status
            params.add("testId", String.valueOf(tmpTestId))
                    .add("status", status.toString()).build();
            newRestClient(ApiMethod.TEST_UPDATE, params).doRequest();

            // Logs
            List<String> logs = Reporter.getOutput(result);
            StringBuilder logsSb = new StringBuilder();
            for (String oneLog : logs) {
                logsSb.append(oneLog).append("\n");
            }
            params.add("testId", String.valueOf(tmpTestId))
                    .add("content", logsSb.toString()).build();
            newRestClient(ApiMethod.TEST_LOGS, params).doRequest();
            if (!result.isSuccess()) {
                Throwable throwable = result.getThrowable();
                StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                StringBuilder stackTrace = new StringBuilder();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    stackTrace.append(stackTraceElement.toString()).append("\n");
                }
                params.add("testId", String.valueOf(tmpTestId))
                        .add("content", stackTrace.toString())
                        .add("isException", "true").build();
                newRestClient(ApiMethod.TEST_LOGS, params).doRequest();
            }

            // Attachments
            Class superClass = result.getTestClass().getRealClass().getSuperclass();
            boolean isBaseTest =
                    superClass == BaseTest.class || superClass == BaseTestParam.class || superClass == BaseTestDataDriven.class;
            if (isBaseTest) {
                byte[] screenshot = Browser.getDriver().getScreenshotAs(OutputType.BYTES);
                params.add("testId", String.valueOf(tmpTestId))
                        .add("content", new String(BASE_64.encode(screenshot)))
                        .add("contentType", "image/png").build();
                newRestClient(ApiMethod.TEST_ATTACHMENTS, params).doRequest();
                byte[] pageSrc = Browser.getDriver().getPageSource().getBytes();
                params.add("testId", String.valueOf(tmpTestId))
                        .add("content", new String(BASE_64.encode(pageSrc)))
                        .add("contentType", "text/html").build();
                newRestClient(ApiMethod.TEST_ATTACHMENTS, params).doRequest();
            }

            // Update author info
            updateAdditionalInfo(result.getTestClass().getRealClass());
        }
    }

    private void postTestInfo(ITestResult result) {
        if (IS_ENABLED) {
            params.add("projectName", PROJECT_NAME)
                    .add("testName", result.getTestContext().getCurrentXmlTest().getName())
                    .add("methodName", result.getTestClass().getRealClass().getName() + "#" + result.getMethod().getMethodName())
                    .add("SID", SID)
                    .add("browser", Browser.currentBrowser.toString());
            try {
                params.add("env", InetAddress.getLocalHost().getHostName()).build();
            } catch (UnknownHostException e) {
                LOGGER.debug(this, e);
                params.add("env", "unknown").build();
            }
            this.tmpTestId = Long.valueOf(newRestClient(ApiMethod.TEST_PUT, params).doRequest());
        }
    }

    private void updateAdditionalInfo(Class testClass) {
        Annotation[] annotations  = testClass.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Author) {
                params.add("testId", String.valueOf(tmpTestId))
                        .add("name", ((Author) annotation).name())
                        .add("login", ((Author) annotation).login())
                        .add("email", ((Author) annotation).email()).build();
                newRestClient(ApiMethod.TEST_AUTHOR, params).doRequest();
            }
            if (annotation instanceof DevTime) {
                params.add("testId", String.valueOf(tmpTestId))
                        .add("devTime", String.valueOf(((DevTime) annotation).value())).build();
                newRestClient(ApiMethod.TEST_DEV_INFO, params).doRequest();
            }
        }
    }

}
