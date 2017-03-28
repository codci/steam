package webdriver.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import webdriver.BaseEntity;
import webdriver.CommonFunctions;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by p.ordenko on 26.03.2014.
 */
public class ExcelUtils extends BaseEntity {

    /**
     * Workbook of document
     */
    private Workbook workBook;

    public static enum FileFormat {
        NEW("xlsx"),
        OLD("xls");

        private String name;

        FileFormat(String fileFormat) {
            this.name = fileFormat;
        }

        public String getName() {
            return this.name;
        }
    }

    /**
     * Constructor for creating new excel file
     * @param fileFormat Excel document format:
     *                   FileFormat.NEW - for Office 2007+ (file extension is XLSX)
     *                   FileFormat.OLD - for Office 2003 and older (file extension is XLS)
     */
    public ExcelUtils(FileFormat fileFormat) {
        if (fileFormat.equals(FileFormat.NEW)) {
            this.workBook = new XSSFWorkbook();
        } else {
            this.workBook = new HSSFWorkbook();
        }
    }

    /**
     * Constructor for working with existing excel file.
     * @param fileName Path to excel file
     */
    public ExcelUtils(String fileName) {
        try {
            if (isFileInOldFormat(fileName)) {
                this.workBook = getOldFormatWorkbook(new FileInputStream(fileName));
            } else {
                this.workBook = getNewFormatWorkbook(new FileInputStream(fileName));
            }
        } catch (Exception ex) {
            logger.debug(this, ex);
        }
    }

    /**
     * Get cell value of sheet by row number and column number
     * @param sheetName Name of existing sheet
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @return String value of cell or empty string if cell is empty.
     */
    public String getCellValue(String sheetName, int rowNum, int colNum) {
        try {
            return this.workBook.getSheet(sheetName).getRow(rowNum).getCell(colNum).toString();
        } catch (NullPointerException npe) {
            logger.debug(this, npe);
            warn("Failed to get cell value at row number '" + String.valueOf(rowNum) + "' and column number '" + colNum + "' on sheet with name '" + sheetName + "'.");
            return "";
        }
    }

    /**
     * Get cell value of sheet by cell address (column name and row number. For example "A4")
     * @param sheetName Name of existing sheet
     * @param cellAddress Cell address (like "A4")
     * @return String value of cell or empty string if cell is empty.
     */
    public String getCellValue(String sheetName, String cellAddress) {
        int[] celIndexes = getRowAndColIndexes(cellAddress);
        try {
            return this.workBook.getSheet(sheetName).getRow(celIndexes[0]).getCell(celIndexes[1]).toString();
        } catch (NullPointerException npe) {
            logger.debug(this, npe);
            warn("Failed to get value of cell '" + cellAddress + "' on sheet with name '" + sheetName + "'.");
            return "";
        }
    }

    /**
     * Get cell value of sheet by row number and column number
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @return String value of cell or empty string if cell is empty.
     */
    public String getCellValue(int sheetIndex, int rowNum, int colNum) {
        return getCellValue(this.workBook.getSheetName(sheetIndex), rowNum, colNum);
    }

    /**
     * Get cell value of sheet by cell address (column name and row number. For example "A4")
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param cellAddress Cell address (like "A4")
     * @return String value of cell or empty string if cell is empty.
     */
    public String getCellValue(int sheetIndex, String cellAddress) {
        return getCellValue(this.workBook.getSheetName(sheetIndex), cellAddress);
    }

    /**
     * Get set of all sheet names in document
     * @return String array of sheet names
     */
    public String[] getSheetNames() {
        int sheetCount = this.workBook.getNumberOfSheets();
        String[] result = new String[sheetCount];
        for (int i = 0; i < sheetCount; i++) {
            result[i] = this.workBook.getSheetName(i);
        }
        return result;
    }

    /**
     * Get all sheet content as string representation by sheet name
     * @param sheetName Name of existing sheet
     * @return Sheet content as string representation
     */
    public String getAllTextOfSheet(String sheetName) {
        int firstRowNum = this.workBook.getSheet(sheetName).getFirstRowNum();
        int lastRowNum = this.workBook.getSheet(sheetName).getLastRowNum();
        StringBuilder result = new StringBuilder();
        for (int i = firstRowNum; i <= lastRowNum; i++){
            int firstCellNum = this.workBook.getSheet(sheetName).getRow(i).getFirstCellNum();
            int lastCellNum = this.workBook.getSheet(sheetName).getRow(i).getLastCellNum();
            for (int j = firstCellNum; j < lastCellNum; j++) {
                try {
                    result.append(this.workBook.getSheet(sheetName).getRow(i).getCell(j));
                } catch (NullPointerException npe) {
                    logger.debug(this, npe);
                    result.append("");
                }
                result.append("\t");
            }
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Get all sheet content as string representation by sheet index
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @return Sheet content as string representation
     */
    public String getAllTestOfSheet(int sheetIndex) {
        return getAllTextOfSheet(this.workBook.getSheetName(sheetIndex));
    }

    /**
     * Get map of existing sheet objects
     * @return Map of existing sheet objects
     */
    public Map<String, Sheet> getAllSheets() {
        Map<String, Sheet> sheetMap = new HashMap<String, Sheet>();
        int sheetCount = this.workBook.getNumberOfSheets();
        for (int i = 0; i < sheetCount; i++) {
            sheetMap.put(this.workBook.getSheetName(i), this.workBook.getSheetAt(i));
        }
        return sheetMap;
    }

    /**
     * Get sheet object by sheet name
     * @param sheetName Name of existing sheet
     * @return Sheet object
     */
    public Sheet getSheetByName(String sheetName) {
        return this.workBook.getSheet(sheetName);
    }

    /**
     * Get sheet object by sheet name
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @return Sheet object
     */
    public Sheet getSheetByIndex(int sheetIndex) {
        return getSheetByName(this.workBook.getSheetName(sheetIndex));
    }

    public String formatLogMsg(String message) {
        return message;
    }

    /**
     * Add new sheet to excel document
     * @param sheetName Name for a new sheet
     */
    public void addSheet(String sheetName) {
        try {
            this.workBook.createSheet(sheetName);
        } catch (Exception ex) {
            logger.debug(this, ex);
            warn("Sheet with name '" + sheetName + "' already exists.");
        }
    }

    /**
     * Add cell with value to sheet with sheet name and cell address
     * @param sheetName Name of sheet where new cell should be placed
     * @param toAddress Cell address with row number and column name (like "B5")
     * @param cellValue Cell value
     */
    public void addCell(String sheetName, String toAddress, String cellValue) {
        int[] cellIndexes = getRowAndColIndexes(toAddress);
        try {
            this.workBook.getSheet(sheetName).getRow(cellIndexes[0]).createCell(cellIndexes[1]).setCellValue(cellValue);
        } catch (Exception ex) {
            logger.debug(this, ex);
            this.workBook.getSheet(sheetName).createRow(cellIndexes[0]).createCell(cellIndexes[1]).setCellValue(cellValue);
        }
    }

    /**
     * Add cell with value to sheet with sheet name and row / column indexes
     * @param sheetName Name of sheet where new cell should be placed
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @param cellValue Cell value
     */
    public void addCell(String sheetName, int rowNum, int colNum, String cellValue) {
        try {
            this.workBook.getSheet(sheetName).getRow(rowNum).createCell(colNum).setCellValue(cellValue);
        } catch (Exception ex) {
            logger.debug(this, ex);
            this.workBook.getSheet(sheetName).createRow(rowNum).createCell(colNum).setCellValue(cellValue);
        }
    }

    /**
     * Add cell with value to sheet with sheet index and cell address
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param toAddress Cell address with row number and column name (like "B5")
     * @param cellValue Cell value
     */
    public void addCell(int sheetIndex, String toAddress, String cellValue) {
        addCell(this.workBook.getSheetName(sheetIndex), toAddress, cellValue);
    }

    /**
     * Add cell with value to sheet with sheet index and row / column indexes
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @param cellValue Cell value
     */
    public void addCell(int sheetIndex, int rowNum, int colNum, String cellValue) {
        addCell(this.workBook.getSheetName(sheetIndex), rowNum, colNum, cellValue);
    }

    /**
     * Remove existing sheet from document by sheet name
     * @param sheetName Name of existing sheet
     */
    public void removeSheet(String sheetName) {
        this.workBook.removeSheetAt(getSheetIndexByName(sheetName));
    }

    /**
     * Remove existing sheet from document by sheet index
     * @param sheetIndex Index of existing sheet (beginning from 0)
     */
    public void removeSheet(int sheetIndex) {
        removeSheet(this.workBook.getSheetName(sheetIndex));
    }

    /**
     * Remove row with row index on sheet with sheet name
     * @param sheetName Name of existing sheet
     * @param rowNum Row index in sheet (beginning from 0)
     */
    public void removeRow(String sheetName, int rowNum) {
        this.workBook.getSheet(sheetName).removeRow(this.workBook.getSheet(sheetName).getRow(rowNum));
    }

    /**
     * Remove row with row index on sheet with sheet index
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param rowNum Row index in sheet (beginning from 0)
     */
    public void removeRow(int sheetIndex, int rowNum) {
        removeRow(this.workBook.getSheetName(sheetIndex), rowNum);
    }

    /**
     * Save current state of document to file
     * @param fileName File name :)
     */
    public void save(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            this.workBook.write(fos);
            fos.close();
        } catch (Exception ex) {
            logger.debug(this, ex);
            warn("Can't create Excel file with name '" + fileName + "'.");
        }
    }

    /**
     * Change cell value, located with cell address (row number and column name) on sheet with sheet name
     * @param sheetName Name of existing sheet
     * @param cellAddress Cell address (like "A4")
     * @param newValue New string value for cell
     */
    public void modifyCellValue(String sheetName, String cellAddress, String newValue) {
        int[] cellIndexes = getRowAndColIndexes(cellAddress);
        this.workBook.getSheet(sheetName).getRow(cellIndexes[0]).getCell(cellIndexes[1]).setCellValue(newValue);
    }

    /**
     * Change cell value, located with row / column indexes on sheet with sheet name
     * @param sheetName Name of existing sheet
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @param newValue New string value for cell
     */
    public void modifyCellValue(String sheetName, int rowNum, int colNum, String newValue) {
        this.workBook.getSheet(sheetName).getRow(rowNum).getCell(colNum).setCellValue(newValue);
    }

    /**
     * Change cell value, located with cell address (row number and column name) on sheet with sheet index
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param cellAddress Cell address (like "A4")
     * @param newValue New string value for cell
     */
    public void modifyCellValue(int sheetIndex, String cellAddress, String newValue) {
        modifyCellValue(this.workBook.getSheetName(sheetIndex), cellAddress, newValue);
    }

    /**
     * Change cell value, located with row / column indexes on sheet with sheet index
     * @param sheetIndex Index of existing sheet (beginning from 0)
     * @param rowNum Row index in sheet (beginning from 0)
     * @param colNum Column index in sheet (beginning from 0)
     * @param newValue New string value for cell
     */
    public void modifyCellValue(int sheetIndex, int rowNum, int colNum, String newValue) {
        modifyCellValue(this.workBook.getSheetName(sheetIndex), rowNum, colNum, newValue);
    }

    /**
     * Get rows count on sheet with name
     * @param sheetName Name of sheet for counting rows
     * @return Rows count
     */
    public int getRowsCount(String sheetName) {
        return this.workBook.getSheet(sheetName).getLastRowNum() + 1;
    }

    /**
     * Get rows count on sheet with index
     * @param sheetIndex Index of sheet for counting rows (beginning from 0)
     * @return Rows count
     */
    public int getRowsCount(int sheetIndex) {
        return getRowsCount(this.workBook.getSheetName(sheetIndex));
    }

    /**
     * Get columns count on sheet with name
     * @param sheetName Name of sheet for counting columns
     * @return Columns count
     */
    public int getColsCount(String sheetName) {
        int result = 0;
        int totalRows = getRowsCount(sheetName);
        Sheet tmpSheet = this.workBook.getSheet(sheetName);
        for (int i = 0; i < totalRows; i++) {
            try {
                int tmpColsCount = tmpSheet.getRow(i).getLastCellNum();
                if (tmpColsCount > result) {
                    result = tmpColsCount;
                }
            } catch (NullPointerException npe) {
                logger.debug(this, npe);
                // ignore. It means that in current row no cells with values
            }
        }
        return result;
    }

    /**
     * Get columns count on sheet with index
     * @param sheetIndex Index of sheet for counting columns (beginning from 0)
     * @return Columns count
     */
    public int getColsCount(int sheetIndex) {
        return getColsCount(this.workBook.getSheetName(sheetIndex));
    }

    /**
     * Get columns count in row
     * @param sheetName Name of sheet for counting columns
     * @param rowNumber Row number for columns count (beginning from 0)
     * @return Columns count
     */
    public int getColsCountInRow(String sheetName, int rowNumber) {
        int result = 0;
        try {
            result = this.workBook.getSheet(sheetName).getRow(rowNumber).getLastCellNum();
        } catch (NullPointerException npe) {
            logger.debug(this, npe);
            // ignore. It means that in current row no cells with values
        }
        return result;
    }

    /**
     * Get columns count in row
     * @param sheetIndex Index of sheet for counting columns (beginning from 0)
     * @param rowNumber Row number for columns count (beginning from 0)
     * @return Columns count
     */
    public int getColsCountInRow(int sheetIndex, int rowNumber) {
        return getColsCountInRow(this.workBook.getSheetName(sheetIndex), rowNumber);
    }

    //
    // Private methods
    //

    /**
     * Retrieve sheet index in workbook by sheet name
     * @param sheetName Name of existing sheet
     * @return
     */
    private int getSheetIndexByName(String sheetName) {
        return this.workBook.getSheetIndex(sheetName);
    }

    /**
     * Get workbook object of old-format Excel file (before Office 2007)
     * @param fs File input stream with excel file on disk (*.xls)
     * @return Workbook object
     * @throws IOException If file not found
     */
    private Workbook getOldFormatWorkbook(FileInputStream fs) throws IOException {
        return new HSSFWorkbook(fs);
    }

    /**
     * Get workbook object of new-format Excel file (Office 2007 and newest)
     * @param fs File input stream with excel file on disk (*.xlsx)
     * @return Workbook object
     * @throws IOException If file not found
     */
    private Workbook getNewFormatWorkbook(FileInputStream fs) throws IOException, InvalidFormatException {
        return new XSSFWorkbook(fs);
    }

    /**
     * Check if Excel file in old format (*.xls)
     * @param fileName Excel file name with extension
     * @return True if Excel file in old format and False otherwise
     */
    private boolean isFileInOldFormat(String fileName) {
        String[] str = fileName.split("\\.");
        return "xls".equals(str[str.length - 1]);
    }

    /**
     * Get column index by it name (For example: "ABC")
     * @param colName Name of column (like "ABC")
     * @return Index of column
     */
    private int getColIndexByName(String colName) {
        String colNameLc = colName.toLowerCase();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int alphabetLength = alphabet.length();
        int cellNameLengthCount = colNameLc.length() - 1;
        int resultIdx = 0;
        for (char currChar : colNameLc.toCharArray()) {
            int curIdx = alphabet.indexOf(currChar) + 1;
            resultIdx = resultIdx + curIdx * (int) Math.pow(alphabetLength, cellNameLengthCount);
            cellNameLengthCount--;
        }
        return resultIdx - 1;
    }

    /**
     * Get row and column indexes from cell address (like "AD12")
     * @param cellAddress Cell address (like "AD12")
     * @return int array with indexes:
     * 0 - row index
     * 1 - column index
     */
    private int[] getRowAndColIndexes(String cellAddress) {
        int[] result = new int[2];

        // Row index
        result[0] = Integer.parseInt(CommonFunctions.regexGetMatch(cellAddress, "\\d+")) - 1;

        // Column index
        result[1] = getColIndexByName(CommonFunctions.regexGetMatch(cellAddress, "[a-zA-Z]+"));
        return result;
    }

}
