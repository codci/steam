package webdriver.utils;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import webdriver.BaseEntity;
import webdriver.CommonFunctions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class ImageMagicUtil extends BaseEntity {

	private static final double DEF_LIMIT = 0.05;
	private static final String ID = CommonFunctions.getTimestamp();
	private String comparePath;
	private String actualPath;

	public String getActualPath() {
		return actualPath;
	}

	public String getID() {
		return ID;
	}

	/**
	 * Compare screenshots
	 * 
	 * @param expected
	 *            path to expected
	 */
	public double compareScreenshots(String expected) {
		double percent = 100;
		URL expectedURL = ClassLoader.getSystemResource(expected);
		File expectedFile = null;
		String expectedPath = null;
		try {
			expectedFile = new File(expectedURL.toURI());
		} catch (Exception e) {
            logger.debug(this, e);
		}

		actualPath = makeScreen(this.getClass());
		File actualFile = new File(actualPath.replaceAll("\\.png", getID()
				+ "\\.png"));
		try {
			FileUtils.copyFile(new File(actualPath), actualFile);
		} catch (IOException e2) {
			logger.debug(this, e2);
		}

		File expInTarget = null;
		try {
			expInTarget = new File(actualPath.split("\\.")[0] + "_etalon.png");
			FileUtils.copyFile(expectedFile, expInTarget);
			expectedPath = expInTarget.getAbsolutePath();
		} catch (IOException e1) {
			logger.debug(this, e1);
		}

		boolean exists = expInTarget.exists();
		if (!exists) {
			debug("Etalon file doesn't exist!");
			debug("Creating etalon file, please place into resource folder and reexecute.");
			try {
				FileUtils.copyFile(new File(actualPath), expInTarget);
				return 100;
			} catch (IOException e) {
				logger.debug(this, e);
				warn(e.getMessage());
			}
		}

		comparePath = actualPath.split("\\.png")[0]
				+ String.format("_diff%1$s.png", getID());
		String convert = String.format("convert.exe %1$s -crop 800x600! %2$s",
				actualPath, actualPath + "x");
		String convert2 = String.format("convert.exe %1$s -crop 800x600! %2$s",
				expectedPath, expectedPath + "x");
		try {
			String mvn = "";
			String cmdConvert1 = mvn + "test-classes\\tools\\" + convert;
			String cmdConvert2 = mvn + "test-classes\\tools\\" + convert2;
			// Convert size of first screenshot
			Runtime rt = Runtime.getRuntime();
			rt.exec(cmdConvert1);

			// Convert size of second screenshot
			rt.exec(cmdConvert2);

			ProcessBuilder builder = new ProcessBuilder(mvn
					+ "test-classes\\tools\\compare.exe", "-metric", "rmse",
					actualPath + "x", expectedPath + "x", comparePath);
			percent = buildAndGetPercentage(builder);

		} catch (IOException e) {
			logger.debug(this, e);
			warn(e.getMessage());
		}
		// percent of differences
		return percent;

	}

	private double buildAndGetPercentage(ProcessBuilder builder) {
        double percent = 0D;
        String line;
        try{
            builder.redirectErrorStream(true);
            Process process = builder.start();

            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stdout));

            while ((line = reader.readLine()) != null) {
                logger.info("Stdout: " + line);
                String percentS = CommonFunctions.regexGetMatchGroup(line,
                        "\\((.*)\\)", 1);
                percent = Double.valueOf(percentS);
            }

        } catch (Exception e) {
            logger.debug(this, e);
            warn(e.getMessage());
        }
        return percent;
    }

	/**
	 * Assert screenshot doesn't differ
	 * 
	 * @param pathToExpectedScreenshot
	 *            pathToExpectedScreenshot
	 * @param limit
	 *            limit of differencies in percent (0.00 - the same , 0.99 ,
	 *            absolutely different)
	 */
	public void assertScreenshot(final String pathToExpectedScreenshot,
			final double limit) {
		double l = compareScreenshots(pathToExpectedScreenshot);
		formReportInfo();
		Assert.assertTrue(
				l < limit,
				String.format(
						"Screenshot differs! Percent is %1$s, but shuold be less then %2$s!",
						l, limit));
	}

	/**
	 * Verify screenshot doesn't differ
	 * 
	 * @param pathToExpectedScreenshot
	 *            pathToExpectedScreenshot
	 * @param limit
	 *            limit of differencies in percent (0.00 - the same , 0.99 ,
	 *            absolutely different)
	 */
	public void verifyScreenshot(final String pathToExpectedScreenshot,
			final double limit) {
		double l = compareScreenshots(pathToExpectedScreenshot);
		formReportInfo();
		if (l > limit) {
			warn(String
					.format("<font size=\"3\" color=\"black\">Screenshot differs! Percent is </font><font size=\"4\" color=\"red\">%1$s</font><font size=\"3\" color=\"black\">, but should be less then </font><font size=\"4\" color=\"red\">%2$s!</font>",
							l, limit));
		}
	}

	/**
	 * Form report block
	 */
	private void formReportInfo() {
		try {
			FileUtils.copyFile(new File(comparePath), new File(actualPath));
		} catch (IOException e) {
            logger.debug(this, e);
			warn(e.getMessage());
		}
		String fileNameDiff = this.getClass().getName()
				+ String.format("_diff%1$s.png", getID());
		String fileNameActual = this.getClass().getName()
				+ String.format("%1$s.png", getID());
		logger.debug("<table border=\"3\" bordercolor=\"black\" style=\"background-color:#EBEBEB\" width=\"100%\" cellpadding=\"1\" cellspacing=\"3\"><tr>"
				+ "<tr><th colspan = \"2\"><h3>Comparing of screenshots</h3></th></tr>"
				+ "<th>Actual screenshot</th>"
				+ "<th>Differences</th>"
				+ "</tr>"
				+ "<tr>"
				+ "<td>"
				+ String.format(
						"<a href=\"Screenshots/%1$s\">"
								+ "<img height=\"250\" width=\"350\" title=\"Actual screenshot\" src=\"Screenshots/%1$s\">"
								+ "</a>", fileNameActual)
				+ "</td>"
				+ "<td>"
				+ String.format(
						"<a href=\"Screenshots/%1$s\">"
								+ "<img height=\"250\" width=\"350\" title=\"Differences\" src=\"Screenshots/%1$s\">"
								+ "</a>", fileNameDiff)
				+ "</td>"
				+ "</tr>"
				+ "</table>");
	}

	/**
	 * Assert screenshot doesn't differ
	 * 
	 * @param pathToExpectedScreenshot
	 *            pathToExpectedScreenshot
	 */
	public void assertScreenshot(final String pathToExpectedScreenshot) {
		assertScreenshot(pathToExpectedScreenshot, DEF_LIMIT);
	}

	/**
	 * Verify screenshot doesn't differ
	 * 
	 * @param pathToExpectedScreenshot
	 *            pathToExpectedScreenshot
	 */
	public void verifyScreenshot(final String pathToExpectedScreenshot) {
		verifyScreenshot(pathToExpectedScreenshot, DEF_LIMIT);
	}

	
	/**
	 * Image to file
	 * @param image BufferedImage
	 * @param fileName fileName
	 */
	private void putImageToFile(final BufferedImage image, final String fileName) {
		File outputfile = new File(fileName);
		try {
			ImageIO.write(image, "jpg", outputfile);
		} catch (IOException e) {
            logger.info(this, e);
        }
	}
	
	
	/**
	 * Get difference beetween 2 images
	 */
	public double compareImages(final BufferedImage image1,
			final BufferedImage image2) {
		
		String stamp = CommonFunctions.getTimestamp();
		
		String firstImage = "first_" + stamp;
		String secondImage = "second_" + stamp;
		
		putImageToFile(image1,firstImage);
		putImageToFile(image2,secondImage);

		double percent = 100;

		try {
			String mvn = new File("target\\test-classes\\tools\\compare.exe").exists()? "target\\" : "";
			ProcessBuilder builder = new ProcessBuilder(mvn + "test-classes\\tools\\compare.exe", "-metric", "rmse",firstImage, secondImage, secondImage + "_diff");
			percent = buildAndGetPercentage(builder);

		} catch (Exception e) {
            logger.debug(this, e);
			warn(e.getMessage());
		}
		
		deleteFile(firstImage);
		deleteFile(secondImage);
		deleteFile(secondImage + "_diff");
		
		// percent of differences
		return percent;
	}

	/**Delete file by fileName
	 */
	private void deleteFile(String name) {
		new File(name).delete();
	}

	@Override
	protected String formatLogMsg(String message) {
		return message;
	}

}
