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
public class ZoneAnnotation extends Annotation
{
  protected String zoneName;

  public String getZoneName()
  {
    return zoneName;
  }

  public void setZoneName(String zoneName)
  {
    this.zoneName = zoneName;
  }

  @Override
  public String toString()
  {
    StringWriter w = new StringWriter();
    w.append("ZONE ANNOTATION {");
    w.append("begin: ");
    w.append(begin.toString());
    w.append("; end: " );
    w.append(end.toString());
    w.append("; zone name: ");
    w.append(zoneName.toString());
    w.append("; conceptText: \"");
    w.append((conceptText == null ? "" : conceptText.replaceAll("\"", "\\\"")));
    w.append("\"");
    w.append("}");
    return w.toString();
  }

  @Override
  public String toI2B2String()
  {
    StringWriter w = new StringWriter();
    w.append("c=\"");
    w.append((conceptText == null ? "" : conceptText.replaceAll("\"", "\\\"")));
    w.append("\" ");
    w.append(begin.toString());
    w.append(" ");
    w.append(end.toString());
    w.append("||");
    w.append("t=\"");
    w.append(getZoneName());
    w.append("\"");
    return w.toString();
  }
}
