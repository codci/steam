package webdriver.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import webdriver.Browser;

@Retention(RetentionPolicy.RUNTIME)
public @interface Jira {

	String url() default "";
	String issue() default "";
	String value() default "";
	String login() default "";
	String password() default "";
	
}
