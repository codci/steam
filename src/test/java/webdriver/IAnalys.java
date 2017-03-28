package webdriver;

/**
 * IAnalys interface
 */
public interface IAnalys {

	boolean shouldAnalys();
	void invokeAnalys(Throwable exc, String bodyText) throws Throwable;

}