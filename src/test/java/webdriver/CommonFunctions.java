package webdriver;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.text.*;
import java.util.*;
import java.util.regex.*;

/**
 * CommonFunctions
 */
public final class CommonFunctions {

	// ==============================================================================================
	// Methods for using Regular expressions

	public static final String MM_DD_YYYY = "MM-dd-yyyy";
	public static final String MMM_YYYY = "MMM-yyyy";
	public static final String MM_YYYY = "MM-yyyy";

    private static final Logger logger = Logger.getInstance();

    private CommonFunctions() {
        // do not instantiate CommonFunctions class
    }

    /**
	 * This method creates a RegExp pattern.
	 * @param regex pattern in a string
	 * @param matchCase should be matching case sensitive?
	 */
	private static Pattern regexGetPattern(String regex, boolean matchCase) {
		int flags;
		if (matchCase) {
			flags = 0;
		} else {
			flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		return Pattern.compile(regex, flags);
	}

	/**
	 * Get first match in the string
	 */
	public static String regexGetMatch(String text, String regex) {
		return regexGetMatch(text, regex, false);
	}

	/**
	 * Get first match in the string
	 */
	public static String regexGetMatch(String text, String regex, boolean matchCase) {
		return regexGetMatchGroup(text, regex, 0, matchCase);
	}

	/**
	 * validate, that string corresponds a pattern
	 */
	public static boolean regexIsMatch(String text, String pattern) {
		return regexIsMatch(text, pattern, false);
	}

	/**
	 * validate, that string corresponds a pattern
	 */
	public static boolean regexIsMatch(String text, String regex, boolean matchCase) {
		Pattern p = regexGetPattern(regex, matchCase);
		Matcher m = p.matcher(text);
		return m.find();
	}

	/**
	 * Get the N-th matching group
	 * @param text - String where we are looking for
	 * @param regex - pattern for which we are looking for
	 * @param groupIndex -Number of matching group we want to find
	 */
	public static String regexGetMatchGroup(String text, String regex, int groupIndex) {
		return regexGetMatchGroup(text, regex, groupIndex, false);
	}

	/**
	 * Get the N-th matching group
	 * @param text - String where we are looking for
	 * @param regex - pattern for which we are looking for
	 * @param groupIndex - Number of matching group we want to find
	 * @param matchCase - Is search case sensitive?
	 */
	public static String regexGetMatchGroup(String text, String regex, int groupIndex, boolean matchCase) {
		Pattern p = regexGetPattern(regex, matchCase);
		Matcher m = p.matcher(text);
		if (m.find()) {
			return m.group(groupIndex);
		} else {
			return null;
		}
	}

	/**
	 * Get the casesCount of groups has been found
	 * @param text - String where we are looking for
	 * @param regex - pattern for which we are looking for * @param matchCase - Is search case sensitive?
	 */
	public static int regexGetNumberMatchGroup(String text, String regex) {
		return regexGetNumberMatchGroup(text, regex, false);
	}

	/**
	 * Get the casesCount of groups has been found
	 * @param text - String where we are looking for
	 * @param regex - pattern for which we are looking for
	 */
	public static int regexGetNumberMatchGroup(String text, String regex, boolean matchCase) {
		int number = 0;
		Pattern p = regexGetPattern(regex, matchCase);
		Matcher m = p.matcher(text);
		while (m.find()) {
			m.group();
			number++;
		}
		return number;
	}

	// ==============================================================================================
	// Methods for using Date

	/**
	 * get current date in the "dd.MM.yyyy" pattern
	 */
	public static String getCurrentDate() {
		return getCurrentDate("dd.MM.yyyy");
	}

	/**
	 * get current date in the custom pattern
	 */
	public static String getCurrentDate(String pattern) {
		return formatDate(new Date(), pattern);
	}

	/**
	 * @param pattern
	 * @return
	 */
	public static String getCurrentDateEnLocale(String pattern) {
		return new SimpleDateFormat(pattern, new Locale("en", "EN")).format(new Date());
	}

	/**
	 * {@link #getCurrentDateEnLocale(String)} with arg "MM-dd-yyyy"
	 * @return
	 */
	public static String getCurrentDateEnLocale() {
		return getCurrentDateEnLocale(MM_DD_YYYY);
	}

	/**
	 * Get the unic suffix based on the current date
	 */
	public static String getTimestamp() {
		return getCurrentDate("yyyyMMddHHmmss");
	}

	/**
	 * Parse string to the Calendar entity
	 * @param s - String to be converted
	 */
	public static Calendar dateString2Calendar(String s) throws ParseException {
		Calendar cal = Calendar.getInstance();
		Date d1 = new SimpleDateFormat("dd.mm.yyyy").parse(s);
		cal.setTime(d1);
		return cal;
	}

	/**
	 * Format date to string using custom pattern
	 * @param date - date to be formatted
	 * @param pattern - custom pattern of the date
	 */
	public static String formatDate(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}
	
	/**
	 * Format date to string using 'en' locale
	 * @param date - date to be formatted
	 * @param pattern - custom pattern of the date
	 */
	public static String formatDateEnLocation(Date date, String pattern) {
		return new SimpleDateFormat(pattern,  new Locale("en", "EN")).format(date);
	}
	/**
	 * Format date in the "dd.MM.yyyy" pattern
	 * @param date - date to be formatted
	 */
	public static Date parseDate(String date) {
		return parseDate(date, "dd.mm.yyyy");
	}

	/**
	 * Parse string to the Date entity
	 * @param date - string to be parsed
	 * @param pattern custom pattern of the date, according to which string should be parsed
	 */
	public static Date parseDate(String date, String pattern) {
		Date result = null;
		try {
			result = new SimpleDateFormat(pattern).parse(date);
		} catch (ParseException e) {
			logger.debug("CommonFunctions.parseDate", e);
		}
		return result;
	}

	/**
	 * Increase date to the N days
	 * @param date date to be increased
	 * @param days casesCount of the days
	 * @return date increased by N days
	 */
	public static Date increaseDateByXDays(final Date date, final int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}

	// ==============================================================================================
	// Methods to escape special symbols

	/**
	 * Escape () and backslash
	 * @param text - text with special symbols
	 * @return escaped text
	 */
	public static String escapeMetaCharacters(final String text) {
		return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
	}

	/**
	 * Escape backslash and quote
	 * @param text - text with special symbols
	 * @return text without backslashes
	 */
	public static String escapeSeleniumCharacters(final String text) {
		return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\\\"");
	}

	/**
	 * Adding \s to the numbers in the string (useful in validation, if there is a space between digits ("99 678" for
	 * example))
	 * @param text - text with numbers
	 * @return escapeSpacesFromNumbers text
	 */
	public static String escapeSpacesFromNumbers(final String text) {
		return text.replaceAll("(\\d)", "$1\\\\s*");
	}

	// ==============================================================================================
	// Environment Methods

	/**
	 * Moves the mouse pointer to the center of screen
	 */
	public static void centerMouse() {
		try {
			Robot robot = new Robot();
			int x = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
			int y = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
			robot.mouseMove(x / 2, y / 2);
		} catch (AWTException e) {
            logger.debug("CommonFunctions.centerMouse", e);
		}
	}

	/**
	 * Away mouse to 0.0 coordinates
	 */
	public static void awayMouse() {
		try {
			Robot robot = new Robot();
			robot.mouseMove(1,1);
		} catch (AWTException e) {
            logger.debug("CommonFunctions.awayMouse", e);
		}
	}

    /**
     * Returns a reference to a file with the specified name that is located
     * somewhere on the path.
     */
    public static File findFileOnPath(final String fileName, final String path) {
		  final String classpath = path;
		  final String pathSeparator = System.getProperty("path.separator");
		  final StringTokenizer tokenizer = new StringTokenizer(classpath, pathSeparator);
		  while (tokenizer.hasMoreTokens()) {
		final String pathElement = tokenizer.nextToken();
		final File directoryOrJar = new File(pathElement);
		final File absoluteDirectoryOrJar = directoryOrJar.getAbsoluteFile();
		if (absoluteDirectoryOrJar.isFile()) {
		    final File target = new File(absoluteDirectoryOrJar.getParent(), fileName);
		    if (target.exists()) {
		  return target;
		    }
		} else {
		    final File target = new File(directoryOrJar, fileName);
		    if (target.exists()) {
		  return target;
		    }
		}
	  }
  return null;
    }



	// =============================================================================
}
