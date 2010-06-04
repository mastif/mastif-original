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
}
