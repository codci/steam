package webdriver.utils.zephyrConnector;

public abstract class Reporter {

    Session session = null;

    boolean reporter = true;

    public abstract void createTestCycle() throws ZephyrJiraException;

    public abstract void createExecutionRecord(String string) throws ZephyrJiraException;

    public abstract void updateExecutionRecord(String string) throws ZephyrJiraException;

    public abstract void updateExecutionRecord(String string, String string1) throws ZephyrJiraException;  //to report comment

    public abstract void updateTestCycle() throws ZephyrJiraException;  //update date field

    public abstract boolean report();

    public abstract void manageTestCycle(String string) throws ZephyrJiraException;

}