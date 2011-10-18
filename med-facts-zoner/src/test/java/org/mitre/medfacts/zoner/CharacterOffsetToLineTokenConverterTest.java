/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.zoner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author MCOARR
 */
public class CharacterOffsetToLineTokenConverterTest
{

  public CharacterOffsetToLineTokenConverterTest()
  {
  }

  @Test
  public void testFromText()
  {
    final String eol = "\r\n";
    String input =
      "Admission Date :" + eol +
      "2011-03-10" + eol +
      "Discharge Date :" + eol +
      "2011-03-15" + eol +
      "Date of Birth :" + eol +
      "1927-10-26" + eol +
      "Sex :" + eol +
      "F" + eol +
      "Service :" + eol +
      "Medford" + eol +
      "HISTORY OF PRESENT ILLNESS :" + eol +
      "This is an 83 year old woman with a history of hypertension who presents with slurred speech when dehydrated ." + eol +
      "She has paraplegia secondary to HTLV exposure while on vacation in the Bahamas six years ago ." + eol;

    CharacterOffsetToLineTokenConverter c =
        new CharacterOffsetToLineTokenConverterDefaultImpl(input);

    int testCharacterOffset1 = 0;
    LineAndTokenPosition output1 = c.convert(testCharacterOffset1);
    int expectedLine1 = 1;
    int expectedToken1 = 0;
    int actualLine1 = output1.getLine();
    int actualTokenOffset1 = output1.getTokenOffset();
    System.out.format("BEGIN TEST #1%ninput char offset: %d%nexpected line: %d%nexpected token offset: %d%n%nactual line: %d%nactual token offset: %d%nEND TEST #1%n%n",
        testCharacterOffset1, expectedLine1, expectedToken1, actualLine1, actualTokenOffset1);
    assertEquals("test #1 line didn't match", expectedLine1, actualLine1);
    assertEquals("test #1 token offset didn't match", expectedToken1, actualTokenOffset1);

    int testCharacterOffset2 = 197;
    LineAndTokenPosition output2 = c.convert(testCharacterOffset2);
    int expectedLine2 = 12;
    int expectedToken2 = 11;
    int actualLine2 = output2.getLine();
    int actualTokenOffset2 = output2.getTokenOffset();
    System.out.format("BEGIN TEST #2%ninput char offset: %d%nexpected line: %d%nexpected token offset: %d%n%nactual line: %d%nactual token offset: %d%nEND TEST #2%n%n",
        testCharacterOffset2, expectedLine2, expectedToken2, actualLine2, actualTokenOffset2);
    assertEquals("test #2 line didn't match", expectedLine2, output2.getLine());
    assertEquals("test #2 token offset didn't match", expectedToken2, output2.getTokenOffset());

  }
}
