package webdriver;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An abstract class that describes the basic test application
 * contains methods for logging and field test settings (options)
 */
public abstract class BaseTestDataDriven extends BaseEntity implements IAnalys {

    private static String iteration = "1";

    /**
     * Making data
     *
     * @param testMethod test Method
     * @return String array
     * @throws Exception Exception
     */
    @DataProvider(name = "DataProv")
    static Object[][] makeData(final Method testMethod) throws IllegalArgumentException {
        String path = props.getProperty("testDataPath");
        String prefix = "..\\";
        Object[][] retObjArr = null;
        Map<String, String> arguments = DataProviderUtils.resolveDataProviderArguments(testMethod);
        path = prefix + String.format(path + "%1$s.xls", arguments.get("filename"));
        if (!new File(path).exists()) {
            path = path.substring(prefix.length());
        }
        retObjArr = getTableArray(path);
        return retObjArr;
    }

    /**
     * To override
     *
     * @param args Parameters
     */
    public abstract void runTest(String... args);

    /**
     * Тест
     *
     * @throws Throwable Throwable
     */
    public void xTest() throws Throwable {
        Class<? extends BaseTestDataDriven> currentClass = this.getClass();
        checkJiraIssue(currentClass);
        checkJiraIssue(currentClass.getMethod("runTest", null));
        try {
            processBugAnnotation();
            logger.logTestName(currentClass.getName());
            runTest();
            logger.logTestEnd(currentClass.getName());
        } catch (Exception | AssertionError e) {
            logger.debug(this, e);
            if (shouldAnalys()) {
                invokeAnalys(e, getBrowser().getDriver().getPageSource());
            } else {
                logger.warn("");
                logger.warnRed(getLoc("loc.test.failed"));
                logger.warn("");
                throw e;
            }
        } finally {
            if (Browser.getAnalyzeTraffic()) {
                getBrowser().assertAnalyzedTrafficResponseCode();
                getBrowser().assertAnalyzedTrafficLoadTime();
                ProxyServ.saveToFile(this.getClass().getName());
            }
            makeScreen(currentClass);
        }
    }

    /**
     * Format of logs
     *
     * @param message Message
     * @return Message
     */
    protected String formatLogMsg(final String message) {
        return message;
    }

    /**
     * getTableArray
     *
     * @param xlFilePath XLS FilePath
     * @return String array
     */
    protected static String[][] getTableArray(final String xlFilePath) {
        String[][] tabArray = null;
        try {
            Workbook workbook = Workbook.getWorkbook(new File(xlFilePath));
            Sheet sheet = workbook.getSheet(0);
            // Total Total No Of Rows in Sheet, will return you no of rows that are
            // occupied with some data
            int rowCount = sheet.getRows();

            // Total Total No Of Columns in Sheet
            int columnCount = sheet.getColumns();

            // Reading Individual Row Content
            tabArray = new String[rowCount - 1][columnCount];
            for (int i = 1; i < rowCount; i++) {
                // Get Individual Row
                Cell[] rowData = sheet.getRow(i);
                for (int j = 0; j < columnCount; j++) {
                    tabArray[i - 1][j] = rowData[j].getContents();
                }
            }
            workbook.close();
        } catch (FileNotFoundException e) {
            logger.debug("BaseTestDataDriven.getTableArray", e);
            logger.error("File not found: " + xlFilePath);
            throw new SkipException("File not found: " + xlFilePath);
        } catch (BiffException | IOException e) {
            logger.debug("BaseTestDataDriven.getTableArray", e);
        }
        return tabArray;
    }

    /**
     * Before Class method
     *
     * @param context ITestContext
     */
    @BeforeMethod
    public void beforeTest(final ITestContext context) {
        context.setAttribute("Iteration", iteration);
    }
}
