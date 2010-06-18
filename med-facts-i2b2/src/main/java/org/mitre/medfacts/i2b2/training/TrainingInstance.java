/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.training;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MCOARR
 */
public class TrainingInstance
{
  protected String filename;
  protected int lineNumber;
  protected String expectedValue;
  protected List<String> featureList = new ArrayList<String>();

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
  public List<String> getFeatureList()
  {
    return featureList;
  }

  /**
   * @param featureList the featureList to set
   */
  public void setFeatureList(List<String> featureList)
  {
    this.featureList = featureList;
  }

  public void addFeature(String conceptTextFeature)
  {
    featureList.add(conceptTextFeature);
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
}
