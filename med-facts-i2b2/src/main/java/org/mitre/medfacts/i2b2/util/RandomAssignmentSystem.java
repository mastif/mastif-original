/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author MCOARR
 */
public class RandomAssignmentSystem
{
  protected float trainingRatio = 0.8f;
  protected Set<RandomAssignmentItem> originalSet;
  protected Set<RandomAssignmentItem> trainingSet;
  protected Set<RandomAssignmentItem> testSet;
  
  public RandomAssignmentSystem()
  {
    originalSet = new TreeSet<RandomAssignmentItem>();
    trainingSet = new TreeSet<RandomAssignmentItem>();
    testSet = new TreeSet<RandomAssignmentItem>();
  }

  public void createSets()
  {
    int counter = 0;
    int splitPoint = Math.round(trainingRatio * originalSet.size());

    for (RandomAssignmentItem current : originalSet)
    {
      float currentRandomValue = current.getRandomValue();
      if (counter < splitPoint)
      {
        trainingSet.add(current);
      } else
      {
        testSet.add(current);
      }
      counter++;
    }
  }

  public void addItem(int newValue)
  {
    RandomAssignmentItem newItem = new RandomAssignmentItem(newValue);
    originalSet.add(newItem);
  }

  /**
   * @return the originalSet
   */
  public Set<RandomAssignmentItem> getOriginalSet()
  {
    return originalSet;
  }

  /**
   * @param originalSet the originalSet to set
   */
  public void setOriginalSet(Set<RandomAssignmentItem> originalSet)
  {
    this.originalSet = originalSet;
  }

  /**
   * @return the trainingSet
   */
  public Set<RandomAssignmentItem> getTrainingSet()
  {
    return trainingSet;
  }

  /**
   * @param trainingSet the trainingSet to set
   */
  public void setTrainingSet(Set<RandomAssignmentItem> trainingSet)
  {
    this.trainingSet = trainingSet;
  }

  /**
   * @return the testSet
   */
  public Set<RandomAssignmentItem> getTestSet()
  {
    return testSet;
  }

  /**
   * @param testSet the testSet to set
   */
  public void setTestSet(Set<RandomAssignmentItem> testSet)
  {
    this.testSet = testSet;
  }

  /**
   * @return the trainingRatio
   */
  public float getTrainingRatio()
  {
    return trainingRatio;
  }

  /**
   * @param trainingRatio the trainingRatio to set
   */
  public void setTrainingRatio(float trainingRatio)
  {
    this.trainingRatio = trainingRatio;
  }
}
