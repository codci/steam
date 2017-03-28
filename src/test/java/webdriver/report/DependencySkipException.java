package webdriver.report;

import org.testng.SkipException;

public class DependencySkipException extends SkipException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1805319980683714503L;

	/**
	 * @param caseStatus - status
	 */
	public DependencySkipException(String caseStatus) {
		super(String.format("Dependency: case with status '%1$s' was not created by previous autotest.",caseStatus));
	}

}
