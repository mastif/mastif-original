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
public class LineTokenToCharacterOffsetConverterTest
{

  public LineTokenToCharacterOffsetConverterTest()
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

    LineTokenToCharacterOffsetConverter c =
        new LineTokenToCharacterOffsetConverter(input);

    int testLine1 = 1;
    int testToken1 = 0;
    LineAndTokenPosition input1 = new LineAndTokenPosition();
    input1.setLine(testLine1);
    input1.setTokenOffset(testToken1);

    Integer actualCharacterOffset1 = c.convert(input1).getBegin();
    Integer expectedCharacterOffset1 = 0;
    System.out.format("BEGIN TEST #1%ninput line:token: %d:%d%nexpected character offset: %d%nactual character offset: %d%nEND TEST #1%n%n",
        testLine1, testToken1, expectedCharacterOffset1, actualCharacterOffset1);
    assertEquals("test #1 character offset didn't match", expectedCharacterOffset1, actualCharacterOffset1);

    int testLine2 = 12;
    int testToken2 = 11;
    LineAndTokenPosition input2 = new LineAndTokenPosition();
    input2.setLine(testLine2);
    input2.setTokenOffset(testToken2);
    Integer actualCharacterOffset2 = c.convert(input2).getBegin();
    Integer expectedCharacterOffset2 = 197;
    System.out.format("BEGIN TEST #2%ninput line:token: %d:%d%nexpected character offset: %d%nactual character offset: %d%nEND TEST #2%n%n",
        testLine2, testToken2, expectedCharacterOffset2, actualCharacterOffset2);
    assertEquals("test #2 character offset didn't match", expectedCharacterOffset2, actualCharacterOffset2);

  }
}
