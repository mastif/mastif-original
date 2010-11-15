/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.util.Random;

/**
 *
 * @author MCOARR
 */
public class RandomAssignmentItem implements Comparable<RandomAssignmentItem>
{
  protected int originalPosition;
  protected float randomValue;
  protected Random random = new Random();;

  public RandomAssignmentItem(int originalPosition)
  {
    this.originalPosition = originalPosition;
    randomize();
  }

  public void randomize()
  {
    randomValue = random.nextFloat();
  }
  
  public int compareTo(RandomAssignmentItem other)
  {
    if (this.randomValue < other.randomValue)
    {
      return -1;
    } else if (this.randomValue > other.randomValue)
    {
      return 1;
    } else
    {
      Integer thisInteger = this.originalPosition;
      Integer otherInteger = other.originalPosition;
      return thisInteger.compareTo(otherInteger);
    }
  }


  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final RandomAssignmentItem other = (RandomAssignmentItem) obj;
    if (this.originalPosition != other.originalPosition)
    {
      return false;
    }
    if (this.randomValue != other.randomValue)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 97 * hash + this.originalPosition;
    hash = 97 * hash + Float.floatToIntBits(this.randomValue);
    return hash;
  }

  /**
   * @return the originalPosition
   */
  public int getOriginalPosition()
  {
    return originalPosition;
  }

  /**
   * @param originalPosition the originalPosition to set
   */
  public void setOriginalPosition(int originalPosition)
  {
    this.originalPosition = originalPosition;
  }

  /**
   * @return the randomValue
   */
  public float getRandomValue()
  {
    return randomValue;
  }

  /**
   * @param randomValue the randomValue to set
   */
  public void setRandomValue(float randomValue)
  {
    this.randomValue = randomValue;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getOriginalPosition());
    sb.append(" [random value: ");
    sb.append(getRandomValue());
    sb.append("]");
    return sb.toString();
  }
}
