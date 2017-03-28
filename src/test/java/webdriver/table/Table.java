package webdriver.table;

import org.openqa.selenium.By;
import org.testng.AssertJUnit;
import webdriver.elements.BaseElement;
import webdriver.elements.CheckBox;
import webdriver.elements.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * abstract table, contains common methods implemented from {@link ITable}
 * Template fields should be overrided
 */
public abstract class Table extends BaseElement implements ITable {

    // constants
    private static final String INPUT_TYPE_CHECKBOX = "//input[@type='checkbox']";


    // protected field

    /**
     * @uml.property name="divTemplateByID"
     */
    protected String divTemplateByID = "//div[@id='%1$s']";
    /**
     * table or tbody tag level
     *
     * @uml.property name="tbodyTemplate"
     */
    protected String tbodyTemplate = "//div[@id='%1$s']//tbody[2]";
    /**
     * row template by index
     *
     * @uml.property name="rowTemplate"
     */
    protected String rowTemplate = tbodyTemplate + "//tr[%2$s]";
    /**
     * cell template by class tbodyTemplate + rowTemplate
     *
     * @uml.property name="cellTemplate"
     */
    protected String cellTemplate = rowTemplate + "//td[contains(@class,'%3$s')]";
    /**
     * cell template by text tbodyTemplate + rowTemplate
     *
     * @uml.property name="cellTemplateByText"
     */
    protected String cellTemplateByText = rowTemplate + "//td[contains(.,'%3$s')]";
    /**
     * @uml.property name="tbodyNumTemplate"
     */
    protected String tbodyNumTemplate = "//div[@id='%1$s']//tbody[%2$s]";
    /**
     * tbodyNumTemplate +
     *
     * @uml.property name="rowTbodyNumTemplate"
     */
    protected String rowTbodyNumTemplate = tbodyNumTemplate + "//tr[%3$s]";
    /**
     * id and text
     *
     * @uml.property name="rowTbodyContains"
     */
    protected String rowTbodyContains = divTemplateByID + "//tr[contains(.,'%2$s')]";
    /**
     * @uml.property name="cellTemplateByIndex"
     */
    protected String cellTemplateByIndex = rowTemplate + "//td[%3$s]";
    /**
     * @uml.property name="currentTableId"
     */
    protected String currentTableId;


    public Table(String id, String name) {
        super(By.id(id), name);
        currentTableId = id;
    }

    public Table(By locator, String name) {
        super(locator, name);
    }

    @Override
    protected String getElementType() {
        return "table";
    }


    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRecordsArray(webdriver.table.IColumnsTags)
     */
    @Override
    public String[] getRecordsArray(IColumnsTags tag) {
        int countRows = getRowsCount();
        List<String> arrayRecords = new ArrayList<String>();
        for (int i = 0; i < countRows; i++) {
            String cellText = getCellText(i + 1, tag);
            if (!cellText.isEmpty()) {
                arrayRecords.add(cellText);
            }
        }
        String[] resultArray = new String[arrayRecords.size()];
        for (int i = 0; i < arrayRecords.size(); i++) {
            resultArray[i] = arrayRecords.get(i).toString();
        }
        return resultArray;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getTextByRowNumber(int)
     */
    @Override
    public String getTextByRowNumber(int rowNumber) {
        String rowLoc = String.format(rowTemplate, currentTableId, String.valueOf(rowNumber));
        Label row = new Label(By.xpath(rowLoc), "row");
        return (row.isPresent()) ? row.getText() : "";
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRowNumber(webdriver.table.IColumnsTags, java.lang.String)
     */
    @Override
    public int getRowNumber(IColumnsTags columnTag, String cellText) {
        long started = System.currentTimeMillis();
        int countRows = getRowsCount();
        // looking for case
        for (int i = 1; i <= countRows; i++) {
            String rowContent = getTextByRowNumber(i);
            if (rowContent.contains(cellText)) {
                String cellTextTemp = getCellText(i, columnTag);
                if (cellTextTemp.contains(cellText)) {
                    // found
                    float sec = (float) (System.currentTimeMillis() - started) / (1000);
                    logger.info("found  in " + sec);
                    return i;
                }
            }
        }
        logger.warn(String.format("Record in column '%1$s' with cell text '%2$s' is not present", columnTag, cellText));
        return 0;
    }


    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRowNumber(webdriver.table.IColumnsTags, java.lang.String, webdriver.table.IColumnsTags, java.lang.String, boolean)
     */
    @Override
    public int getRowNumber(IColumnsTags primaryTag, String primText, IColumnsTags secondaryTag, String secondText,
                            boolean shouldExceptRowWithSeconText) {
        int countRows = getRowsCount();
        // looking for case
        for (int rowN = 1; rowN <= countRows; rowN++) {
            String cellText1 = getCellText(rowN, primaryTag);
            if (cellText1.contains(primText)) {
                // found
                String cellText2 = getCellText(rowN, secondaryTag);

                boolean elementPresent = cellText2.contains(secondText);
                if (skipSecondText(shouldExceptRowWithSeconText, elementPresent)) {
                    return rowN;
                }
            }
        }
        logger.info(String.format(
                "Record in column '%1$s' with cell text '%2$s' AND text '%3$s' in column '%4$s' is not present",
                primaryTag, primText, secondText, secondaryTag));
        return 0;
    }

    private boolean skipSecondText(boolean shouldExceptRowWithSeconText, boolean isPresent) {
        if (shouldExceptRowWithSeconText) {
            // if should except row containing secondary text
            if (!isPresent)
                return true;
        } else {
            // if should find row containing both cell
            if (isPresent)
                return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRowsCount()
     */
    @Override
    public int getRowsCount() {
        int countRows = 0;
        try {
            countRows = getBrowser().getDriver().findElementsByXPath(String.format(tbodyTemplate, currentTableId) + "//tr").size();
        } catch (Exception e) {
            logger.debug(this, e);
            logger.info("exception while counting rows");
        }
        return countRows;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRandomRowNumber()
     */
    @Override
    public int getRandomRowNumber() {
        int totalRows = getRowsCount();
        int result = (int) (Math.random() * totalRows);
        result = (result == 0) ? 1 : result;// avoiding 0 row
        logger.info(String.format("Choosing row '%1$d' (total rows: '%2$d')", result, totalRows));
        return result;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getCellText(int, webdriver.table.IColumnsTags)
     */
    @Override
    public String getCellText(int rowNumber, IColumnsTags columnTag) {
        String cellText = getBrowser().getDriver().findElementByXPath(String.format(cellTemplate,
                currentTableId, rowNumber, columnTag.toString())).getText();
        return cellText;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getCellText(int, webdriver.table.IColumnsTags, int)
     */
    @Override
    public String getCellText(int rowNumber, IColumnsTags columnTag, int tbodyNum) {
        String cellText = getBrowser().getDriver().findElementByXPath(String.format(rowTbodyNumTemplate + "//td[%4$s]/div",
                currentTableId, tbodyNum, rowNumber, columnTag.toString())).getText();
        return cellText;
    }


    /* (non-Javadoc)
     * @see webdriver.table.ITable#clickCell(int, webdriver.table.IColumnsTags)
     */
    @Override
    public void clickCell(int rowNumber, IColumnsTags columnTag) {
        getBrowser().getDriver().findElementByXPath(
                String.format(cellTemplate, currentTableId, rowNumber, columnTag.toString())).click();
    }


    /* (non-Javadoc)
     * @see webdriver.table.ITable#getCellCheckBoxState(int, webdriver.table.IColumnsTags)
     */
    @Override
    public boolean getCellCheckBoxState(int rowNumber, IColumnsTags columnTag) {
        String loc = String.format(cellTemplate, currentTableId, rowNumber, columnTag) + INPUT_TYPE_CHECKBOX;
        CheckBox cbCell = new CheckBox(By.xpath(loc), "Cell CheckBox");
        return cbCell.isChecked();
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getCellCheckBox(int, webdriver.table.IColumnsTags)
     */
    @Override
    public CheckBox getCellCheckBox(int rowNumber, IColumnsTags columnTag) {
        String loc = String.format(cellTemplate, currentTableId, rowNumber, columnTag) + INPUT_TYPE_CHECKBOX;
        return new CheckBox(By.xpath(loc), "Cell CheckBox");
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertRecordExist(webdriver.table.IColumnsTags, java.lang.String)
     */
    @Override
    public void assertRecordExist(IColumnsTags tag, String cellValue) {
        int row = getRowNumber(tag, cellValue);
        AssertJUnit.assertTrue(
                String.format("Record with cell text '%1$s' in column '%2$s' is not present", cellValue, tag), row != 0);
        logger.info(String.format("Record with cell text '%1$s' in column '%2$s' is present", cellValue, tag));
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertRecordAbsent(webdriver.table.IColumnsTags, java.lang.String)
     */
    @Override
    public void assertRecordAbsent(IColumnsTags tag, String cellValue) {
        int row = getRowNumber(tag, cellValue);
        AssertJUnit.assertTrue(
                String.format("Record with cell text '%1$s' in column '%2$s' is present", cellValue, tag), row == 0);
        logger.info(String.format("Record with cell text '%1$s' in column '%2$s' is not present", cellValue, tag));
    }


    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertRowsCount(int)
     */
    @Override
    public void assertRowsCount(int casesCount) {
        AssertJUnit.assertEquals("row's count is wrong", casesCount, getRowsCount());
        logger.info("Row's count is correct");
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#getRowNumberWithoutValue(webdriver.table.IColumnsTags, java.lang.String)
     */
    @Override
    public int getRowNumberWithoutValue(IColumnsTags primaryTag, String value) {
        int countRows = getRowsCount();
        // looking for case
        for (int i = 1; i <= countRows; i++) {
            String cellText = getCellText(i, primaryTag);
            if (!cellText.contains(value)) {
                return i;
            }
        }
        logger.info(String.format("Record in column '%1$s' without cell text '%2$s' is not present", primaryTag, value));
        return 0;
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertCellTextExist(webdriver.table.IColumnsTags, java.lang.String, int)
     */
    @Override
    public void assertCellTextExist(IColumnsTags tag, String cellText, int rowNum) {
        Label cell = new Label(By.xpath(String.format(cellTemplate, currentTableId,
                rowNum, tag.toString())), "cell");
        cell.waitForIsElementPresent();
        Label cellWithText = new Label(By.xpath(String.format(cellTemplate + "/div", currentTableId,
                rowNum, tag.toString())), "cell");
        AssertJUnit.assertTrue(
                String.format("Record with cell text '%1$s' in column '%2$s' and row '%3$d' isn't present",
                        cellText, tag, rowNum), cellWithText.getText().contains(cellText));
        logger.info(String.format("Record in row with number '%1$s' with cell text '%2$s' in column '%3$s' is present",
                rowNum, cellText, tag));
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertCellTextContains(webdriver.table.IColumnsTags, java.lang.String, int)
     */
    @Override
    public void assertCellTextContains(IColumnsTags tag, String cellText, int rowNum) {
        Label cell = new Label(By.xpath(String.format(cellTemplate, currentTableId,
                rowNum, tag.toString())), "cell");
        cell.waitForIsElementPresent();
        Label cellWithText = new Label(By.xpath(String.format(cellTemplate + "/div", currentTableId,
                rowNum, tag.toString())), "cell");
        AssertJUnit.assertTrue(
                String.format("Record with cell text '%1$s' in column '%2$s' and row '%3$d' isn't present.",
                        cellText, tag, rowNum), cellWithText.getText().contains(cellText));
        logger.info(String.format("Record in row with number '%1$s' with cell text '%2$s' in column '%3$s' is present.",
                rowNum, cellText, tag));
    }

    /* (non-Javadoc)
     * @see webdriver.table.ITable#assertRecordExist(webdriver.table.IColumnsTags, java.lang.String, webdriver.table.IColumnsTags, java.lang.String)
     */
    @Override
    public void assertRecordExist(IColumnsTags tag, String historyTableItem, IColumnsTags tag2, String historyTableItem2) {
        int row = getRowNumber(tag, historyTableItem);
        int row2 = getRowNumber(tag2, historyTableItem2);
        AssertJUnit.assertEquals("Cells are placed in different rows", row, row2);
        logger.info(String.format("Record  with cell text '%1$s' in column '%2$s' and cell text '%3$s'" +
                "in column '%4$s' is present", historyTableItem, tag, historyTableItem2, tag2));

    }
}
