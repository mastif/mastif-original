package org.mitre.medfacts.i2b2.annotation;

import java.io.StringWriter;

/**
 *
 * @author WELLNER
 */
public class PartOfSpeechAnnotation extends Annotation
{

  protected String partOfSpeech;

  /**
   * Get the value of cueWordText
   *
   * @return the value of cueWordText
   */
  public String getPartOfSpeech()
  {
    return partOfSpeech;
  }

  public void setPartOfSpeech(String pos) {
    partOfSpeech = pos;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("Part of Speech ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("}");
    return w.toString();
  }

  public String toI2B2String()
  {
    StringWriter w = new StringWriter();
    w.append("c=\"");
    w.append("\"");
    return w.toString();
  }

}
