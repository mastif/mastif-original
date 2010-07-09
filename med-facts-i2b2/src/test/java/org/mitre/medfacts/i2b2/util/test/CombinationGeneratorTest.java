/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util.test;

import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mitre.medfacts.i2b2.util.CombinationGenerator;
import static org.junit.Assert.*;

/**
 *
 * @author MCOARR
 */
public class CombinationGeneratorTest
{

  @Test
  public void testGenerateCombinations()
  {
    CombinationGenerator g = new CombinationGenerator();
    int inputSize = 4;
    List<Set<Integer>> combinations = g.generateCombinations(inputSize);


    int expectedSize = g.twoToThePowerOf(inputSize) - 1;
    int actualSize = combinations.size();
    assertEquals(String.format("number of combinations for an input size of %d should be %d instead of %d", inputSize, expectedSize, actualSize), expectedSize, actualSize);
  }

  @Ignore
  @Test
  public void testTwoToThePowerOf()
  {
    CombinationGenerator g = new CombinationGenerator();

    int input = 0;
    int expectedOutput = 1;
    int actualOutput = g.twoToThePowerOf(input);

    assertEquals(String.format("2 to the %d should equal %d; actual value was %d", input, expectedOutput, actualOutput), expectedOutput, actualOutput);


    input = 1;
    expectedOutput = 2;
    actualOutput = g.twoToThePowerOf(input);

    assertEquals(String.format("2 to the %d should equal %d; actual value was %d", input, expectedOutput, actualOutput), expectedOutput, actualOutput);

    input = 2;
    expectedOutput = 4;
    actualOutput = g.twoToThePowerOf(input);

    assertEquals(String.format("2 to the %d should equal %d; actual value was %d", input, expectedOutput, actualOutput), expectedOutput, actualOutput);

    input = 3;
    expectedOutput = 8;
    actualOutput = g.twoToThePowerOf(input);

    assertEquals(String.format("2 to the %d should equal %d; actual value was %d", input, expectedOutput, actualOutput), expectedOutput, actualOutput);

    input = 4;
    expectedOutput = 16;
    actualOutput = g.twoToThePowerOf(input);

    assertEquals(String.format("2 to the %d should equal %d; actual value was %d", input, expectedOutput, actualOutput), expectedOutput, actualOutput);

  }

}