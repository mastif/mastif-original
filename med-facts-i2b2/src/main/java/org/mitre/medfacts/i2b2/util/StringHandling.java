/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MCOARR
 */
public class StringHandling
{
  private static final Pattern RESERVED_CHARACTER_PATTERN = Pattern.compile("[ :]");
  private static final String RESERVED_CHARACTER_REPLACEMENT = "_";


  public static String escapeStringForFeatureName(String input)
  {
    Matcher matcher = RESERVED_CHARACTER_PATTERN.matcher(input.toLowerCase());
    String output = matcher.replaceAll(RESERVED_CHARACTER_REPLACEMENT);
    return output;
  }

  protected static final String ABSOLUTE_FILE_NAME_PREFIX_REGEX_STRING;
  protected static Pattern ABSOLUTE_FILE_NAME_PREFIX_REGEX;

  static
  {
    ABSOLUTE_FILE_NAME_PREFIX_REGEX_STRING =
      "^((/)|([A-Za-z]:[/\\\\])).*";
    ABSOLUTE_FILE_NAME_PREFIX_REGEX =
      Pattern.compile(ABSOLUTE_FILE_NAME_PREFIX_REGEX_STRING);
  }

  public static boolean isAbsoluteFileName(String fileName)
  {
    Matcher matcher = ABSOLUTE_FILE_NAME_PREFIX_REGEX.matcher(fileName);
    return matcher.matches();
  }

}
