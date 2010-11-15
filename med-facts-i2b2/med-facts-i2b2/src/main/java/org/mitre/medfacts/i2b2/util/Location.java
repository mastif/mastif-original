package org.mitre.medfacts.i2b2.util;

public class Location
{
  protected int line;
  protected int tokenOffset;

  public Location()
  {
    line = -1;
    tokenOffset = -1;
  }

  public Location(int line, int character)
  {
    this.line = line;
    this.tokenOffset = character;
  }

  public Location(String lineText, String tokenOffsetText)
  {
    int line = Integer.parseInt(lineText);
    this.line = line;
    int tokenOffset = Integer.parseInt(tokenOffsetText);
    this.tokenOffset = tokenOffset;
  }

  /**
   * @return the line
   */
  public int getLine()
  {
    return line;
  }

  /**
   * @param line the line to set
   */
  public void setLine(int line)
  {
    this.line = line;
  }

  /**
   * @return the tokenOffset
   */
  public int getTokenOffset()
  {
    return tokenOffset;
  }

  /**
   * @param tokenOffset the tokenOffset to set
   */
  public void setTokenOffset(int tokenOffset)
  {
    this.tokenOffset = tokenOffset;
  }

  @Override
  public String toString()
  {
    return getLine() + ":" + getTokenOffset();
  }
}
