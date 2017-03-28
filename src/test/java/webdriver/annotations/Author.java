package webdriver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by p.ordenko on 21.05.2015, 17:28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Author {

    /**
     * Login like v.pupken
     * @return
     */
    String login();

    /**
     * Name like Pupken, Vasiliy
     * @return
     */
    String name();

    /**
     * E-mail like v.pupken@a1qa.com
     * @return
     */
    String email();

}
