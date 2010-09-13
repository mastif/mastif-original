/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.zoner;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author MCOARR
 */
public class LineTokenToCharacterOffsetConverter
{
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

    System.out.println("before line loop");
    boolean lineFound = false;
    int lineNumber = 1;
    if (line == 1)
    {
      System.out.println("searching for first line, not going into line loop.");
      lineFound = true;
    } else
    while(!lineFound && iterator.hasNext())
    {
      currentEntry = iterator.next();
      System.out.format("::currentEntry (%d, %s) [line loop]%n", currentEntry.getKey(), currentEntry.getValue());
      if (WhitespaceType.EOL == currentEntry.getValue())
      {
        lineNumber++;
        System.out.format("processed line %d%n", lineNumber);
      }
      if (lineNumber == line)
      {
        System.out.format("found requested line!%n");
        lineFound = true;
      }
    }
    System.out.println("after line loop");

    if (token == 0)
    {
      System.out.format("token 0 was requested on line %d, so not going through second loop%n", line);
      return (currentEntry == null) ? 0 : currentEntry.getKey();
    }

    boolean tokenFound = false;
    int tokenCount = 0;
    System.out.println("before token loop");
    if (token == 0)
    {
      System.out.println("searching for first token, not going into token loop.");
      tokenFound = true;
    }
    while(!tokenFound && iterator.hasNext())
    {
      System.out.println("inside token loop...");
      currentEntry = iterator.next();
      System.out.format("::currentEntry (%d, %s) [token loop]%n", currentEntry.getKey(), currentEntry.getValue());
      if (WhitespaceType.EOL == currentEntry.getValue())
      {
        System.err.println("ERROR: found EOL before finding token!");
        return null;
      }
      if (WhitespaceType.SPACE == currentEntry.getValue())
      {
        tokenCount++;
        System.out.format("processed token %d%n", tokenCount);
      }

      if (tokenCount == token)
      {
        tokenFound = true;
        System.out.format("found requested token!%n");
      }
    }
    System.out.println("after line loop");

    if (lineFound && tokenFound)
    {
      System.out.format("token 0 was requested on line %d, so not going through second loop%n", line);
      return (currentEntry == null) ? null : currentEntry.getKey();
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
