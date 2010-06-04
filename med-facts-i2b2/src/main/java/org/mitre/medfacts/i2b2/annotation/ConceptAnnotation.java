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
public class ConceptAnnotation extends Annotation
{
  protected ConceptType conceptType;

  /**
   * @return the conceptType
   */
  public ConceptType getConceptType()
  {
    return conceptType;
  }

  /**
   * @param conceptType the conceptType to set
   */
  public void setConceptType(ConceptType conceptType)
  {
    this.conceptType = conceptType;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("CONCEPT ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; concept type: ");
    w.append(conceptType.toString());
    w.append("; conceptText: \"");
    w.append(conceptText.replaceAll("\"", "\\\""));
    w.append("\"");
    w.append("}");
    return w.toString();
  }

  public String toI2B2String()
  {
    StringWriter w = new StringWriter();
    w.append("c=\"");
    w.append(getConceptText().replaceAll("\"", "\\\""));
    w.append("\" ");
    w.append(begin.toString());
    w.append(" ");
    w.append(end.toString());
    w.append("||");
    w.append("t=\"");
    w.append(getConceptType().toString().toLowerCase());
    w.append("\"");
    return w.toString();
  }

}
