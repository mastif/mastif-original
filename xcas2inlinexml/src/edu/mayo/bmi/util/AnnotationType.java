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
public class AnnotationType
{
  public static int getTypeId(String typeName)
  {
    for(int i=0;i<types.length; i++)
    {
      if(types[i][0].equals(typeName))
      {
        int val = TYPE_ID_UNKNOWN;
        
        try
        { val = Integer.parseInt(types[i][1]); }
        catch(NumberFormatException nfe)
        {} //ignore exception
        
        return val;
      }
    }
    return TYPE_ID_UNKNOWN;
  }

  public static String getTypeName(String typeId)
  {
    for(int i=0;i<types.length; i++)
    {
      if(types[i][1].equals(typeId))
        return types[i][0];
    }
    return TYPE_NAME_UNKNOWN;    
  }
  
  public static String getTypeName(int typeId)
  { return getTypeName(String.valueOf(typeId)); }
  
  public static boolean isTypeNamedEntity(int type)
  {
    if (type == TYPE_ID_NAMED_ENTITY)
      return true;
    else if(type == TYPE_ID_DRUGS)
      return true;
    else if(type == TYPE_ID_DISORDER)
      return true;
    else if(type == TYPE_ID_SIGNS_SYMPTONS)
      return true;
    
    return false;
  }
  
  
  public static String[][] types= 
                            {
                              {"Drugs", "1"},
                              {"Disorder", "2"},
                              {"Signs/Symptoms","3"},
                              {"Activity", "4"},
                              {"Procedure", "5"},
                              {"Anatomical Site", "6"},
                              };
  
  public static int TYPE_ID_DRUGS = 1;
  public static int TYPE_ID_DISORDER = 2;
  public static int TYPE_ID_SIGNS_SYMPTONS = 3;
  public static int TYPE_ID_UNKNOWN = -1;
  public static int TYPE_ID_ANY = 0;
  
  //---- collection of types -------------
  public static int TYPE_ID_NAMED_ENTITY = 100;
  
  public static String TYPE_NAME_UNKNOWN = "Unknown";
}
