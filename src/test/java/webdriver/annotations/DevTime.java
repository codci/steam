package webdriver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by p.ordenko on 21.05.2015, 17:28.
 * Development time info
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DevTime {

    /**
     * Float value in hours. For example: 4.5 hours == 4.5F
     */
    float value();

}
