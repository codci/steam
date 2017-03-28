package webdriver;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.sikuli.api.ScreenRegion;
import org.testng.Assert;
import webdriver.elements.Label;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Base form
 */
public abstract class BaseForm extends BaseEntity {

    private static final String CREATE_SCREEN_EVERY_STEP = "org.uncommons.reportng.everystepscreen";
    private boolean isFirstCall = true;

    /**
     * @uml.property name="titleLocator"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    protected By titleLocator; // detect form opening locator
    /**
     * @uml.property name="title"
     */
    protected String title; // title of a form
    /**
     * @uml.property name="name"
     */
    protected String name; // full name of form that outputted to log, for example, "Form 'Login'"

    private ScreenRegion sikuliRegion; // cache of image for fast checking presence

    /**
     * Contructor
     *
     * @param locator   Locator
     * @param formTitle Name
     */
    protected BaseForm(final By locator, final String formTitle) {
        init(locator, formTitle);
        assertIsOpen();
    }

    /**
     * Contructor
     *
     * @param formlocator formlocator
     * @param formTitle   formTitle
     */
    public BaseForm(final String formlocator, final String formTitle) {
        long before = new Date().getTime();
        title = formTitle;
        Label titlePicture = (Label) new Label(formlocator, title);
        try {
            Assert.assertTrue(titlePicture.isPresent());
            sikuliRegion = titlePicture.getSikuliRegion();
            long openTime = new Date().getTime() - before;
            if (shouldCreateScreenEveryStep()) {
                formReportInfo(openTime);
            } else {
                info(String.format(getLoc("loc.form.appears"), title) + String.format(" in %smsec", openTime));
            }
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            fatal(String.format(getLoc("loc.form.doesnt.appears"), title));
        }
    }

    /**
     * For logs
     *
     * @param message Message
     * @return Message
     */
    protected String formatLogMsg(final String message) {
        return message;
    }

    /**
     * In report: If "true": when opening page screenshot is taken
     *
     * @return boolean
     */
    public boolean shouldCreateScreenEveryStep() {
        return "true".equalsIgnoreCase(System.getProperty(CREATE_SCREEN_EVERY_STEP, "false"));
    }

    /**
     * Init
     *
     * @param locator   Locator
     * @param formTitle Name
     */
    private void init(final By locator, final String formTitle) {
        titleLocator = locator;
        title = formTitle;
        name = String.format(getLoc("loc.form") + " '%1$s'", this.title);
    }

    /**
     * Check the opening form If the form is not open, the test stops working
     */
    public void assertIsOpen() {
        long before = new Date().getTime();
        Label elem = new Label(titleLocator, title);
        try {
            elem.waitAndAssertIsPresent();
            long openTime = new Date().getTime() - before;
            if (shouldCreateScreenEveryStep()) {
                formReportInfo(openTime);
            } else {
                info(String.format(getLoc("loc.form.appears"), title) + String.format(" in %smsec", openTime));
            }
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            fatal(String.format(getLoc("loc.form.doesnt.appears"), title));
        } finally {
            if (isFirstCall && shouldLogPageLoadTime()) {
                logPageLoadStat();
                isFirstCall = false;
            }
        }
    }

    /**
     * Form report block
     *
     * @param openTime
     */
    private void formReportInfo(long openTime) {
        String timestamp = CommonFunctions.getTimestamp();
        String newFilePath = null;
        String newFilePathRelated = this.getClass().getPackage().getName() + "." + this.getClass().getSimpleName() + String.format("%1$s.png", timestamp);
        try {
            String pathCreatedScreen = makeScreen(this.getClass(), false);
            newFilePath = pathCreatedScreen.replaceAll("\\.png", timestamp + "\\.png");
            FileUtils.moveFile(new File(pathCreatedScreen), new File(newFilePath));
        } catch (IOException e) {
            logger.debug(this, e);
            warn(e.getMessage());
        }
        logger.debug("<table border=\"3\" bordercolor=\"black\" style=\"background-color:#EBEBEB\" cellpadding=\"1\" cellspacing=\"3\"><tr>" +
                "<th colspan = \"1\"><h3>" +
                String.format(getLoc("loc.form.appears"), title) + String.format(" in %smsec", openTime) + "</h3></th></tr>" +
                "<tr>" +
                "<td>" +
                String.format("<a href=\"Screenshots/%1$s\">" +
                        "<img height=\"250\" width=\"350\" title=\"Actual screenshot\" src=\"Screenshots/%1$s\">" +
                        "</a>", newFilePathRelated) +
                "</td>" +
                "</tr>" +
                "</table>");
    }

    /**
     * Check the opening form If the form is not open, the test stops working
     */
    public void assertIsClosed() {
        Label elem = new Label(titleLocator, title);
        elem.assertIsAbsent();
    }

    /**
     * get Region From Cache (sikuli)
     *
     * @return ScreenRegion
     */
    public ScreenRegion getRegionFromCache() {
        return sikuliRegion;
    }

    /**
     * Assert Js error is absent
     */
    protected void assertJsErrorIsAbsent() {
        getBrowser().assertNoJsErrors();
    }

    /**
     * Verify Js errors are absent
     */
    protected void verifyJsErrorIsAbsent() {
        getBrowser().verifyNoJsErrors();
    }

}
