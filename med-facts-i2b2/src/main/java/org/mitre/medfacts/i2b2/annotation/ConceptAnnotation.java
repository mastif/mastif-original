/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.annotation;

import java.io.StringWriter;
import java.util.List;

/**
 *
 * @author MCOARR
 */
public class ConceptAnnotation extends Annotation
{
  protected ConceptType conceptType;
  protected List<ScopeAnnotation> EnclosingScopes; /* Alex Yeh: list of scopes that contain this concept.
                                                    *  Declare here instead of in AssertionAnnotation class because the latter might not be used with "assertions" that do not mention the expected value.
                                                    */

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

  /**
   * @return the EnclosingScopes
   */
  public List<ScopeAnnotation> getEnclosingScopes()
  {
    return EnclosingScopes;
  }

  /**
   * @param EnclosingScopes the EnclosingScopes to set
   */
  public void setEnclosingScopes(List<ScopeAnnotation> EnclosingScopes)
  {
    this.EnclosingScopes = EnclosingScopes;
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
