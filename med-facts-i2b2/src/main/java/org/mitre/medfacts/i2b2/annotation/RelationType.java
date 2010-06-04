/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.annotation;

/**
 *
 * @author MCOARR
 */
public enum RelationType
{
  TRIP("TrIP", "treatment improves medical problem"),
  TRWP("TrWP", "treatment worsens medical problem"),
  TRCP("TrCP", "treatment causes medical problem"),
  TRAP("TrAP", "treatment is administered for medical problem"),
  TrNAP("TrNAP", "treatment is not administered because of medical problem"),
  PIP("PIP", "medical problem indicates medical problem"),
  TERP("TeRP", "test reveals medical problem"),
  TECP("TeCP", "test conducted to investigate medical problem")
//  ,
//  NONEPP("NONEPP", "NonePP"),
//  NONETRP("NONETRP", "NONETRP"),
  ,
  TRNAP("TRNAP", "TRNAP")
//  NONETEP("NONETEP", "NONETEP"),
//  UNCERTAINT("UNCERTAINT", "UNCERTAINT")
  ;

  protected String shortName;
  protected String longName;

  RelationType(String shortName, String longName)
  {
    this.shortName = shortName;
    this.longName = longName;
  }

  /**
   * @return the shortName
   */
  public String getShortName()
  {
    return shortName;
  }

  /**
   * @param shortName the shortName to set
   */
  public void setShortName(String shortName)
  {
    this.shortName = shortName;
  }

  /**
   * @return the longName
   */
  public String getLongName()
  {
    return longName;
  }

  /**
   * @param longName the longName to set
   */
  public void setLongName(String longName)
  {
    this.longName = longName;
  }
}
