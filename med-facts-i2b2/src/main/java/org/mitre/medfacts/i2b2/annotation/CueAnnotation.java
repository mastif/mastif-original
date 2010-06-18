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
public class CueAnnotation extends ScopeOrCueAnnotation
{
  protected CueSubType cueSubType;
  protected int scopeIdReference;

  /**
   * @return the cueSubType
   */
  public CueSubType getCueSubType()
  {
    return cueSubType;
  }

  /**
   * @param cueSubType the cueSubType to set
   */
  public void setCueSubType(CueSubType cueSubType)
  {
    this.cueSubType = cueSubType;
  }

  /**
   * @return the scopeIdReference
   */
  public int getScopeIdReference()
  {
    return scopeIdReference;
  }

  /**
   * @param scopeIdReference the scopeIdReference to set
   */
  public void setScopeIdReference(int scopeIdReference)
  {
    this.scopeIdReference = scopeIdReference;
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
    w.append("\"; cue sub type: ");
    w.append(cueSubType.toString().toLowerCase());
    w.append("; ");
    w.append("scope ref id: ");
    w.append(Integer.toString(scopeIdReference));
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
    w.append("sub_t=\"");
    w.append(cueSubType.toString().toLowerCase());
    w.append("\" ");
    w.append("ref=\"");
    w.append(Integer.toString(scopeIdReference));
    w.append("\"");
    return w.toString();
  }

}
