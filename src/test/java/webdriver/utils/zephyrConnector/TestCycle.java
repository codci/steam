package webdriver.utils.zephyrConnector;

public class TestCycle {




    String testcycle_Name = "", testcycle_Id = "-1";
    String build_Name = "";

    ExecutionRecord current_execution;

    public String getTestcycle_Name() {
        return testcycle_Name;
    }

    public void setTestcycle_Name(String testcycle_Name) {
        this.testcycle_Name = testcycle_Name;
    }

    public String getBuild_Name() {
        return build_Name;
    }

    public void setBuild_Name(String build_Name) {
        this.build_Name = build_Name;
    }

}
