/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author MCOARR
 */
public class CombinationGenerator
{
  public static final BigInteger TWO = BigInteger.valueOf(2);
  
  public static List<Set<Integer>> generateCombinations(int conceptCount)
  {
    int max = twoToThePowerOf(conceptCount);
    List<Set<Integer>> accumulatedList = new ArrayList<Set<Integer>>(max);

    for (int i = 1; i <= max - 1; i++)
    {
      System.out.format("i: %d%n", i);
      Set<Integer> currentSet = new TreeSet<Integer>();
      for (int j = 1; j <= conceptCount; j++)
      {
        boolean toBeAdded = (i & twoToThePowerOf(j - 1)) != 0;
        System.out.format("  - i: %d; j: %d (%s)%n", i, j, (toBeAdded ? "add" : "-"));
        if (toBeAdded)
        {
          currentSet.add(j);
        }
      }
      accumulatedList.add(currentSet);
    }

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    pw.println("[");
    int listSize = accumulatedList.size();
    int positionInList = 0;
    for (Set<Integer> currentSet : accumulatedList)
    {
      int positionInSet = 0;
      int setSize = currentSet.size();
      pw.print("{");
      for (Integer currentInteger : currentSet)
      {
        pw.print(currentInteger);
        boolean isLastInSet = (positionInSet == setSize - 1);
        if (!isLastInSet) { pw.print(","); }
        positionInSet++;
      }
      pw.print("}");
      boolean isLastInList = (positionInList == listSize - 1);
      if (!isLastInList) { pw.print(","); }
      pw.println();
      positionInList++;
    }
    pw.println("]");
    String debugOutput = sw.toString();
    System.out.println(debugOutput);

    return accumulatedList;
  }

  public static int twoToThePowerOf(int powerOfTwo)
  {
    //return TWO.pow(conceptCount).longValue();
    return 1 << powerOfTwo;
  }


}
