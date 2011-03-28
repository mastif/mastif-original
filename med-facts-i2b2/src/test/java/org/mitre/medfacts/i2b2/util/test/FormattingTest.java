/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.util.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormattingTest
{

  public FormattingTest()
  {
  }

  @Test
  public void testFormatting()
  {
    String expectedOutput = "00013";
    int input = 13;

    String actualOutput = String.format("%05d", input);
    System.out.println(actualOutput);

    assertEquals(expectedOutput, actualOutput);
  }

}
