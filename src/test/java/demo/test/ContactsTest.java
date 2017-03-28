package demo.test;

import demo.test.forms.ContactPage;
import demo.test.forms.MainPage;
import demo.test.forms.elements.ContinueInEnglishPopUp;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import webdriver.BaseTest;
import static demo.test.forms.elements.MenuElement.TopLevelMenuItem;
import static demo.test.forms.elements.ContinueInEnglishPopUp.ContinueInEnglishValues;

public class ContactsTest extends BaseTest {

	private String city;

	@BeforeClass
	@Parameters({"city"})
	public void readParams(final String city) throws Throwable {
		this.city = city;
	}

	@Override
	public void runTest() {
		logger.step(1);
		logger.info("Заходим на сайт. В окне предложения продолжить на английском языке нажимаем \"Нет\"");
		ContinueInEnglishPopUp continueInEnglishPopUp = new ContinueInEnglishPopUp();
		continueInEnglishPopUp.chooseContinueInEnglish(ContinueInEnglishValues.NO);
		continueInEnglishPopUp.assertIsClosed();
		MainPage mainPage = new MainPage();

		logger.step(2);
		logger.info("Заходим в меню Контакты и выбираем город " + city);
		mainPage.getMenu().navigate(TopLevelMenuItem.CONTACTS, city);

		logger.step(3);
		logger.info("На открывшейся странице города проверяем его наличие в заголовке");
		ContactPage contactPage = new ContactPage();
		contactPage.assertCity(city);
	}

}
