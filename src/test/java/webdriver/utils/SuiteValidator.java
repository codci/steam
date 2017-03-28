package webdriver.utils;

import webdriver.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Checking test suites in given directory for duplicate test names in it (it should be unique inside one suite).
 * Requirements: Java JDK 1.6+, maven 2+, plugin for maven: exec-maven-plugin
 * For check enable it should be specified in pom.xml of project.
 * Example:
 * ...
 * <build>
 *     <plugins>
 *         <plugin>
 *            <groupId>org.codehaus.mojo</groupId>
 *            <artifactId>exec-maven-plugin</artifactId>
 *            <version>1.3</version>
 *            <executions>
 *                <execution>
 *                   <goals>
 *                       <goal>java</goal>
 *                   </goals>
 *                </execution>
 *            </executions>
 *            <configuration>
 *                <classpathScope>test</classpathScope>
 *                <mainClass>webdriver.utils.SuiteValidator</mainClass>
 *                <arguments>
 *                    <argument>${basedir}/src/test/resources</argument>
 *                 </arguments>
 *            </configuration>
 *         </plugin>
 *         ...
 *     </plugins>
 *     ...
 * </build>
 * ...
 * After that SuiteValidator can be run by maven command:
 * mvn clean install exec:java -DskipTests
 */
public class SuiteValidator {

    private static final Logger logger = Logger.getInstance();

    /**
     * Main method, which checked suites for duplicate test names in it
     * @param args should be one arg to directory with test suites
     * @throws Exception If one ore more test cases found with same names - Exception will be throws
     */
    public static void main(String[] args) throws AssertionError {

        // Getting list of suite files in given dir (dir - is first argument)
        List<File> suiteFiles = getSuiteFiles(new File(args[0]));
        int errorsCount = 0; // errors counter
        for (File suiteFile : suiteFiles) {
            boolean isValid = checkSuite(suiteFile);
            if (!isValid) {
                // Increase error counter by one if test suite validation was not successful
                errorsCount++;
            }
        }
        if (errorsCount != 0) {
            // If one of suite checks was unsuccessful - throw exception (detailed log will be printed by checkSuite() method to stdErr output)
            throw new AssertionError("Duplicate test names found");
        }

    }

    /**
     * Get list with File object of suite files, placed on "dir" and nested directories of "dir" (except ".svn" directories).
     * Method searching all files with ".xml" extension, placed in "dir"
     * @param dir Path to directory with suite files (by default, should be "${basedir}/src/test/resources")
     * @return List with file objects of suite files
     */
    public static List<File> getSuiteFiles(File dir) {
        List<File> suiteFiles = new ArrayList<File>();
        for (File suiteFile : dir.listFiles()) {
            // Skipping ".svn" subdirs
            if (!suiteFile.getAbsolutePath().contains(".svn")) {
                // Including only files with ".xml" extension
                if (suiteFile.isFile() && suiteFile.getAbsolutePath().contains(".xml")) {
                    suiteFiles.add(suiteFile);
                } else if (suiteFile.isDirectory()) {
                    suiteFiles.addAll(getSuiteFiles(suiteFile));
                }
            }
        }
        return suiteFiles;
    }

    /**
     * Check that suite file contents only unique names for test cases (attribute "name" of "test" element)
     * @param suiteFile File with test suite (object of type File with XML test suite)
     * @return True - if only unique test names exists in suite. Otherwise - false. Also, method will be print
     *         non-unique test name with absolute path to suite file to stdErr output.
     *         Example of this output:
     *         ERROR: Duplicate tests found with name: 'EMIAS-15797:Создание списка пациентов - добавление из БД по ФИО и ОМС.' in file:
     *         C:\Jenkins\workspace\EMIAS_SuiteValidator_2_5_0\src\test\resources\suites2\Vakcinacia\Vrach.xml
     */
    public static boolean checkSuite(File suiteFile) {
        boolean isValid = true;

        // Class for "test" element in suite
        @XmlRootElement
        class Test {
            String name;

            @XmlAttribute
            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

        // Class for "suite" element of suite
        @XmlRootElement
        class Suite {

            List<Test> testList = new ArrayList<Test>();
            String name;

            @XmlElements({@XmlElement(name="test", type=Test.class)})
            public void setTest(Test test) {
                this.testList.add(test);
            }

            @XmlAttribute
            public void setName(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }

            public List<Test> getTestList() {
                return testList;
            }

        }
        try {
            // Getting JAXBContext instance
            JAXBContext context = JAXBContext.newInstance(Suite.class);
            Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();

            // Convert parsed XML suites to object of Suite class and setting the list of it with objects of Test class
            Suite suite = (Suite) jaxbUnmarshaller.unmarshal(suiteFile);

            // Checking for duplicate test names in suite
            List<String> testNames = new ArrayList<String>();
            for (Test test : suite.getTestList()) {
                String testName = test.getName();
                if (testNames.contains(testName)) {
                    // Print error to stdErr output if duplicate name found and setting false result for returning
                    logger.error("ERROR: Duplicate tests found with name: '" + testName + "' in file: " + suiteFile.getAbsolutePath());
                    isValid = false;
                }
                testNames.add(testName);
            }
        } catch (JAXBException e) {
            logger.info("SuiteValidator.checkSuite", e);
            isValid = false;
        }
        return isValid;
    }

}
