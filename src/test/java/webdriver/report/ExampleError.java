package webdriver.report;

import org.openqa.selenium.By;

import webdriver.Logger;
import webdriver.elements.Label;

public class ExampleError extends AssertionError {

	private static final Label lblDataError = new Label(By.xpath("//td/div[contains(.,'Data error')]"), "Data error");
	private static final long serialVersionUID = 354899061504456795L;

	private ExampleError(Errors error, String detailMessage) {
		super(error.toString() + " " + detailMessage);
		Logger.getInstance().info("mark as " + error.toString());
	}

	public static void analyzeAndThrowException(Throwable error, String browserBody) {
		String message = error.getLocalizedMessage();
		ExampleError exampleError;
		Logger.getInstance().info("error message is " + message);
		if (message != null) {
			Errors errors = Errors.getByExistingIn(browserBody);
            if (errors != null) {
                throwError(errors, message, error);
            }
            errors = Errors.getByExistingIn(message);
            if (errors != null) {
                throwError(errors, message, error);
            }
            if (lblDataError.isPresent()) {
                // if data error occured
                throwError(Errors.DATA_ERROR, message, error);
            }
		}
		// if not found custom exception or message is null
		exampleError = new ExampleError(Errors.UNKNOWN_ERROR, message);
		exampleError.setStackTrace(error.getStackTrace());
		throw exampleError;
	}

    private static void throwError(Errors errors, String message, Throwable error) {
        ExampleError exampleError = new ExampleError(errors, message);
        exampleError.setStackTrace(error.getStackTrace());
        throw exampleError;
    }

}
