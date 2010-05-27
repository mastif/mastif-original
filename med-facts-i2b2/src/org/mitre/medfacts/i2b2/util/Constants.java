package org.mitre.medfacts.i2b2.util;

import java.util.regex.Pattern;

public interface Constants
{
  public static String FILE_EXTENSION_TEXT_FILE = ".txt";
  public static String FILE_EXTENSION_CONCEPT_FILE = ".con";
  public static String FILE_EXTENSION_ASSERTION_FILE = ".ast";
  public static String FILE_EXTENSION_RELATION_FILE = ".rel";

  public static Pattern TEXT_FILE_EXTENSTION_PATTERN = Pattern.compile("\\.txt$");
}
