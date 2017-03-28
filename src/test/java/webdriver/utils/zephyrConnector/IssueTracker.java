package webdriver.utils.zephyrConnector;

import webdriver.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Public class for working with properties file for zephyr issues
 */
public class IssueTracker {
    static String directoryLoc;
    static File tsFile;
    static Map<String, String> hMap;
    static boolean changeInIssueIdentifierFile = false;
    static boolean newFileCreated = false;
    private static final Logger logger = Logger.getInstance();


    public IssueTracker() throws ZephyrJiraException {
        directoryLoc = getAppdataDirectory();
        hMap = new HashMap<String, String>();
        File parentDirectory = new File(directoryLoc);
        try {
            if (!parentDirectory.isDirectory() && !parentDirectory.exists()) {
                parentDirectory.mkdirs();
            }
        } catch (Exception e) {
            logger.debug(this, e);
            throw new ZephyrJiraException("Error while creating folder at " + parentDirectory.getAbsolutePath() + "\n" + e.getMessage());
        }
    }

    /**
     * @return path to directory where zephyr properties would be created
     */
    public static String getAppdataDirectory() {
        return System.getProperty("basedir",System.getProperty("user.dir")) + File.separator + "src" + File.separator + "test" + File.separator + "resources";
    }

    /**
     * Create zephyr issue properties file
     * @param tcName TestCycle name
     * @throws Exception
     */
    void createFile(String tcName) throws ZephyrJiraException {
        directoryLoc = getAppdataDirectory();
        hMap = new HashMap<String, String>();
        tsFile = new File(directoryLoc + File.separator + tcName + ".properties");
        if (!tsFile.exists() || tsFile.length() == 0) {
            try {
                tsFile.createNewFile();
                newFileCreated = true;
            } catch (IOException e) {
                logger.debug(this, e);
                throw new ZephyrJiraException("Error while creating file: " + tsFile + "\n" + e.getMessage());
            }
        } else {
            newFileCreated = false;
        }
    }

    /**
     * Adds value to properties file
     * @param key test issue summary
     * @param val test id
     */
    void addToHashMap(String key, String val) {
        changeInIssueIdentifierFile = true;
        hMap.put(key, val);
    }

    /**
     * Reads value to properties file
     * @param key test issue summary
     * @return id
     */
    String getValFromHashMap(String key) {
        if (hMap.containsKey(key)) {
            return hMap.get(key);
        }
        return null;
    }

    /**
     * Work with zephyr issue properties file, makes hashmap from file
     * @throws Exception
     */
    void populateHashMapFromTsFile() throws ZephyrJiraException {
        String[] arr;
        try {
            BufferedReader br = new BufferedReader(new FileReader(tsFile));
            for (String line; (line = br.readLine()) != null; ) {
                if (line.contains("=")) {
                    arr = line.split("=", 2);
                    hMap.put(arr[0].trim(), arr[1].trim());
                }
            }

            br.close();
        } catch (Exception e) {
            logger.debug(e);
            throw new ZephyrJiraException("Error while reading file and populating hashmap. File :" + tsFile.getAbsolutePath() + "\n" + e.getMessage());
        }
    }

    /**
     * Writes hasmap to properties file like this:
     * test summary = test id
     * @throws Exception
     */
    void populateTsFileFromHashMap() throws ZephyrJiraException {
        if (changeInIssueIdentifierFile) {
            FileWriter fileWriter;
            try {

                fileWriter = new FileWriter(tsFile);
                fileWriter.close();
                fileWriter = new FileWriter(tsFile, true);
            } catch (IOException e1) {
                logger.debug(this, e1);
                throw new ZephyrJiraException("Error while clearing file content. " + e1.getMessage());
            }
            BufferedWriter bufferFileWriter = new BufferedWriter(fileWriter);
            for (String key : hMap.keySet()) {
                String val = hMap.get(key);
                try {
                    fileWriter.append(key).append("=").append(val).append(System.getProperty("line.separator"));
                } catch (IOException e) {
                    logger.debug(this, e);
                    throw new ZephyrJiraException("Error while writing to file. File :" + tsFile + "\n" + e.getMessage());
                }
            }
            try {
                bufferFileWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                logger.debug(this, e);
            }
        }
    }
}