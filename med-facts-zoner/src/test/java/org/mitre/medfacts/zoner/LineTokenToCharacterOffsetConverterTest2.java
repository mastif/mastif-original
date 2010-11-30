package org.mitre.medfacts.zoner;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter.BeginAndEndCharacterOffsetPair;

/**
 *
 * @author MCOARR
 */
public class LineTokenToCharacterOffsetConverterTest2
{
  public static final Charset CHARSET_UTF8 = Charset.forName("utf-8");

  private static final Logger logger = Logger.getLogger(LineTokenToCharacterOffsetConverterTest2.class.getName());

  @Test
  public void testI2b2Doc1() throws IOException
  {
    URL documentUrl = getClass().getClassLoader().getResource("0001.txt");
    InputStream inputStream = documentUrl.openStream();
    String inputDocument =
      readEntireContentsStream(inputStream);
    inputStream.close();

    //logger.info(String.format("inputDocument: ===%n%s%n===%n", inputDocument));

    LineTokenToCharacterOffsetConverter c = new LineTokenToCharacterOffsetConverter(inputDocument);

    Integer beginExpectedOutputFirstCharacter = 5896; //5800;
    Integer beginExpectedOutputLastCharacter = 5898;
       // 5896 would be the beginning of the word;
       // 5898 would be the end of the word

//       // 5800 would be the beginning of the word;
//       // 5802 would be the end of the word

    LineAndTokenPosition beginPosition = new LineAndTokenPosition();
    beginPosition.setLine(111);
    beginPosition.setTokenOffset(20);
    BeginAndEndCharacterOffsetPair firstWordPosition = c.convert(beginPosition);
    Integer beginActualOutputFirstCharacter = firstWordPosition.getBegin();
    Integer beginActualOutputLastCharacter = firstWordPosition.getEnd();

    assertEquals("111:20's output first character offset did not match", beginExpectedOutputFirstCharacter, beginActualOutputFirstCharacter);
    assertEquals("111:20's output last character offset did not match", beginExpectedOutputLastCharacter, beginActualOutputLastCharacter);


    Integer endExpectedOutputFirstCharacter = 5909; //5813;
    Integer endExpectedOutputLastCharacter = 5913;
       // 5909 would be the beginning of the word;
       // 5913 would be the end of the word
//       // 5813 would be the beginning of the word;
//       // 5817 would be the end of the word

    LineAndTokenPosition endPosition = new LineAndTokenPosition();
    endPosition.setLine(111);
    endPosition.setTokenOffset(23);
    BeginAndEndCharacterOffsetPair lastWordPosition = c.convert(endPosition);
    Integer endActualOutputFirstCharacter = lastWordPosition.getBegin();
    Integer endActualOutputLastCharacter = lastWordPosition.getEnd();

    assertEquals("111:23's output character offset did not match", endExpectedOutputFirstCharacter, endActualOutputFirstCharacter);
    assertEquals("111:23's output character offset did not match", endExpectedOutputLastCharacter, endActualOutputLastCharacter);
  }

  @Test
  public void testI2b2Doc1Reverse() throws IOException
  {
    URL documentUrl = getClass().getClassLoader().getResource("0001.txt");
    InputStream inputStream = documentUrl.openStream();
    String inputDocument =
      readEntireContentsStream(inputStream);
    inputStream.close();

    //logger.info(String.format("inputDocument: ===%n%s%n===%n", inputDocument));

    LineTokenToCharacterOffsetConverter c = new LineTokenToCharacterOffsetConverter(inputDocument);

    Integer inputFirstTokenFirstCharacter = 5896;
    Integer inputFirstTokenLastCharacter = 5898;

    // SCENARIO #1: input first character of first token
    
    LineAndTokenPosition expectedFirstWordPosition = new LineAndTokenPosition();
    Integer expectedFirstWordPositionLine = 111;
    Integer expectedFirstWordPositionToken = 20;
    expectedFirstWordPosition.setLine(expectedFirstWordPositionLine);
    expectedFirstWordPosition.setTokenOffset(expectedFirstWordPositionToken);
    
    LineAndTokenPosition actualFirstWordPosition = c.convertReverse(inputFirstTokenFirstCharacter);
    Integer actualFirstWordPositionLine = actualFirstWordPosition.getLine();
    Integer actualFirstWordPositionToken = actualFirstWordPosition.getTokenOffset();
    
    assertEquals("111:20's (offset 5896's -- first token first character) output line offset did not match", expectedFirstWordPositionLine, actualFirstWordPositionLine);
    assertEquals("111:20's (offset 5896's -- first token first character) output token offset did not match", expectedFirstWordPositionToken, actualFirstWordPositionToken);


    // SCENARIO #2: input last character of first token

    LineAndTokenPosition actualFirstWordLastCharacterPosition = c.convertReverse(inputFirstTokenLastCharacter);
    Integer actualFirstWordLastCharacterPositionLine = actualFirstWordLastCharacterPosition.getLine();
    Integer actualFirstWordLastCharacterPositionToken = actualFirstWordLastCharacterPosition.getTokenOffset();

    assertEquals("111:20's (offset 5898's -- first token last character) output line offset did not match", expectedFirstWordPositionLine, actualFirstWordLastCharacterPositionLine);
    assertEquals("111:20's (offset 5898's -- first token last character) output token offset did not match", expectedFirstWordPositionToken, actualFirstWordLastCharacterPositionToken);

    ////


    Integer inputLastTokenFirstCharacter = 5909;
    Integer inputLastTokenLastCharacter = 5913;

    // SCENARIO #3: input first character of last token

    LineAndTokenPosition expectedLastWordPosition = new LineAndTokenPosition();
    Integer expectedLastWordPositionLine = 111;
    Integer expectedLastWordPositionToken = 23;
    expectedLastWordPosition.setLine(expectedLastWordPositionLine);
    expectedLastWordPosition.setTokenOffset(expectedLastWordPositionToken);

    LineAndTokenPosition actualLastWordFirstCharacterPosition = c.convertReverse(inputLastTokenFirstCharacter);
    Integer actualLastWordFirstCharacterPositionLine = actualLastWordFirstCharacterPosition.getLine();
    Integer actualLastWordFirstCharacterPositionToken = actualLastWordFirstCharacterPosition.getTokenOffset();

    assertEquals("111:23's (offset 5909's -- last token first character) output line offset did not match", expectedLastWordPositionLine, actualLastWordFirstCharacterPositionLine);
    assertEquals("111:23's (offset 5909's -- last token first character) output token offset did not match", expectedLastWordPositionToken, actualLastWordFirstCharacterPositionToken);


    // SCENARIO #4: input last character of last token

    LineAndTokenPosition actualLastWordLastCharacterPosition = c.convertReverse(inputLastTokenLastCharacter);
    Integer actualLastWordLastCharacterPositionLine = actualLastWordLastCharacterPosition.getLine();
    Integer actualLastWordLastCharacterPositionToken = actualLastWordLastCharacterPosition.getTokenOffset();

    assertEquals("111:23's (offset 5913's -- last token last character) output line offset did not match", expectedLastWordPositionLine, actualLastWordLastCharacterPositionLine);
    assertEquals("111:23's (offset 5913's -- last token last character) output token offset did not match", expectedLastWordPositionToken, actualLastWordLastCharacterPositionToken);

  
  }

  public static String readEntireContentsFile(File modelFile) throws IOException
  {
      FileReader fileReader = new FileReader(modelFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      String returnValue = readEntireContentsReader(bufferedReader);

      bufferedReader.close();
      fileReader.close();

      return returnValue;
  }

  public static String readEntireContentsStream(InputStream inputStream) throws IOException
  {
      BufferedInputStream bis = new BufferedInputStream(inputStream);
      InputStreamReader reader = new InputStreamReader(bis, CHARSET_UTF8);

      String returnValue = readEntireContentsReader(reader);

      reader.close();
      bis.close();

      return returnValue;
  }

  public static String readEntireContentsReader(Reader reader) throws IOException
  {
      StringBuilder sb = new StringBuilder();

      char buffer[] = new char[2048];

      int charsRead = 0;

      while ((charsRead = reader.read(buffer)) >= 0)
      {
        sb.append(buffer, 0, charsRead);
      }

      String result = sb.toString();
      return result;

  }

}
