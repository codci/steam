package demo.test;

import demo.test.forms.StoreSteamForm;
import demo.test.forms.TutSearchForm;
import webdriver.BaseTest;

public class SteamTest extends BaseTest {

	
	public void runTest() {

		logger.step(1);
		StoreSteamForm ssf = new StoreSteamForm();
		ssf.assertLogo();

		logger.step(2);
		ssf.selectCategoryGameAction();

		logger.step(3);
		ssf.selectDiscountTab();
	}
}
