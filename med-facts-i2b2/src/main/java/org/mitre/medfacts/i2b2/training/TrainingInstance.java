/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;

/**
 *
 * @author MCOARR
 */
public class TrainingInstance
{
  protected String filename;
  protected int lineNumber;
  protected String expectedValue;
  protected Set<String> featureSet = new HashSet<String>();
  protected AssertionAnnotation AssertAnnotateForTI; /* Alex Yeh: the assertion annotation associated with this training instance */

  /**
   * @return the expectedValue
   */
  public String getExpectedValue()
  {
    return expectedValue;
  }

  /**
   * @param expectedValue the expectedValue to set
   */
  public void setExpectedValue(String expectedValue)
  {
    this.expectedValue = expectedValue;
  }

  /**
   * @return the featureList
   */
  public Set<String> getFeatureSet()
  {
    return featureSet;
  }

  /**
   * @param featureList the featureList to set
   */
  public void setFeatureSet(Set<String> featureList)
  {
    this.featureSet = featureList;
  }

  public void addFeature(String conceptTextFeature)
  {
    featureSet.add(conceptTextFeature);
  }

  /**
   * @return the filename
   */
  public String getFilename()
  {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename)
  {
    this.filename = filename;
  }

  /**
   * @return the lineNumber
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * @param lineNumber the lineNumber to set
   */
  public void setLineNumber(int lineNumber)
  {
    this.lineNumber = lineNumber;
  }

  /**
   * @return the lineNumber
   */
  public AssertionAnnotation getAssertAnnotateForTI()
  {
    return AssertAnnotateForTI;
  }

  /**
   * @param lineNumber the lineNumber to set
   */
  public void setAssertAnnotateForTI(AssertionAnnotation AssertAnnotateForTI)
  {
    this.AssertAnnotateForTI = AssertAnnotateForTI;
  }

  public String toStringWithExpectedValue()
  {
    StringBuilder b = new StringBuilder();
    b.append(expectedValue);

    for(String currentFeature : featureSet)
    {
      b.append(" ");
      b.append(currentFeature);
    }

    return b.toString();
  }
}
