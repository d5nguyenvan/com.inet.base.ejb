/*****************************************************************
   Copyright 2006 by Dung Nguyen (dungnguyen@truthinet.com)

   Licensed under the iNet Solutions Corp.,;
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.truthinet.com/licenses

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 *****************************************************************/
package com.inet.base.ejb.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * StringUtils.
 *
 * @author <a href="mailto:dungnguyen@truthinet.com">Dung Nguyen</a>
 * @version $Id: StringUtils.java 2009-07-30z 14:44:40z AM nguyen_dv $
 *
 * @since 1.0
 */
public final class StringUtils {
  /** the empty string. */
  public static final String EMPTY_STRING = "";
  /** the not answer string. */
  public static final String NA_STRING = "N/A";
  /** comma character. */
  public static final char COMMA = ',';
  /** comma string. */
  public static final String COMMA_STR = ",";
  /** escape character. */
  public static final char ESCAPE = '\\';
  /** equal character. */
  public static final char EQUALS = '=';
  /** blank value. */
  public static final char BLANK = ' ';
  /** C NULL String. */
  public static final byte C_NULL_STRING = 0;
  /* the file seperator. */
  private static final String FILE_SEPARATOR = System.getProperty("file.separator");
  /* folder separator. */
  private static final String FOLDER_SEPARATOR = "/";
  /* windows folder separator. */
  private static final String WINDOWS_FOLDER_SEPARATOR = "\\";
  /* top path. */
  private static final String TOP_PATH = "..";
  /* current path. */
  private static final String CURRENT_PATH = ".";

  /**
   * Returns if the string is not {@code null}.
   *
   * @param field the given string to test.
   * @return if the string is not {@code null}.
   */
  public static boolean isset(final String field) {
    return (field != null);
  }

  /**
   * Returns if the string is empty string.
   *
   * @param field the given string to test.
   * @return if the string is empty string.
   */
  public static boolean isEmpty(final String field) {
    return (field != null && field.isEmpty());
  }

  /**
   * Returns if the string is not {@code null} and not empty.
   *
   * @param field the given string to test.
   * @return if the string is not {@code null} and not empty.
   */
  public static boolean hasLength(final String field) {
    return (field != null && !field.isEmpty());
  }

  /**
   * Get part value of field from the given data and field name.
   *
   * Field format: the collection of pair (name,value) separate by '=' signature. For example:
   * name1=value1,name2=value2 A B
   *
   * @param data the given data that stored the field value.
   * @param field the given field name.
   * @param separate the given separate character.
   * @return the field data.
   */
  public static String getPartValue(final String data, final String field, final char separate) {
    // the data is null.
    if (data == null) {
      return StringUtils.EMPTY_STRING;
    }

    // get startPos.
    int startPos = StringUtils.getRightField(data, field, separate);

    // the data could not contain that field.
    if (startPos < 0) {
      return StringUtils.EMPTY_STRING;
    }

    // get data length.
    int length = data.length();

    // move startPos
    startPos += field.length();

    // check the data.
    while ((data.charAt(startPos) == StringUtils.BLANK) && (length > startPos++)) {
      ;
    }

    // could not find the equal.
    if ((startPos >= length) || data.charAt(startPos) != StringUtils.EQUALS) {
      return StringUtils.EMPTY_STRING;
    }

    // find the end position.
    int endPos = data.indexOf(StringUtils.EQUALS, startPos + 1);

    // end of data.
    if (endPos < 0) {
      endPos = length;
    } else {
      while ((data.charAt(endPos) != separate) && (endPos-- > 0)) {
        ;
      }
    }

    // invalid data.
    if (startPos >= endPos) {
      return StringUtils.EMPTY_STRING;
    }

    return data.substring(startPos + 1, endPos);
  }

  // //////////////////////////////////////////////////////////////////////////
  // Get display value from the Object value. CHECK NULL OBJECT
  //
  /**
   * Get the display of this String value, if value=null then return empty string.
   *
   * @param value the given value need display
   * @return the value as string or empty string.
   * @see StringUtils#getValue(String, String)
   */
  public static String getValue(final String value) {
    return getValue(value, StringUtils.EMPTY_STRING);
  }

  /**
   * Get the display of this String value, if value=null then return default string value.
   *
   * @param value the given value need display
   * @param defaultValue the given default value if the input value is null
   * @return the value as string or default value when value is null.
   */
  public static String getValue(final String value, final String defaultValue) {
    return (value == null ? defaultValue : value);
  }

  /**
   * Get the display of this Long value, if value=null then return empty string.
   *
   * @param value the given value need display
   * @return the value as string or empty string.
   * @see StringUtils#getValue(Long, String)
   */
  public static String getValue(final Long value) {
    return StringUtils.getValue(value, StringUtils.EMPTY_STRING);
  }

  /**
   * Get the display of this Long value, if value=null then return default string value.
   *
   * @param value the given value need display
   * @param defaultValue the given default value if the input value is null
   * @return the value as string or default value.
   */
  public static String getValue(final Long value, final String defaultValue) {
    return (value == null ? defaultValue : String.valueOf(value));
  }

  /**
   * Get the display of this Integer value, if value=null then return empty string.
   *
   * @param value : the given value need display
   * @return the value as string or empty string.
   * @see StringUtils#getValue(Integer, String)
   */
  public static String getValue(final Integer value) {
    return StringUtils.getValue(value, StringUtils.EMPTY_STRING);
  }

  /**
   * Get the display of this Integer value, if value=null then return default string value.
   *
   * @param value the given value need display
   * @param defaultValue the given default value if the input value is null
   * @return the value as string or default value.
   */
  public static String getValue(final Integer value, final String defaultValue) {
    return (value == null ? defaultValue : String.valueOf(value));
  }

  /**
   * Returns the string representation of the given {@code bytes}.
   *
   * @param bytes the given bytes array to convert.
   * @param start the given start index, inclusively.
   * @param end the given end index, exclusively.
   * @return hex string representation as {@code bytes}.
   */
  public static String bytesToHexString(final byte[] bytes, int start, int end) {
    if (bytes == null || bytes.length == 0) {
      return EMPTY_STRING;
    }

    final StringBuilder builder = new StringBuilder((end - start) << 1);
    for (int index = start; index < end; index++) {
      builder.append(String.format("%02x", bytes[index]));
    }

    return builder.toString();
  }

  /**
   * Returns the array of bytes representation of the given {@code hex}.
   *
   * @param hex the given hex string to convert.
   * @return the array of bytes.
   */
  public static byte[] hexStringToBytes(final String hex) {
    final byte[] bytes = new byte[hex.length() >> 1];

    for (int index = 0; index < bytes.length; index ++) {
      bytes[index] = (byte) Integer.parseInt(hex.substring(index << 1, 2 + (index << 1)), 16);
    }

    return bytes;
  }

  /**
   * Returns the string representation as {@code strs}.
   *
   * @param strs the given list of string to convert.
   * @return the string representation as array of strings.
   */
  public static String arrayToString(final String[] strs) {
    return arrayToString(strs, ESCAPE, COMMA);
  }

  /**
   * Returns the string representation as {@code strs}.
   *
   * @param strs the given list of string to convert.
   * @param escapeChar the escape character.
   * @param separator the separator character.
   * @return the string representation as array of strings.
   */
  public static String arrayToString(final String[] strs, char escapeChar, char separator) {
    if (strs == null || strs.length == 0) {
      return EMPTY_STRING;
    }

    final StringBuilder builder = new StringBuilder();
    builder.append(escapeString(strs[0], escapeChar, separator));
    for(int index = 1; index < strs.length; index++) {
      if (strs[index] == null || EMPTY_STRING.equals(strs[index])) {
        continue;
      }

      builder.append(',');
      builder.append(escapeString(strs[index], escapeChar, separator));
    }

    return builder.toString();
  }

  /**
   * Returns the string representation of the given {@code bytes}.
   *
   * @param bytes the given bytes array to convert.
   * @return hex string representation as {@code bytes}.
   */
  public static String bytesToHexString(final byte[] bytes) {
    return bytesToHexString(bytes, 0, bytes.length);
  }

  /**
   * Returns the array of Strings separated by the default delimited String.
   *
   * @param src the given source string to split.
   * @return the array of string data.
   */
  public static String[] getStrings(final String src) {
    return getStrings(src, ESCAPE, COMMA);
  }

  /**
   * Returns the array of Strings separate by the {@code delim}.
   *
   * @param src the given source string to split.
   * @param delim the given delimited string.
   * @return the array of Strings.
   */
  public static String[] getStrings(final String src, char escapeChar, char separator) {
    // convert to list of strings.
    final String[] strs = split(src, escapeChar, separator);

    // un escape data.
    final List<String> strings = new ArrayList<String>(strs.length);
    for (final String str : strs) {
      strings.add(unEscapeString(str, escapeChar, separator));
    }

    return strings.toArray(new String[strings.size()]);
  }

  /**
   * Returns the collection of strings from the given default delimited.
   *
   * @param src the given source string to split.
   * @return the collection as Strings; never {@code null}
   */
  public static Collection<String> getStringCollection(final String src) {
    return getStringCollection(src, COMMA_STR);
  }

  /**
   * Returns the collection of strings.
   *
   * @param src the given source string to split.
   * @param delim the given string delimited.
   * @return the collection as Strings; never {@code null}.
   */
  public static Collection<String> getStringCollection(final String src,
      final String delim) {
    if (src == null || EMPTY_STRING.equals(src)) {
      return Collections.emptyList();
    }

    final StringTokenizer tokenizer = new StringTokenizer(src, delim);
    final List<String> values = new ArrayList<String>();
    while(tokenizer.hasMoreTokens()) {
      values.add(tokenizer.nextToken());
    }
    return values;
  }

  /**
   * Split a given string.
   *
   * @param str the source string.
   * @return the array of none empty string.
   */
  public static String[] split(final String str) {
    return split(str, ESCAPE, COMMA);
  }

  /**
   * Split a given string using the given separator.
   *
   * @param str the given string to split.
   * @param escapeChar the given escape character.
   * @param separator the given separator.
   * @return the array of none empty string.
   */
  public static String[] split(final String str, char escapeChar, char separator) {
    // null string.
    if (str == null) {
      return null;
    }

    // create storage to store data.
    final List<String> strings = new ArrayList<String>();

    // split data.
    final StringBuilder split = new StringBuilder();
    int index = 0;
    while ((index = findNext(str, separator, escapeChar, index, split)) >= 0) {
      ++index;
      strings.add(split.toString());
      split.setLength(0); // reset buffer.
    }

    // add the last string.
    strings.add(split.toString());

    // remove empty character.
    final Iterator<String> iterator = strings.iterator();
    while (iterator.hasNext()) {
      if (EMPTY_STRING.equals(iterator.next())) {
        iterator.remove();
      }
    }

    // return split data.
    return strings.toArray(new String[strings.size()]);
  }

  /**
   * Finds the first occurrence of the separator character ignoring the escaped separators
   * starting from the index.
   *
   * @param str the source string.
   * @param separator the separator character.
   * @param escapeChar the escape character.
   * @param start from where to search.
   * @param split used to pass back the extracted string.
   * @return the first occurrence separator; or -1 if the separator could not be found.
   */
  public static int findNext(final String str, char separator, char escapeChar,
      int start, final StringBuilder split) {
    int numPreEscapes = 0;

    for (int index = start; index < str.length(); index++) {
      char ch = str.charAt(index);
      if (numPreEscapes == 0 && ch == separator) { // separator.
        return index;
      } else {
        split.append(ch);
        numPreEscapes = (ch == escapeChar) ? (++numPreEscapes) % 2 : 0;
      }
    }

    return -1;
  }

  /**
   * Check the given character is contains on the list of characters.
   *
   * @param character the given character to check.
   * @param chars the given list of characters.
   * @return if the given character is contained on the list of characters.
   */
  private static boolean hasChar(char character, char...chars) {
    if (chars == null) {
      return false;
    }

    for (char ch : chars) {
      if (ch == character) {
        return true;
      }
    }

    return false;
  }

  /**
   * Replace all occurrences of a substring within a string with another string.
   *
   * @param inString String to examine
   * @param oldPattern String to replace
   * @param newPattern String to insert
   *
   * @return a String with the replacements
   */
  public static String replace(String inString, String oldPattern, String newPattern) {
    if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
      return inString;
    }

    final StringBuffer sbuf = new StringBuffer();

    // output StringBuffer we'll build up
    int pos = 0; // our position in the old string
    int index = inString.indexOf(oldPattern);

    // the index of an occurrence we've found, or -1
    int patLen = oldPattern.length();
    while (index >= 0) {
      sbuf.append(inString.substring(pos, index));
      sbuf.append(newPattern);
      pos = index + patLen;
      index = inString.indexOf(oldPattern, pos);
    }
    sbuf.append(inString.substring(pos));

    // remember to append any characters to the right of a match
    return sbuf.toString();
  }

  /**
   * Delete all occurrences of the given substring.
   *
   * @param inString the original String
   * @param pattern the pattern to delete all occurrences of
   *
   * @return the resulting String
   */
  public static String delete(String inString, String pattern) {
    return replace(inString, pattern, "");
  }

  /**
   * Escapes the given source string from the default escape character.
   *
   * @param src the given source string to escape.
   * @return the escaped string.
   */
  public static String escapeString(final String src) {
    return escapeString(src, ESCAPE, COMMA);
  }

  /**
   * Escapes the given source string.
   *
   * @param src the given string to escape.
   * @param escapeChar the given escape character.
   * @param charsToEscape the list of characters to escape.
   * @return the escaped string.
   */
  public static String escapeString(final String src, char escapeChar, char ... charsToEscape) {
    if (src == null) {
      return null;
    }

    final StringBuilder builer = new StringBuilder();
    for (int index = 0; index < src.length(); index++) {
      char curChar = src.charAt(index);
      if (curChar == escapeChar || hasChar(curChar, charsToEscape)) {
        builer.append(escapeChar);
      }
      builer.append(curChar);
    }
    return builer.toString();
  }

  /**
   * Unescapes the given source string.
   *
   * @param src the given source string to unescapes.
   * @return the normal string.
   */
  public static String unEscapeString(final String src)
    throws IllegalStateException {
    return unEscapeString(src, ESCAPE, COMMA);
  }

  /**
   * Unescapes the given source string.
   *
   * @param src the given source string.
   * @param escapeChar the given escape character.
   * @param charsToEscape the given list of characters to escape.
   * @return the normal string.
   * @throws IllegalStateException if the string contain invalid escape character.
   */
  public static String unEscapeString(final String src, char escapeChar, char ... charsToEscape)
    throws IllegalStateException {
    if (src == null) {
      return null;
    }

    final StringBuilder result = new StringBuilder();
    boolean hasPreEscape = false;
    for (int index = 0; index < src.length(); index++) {
      char curChar = src.charAt(index);
      // has pre escape.
      if (hasPreEscape) {
        if (curChar != escapeChar && !hasChar(curChar, charsToEscape)) {
          // no special character.
          throw new IllegalStateException("Illegal escaped string " + src
              + " unescaped " + escapeChar + " at " + (index - 1));
        }

        // unescape character.
        result.append(curChar);
        hasPreEscape = false;
      } else {
        if (hasChar(curChar, charsToEscape)) {
          throw new IllegalStateException("Illegal escaped string " + src
              + " unescaped " + curChar + " at " + (index -1));
        } else if (curChar == escapeChar) {
          hasPreEscape = true;
        } else {
          result.append(curChar);
        }
      }
    }

    // contain invalid escape character.
    if (hasPreEscape) {
      throw new IllegalStateException("Illegal escape string " + src
          + " unescaped " + escapeChar + " in the end. ");
    }

    // return the result string.
    return result.toString();
  }

  /**
   * Apply the given relative path to the given path, assuming standard Java folder separation
   * (i.e. "/" separators);
   *
   * @param path the path to start from (usually a full file path)
   * @param relativePath the relative path to apply (relative to the full file path above).
   *
   * @return the full file path that results from applying the relative path
   */
  public static String applyRelativePath(String path, String relativePath) {
    int separatorIndex = path.lastIndexOf(FILE_SEPARATOR);
    if (separatorIndex != -1) {
      String newPath = path.substring(0, separatorIndex);
      if (!relativePath.startsWith(FILE_SEPARATOR)) {
        newPath += FILE_SEPARATOR;
      }
      return newPath + relativePath;
    } else {
      return relativePath;
    }
  }

  /**
   * Copy the given Collection into a String array. The Collection must contain String elements
   * only.
   *
   * @param collection the Collection to copy
   * @return the String array ({@code null} if the passed-in Collection was {@code null})
   */
  public static String[] toStringArray(Collection<String> collection) {
    if (collection == null) {
      return null;
    }

    return (String[]) collection.toArray(new String[collection.size()]);
  }

  /**
   * Copy the given Enumeration into a String array. The Enumeration must contain String elements
   * only.
   *
   * @param enumeration the Enumeration to copy
   * @return the String array ({@code null} if the passed-in Enumeration was {@code null})
   */
  public static String[] toStringArray(Enumeration<String> enumeration) {
    if (enumeration == null) {
      return null;
    }

    final List<String> list = Collections.list(enumeration);
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * Normalize the path by suppressing sequences like "path/.." and
   * inner simple dots.
   *
   * <p>The result is convenient for path comparison. For other uses,
   * notice that Windows separators ("\") are replaced by simple slashes.
   * @param path the original path
   * @return the normalized path
   */
  public static String cleanPath(String path) {
    if (path == null) {
      return null;
    }

    String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

    // Strip prefix from path to analyze, to not treat it as part of the
    // first path element. This is necessary to correctly parse paths like
    // "file:core/../core/io/Resource.class", where the ".." should just
    // strip the first "core" directory while keeping the "file:" prefix.
    int prefixIndex = pathToUse.indexOf(":");
    String prefix = "";
    if (prefixIndex != -1) {
      prefix = pathToUse.substring(0, prefixIndex + 1);
      pathToUse = pathToUse.substring(prefixIndex + 1);
    }
    if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
      prefix = prefix + FOLDER_SEPARATOR;
      pathToUse = pathToUse.substring(1);
    }

    final String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
    final List<String> pathElements = new LinkedList<String>();
    int tops = 0;

    for (int i = pathArray.length - 1; i >= 0; i--) {
      String element = pathArray[i];
      if (CURRENT_PATH.equals(element)) {
        // Points to current directory - drop it.
      } else if (TOP_PATH.equals(element)) {
        // Registering top path found.
        tops++;
      } else {
        if (tops > 0) {
          // Merging path element with element corresponding to top path.
          tops--;
        } else {
          // Normal path element found.
          pathElements.add(0, element);
        }
      }
    }

    // Remaining top paths need to be retained.
    for (int i = 0; i < tops; i++) {
      pathElements.add(0, TOP_PATH);
    }

    return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
  }

  /**
   * Take a String which is a delimited list and convert it to a String array.
   * <p>A single delimiter can consists of more than one character: It will still
   * be considered as single delimiter string, rather than as bunch of potential
   * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
   *
   * @param str the input String
   * @param delimiter the delimiter between elements (this is a single delimiter,
   *        rather than a bunch individual delimiter characters)
   *
   * @return an array of the tokens in the list
   * @see #tokenizeToStringArray
   */
  public static String[] delimitedListToStringArray(String str, String delimiter) {
    return delimitedListToStringArray(str, delimiter, null);
  }

  /**
   * Take a String which is a delimited list and convert it to a String array.
   * <p>A single delimiter can consists of more than one character: It will still
   * be considered as single delimiter string, rather than as bunch of potential
   * delimiter characters - in contrast to <code>tokenizeToStringArray</code>.
   *
   * @param str the input String
   * @param delimiter the delimiter between elements (this is a single delimiter,
   *        rather than a bunch individual delimiter characters)
   * @param charsToDelete a set of characters to delete. Useful for deleting unwanted
   *        line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a String.
   *
   * @return an array of the tokens in the list
   * @see #tokenizeToStringArray
   */
  public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {
    if (str == null) {
      return new String[0];
    }

    if (delimiter == null) {
      return new String[] {str};
    }

    final List<String> result = new ArrayList<String>();
    if ("".equals(delimiter)) {
      for (int i = 0; i < str.length(); i++) {
        result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
      }
    } else {
      int pos = 0;
      int delPos = 0;
      while ((delPos = str.indexOf(delimiter, pos)) != -1) {
        result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
        pos = delPos + delimiter.length();
      }

      if (str.length() > 0 && pos <= str.length()) {
        result.add(deleteAny(str.substring(pos), charsToDelete));
      }
    }

    return toStringArray(result);
  }

  /**
   * Delete any character in a given String.
   *
   * @param inString the original String
   * @param charsToDelete a set of characters to delete.
   *        E.g. "az\n" will delete 'a's, 'z's and new lines.
   *
   * @return the resulting String
   */
  public static String deleteAny(String inString, String charsToDelete) {
    if (!hasLength(inString) || !hasLength(charsToDelete)) {
      return inString;
    }

    final StringBuffer out = new StringBuffer();
    for (int i = 0; i < inString.length(); i++) {
      char c = inString.charAt(i);
      if (charsToDelete.indexOf(c) == -1) {
        out.append(c);
      }
    }

    return out.toString();
  }

  /**
   * Extract the filename from the given path, e.g. "mypath/myfile.txt" -> "myfile.txt".
   *
   * @param path the file path (may be {@code null})
   * @return the extracted filename, or {@code null} if none
   */
  public static String getFilename(String path) {
    if (path == null) {
      return null;
    }

    int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
    return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
  }

  /**
   * Convenience method to return a Collection as a delimited (e.g. CSV)
   * String. E.g. useful for <code>toString()</code> implementations.
   *
   * @param coll the Collection to display
   * @param delim the delimiter to use (probably a ",")
   * @param prefix the String to start each element with
   * @param suffix the String to end each element with
   *
   * @return the delimited String
   */
  public static String collectionToDelimitedString(Collection<String> coll, String delim,
      String prefix, String suffix) {
    if (coll == null || coll.isEmpty()) {
      return "";
    }

    final StringBuffer sb = new StringBuffer();

    Iterator<String> it = coll.iterator();
    while (it.hasNext()) {
      sb.append(prefix).append(it.next()).append(suffix);
      if (it.hasNext()) {
        sb.append(delim);
      }
    }

    return sb.toString();
  }

  /**
   * Convenience method to return a Collection as a delimited (e.g. CSV)
   * String. E.g. useful for <code>toString()</code> implementations.
   * @param coll the Collection to display
   * @param delim the delimiter to use (probably a ",")
   * @return the delimited String
   */
  public static String collectionToDelimitedString(Collection<String> coll, String delim) {
    return collectionToDelimitedString(coll, delim, "", "");
  }

  /**
   * Tokenize the given String into a String array via a StringTokenizer.
   * Trims tokens and omits empty tokens.
   * <p>The given delimiters string is supposed to consist of any number of
   * delimiter characters. Each of those characters can be used to separate
   * tokens. A delimiter is always a single character; for multi-character
   * delimiters, consider using <code>delimitedListToStringArray</code>
   *
   * @param str the String to tokenize
   * @param delimiters the delimiter characters, assembled as String
   *        (each of those characters is individually considered as delimiter).
   * @return an array of the tokens
   * @see java.util.StringTokenizer
   * @see java.lang.String#trim()
   * @see #delimitedListToStringArray
   */
  public static String[] tokenizeToStringArray(String str, String delimiters) {
    return tokenizeToStringArray(str, delimiters, true, true);
  }

  /**
   * Tokenize the given String into a String array via a StringTokenizer.
   * <p>The given delimiters string is supposed to consist of any number of
   * delimiter characters. Each of those characters can be used to separate
   * tokens. A delimiter is always a single character; for multi-character
   * delimiters, consider using <code>delimitedListToStringArray</code>.
   *
   * @param str the String to tokenize
   * @param delimiters the delimiter characters, assembled as String
   *        (each of those characters is individually considered as delimiter)
   * @param trimTokens trim the tokens via String's <code>trim</code>
   * @param ignoreEmptyTokens omit empty tokens from the result array
   *        (only applies to tokens that are empty after trimming; StringTokenizer
   *        will not consider subsequent delimiters as token in the first place).
   * @return an array of the tokens (<code>null</code> if the input String was <code>null</code>).
   * @see java.util.StringTokenizer
   * @see java.lang.String#trim()
   * @see #delimitedListToStringArray
   */
  public static String[] tokenizeToStringArray(String str, String delimiters,
      boolean trimTokens, boolean ignoreEmptyTokens) {
    if (str == null) {
      return null;
    }

    StringTokenizer st = new StringTokenizer(str, delimiters);
    List<String> tokens = new ArrayList<String>();
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (trimTokens) {
        token = token.trim();
      }
      if (!ignoreEmptyTokens || token.length() > 0) {
        tokens.add(token);
      }
    }

    return toStringArray(tokens);
  }

  /**
   * Count the occurrences of the substring in string s.
   *
   * @param str string to search in. Return 0 if this is null.
   * @param sub string to search for. Return 0 if this is null.
   */
  public static int countOccurrencesOf(String str, String sub) {
    if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
      return 0;
    }

    int count = 0, pos = 0, idx = 0;
    while ((idx = str.indexOf(sub, pos)) != -1) {
      ++count;
      pos = idx + sub.length();
    }

    return count;
  }
  // //////////////////////////////////////////////////////////////////////////
  // Helper function.
  //
  /**
   * Get right field position.
   *
   * @param data the given data to find.
   * @param field the given field.
   * @param separator the given separator character.
   *
   * @return the field position.
   */
  private static int getRightField(final String data, final String field, final char separator) {
    return data.indexOf(field + separator);
  }

  /**
   * Only Java Virtual Machine can create object from this class.
   */
  private StringUtils() {
    // TODO do nothing as of yet.
  }
}
