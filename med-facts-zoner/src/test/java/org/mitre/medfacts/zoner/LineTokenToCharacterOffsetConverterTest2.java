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

/**
 *
 * @author MCOARR
 */
public class LineTokenToCharacterOffsetConverterTest2
{
  public static final Charset CHARSET_UTF8 = Charset.forName("utf-8");

  private static final Logger logger = Logger.getLogger(LineTokenToCharacterOffsetConverterTest2.class.getName());

  @Test
  //@Ignore
  public void testI2b2Doc1() throws IOException
  {
    URL documentUrl = getClass().getClassLoader().getResource("0001.txt");
    InputStream inputStream = documentUrl.openStream();
    String inputDocument =
      readEntireContentsStream(inputStream);
    inputStream.close();

    //logger.info(String.format("inputDocument: ===%n%s%n===%n", inputDocument));

    LineTokenToCharacterOffsetConverter c = new LineTokenToCharacterOffsetConverter(inputDocument);

    Integer beginExpectedOutput = 5896; //5800;
       // 5896 would be the beginning of the word;
       // 5898 would be the end of the word

//       // 5800 would be the beginning of the word;
//       // 5802 would be the end of the word

    LineAndTokenPosition beginPosition = new LineAndTokenPosition();
    beginPosition.setLine(111);
    beginPosition.setTokenOffset(20);
    Integer beginActualOutput = c.convert(beginPosition);

    assertEquals("111:20's output character offset did not match", beginExpectedOutput, beginActualOutput);


    Integer endExpectedOutput = 5909; //5813;
       // 5909 would be the beginning of the word;
       // 5913 would be the end of the word
//       // 5813 would be the beginning of the word;
//       // 5817 would be the end of the word

    LineAndTokenPosition endPosition = new LineAndTokenPosition();
    endPosition.setLine(111);
    endPosition.setTokenOffset(23);
    Integer endActualOutput = c.convert(endPosition);

    assertEquals("111:23's output character offset did not match", endExpectedOutput, endActualOutput);
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
