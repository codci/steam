package webdriver.table;

import webdriver.elements.CheckBox;

public interface ITable {

	/**
	 * Get array records. 
	 * @param tag - column of table.
	 * @return resultArray
	 */
	String[] getRecordsArray(IColumnsTags tag);

	/**
	 * @param rowNumber
	 * @return row text by row number
	 */
	String getTextByRowNumber(int rowNumber);

	/**
	 * looking row contains text in specified column
	 * 
	 * @param columnTag
	 * @param cellText
	 * @return row number, contains current text
	 */
	int getRowNumber(IColumnsTags columnTag, String cellText);

	/**
	 * Looking for row by two cells
	 * 
	 * @param primaryTag
	 *            - first column
	 * @param primText
	 *            - column's cell should contain this text
	 * @param secondaryTag
	 *            - secondary column
	 * @param secondText
	 *            - column's cell should or not contain this text
	 * @param shouldExceptRowWithSeconText
	 *            - if true - will except rows contains both primText and secondText, false - will find row containing both cell
	 * 
	 * @return Row Number
	 */
	int getRowNumber(IColumnsTags primaryTag, String primText, IColumnsTags secondaryTag,
                     String secondText,
                     boolean shouldExceptRowWithSeconText);

	/**
	 * @return RowsCount in table (tbodyTemplate, currentTableId) 
	 *
	 */
	int getRowsCount();

	/**
	 * choosing random row from all available rows <br>
	 * (<i> using for 'search' cases</i>)
	 * 
	 * @return row number
	 */
	int getRandomRowNumber();

	/**
	 * 
	 * @param rowNumber
	 * @param columnTag
	 * @return cell text by row number and column name
	 */
	String getCellText(int rowNumber, IColumnsTags columnTag);

	/**
	 * 
	 * @param rowNumber
	 * @param columnTag
	 * @param tbodyNum - tbody[??]
	 * @return cell text by row number and column name
	 */
	String getCellText(int rowNumber, IColumnsTags columnTag, int tbodyNum);

	/**
	 * clicking cell using At
	 * 
	 * @param rowNumber
	 * @param columnTag
	 */
	void clickCell(int rowNumber, IColumnsTags columnTag);

	/**
	 * return state of checkbox
	 * 
	 * @param rowNumber
	 * @param columnTag
	 * @return
	 */
	boolean getCellCheckBoxState(int rowNumber, IColumnsTags columnTag);

	/**
	 * get checkbox in cell
	 * 
	 * @param rowNumber
	 * @param columnTag
	 * @return checkbox founded in cell
	 */
	CheckBox getCellCheckBox(int rowNumber, IColumnsTags columnTag);

	/**
	 * assertion on existince
	 * 
	 * @param cellValue
	 *            value in cell
	 * @param tag
	 *            column tag
	 */
	void assertRecordExist(IColumnsTags tag, String cellValue);

	/**
	 * assertion on absence
	 * 
	 * @param cellValue
	 *            value in cell
	 * @param tag
	 *            column tag
	 */
	void assertRecordAbsent(IColumnsTags tag, String cellValue);

	/**
	 * aserting rows count
	 * 
	 * @param casesCount
	 */
	void assertRowsCount(int casesCount);

	/**
	 * Get first record without value in column
	 * 
	 * @param primaryTag
	 *            - column without special value
	 * @param value
	 * @return Row Number
	 */
	int getRowNumberWithoutValue(IColumnsTags primaryTag, String value);

	/**
	 * Assert text in cell
	 * 
	 * @param tag
	 *            column tag
	 * @param cellText
	 *            - value in cell
	 * @param rowNum
	 *            - number row
	 */
	void assertCellTextExist(IColumnsTags tag, String cellText, int rowNum);

	/**
	 * Assert text contains in cell.
	 * 
	 * @param tag
	 *            column tag
	 * @param cellText
	 *            - value in cell
	 * @param rowNum
	 *            - number row
	 */
	void assertCellTextContains(IColumnsTags tag, String cellText, int rowNum);

	void assertRecordExist(IColumnsTags tag, String historyTableItem, IColumnsTags tag2,
                           String historyTableItem2);

}