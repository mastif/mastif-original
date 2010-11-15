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
public class RelationAnnotation extends Annotation
{
  protected RelationType relationType;
  protected String otherConceptText;
  protected Location otherConceptBegin;
  protected Location otherConceptEnd;

  /**
   * @return the relationType
   */
  public RelationType getRelationType()
  {
    return relationType;
  }

  /**
   * @param relationType the relationType to set
   */
  public void setRelationType(RelationType relationType)
  {
    this.relationType = relationType;
  }

  /**
   * @return the otherConceptText
   */
  public String getOtherConceptText()
  {
    return otherConceptText;
  }

  /**
   * @param otherConceptText the otherConceptText to set
   */
  public void setOtherConceptText(String otherConceptText)
  {
    this.otherConceptText = otherConceptText;
  }

  /**
   * @return the otherConceptBegin
   */
  public Location getOtherConceptBegin()
  {
    return otherConceptBegin;
  }

  /**
   * @param otherConceptBegin the otherConceptBegin to set
   */
  public void setOtherConceptBegin(Location otherConceptBegin)
  {
    this.otherConceptBegin = otherConceptBegin;
  }

  /**
   * @return the otherConceptEnd
   */
  public Location getOtherConceptEnd()
  {
    return otherConceptEnd;
  }

  /**
   * @param otherConceptEnd the otherConceptEnd to set
   */
  public void setOtherConceptEnd(Location otherConceptEnd)
  {
    this.otherConceptEnd = otherConceptEnd;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("RELATION ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; relation type: ");
    w.append(relationType.toString());
    w.append("; conceptText: \"");
    w.append(conceptText.replaceAll("\"", "\\\""));
    w.append("\";");
    w.append("other begin: ");
    w.append(otherConceptBegin.toString());
    w.append("; other end: " );
    w.append(otherConceptEnd.toString());
    w.append("}");
    return w.toString();
  }

  public String toI2B2String()
  {
    StringWriter w = new StringWriter();
    w.append("c=\"");
    w.append(getConceptText());
    w.append("\" ");
    w.append(begin.toString());
    w.append(" ");
    w.append(end.toString());
    w.append("||");
    w.append("t=\"");
    w.append(getRelationType().toString().toLowerCase());
    w.append("\"");
    w.append("||");
    w.append("c=\"");
    w.append(getOtherConceptText().replaceAll("\"", "\\\""));
    w.append("\"");
    w.append(" ");
    w.append(getOtherConceptBegin().toString());
    w.append(" ");
    w.append(getOtherConceptEnd().toString());
    return w.toString();
  }


}
