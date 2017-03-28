package webdriver.utils.zephyrConnector;

import webdriver.Logger;

public class ZephyrJiraException extends Exception {
    public ZephyrJiraException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZephyrJiraException(String message) {
        super(message);
        Logger.getInstance().error(message);
    }
}
