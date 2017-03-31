package demo.test;

import demo.test.forms.StoreSteamForm;
import demo.test.forms.StoreSteamGamePageForm;
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
		ssf.selectGameByMaxSale();

		logger.step(4);
		StoreSteamGamePageForm ssgpf = new StoreSteamGamePageForm();
		ssgpf.assertSaleValue("75");
		ssgpf.assertPriceValue("6.24");

		logger.step(5);
	}
}
