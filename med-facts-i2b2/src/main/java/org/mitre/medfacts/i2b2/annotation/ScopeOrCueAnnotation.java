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
public class ScopeOrCueAnnotation extends Annotation
{
  protected ScopeType scopeType;

  /**
   * @return the scopeType
   */
  public ScopeType getScopeType()
  {
    return scopeType;
  }

  /**
   * @param scopeType the scopeType to set
   */
  public void setScopeType(ScopeType scopeType)
  {
    this.scopeType = scopeType;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("SCOPE OR CUE ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; scope type: ");
    w.append(scopeType.toString());
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
    w.append(getScopeType().toString().toLowerCase());
    w.append("\"");
    return w.toString();
  }

}
