/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.scanners;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MCOARR
 */
class CueItem
{
  protected List<String> tokenList = new ArrayList<String>();

  public void addToken(String token)
  {
    tokenList.add(token);
  }

  public int getSize()
  {
    return tokenList.size();
  }

  public String tokenAtPosition(int i)
  {
    int size = tokenList.size();
    if (i >= 0 && i < size)
    {
      return tokenList.get(i);
    } else
    {
      throw new IndexOutOfBoundsException(String.format("position i==%d is not valid on tokenList of size %d", i, size));
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("CueItem { ");
    int size = tokenList.size();
    for (int i = 0; i < size; i++)
    {
      boolean isLast = (i == size - 1);
      String currentToken = tokenList.get(i);
      sb.append("\"");
      sb.append(currentToken);
      sb.append("\"");
      if (!isLast) { sb.append(", "); }
    }
    sb.append("}");
    return sb.toString();
  }

}
