package edu.mayo.bmi.util;
/*
 * Copyright: (c) 2007-2008   Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */

/**
 * @version 1.01
 */
public class UmlsConcept
{
  private String B = " "; // blank character
  public UmlsConcept(String id, 
      String codingSchema, 
      String code, 
      String oid, 
      String cui, 
      String tui)
  {
    super();
    this.id = id;
    this.codingSchema = codingSchema;
    this.code = code;
    this.oid = oid;
    this.cui = cui;
    this.tui = tui;
  }
  
  public String getCode()
  { return code; }
  
  public String getCodingSchema()
  { return codingSchema; }
  
  public String getCui()
  { return cui; }
  
  public String getId()
  { return id; }

  public int getIntId()
  { 
    int i = 0;
    try
    {
      i = Integer.parseInt(id);
    }
    catch(NumberFormatException nfe)
    { } //ignore exception
    return i; 
  }
  
  public String getOid()
  { return oid; }
  
//  public String getTui()
//  { return tui; }
  
  public String toString() {
	  return getCodingSchema() + B + getCode() + B + getCui() + B + getOid();
  }
  private String id;
  private String codingSchema;
  private String code;
  private String oid;
  private String cui;
  private String tui;
}
