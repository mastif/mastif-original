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
public class ScopeAnnotation extends ScopeOrCueAnnotation
{
  protected int scopeId;

  /**
   * @return the scopeId
   */
  public int getScopeId()
  {
    return scopeId;
  }

  /**
   * @param scopeId the scopeId to set
   */
  public void setScopeId(int scopeId)
  {
    this.scopeId = scopeId;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("SCOPE ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; scope type: ");
    w.append(scopeType.toString().toLowerCase());
    w.append("; conceptText: \"");
    w.append(conceptText.replaceAll("\"", "\\\""));
    w.append("\"; scope id: ");
    w.append(Integer.toString(scopeId));
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
    w.append("||");
    w.append("id=\"");
    w.append(Integer.toString(scopeId));
    w.append("\"");
    return w.toString();
  }

}
