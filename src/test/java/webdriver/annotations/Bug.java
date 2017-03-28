package webdriver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mark test with bug from Jira
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Bug {

    /**
     * Bug title, with ID at first.
     * For example, "PROJECTNAME-123: Login button not clickable"
     * @return String with bug title
     */
    String title();
}
