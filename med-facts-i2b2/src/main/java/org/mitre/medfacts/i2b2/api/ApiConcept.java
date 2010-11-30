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

  public ApiConcept()
  {
  }

  public ApiConcept(int begin, int end, String type)
  {
    this.begin = begin;
    this.end = end;
    this.type = type;
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
}
