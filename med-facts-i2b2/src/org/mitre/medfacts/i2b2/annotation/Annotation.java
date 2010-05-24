/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.annotation;

import java.io.StringWriter;
import org.mitre.medfacts.i2b2.util.Location;

/**
 *
 * @author MCOARR
 */
abstract public class Annotation
{
  protected Location begin;
  protected Location end;
  protected String conceptText;

  /**
   * @return the begin
   */
  public Location getBegin()
  {
    return begin;
  }

  /**
   * @param begin the begin to set
   */
  public void setBegin(Location begin)
  {
    this.begin = begin;
  }

  /**
   * @return the end
   */
  public Location getEnd()
  {
    return end;
  }

  /**
   * @param end the end to set
   */
  public void setEnd(Location end)
  {
    this.end = end;
  }

  /**
   * @return the conceptText
   */
  public String getConceptText()
  {
    return conceptText;
  }

  /**
   * @param conceptText the conceptText to set
   */
  public void setConceptText(String conceptText)
  {
    this.conceptText = conceptText;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; conceptText: \"");
    w.append(conceptText.replace("\"", "\\\""));
    w.append("\"");
    w.append("}");
    return w.toString();
  }

  abstract public String toI2B2String();
}
