/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.zoner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
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
  protected static final Pattern spacePattern = Pattern.compile(" +");
  protected TreeSet<Integer> eolPositionSet = new TreeSet<Integer>();
  protected TreeMap<Integer,WhitespaceType> eolOrSpacePositionMap = new TreeMap<Integer,WhitespaceType>();

  protected ArrayList<ArrayList<Integer>> offsets = null;

  public LineTokenToCharacterOffsetConverter(String inputString)
  {
    ArrayList<ArrayList<Integer>> all = new ArrayList<ArrayList<Integer>>();

//    ArrayList<Integer> currentLine = new ArrayList<Integer>();
//    all.add(currentLine);

    /////

    Matcher eolMatcher = endOfLinePattern.matcher(inputString);
    //ArrayList<String> lineList = new ArrayList<String>();
    //ArrayList<Integer> lineBeginOffsetList = new ArrayList<Integer>();

    ArrayList<ArrayList<Integer>> offsets = new ArrayList<ArrayList<Integer>>();

    int i = 0;
    while (eolMatcher.find())
    {
      int eolStart = eolMatcher.start();
      int eolEnd = eolMatcher.end();

      ArrayList<Integer> lineOffsets = new ArrayList<Integer>();
      offsets.add(lineOffsets);

      String line = inputString.substring(i, eolStart);

      logger.info(String.format("LINE [%d-%d] \"%s\"", i, eolStart - 1, line));
      parseLine(line, lineOffsets, i);

      //lineList.add(line);
      //lineBeginOffsetList.add(i);
      i = eolEnd;
    }
    if (i < inputString.length())
    {
      String line = inputString.substring(i);
      ArrayList<Integer> lineOffsets = new ArrayList<Integer>();
      logger.info(String.format("LINE (before eof) [%d-%d] \"%s\"", i, inputString.length() - 1, line));
      offsets.add(lineOffsets);

      parseLine(line, lineOffsets, i);
    }

    this.offsets = offsets;
  }

  private void parseLine(String line, ArrayList<Integer> lineOffsets, int startOfLineOffset)
  {
    Matcher spaceMatcher = spacePattern.matcher(line);
    int j = 0;
    while (spaceMatcher.find())
    {
      int spaceBegin = spaceMatcher.start();
      int spaceEnd = spaceMatcher.end();

      int wordBegin = j;;
      int wordEnd = spaceBegin - 1;

      int wordBeginOverall = startOfLineOffset + wordBegin;
      int wordEndOverall = startOfLineOffset + wordEnd;

      String token = line.substring(j, spaceBegin);

      logger.info(String.format("    TOKEN [%d-%d] [%d-%d] \"%s\"", wordBegin, wordEnd, wordBeginOverall, wordEndOverall, token));
      lineOffsets.add(wordBeginOverall);
      j = spaceEnd;
    }
    if (j < line.length())
    {
      int wordBegin = j;
      int wordEnd = line.length() - 1;

      int wordBeginOverall = startOfLineOffset + wordBegin;
      int wordEndOverall = startOfLineOffset + wordEnd;

      String token = line.substring(j);
      logger.info(String.format("    TOKEN (before eol) [%d-%d] [%d-%d] \"%s\"", wordBegin, wordEnd, wordBeginOverall, wordEndOverall, token));

      lineOffsets.add(j);
    }
  }

  public Integer convert(LineAndTokenPosition lineAndTokenPosition)
  {
    int line = lineAndTokenPosition.getLine();
    int token = lineAndTokenPosition.getTokenOffset();

    ArrayList<Integer> lineArray = offsets.get(line - 1);
    if (lineArray == null) { return null; }
    Integer offsetForToken = lineArray.get(token);
    
    return offsetForToken;
  }

  public Integer convertOld(LineAndTokenPosition lineAndTokenPosition)
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

  public class CharacterOffsetAndWhitespaceType
  {
    protected int characterOffset;
    protected WhitespaceType whitespaceType;

    public int getCharacterOffset()
    {
      return characterOffset;
    }

    public void setCharacterOffset(int characterOffset)
    {
      this.characterOffset = characterOffset;
    }

    public WhitespaceType getWhitespaceType()
    {
      return whitespaceType;
    }

    public void setWhitespaceType(WhitespaceType whitespaceType)
    {
      this.whitespaceType = whitespaceType;
    }
  }
}
