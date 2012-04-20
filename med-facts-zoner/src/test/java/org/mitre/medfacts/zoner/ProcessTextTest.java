/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.zoner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessTextTest
{

  public ProcessTextTest()
  {
  }

  @Test
  public void testProcessTextBufferedReader() throws IOException
  {
    String input = "018636330 DH";
    String expectedOutput[] = { "018636330", "DH"};

    String actualOutput[] = null;

    StringReader stringReader = new StringReader(input);
    BufferedReader bufferedReader = new BufferedReader(stringReader);

    ParsedTextFile parsedTextFile =
        ZonerCli.processTextBufferedReader(bufferedReader);

    actualOutput = parsedTextFile.getTokens()[0];

    assertArrayEquals(expectedOutput, actualOutput);

  }
}
