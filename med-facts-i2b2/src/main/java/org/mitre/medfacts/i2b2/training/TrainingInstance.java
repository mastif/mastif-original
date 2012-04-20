/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.training;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mitre.medfacts.i2b2.annotation.Annotation;
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
  protected AssertionAnnotation AssertAnnotateForTI; //The assertion annotation associated with this training instance -Alex Yeh
  protected List<Annotation> annotationsForLine; //List of annotations for the line that this training instance is on (use for print outs) -Alex Yeh
  protected String tokensForLine[]; //String with the tokens for the line that this training instance is on (use for print outs) -Alex Yeh

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
   * @return the assertion annotation for this training instance
   */
  public AssertionAnnotation getAssertAnnotateForTI()
  {
    return AssertAnnotateForTI;
  }

  /**
   * @param AssertAnnotateForTI the AssertAnnotateForTI to set
   */
  public void setAssertAnnotateForTI(AssertionAnnotation AssertAnnotateForTI)
  {
    this.AssertAnnotateForTI = AssertAnnotateForTI;
  }

  /**
   * @return the annotations for the line that this training instance is on
   */
  public List<Annotation> getAnnotationsForLine()
  {
    return annotationsForLine;
  }

  /**
   * @param annotationsForLine the annotationsForLine to set
   */
  public void setAnnotationsForLine(List<Annotation> annotationsForLine)
  {
    this.annotationsForLine = annotationsForLine;
  }

/**
   * @return the token string for the line that this training instance is on
   */
  public String[] getTokensForLine()
  {
    return tokensForLine;
  }

  /**
   * @param tokensForLine the tokensForLine to set
   */
  public void setTokensForLine(String[] tokensForLine)
  {
    this.tokensForLine = tokensForLine;
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

  public String toString()
  {
    StringBuilder b = new StringBuilder();

    int i = 0;
    int size = featureSet.size();
    for(String currentFeature : featureSet)
    {
      b.append(currentFeature);
      i++;
      if (i < size) { b.append(" "); }
    }

    return b.toString();
  }
}
