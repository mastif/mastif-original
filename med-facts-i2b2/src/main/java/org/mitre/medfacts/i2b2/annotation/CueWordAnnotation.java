/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.annotation;

import java.io.StringWriter;

/**
 *
 * @author MCOARR
 */
public class CueWordAnnotation extends Annotation
{

  protected CueWordType cueWordType;
  protected String cueWordClass;
  protected String cueWordText;

  /**
   * Get the value of cueWordText
   *
   * @return the value of cueWordText
   */
  public String getCueWordText()
  {
    return cueWordText;
  }

  /**
   * Set the value of cueWordText
   *
   * @param cueWordText new value of cueWordText
   */
  public void setCueWordText(String cueWordText)
  {
    this.cueWordText = cueWordText;
  }

  /**
   * Get the value of cueWordType
   *
   * @return the value of cueWordType
   */
  public CueWordType getCueWordType()
  {
    return cueWordType;
  }

  /**
   * Set the value of cueWordType
   *
   * @param cueWordType new value of cueWordType
   */
  public void setCueWordType(CueWordType cueWordType)
  {
    this.cueWordType = cueWordType;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("CUE WORD ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; cue word type: ");
    w.append(cueWordType.toString().toLowerCase());
    w.append("; cueWordText: \"");
    w.append(cueWordText.replaceAll("\"", "\\\""));
    w.append("\"");
    w.append("}");
    return w.toString();
  }

  public String toI2B2String()
  {
    StringWriter w = new StringWriter();
    w.append("c=\"");
    w.append(cueWordText.replaceAll("\"", "\\\""));
    w.append("\" ");
    w.append(begin.toString());
    w.append(" ");
    w.append(end.toString());
    w.append("||");
    w.append("t=\"");
    w.append(getCueWordType().toString().toLowerCase());
    w.append("\"");
    return w.toString();
  }

  public String getCueWordClass()
  {
    return cueWordClass;
  }

  public void setCueWordClass(String cueWordClass)
  {
    this.cueWordClass = cueWordClass;
  }

}
