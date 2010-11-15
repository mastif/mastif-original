/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

/**
 *
 * @author MCOARR
 */
public class ArrayPrinter
{
  public static String toString(String array[])
  {
    StringBuilder b = new StringBuilder();
    b.append("{ ");
    for (int i = 0; i < array.length - 1; i++)
    {
      b.append('"');
      b.append(array[i]);
      b.append("\", ");
    }
    if (array.length >= 1)
    {
      b.append('"');
      b.append(array[array.length - 1]);
      b.append('"');
    }
    b.append(" }");
    return b.toString();
  }
}
