package webdriver.utils.zephyrConnector;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import webdriver.Logger;
import webdriver.PropertiesResourceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static webdriver.utils.zephyrConnector.RestClient.disableCertificateValidation;


public class Session {

    public static final PropertiesResourceManager props;
    static final String PROPERTIES_FILE = "zephyr.properties";
    static {
        props = new PropertiesResourceManager(PROPERTIES_FILE);
    }

    private static final Logger logger = Logger.getInstance();
    private static final String REPLACE_CHARACTER = "-";
    private static final int MAX_RESULTS = 200;
    public static final String GET = "GET", POST = "POST", PUT = "PUT";
    String domain;    //ip address of the machine where jira is running
    String username = "null", password = "null";    //jira login credentials
    String product_Name;
    String product_Id;
    String version_Name;
    String version_Id;
    RestClient client = null;
    JSONParser parseJSON = null;
    String requestHeader = null;
    TestCycle testCycle = null;
    IssueTracker iTracker = null;
    Date date = new Date();

    public Session() throws ZephyrJiraException {
        disableCertificateValidation();
        client = new RestClient();
        parseJSON = new JSONParser();
        iTracker = new IssueTracker();
        product_Id = props.getProperty("productid","-1");
        version_Id = props.getProperty("versionid","-1");
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Initialise zapi session with username and password
     * @throws Exception
     */
    void createSession() {
        testCycle = new TestCycle();
        String usr_pswrd = username + ":" + password;
        byte[] encodedBytes = Base64.encodeBase64(usr_pswrd.getBytes());
        String authentication = new String(encodedBytes);
        requestHeader = "content-type=application/json&&Authorization=Basic " + authentication;
    }

    /**
     * Create test case properties file
     * and populate it with properties like testCase=testCaseID
     * @param tcName test case name
     * @throws Exception
     */
    void initialization(String tcName) throws ZephyrJiraException{
        iTracker.createFile(tcName);
        if (IssueTracker.newFileCreated) {
            getAllIssuesAndPopulateMap(tcName);
        } else {
            iTracker.populateHashMapFromTsFile();
        }
    }

    /**
     * populate properties file with properties like testCase=testCaseID
     * @param summary test case summary
     * @throws Exception
     */
    void getAllIssuesAndPopulateMap(String summary) throws ZephyrJiraException {
        String service, response = null, parsedIssueSummary = null, parsedIssueId = null;
        int issue_cnt = 0;
        int temp = 0;
        int startAt = 0;
        boolean more = false;
        do {
            service = domain + "/rest/api/2/search?jql=project=" + product_Name + "%20AND%20summary~%22" + summary + "%22%20AND%20issuetype=Test&startAt=" + startAt + "&maxResults=" + MAX_RESULTS + "&fields=id,key,summary";
            String request = "";
            try {
                response = client.sendRequest(service, GET, requestHeader, request);
            } catch (Exception e) {
                logger.debug(this, e);
                throw new ZephyrJiraException("Session.getAllIssuesAndPopulateMap() Failed to get proper server response. \n" + e.getMessage());
            }
            try {
                if (!more) {
                    issue_cnt = Integer.parseInt(JSONParser.getJsonElement(response, "$.total"));
                }
                if (issue_cnt > 0) {
                    if ((issue_cnt - startAt) > MAX_RESULTS) {
                        temp = MAX_RESULTS;
                        more = true;
                    } else {
                        temp = issue_cnt - startAt;
                        more = false;
                    }
                    for (int i = 0; i < temp; i++) {
                        parsedIssueSummary = JSONParser.getJsonElement(response, "$.issues[" + i + "].fields.summary");
                        parsedIssueId = JSONParser.getJsonElement(response, "$.issues[" + i + "].id");
                        iTracker.addToHashMap(parsedIssueSummary, parsedIssueId);
                    }
                    iTracker.populateTsFileFromHashMap();
                }
            } catch (Exception ex) {
                logger.debug(this, ex);
                logger.error("ERROR : Session:getAllIssuesAndPopulateMap() :: Failed to parse server response to retrieve issue count.\n" + ex.getMessage() + "\nResponse : " + response);
                throw new ZephyrJiraException(ex.getMessage());
            }
            if (more) {
                startAt += MAX_RESULTS;
                continue;
            }
        } while (more);
    }


    /**
     * If test cycle does not exists create it
     * @throws Exception
     */
    void createTestCycle() throws ZephyrJiraException {
        String testcycleId = getLatestCycle(testCycle.testcycle_Name, testCycle.build_Name, product_Id, version_Id);
        if (testcycleId == null) {
            testcycleId = testCycle_write(testCycle.testcycle_Name, testCycle.build_Name, product_Id, version_Id);
        }
        testCycle.testcycle_Id = testcycleId;
    }

    /**
     * Create execution record to test case before change status
     * If test case does not exists create it
     * @param issue test case summary/name
     * @throws Exception
     */
    public void createExecutionRecord(String issue) throws ZephyrJiraException {
        String issueId = null;
        testCycle.current_execution = new ExecutionRecord();
        boolean isIssueFound = false;

        issueId = iTracker.getValFromHashMap(issue);

        if (issueId == null) {
            try {
                issueId = getIssueId(issue.replace("_"," "));
            } catch (Exception e) {
                logger.debug(this, e);
                logger.warn("Session.createExecutionRecord() Creating new Issue. Issue = " + issue + "\n" + e.getMessage());
                try {
                    issueId = createIssue(issue);
                } catch (Exception f) {
                    logger.debug(this, f);
                    logger.warn("Session.createExecutionRecord() Exception while creating new issue. Issue = " + issue + ". \n" + f.getMessage());
                }
            }
            if (issueId != null) {
                iTracker.addToHashMap(issue, issueId);
            }
        }
        isIssueFound = true;

        if (isIssueFound) {
            testCycle.current_execution.execution_Id = testExecution_write(issueId, testCycle.testcycle_Id, product_Id, version_Id);
        }
    }

    /**
     * Update date and status of execution
     * @param status status of test case execution
     * @throws Exception
     */
    public void updateExecutionRecord(String status) throws ZephyrJiraException {
        if (testCycle.current_execution.execution_Id != null) {
            testExecution_setOutcome(testCycle.current_execution.execution_Id, status);
        }
    }

    /**
     * Update date and status of execution
     * @param status status of test case execution
     * @param comment comment for test case execution
     * @throws Exception
     */
    public void updateExecutionRecord(String status, String comment) throws ZephyrJiraException {
        if (testCycle.current_execution.execution_Id != null) {
            testExecution_setOutcome(testCycle.current_execution.execution_Id, status, comment);
        }
    }

    /**
     * Update test cycle execution info
     * @throws Exception
     */
    public void updateTestCycle() throws ZephyrJiraException {
        String service, testcycle_id = null, response = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy",Locale.ENGLISH);
        service = domain + "/rest/zapi/latest/cycle";

        String request = "{\"id\": \"" + testCycle.testcycle_Id + "\",\"endDate\": \"" + dateFormat.format(date) + "\"}";
        try {
            response = client.sendRequest(service, PUT, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.updateTestCycle() Failed to get proper server response. \n" + e.getMessage());
        }
        try {
            testcycle_id = parseJSON.getJsonElement(response, "$.id");
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.updateTestCycle() Failed to parse server response to retrieve test cycle id.\n Exception : " + e.getMessage() + "\nResponse : " + response);
        }
        iTracker.populateTsFileFromHashMap();
    }


    public void retrieveAllIssues(String ss)  {
        String service, response = null;
        service = domain + "/rest/zapi/latest/issues";
        String request = "";
        try {
            response = client.sendRequest(service, GET, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            logger.warn("Session.retrieveAllIssues() Failed to get proper server response. \n " + e.getMessage() + "\nCreating a fresh test cycle.");
        }
    }

    public void retrieveAllIssues() {
        String service, response = null;
        service = domain + "/rest/zapi/latest/issues";
        String request = "{\"currentIssueKey\":\"\",\"currentJQL\":\"type=Test\",\"currentProjectId\":" + product_Id + ",\"query\":\"test\"}";
        String params = "currentProjectId=" + product_Id;
        service = service + "?" + params;
        try {
            response = client.sendRequest(service, GET, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            logger.warn("Session.retrieveAllIssues() Failed to get proper server response. \n " + e.getMessage() + "\nCreating a fresh test cycle.");
        }
    }


    public String retrieveAllTCycles(String product_id, String version_id) {
        String service, response = null;
        service = domain + "/rest/zapi/latest/cycle?versionId=" + version_id;

        String request = "";//"{\"projectId\": "+product_id+",\"versionId\": "+version_id+",\"offset\": 0,\"expand\": \"executionSummaries\"}";
        try {
            response = client.sendRequest(service, GET, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            logger.warn("Session.retrieveAllTCycles() Failed to get proper server response. \n " + e.getMessage() + "\nCreating a fresh test cycle.");
        }
        return response;
    }

    public String getLatestCycle(String tcycle_name, String build_name, String product_Id, String version_Id) throws ZephyrJiraException {
        String service, testcycle_id = null;

        try {
            String allcyclesJSON = retrieveAllTCycles(product_Id, version_Id);
            String keys_list = parseJSON.getObjectKeys(allcyclesJSON, "$.");
            String[] arr = keys_list.split(",");

            for (String key : arr) {
                if (!key.contains("recordsCount")) {
                    String tcycle = parseJSON.getJsonElement(allcyclesJSON, "$." + key + ".name");
                    String build = parseJSON.getJsonElement(allcyclesJSON, "$." + key + ".build");
                    if (tcycle_name.equals(tcycle) && build_name.equals(build)) {
                        testcycle_id = key;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug(this, e);
            logger.warn("Session.getLatestCycle() " + e.getMessage());
        }
        return testcycle_id;
    }

    /**
     * Create test cyclr
     * @param tcycle_name test cysle name
     * @param build_name build name
     * @param product_Id project name
     * @param version_Id version
     * @return created test cycle id
     * @throws Exception
     */
    public String testCycle_write(String tcycle_name, String build_name, String product_Id, String version_Id) throws ZephyrJiraException {
        String service, testcycle_id = null, response = null;
        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy",Locale.ENGLISH);
        service = domain + "/rest/zapi/latest/cycle";
        String request = "{\"name\": \"" + tcycle_name + "\",\"build\": \"" + build_name + "\",\"description\": \"auto generated by SMART-framework\",\"startDate\": \"" + dateFormat.format(date) + "\", \"projectId\": \"" + product_Id + "\",\"versionId\": \"" + version_Id + "\"}";
        try {
            response = client.sendRequest(service, POST, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.testCycle_write() Failed to get proper server response. \n" + e.getMessage());
        }
        try {
            testcycle_id = parseJSON.getJsonElement(response, "$.id");
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.testCycle_write() Failed to parse server response to retrieve test cycle id." + "\nResponse : " + response);
        }
        return testcycle_id;
    }

    /**
     * Find test suite
     * @param summary test suite summary/name
     * @return id
     * @throws Exception
     */
    public String getIssueId(String summary) throws ZephyrJiraException, JSONException {
        String service, issue_id = null, response = null, parsed_issue_key = null;
        boolean issueFound = false;
        int issue_cnt = -1, i;
        service = domain + "/rest/api/2/search?";
        String request = "jql=project=" + product_Name + "%20AND%20summary~%22" + summary.replace(" ","%20") + "%22%20AND%20issuetype=Test&fields=id,key,summary";
        try {
            response = client.sendRequest(service + request, GET, requestHeader, "");
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("JiraClient.getIssueId() Failed to get proper server response. " + e.getMessage());
        }
        try {
            issue_cnt = Integer.parseInt(JSONParser.getJsonElement(response, "$.total"));
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("JiraClient.getIssueId() Failed to parse server response to retrieve issue count.\n" + ex.getMessage() + "\nResponse : " + response);
        }
        if (issue_cnt == 0) {
            throw new ZephyrJiraException("Session.getIssueId() Issue not found of type Test. Issue Summary : " + summary);
        } else {
            for (i = 0; i < issue_cnt; i++) {
                parsed_issue_key = JSONParser.getJsonElement(response, "$.issues[" + i + "].fields.summary");
                if (parsed_issue_key.equalsIgnoreCase(summary)) {
                    issueFound = true;
                    break;
                }
            }
            if (issueFound) {
                issue_id = JSONParser.getJsonElement(response, "$.issues[" + i + "].id");
            } else {
                throw new ZephyrJiraException("Session.getIssueId() Issue not found of type Test. Issue Summary : " + summary);
            }
        }
        return issue_id;
    }

    public String addTestToTCycle(String issue_key, String tcycle_id, String product_id, String version_id) throws Exception {
        String service, execution_id = null;
        try {
            service = domain + "/rest/zapi/latest/execution/addTestsToCycle/";

            String request = "{\"issues\": [\"" + issue_key + "\"],\"versionId\": " + version_id + ",\"cycleId\": \"" + tcycle_id + "\",\"projectId\": \"" + product_id + "\",\"method\": \"1\"}";
            String response = client.sendRequest(service, POST, requestHeader, request);

        } catch (Exception ex) {
            logger.debug(this, ex);
        }
        return execution_id; // FIXME execution_id is always null. Wtf?
    }

    /**
     * Change test execution status
     * @param issue_id test suite id
     * @param tcycle_id test cycle id
     * @param product_id project id
     * @param version_id version
     * @return test execution id
     * @throws Exception
     */
    public String testExecution_write(String issue_id, String tcycle_id, String product_id, String version_id) throws ZephyrJiraException {
        String service, execution_id = null, keys = null, response = null;
        boolean continue_execution = true;
        service = domain + "/rest/zapi/latest/execution";

        String request = "{\"issueId\": " + issue_id + ",\"versionId\": \"" + version_id + "\",\"cycleId\": \"" + tcycle_id + "\",\"projectId\": " + product_id + "}";
        try {
            response = client.sendRequest(service, POST, requestHeader, request);

        } catch (Exception e) {
            logger.debug(this, e);
            continue_execution = false;
            logger.warn("Session.testExecution_write() Failed to get proper server response. \n" + e.getMessage());
        }
        if (continue_execution) {
            try {
                keys = parseJSON.getObjectKeys(response, "$.");
                execution_id = parseJSON.getJsonElement(response, "$." + keys + ".id");

            } catch (Exception ex) {
                logger.debug(this, ex);
                logger.warn("Session.testExecution_write() Failed to parse server response to retrieve test execution id.\n Exception : " + ex.getMessage() + "\nResponse : " + response);
            }
        }
        return execution_id;
    }

    /**
     * Make test execution
     * @param execution_id test execution id
     * @param status change status like pass/fail
     * @return test execution id
     * @throws Exception
     */
    public String testExecution_setOutcome(String execution_id, String status) throws ZephyrJiraException {
        String service, result_execution_id = null, response = null;

        service = domain + "/rest/zapi/latest/execution/" + execution_id + "/quickExecute";

        String request = "{\"status\": " + status + "}";
        try {
            response = client.sendRequest(service, POST, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.testExecution_setOutcome() Failed to get proper server response.\n" + e.getMessage());
        }
        try {
            result_execution_id = parseJSON.getObjectKeys(response, "$.");
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("Session.testExecution_setOutcome() Failed to parse server response to retrieve execution id.\n Exception : " + ex.getMessage() + "\nResponse : " + response);
        }
        return result_execution_id;
    }

    /**
     * Make test execution
     * @param execution_id test execution id
     * @param status change status like pass/fail
     * @param comment comment for test execution
     * @return test execution id
     * @throws Exception
     */
    public String testExecution_setOutcome(String execution_id, String status, String comment) throws ZephyrJiraException {
        String service, result_execution_id = null, response = null;

        service = domain + "/rest/zapi/latest/execution/" + execution_id + "/execute";

        String request = "{\"status\": " + status + ", \"comment\":\"" + comment + "\"}";
        try {
            response = client.sendRequest(service, PUT, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.testExecution_setOutcome() Failed to get proper server response. \n" + e.getMessage());
        }
        try {
            result_execution_id = parseJSON.getObjectKeys(response, "$.");
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("Session.testExecution_setOutcome() Failed to parse server response to retrieve execution id.\n Exception : " + ex.getMessage() + "\nResponse : " + response);
        }
        return result_execution_id;
    }

    public ExecutionRecord getCurrent_execution() {
        return testCycle.current_execution;
    }

    public String getProduct_Name() {
        return product_Name;
    }

    public void setProduct_Name(String product_Name) throws ZephyrJiraException {
        this.product_Name = product_Name;
        this.product_Id = getProductId();
    }

    public String getVersion_Name() {
        return version_Name;
    }

    public void setVersion_Name(String version_Name) throws ZephyrJiraException {
        this.version_Name = version_Name;
        this.version_Id = getVersionId();
    }

    /**
     * Get project id by name
     * @return project id
     * @throws Exception
     */
    public String getProductId() throws ZephyrJiraException {
        String service, product_id = null, response = null;
        service = domain + "/rest/api/2/project/" + product_Name;
        try {
            response = client.sendRequest(service, GET, requestHeader, "");
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.getProductId() Failed to get proper server response. \n" + e.getMessage());
        }
        try {
            product_id = JSONParser.getJsonElement(response, "$.id");
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("Session.getProductId() Failed to parse Server Response. \nException : " + ex.getMessage() + "\nResponse : " + response);
        }
        return product_id;
    }

    /**
     * Get version id by value
     * @return version id
     * @throws Exception
     */
    public String getVersionId() throws ZephyrJiraException {
        String service, version_id = null, response = "";

        service = domain + "/rest/api/2/project/" + product_Name + "/versions";
        try {
            response = client.sendRequest(service, GET, requestHeader, "");
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.getVersionId() Failed to get proper server response. \n" + e.getMessage());
        }
        try {
            String JSON_txt = parseJSON.parseJsonArray(response, "name", version_Name);
            version_id = JSONParser.getJsonElement(JSON_txt, "$.id");
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("Session.getVersionId() Failed to parse Server Response. \nException : " + ex.getMessage() + "\nResponse : " + response);
        }
        return version_id;
    }

    /**
     * Create test case when it is not created
     * @param summary test case summary/name
     * @return test case id
     * @throws Exception
     */
    public String createIssue(String summary) throws ZephyrJiraException {
        String service, issue_id = null, response = null, json_path = null, issue_type = null;
        service = domain + "/rest/api/2/issue";
        String request = "{\"fields\":{\"project\":{\"id\":\"" + product_Id + "\"},\"summary\":\"" + replaceCharacters(summary).replace("_"," ") + "\",\"issuetype\":{\"name\":\"Test\"},\"reporter\":{\"name\":\"" + username + "\"},\"versions\":[{\"id\":\"" + version_Id + "\"}],\"description\":\"" + "" + "\"}}";

        boolean create = false;
        try {
            response = client.sendRequest(service, POST, requestHeader, request);
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Session.createIssue() Failed to get proper server response. " + e.getMessage());
        }
        try {
            issue_id = JSONParser.getJsonElement(response, "$.id");
        } catch (Exception ex) {
            logger.debug(this, ex);
            throw new ZephyrJiraException("Session:createIssue() Failed to parse server response to retrieve issue id.\n" + ex.getMessage() + "\nResponse : " + response);
        }
        return issue_id;
    }

    private String replaceCharacters(String str1) {
        if (str1 != null)
            return str1.replaceAll("\\\\", REPLACE_CHARACTER).replaceAll("/", REPLACE_CHARACTER);
        else
            return "";
    }
}