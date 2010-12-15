package org.mitre.medfacts.i2b2.api;

/**
 *
 * @author MCOARR
 */
public class ApiConcept
{
  protected int begin;
  protected int end;
  protected String type;
  protected String text;

  public ApiConcept()
  {
  }

  public ApiConcept(int begin, int end, String type, String text)
  {
    this.begin = begin;
    this.end = end;
    this.type = type;
    this.text = text;
  }

  public int getBegin()
  {
    return begin;
  }

  public void setBegin(int begin)
  {
    this.begin = begin;
  }

  public int getEnd()
  {
    return end;
  }

  public void setEnd(int end)
  {
    this.end = end;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String toString()
  {
    return String.format("[%d-%d] %s \"%s\"", begin, end, type, text.replace("\"", "\\\""));
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }
}
