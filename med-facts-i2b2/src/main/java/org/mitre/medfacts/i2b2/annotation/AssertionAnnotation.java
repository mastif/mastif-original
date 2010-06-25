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
public class AssertionAnnotation extends ConceptAnnotation
{
  protected AssertionValue assertionValue;

  /**
   * @return the assertionValue
   */
  public AssertionValue getAssertionValue()
  {
    return assertionValue;
  }

  /**
   * @param assertionValue the assertionValue to set
   */
  public void setAssertionValue(AssertionValue assertionValue)
  {
    this.assertionValue = assertionValue;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("ASSERTION ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; concept type: ");
    w.append(conceptType.toString());
    w.append("; conceptText: \"");
    w.append(conceptText.replace("\"", "\\\""));
    w.append("\";");
    w.append("assertion value: ");
    w.append(assertionValue.toString());
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
    w.append(getConceptType().toString().toLowerCase());
    w.append("\"");
    w.append("||");
    w.append("a=\"");
    w.append(getAssertionValue().toString().toLowerCase());
    w.append("\"");
    return w.toString();
  }

}
