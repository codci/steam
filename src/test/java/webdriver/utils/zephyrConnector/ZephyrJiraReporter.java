package webdriver.utils.zephyrConnector;

import webdriver.PropertiesResourceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ZephyrJiraReporter extends Reporter {

    private static final PropertiesResourceManager props;
    static final String PROPERTIES_FILE = "zephyr.properties";
    static {
        props = new PropertiesResourceManager(PROPERTIES_FILE);
    }

    /**
     * Initialise jira properties and check is properties exists
     * @throws Exception
     */
    public ZephyrJiraReporter() throws ZephyrJiraException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String tc_name = dateFormat.format(date) + "-" + timeFormat.format(calendar.getTime());
        session = new Session();
        if (props.getProperty("domain") == null || props.getProperty("domain").isEmpty()) {
            reporter = false;
            return;
        } else {
            session.setDomain(props.getProperty("domain", "http://localhost:8080"));
        }
        if (reporter) {
            if (props.getProperty("username") == null || props.getProperty("username").isEmpty()) {
                throw new ZephyrJiraException("Username missing!");
            } else {
                session.setUsername(props.getProperty("username"));
            }

            if (props.getProperty("password") == null || props.getProperty("password").isEmpty()) {
                throw new ZephyrJiraException("Password missing!");
            } else {
                session.setPassword(props.getProperty("password"));
            }

            session.createSession();

            if (props.getProperty("product") == null || props.getProperty("product").isEmpty()) {
                throw new ZephyrJiraException("Product Code missing!");
            } else {
                session.setProduct_Name(props.getProperty("product"));
            }

            if (props.getProperty("version") == null || props.getProperty("version").isEmpty()) {
                throw new ZephyrJiraException("Version Name missing!");
            } else {
                session.setVersion_Name(props.getProperty("version"));
            }

            session.testCycle = new TestCycle();

            if (props.getProperty("testcycle") == null || props.getProperty("testcycle").isEmpty()) {
                session.testCycle.setTestcycle_Name(tc_name);
            } else {
                session.testCycle.setTestcycle_Name(props.getProperty("testcycle", tc_name));
            }
            session.testCycle.setBuild_Name(props.getProperty("build", ""));

        }
    }

    public boolean report() {
        return reporter;
    }

    public void createTestCycle() throws ZephyrJiraException {
        session.createTestCycle();
    }

    public void createExecutionRecord(String issue) throws ZephyrJiraException {
        session.createExecutionRecord(issue);
    }

    public void updateExecutionRecord(String status) throws ZephyrJiraException {
        session.updateExecutionRecord(status);
    }

    public void updateExecutionRecord(String status, String comment) throws ZephyrJiraException {
        session.updateExecutionRecord(status, comment);
    }

    public void manageTestCycle(String tcName) throws ZephyrJiraException {
        session.initialization(tcName);
    }

    @Override
    public void updateTestCycle() throws ZephyrJiraException {
        session.updateTestCycle();
    }
}

