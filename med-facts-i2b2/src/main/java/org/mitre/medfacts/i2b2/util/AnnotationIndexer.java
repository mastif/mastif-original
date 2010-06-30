/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mitre.medfacts.i2b2.annotation.Annotation;

/**
 *
 * @author MCOARR
 */
public class AnnotationIndexer
{
  protected Map<Long, List<Annotation>> annotationByLine;
  protected Map<Long, Map<Long, List<Annotation>>> annotationByLineAndToken;

  public void indexAnnotations(List<Annotation> allAnnotationsList)
  {
    setAnnotationByLine(new HashMap<Long, List<Annotation>>());
    setAnnotationByLineAndToken(new HashMap<Long, Map<Long, List<Annotation>>>());

    for (Annotation currentAnnotation : allAnnotationsList)
    {
      Location begin = currentAnnotation.getBegin();
      Location end = currentAnnotation.getEnd();
      long beginLine = begin.getLine();
      long beginToken = begin.getCharacter();
      long endToken = end.getCharacter();

      List<Annotation> annotationByLineCurrent = null;
      if (!annotationByLine.containsKey(beginLine))
      {
        annotationByLineCurrent = new ArrayList<Annotation>();
        getAnnotationByLine().put(beginLine, annotationByLineCurrent);
      } else
      {
        annotationByLineCurrent = annotationByLine.get(beginLine);
      }

      annotationByLineCurrent.add(currentAnnotation);

      ////

      Map<Long, List<Annotation>> annotationByLineAndTokenCurrentLine = null;
      if (!annotationByLineAndToken.containsKey(beginLine))
      {
        annotationByLineAndTokenCurrentLine = new HashMap<Long, List<Annotation>>();
        getAnnotationByLineAndToken().put(beginLine, annotationByLineAndTokenCurrentLine);
      } else
      {
        annotationByLineAndTokenCurrentLine = annotationByLineAndToken.get(beginLine);
      }

      for (long i=beginToken; i <= endToken; i++)
      {
        List<Annotation> annotationByLineAndTokenCurrentToken = null;

        if (!annotationByLineAndTokenCurrentLine.containsKey(i))
        {
          annotationByLineAndTokenCurrentToken = new ArrayList<Annotation>();
          annotationByLineAndTokenCurrentLine.put(i, annotationByLineAndTokenCurrentToken);
        } else
        {
          annotationByLineAndTokenCurrentToken = annotationByLineAndTokenCurrentLine.get(i);
        }

        annotationByLineAndTokenCurrentToken.add(currentAnnotation);
      }
    }

  }

  public List<Annotation> findAnnotationsForPosition(long line, long token)
  {
    Map<Long, List<Annotation>> annotationByLineAndTokenCurrentLine = null;
    if (!annotationByLineAndToken.containsKey(line))
    {
      return null;
    }

    annotationByLineAndTokenCurrentLine = annotationByLineAndToken.get(line);

    if (!annotationByLineAndTokenCurrentLine.containsKey(token))
    {
      return null;
    }

    return annotationByLineAndTokenCurrentLine.get(token);
  }

  /**
   * @return the annotationByLine
   */
  public Map<Long, List<Annotation>> getAnnotationByLine()
  {
    return annotationByLine;
  }

  /**
   * @param annotationByLine the annotationByLine to set
   */
  public void setAnnotationByLine(Map<Long, List<Annotation>> annotationByLine)
  {
    this.annotationByLine = annotationByLine;
  }

  /**
   * @return the annotationByLineAndToken
   */
  public Map<Long, Map<Long, List<Annotation>>> getAnnotationByLineAndToken()
  {
    return annotationByLineAndToken;
  }

  /**
   * @param annotationByLineAndToken the annotationByLineAndToken to set
   */
  public void setAnnotationByLineAndToken(Map<Long, Map<Long, List<Annotation>>> annotationByLineAndToken)
  {
    this.annotationByLineAndToken = annotationByLineAndToken;
  }

}
