/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.zoner;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MCOARR
 */
public class LineTokenToCharacterOffsetConverter
{
  public static final Logger logger = Logger.getLogger(LineTokenToCharacterOffsetConverter.class.getName());

  protected static final Pattern endOfLinePattern = Pattern.compile("\\r?\\n");
  protected static final Pattern eolOrSpacePattern = Pattern.compile("( +)|(\\r?\\n)");
  protected TreeSet<Integer> eolPositionSet = new TreeSet<Integer>();
  protected TreeMap<Integer,WhitespaceType> eolOrSpacePositionMap = new TreeMap<Integer,WhitespaceType>();

  public LineTokenToCharacterOffsetConverter(String inputString)
  {
    Matcher eolMatcher = endOfLinePattern.matcher(inputString);
    while (eolMatcher.find())
    {
      int begin = eolMatcher.start();
      eolPositionSet.add(begin);
    }

    Matcher eolOrSpaceMatcher = eolOrSpacePattern.matcher(inputString);
    while (eolOrSpaceMatcher.find())
    {
      int begin = eolOrSpaceMatcher.start();
      WhitespaceType type=null;
      if (" ".equals(eolOrSpaceMatcher.group(0)))
      {
        type = WhitespaceType.SPACE;
      } else
      {
        type = WhitespaceType.EOL;
      }
      eolOrSpacePositionMap.put(begin, type);
    }
  }

  public Integer convert(LineAndTokenPosition lineAndTokenPosition)
  {
    int line = lineAndTokenPosition.getLine();
    int token = lineAndTokenPosition.getTokenOffset();

    Set<Entry<Integer, WhitespaceType>> fullEntrySet = eolOrSpacePositionMap.entrySet();
    Iterator<Entry<Integer, WhitespaceType>> iterator = fullEntrySet.iterator();

    Entry<Integer, WhitespaceType> currentEntry = null;

    logger.log(Level.FINEST,("before line loop"));
    boolean lineFound = false;
    int lineNumber = 1;
    if (line == 1)
    {
      logger.log(Level.FINEST,("searching for first line, not going into line loop."));
      lineFound = true;
    } else
    while(!lineFound && iterator.hasNext())
    {
      currentEntry = iterator.next();
      logger.log(Level.FINEST,String.format("::currentEntry (%d, %s) [line loop]%n", currentEntry.getKey(), currentEntry.getValue()));
      if (WhitespaceType.EOL == currentEntry.getValue())
      {
        lineNumber++;
        logger.log(Level.FINEST,String.format("processed line %d%n", lineNumber));
      }
      if (lineNumber == line)
      {
        logger.log(Level.FINEST,String.format("found requested line!%n"));
        lineFound = true;
      }
    }
    logger.log(Level.FINEST,("after line loop"));

    if (token == 0)
    {
      logger.log(Level.FINEST,String.format("token 0 was requested on line %d, so not going through second loop%n", line));
      return (currentEntry == null) ? 0 : currentEntry.getKey() + 2;
    }

    boolean tokenFound = false;
    int tokenCount = 0;
    logger.log(Level.FINEST,("before token loop"));
    if (token == 0)
    {
      logger.log(Level.FINEST,("searching for first token, not going into token loop."));
      tokenFound = true;
    }
    while(!tokenFound && iterator.hasNext())
    {
      logger.log(Level.FINEST,("inside token loop..."));
      currentEntry = iterator.next();
      logger.log(Level.FINEST,String.format("::currentEntry (%d, %s) [token loop]%n", currentEntry.getKey(), currentEntry.getValue()));
      if (WhitespaceType.EOL == currentEntry.getValue())
      {
        System.err.println("ERROR: found EOL before finding token!");
        return null;
      }
      if (WhitespaceType.SPACE == currentEntry.getValue())
      {
        tokenCount++;
        logger.log(Level.FINEST,String.format("processed token %d%n", tokenCount));
      }

      if (tokenCount == token)
      {
        tokenFound = true;
        logger.log(Level.FINEST,String.format(("found requested token!%n")));
        Entry<Integer, WhitespaceType> whatWouldHaveBeenNext = iterator.next();
        logger.log(Level.FINEST,String.format("::currentEntry (%d, %s) [token loop]%n", whatWouldHaveBeenNext.getKey(), whatWouldHaveBeenNext.getValue()));
      }
    }
    logger.log(Level.FINEST,("after line loop"));

    if (lineFound && tokenFound)
    {
      logger.log(Level.FINEST,String.format("token 0 was requested on line %d, so not going through second loop%n", line));
      return (currentEntry == null) ? null : currentEntry.getKey() + 2;
    } else
    {
      return null;
    }
  }

  public enum WhitespaceType
  {
    SPACE,
    EOL
  }
}
