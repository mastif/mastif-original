/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

  public static String readEntireContents(File modelFile)
  {
    try
    {
      FileReader fileReader = new FileReader(modelFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      StringBuilder sb = new StringBuilder();

      char buffer[] = new char[2048];

      int charsRead = 0;

      while ((charsRead = bufferedReader.read(buffer)) >= 0)
      {
        sb.append(buffer, 0, charsRead);
      }

      String result = sb.toString();
      return result;

    } catch(IOException e)
    {
      String message = "IOException while reading model file";
      throw new RuntimeException(message, e);
    }

  }

}
