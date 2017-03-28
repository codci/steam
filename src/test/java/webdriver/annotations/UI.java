package webdriver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openqa.selenium.support.How;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UI {
  How how() default How.ID;

  String title() default "";
  
  String using() default "";

  String id() default "";

  String name() default "";

  String className() default "";

  String css() default "";

  String tagName() default "";

  String linkText() default "";

  String partialLinkText() default "";

  String xpath() default "";
}
