package org.mitre.medfacts.i2b2.util;

public class Location
{
  protected int line;
  protected int character;

  public Location()
  {
    line = -1;
    character = -1;
  }

  public Location(int line, int character)
  {
    this.line = line;
    this.character = character;
  }

  public Location(String lineText, String characterText)
  {
    int line = Integer.parseInt(lineText);
    this.line = line;
    int character = Integer.parseInt(characterText);
    this.character = character;
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
   * @return the character
   */
  public int getCharacter()
  {
    return character;
  }

  /**
   * @param character the character to set
   */
  public void setCharacter(int character)
  {
    this.character = character;
  }

  @Override
  public String toString()
  {
    return getLine() + ":" + getCharacter();
  }
}
