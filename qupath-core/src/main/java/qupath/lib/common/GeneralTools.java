/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package qupath.lib.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of generally useful static methods.
 * 
 * @author Pete Bankhead
 *
 */
public class GeneralTools {
	
	final private static Logger logger = LoggerFactory.getLogger(GeneralTools.class);
	
	/**
	 * Check if a string is blank, i.e. it is null or its length is 0.
	 * @param s
	 * @param trim If true, any string will be trimmed before its length checked.
	 * @return True if the string is null or empty.
	 */
	public static boolean blankString(final String s, final boolean trim) {
		return s == null || s.trim().length() == 0;
	}

	/**
	 * Check if a string is blank, i.e. it is null or its length is 0.
	 * @param s
	 * @return True if the string is null or empty.
	 */
	public static boolean blankString(final String s) {
		return s == null || s.length() == 0;
	}
	
	/**
	 * Escape backslashes in an absolute file path - useful when scripting.
	 * 
	 * @param path
	 * @return
	 */
	public static String escapeFilePath(final String path) {
		return path.replace("\\", "\\\\");
	}
	
	/**
	 * Clip a value to be within a specific range.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static int clipValue(final int value, final int min, final int max) {
		return value < min ? min : (value > max ? max : value);
	}
	
	/**
	 * Clip a value to be within a specific range.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static double clipValue(final double value, final double min, final double max) {
		return value < min ? min : (value > max ? max : value);
	}

	/**
	 * Test if two doubles are approximately equal, within a specified tolerance;
	 * the absolute difference is divided by the first of the numbers before the tolerance is checked.
	 * @param n1
	 * @param n2
	 * @param tolerance
	 * @return
	 */
	public static boolean almostTheSame(double n1, double n2, double tolerance) {
		return Math.abs(n1 - n2)/n1 < tolerance;
	}

	/**
	 * Convert a double array to string, with a specified number of decimal places; trailing zeros are
	 * not included.
	 * @param array
	 * @param nDecimalPlaces
	 * @return
	 */
	public static String arrayToString(double[] array, int nDecimalPlaces) {
		StringBuilder sb = new StringBuilder();
		if (array.length == 0)
			return "";
		DecimalFormat df = getFormatter(nDecimalPlaces);
		for (int i = 0; i < array.length; i++) {
			sb.append(df.format(array[i]));
			if (i < array.length)
				sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * Convert a String array to a single string, with a specified separator string.
	 * @param array
	 * @param nDecimalPlaces
	 * @return
	 */
	public static String arrayToString(Object[] array, String separator) {
		StringBuilder sb = new StringBuilder();
		if (array.length == 0)
			return "";
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (i < array.length - 1)
				sb.append(separator);
		}
		return sb.toString();
	}
	
	
	/**
	 * Split new lines (in a cross-platform way... i.e. not with s.split("\n"), which is asking for trouble).
	 * 
	 * @param s
	 * @return
	 */
	public static String[] splitLines(final String s) {
		List<String> lines = new ArrayList<>();
		try (Scanner scanner = new Scanner(s)) {
			while (scanner.hasNextLine())
				lines.add(scanner.nextLine());
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	
	/**
	 * Get a DecimalFormat that may be used to convert a number to have a maximum of nDecimalPlaces
	 * (trailing zeros are not shown).
	 * @param nDecimalPlaces
	 * @return
	 */
	public static DecimalFormat getFormatter(final int nDecimalPlaces) {
		switch (nDecimalPlaces) {
		case 0: return new DecimalFormat("#."); // TODO: Check if this is correct!
		case 1: return new DecimalFormat("#.#");
		case 2: return new DecimalFormat("#.##");
		case 3: return new DecimalFormat("#.###");
		case 4: return new DecimalFormat("#.####");
		case 5: return new DecimalFormat("#.#####");
		case 6: return new DecimalFormat("#.######");
		case 7: return new DecimalFormat("#.#######");
		case 8: return new DecimalFormat("#.########");
		case 9: return new DecimalFormat("#.#########");
		default:
			StringBuilder sb = new StringBuilder();
			sb.append("#.");
			for (int i = 0; i < nDecimalPlaces; i++)
				sb.append("#");
			return new DecimalFormat(sb.toString());
		}
	}

	// TODO: MOVE SOMEWHERE SENSIBLE
	public static Map<String, String> parseArgStringValues(String s) {
		if (s == null)
			return Collections.emptyMap();

		s = s.trim();
		if (!s.startsWith("{")) { // || !s.startsWith("{")) { // Don't know what I was planning here...?
			throw new IllegalArgumentException(s + " is not a valid parameter string!");
		}

		// Minimum length of 7, for {"a":1}
		if (s.replace(" ",  "").length() < 7 || !s.contains("\"") || !s.contains(":"))
			return Collections.emptyMap();

		//		logger.info(s);
		//		s = s.substring(s.indexOf("\""), s.lastIndexOf("\"") + 1);

		Map<String, String> map = new LinkedHashMap<>();


		String quotedString = "\"([^\"]*)\"[\\s]*";
		// Match quoted strings, followed by : (potential keys)
		Pattern p = Pattern.compile(quotedString + ":");
		// Match everything up to the next potential key, or to the end brace }
		Pattern p2 = Pattern.compile(".+?(?=("+quotedString+"[:])|})");

		Scanner scanner = new Scanner(s);
		scanner.useDelimiter("");
		String key;
		while ((key = scanner.findInLine(p)) != null) {

			// Trim the key
			key = key.substring(key.indexOf("\"")+1, key.lastIndexOf("\""));

			String value = scanner.findInLine(p2);
			if (value == null) {
				logger.error("Unable to associated value with key {}", key);
				continue;
			}

			// Trim any whitespace, commas or quotation marks
			value = value.trim();
			if (value.endsWith(","))
				value = value.substring(0, value.length()-1);
			if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
				value = value.substring(value.indexOf("\"")+1, value.lastIndexOf("\""));
			}

			if (value.length() == 0) {
				System.err.println("Unable to associated value with key " + key);
				continue;
			}

			// Decide what we have & put into the map
			map.put(key, value);
		}
		scanner.close();

		return map;
	}

	
	/**
	 * Get a string to represent um (but with the proper 'mu' symbol)
	 * 
	 * @return
	 */
	public final static String micrometerSymbol() {
		return '\u00B5' + "m";
	}

	/**
	 * Check if a collection returns at least one object of a specified class.
	 * 
	 * @param collection
	 * @param cls
	 * @return true if an object is contained within the collection that is an instance of the specified class (including subclasses), false otherwise
	 */
	public static boolean containsClass(Collection<?> collection, Class<?> cls) {
		if (collection == null)
			return false;
		for (Object o : collection) {
			if (cls.isInstance(o))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Read the entire contents of a file into a single String.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String readFileAsString(final String path) throws IOException {
		Scanner scanner = new Scanner(new File(path));
		String contents = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return contents;
	}
	
	
	/**
	 * Read the entire contents of an InputStream into a single String.
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String readInputStreamAsString(final InputStream stream) throws IOException {
		Scanner scanner = new Scanner(stream);
		String contents = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return contents;
	}
	

	/**
	 * Check whether a path ends with one of a number of specified extensions (case insensitive).
	 * 
	 * @param path
	 * @param extensions
	 * @return
	 */
	public static boolean checkExtensions(final String path, String... extensions) {
		String pathLower = path.toLowerCase();
		for (String ext : extensions) {
			if (!ext.startsWith("."))
				ext = "." + ext;
			if (pathLower.endsWith(ext))
				return true;
		}
		return false;
	}

	public static boolean isMac() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0;
	}

	public static boolean isLinux() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("nux") >= 0;
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("win") >= 0;
	}

}
