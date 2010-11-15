/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.util.Comparator;

/**
 *
 * @author MCOARR
 */
public class RandomAssignmentItemOriginalPositionComparator implements Comparator<RandomAssignmentItem>
{
  public int compare(RandomAssignmentItem thisItem, RandomAssignmentItem otherItem)
  {
    if (thisItem.getOriginalPosition() < otherItem.getOriginalPosition())
    {
      return -1;
    } else if (thisItem.getOriginalPosition() > otherItem.getOriginalPosition())
    {
      return 1;
    } else
    {
      return 0;
    }
  }

}
