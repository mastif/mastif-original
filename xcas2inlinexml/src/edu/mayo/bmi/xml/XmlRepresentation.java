package edu.mayo.bmi.xml;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.mayo.bmi.xml.XmlDocument;

/**
 * @author m039575
 * @version 1.01
 * 
 * Generic class to represent a document with annotation.
 * Note: This class must not be used as a concrete class but must be extended. 
 */

public abstract class XmlRepresentation 
implements XmlDocument
{
  public static String ATTRIBUTE_TYPE_ID = "typeID";
  public static String ATTRIBUTE_BEGIN = "begin";
  public static String ATTRIBUTE_END = "end";	
  public static String TAG_NAMED_ENTITIY = "edu.mayo.bmi.uima.common.types.NamedEntityAnnotation";
  public static String ATTRIBUTE_COVERED_TEXT = "plain.text.covered.text";
	
  //-- constructors ----
  public XmlRepresentation(File file)
  throws Exception
  { 
    /**
     * some cases (create from current, etc) pass a null, assume no file operations performed
     */
    if(file != null) 
      init(file);
    
    initArrays();
  }

  public XmlRepresentation(InputStream fis)
  throws Exception
  { 
    this.fis = fis;
    /**
     * some cases (create from current, etc) pass a null, assume no file operations performed
     */
    if(this.fis != null) 
      init();
    
    initArrays();
  }

  /**
   * constructor
   * @param xmlRepresentation
   */
  public XmlRepresentation(XmlRepresentation xmlRepresentation)
  throws Exception
  {
    initArrays();
    this.fis = xmlRepresentation.fis;
    this.xmlDocument = xmlRepresentation.xmlDocument;
    this.annotations.addAll(xmlRepresentation.annotations);
    this.annotationMatches.addAll(xmlRepresentation.annotationMatches);
  }

  //-- primary public methods -------
  
  /** 
   * output to stdout some information useful for debugging/verifying
   */
  public void outputSizesOfLists()
  {
	  System.out.println("  annotationMatches.size() = " + (annotationMatches==null ? -1 : annotationMatches.size()));
	  System.out.println("  annotations.size() = " +  (annotations==null ? -1 : annotations.size()));
  }
  
  
  /**
   * Note: This method is to be used with caution.
   * This is intended to be used only to process a collection of documents
   */
  public void accumulate(XmlRepresentation xr)
  {
    annotations.addAll(xr.annotations);
    annotationMatches.addAll(xr.annotationMatches);
  }

  
  //-- protected members ----------
  protected Element getFirstChildByTagName(Node parent, String tagName)
  {
    NodeList nl = parent.getChildNodes();
    for(int i=0; i<nl.getLength(); i++)
    {
      Node n = nl.item(i);
      if(n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase(tagName))
        return (Element)n;
    }
    return null;
  }  

  protected List getChildrenByTagName(Node parent, String tagName)
  {
    List l = new ArrayList();
    
    NodeList nl = parent.getChildNodes();
    for(int i=0; i<nl.getLength(); i++)
    {
      Node n = nl.item(i);
      if(n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase(tagName))
        l.add(n);
    }
    return l;
  }  
  
  //-- public, protected getters ---
  public List getAnnotations()
  { return annotations; }
  
  public List getCorrespondingAnnotations()
  { return annotationMatches; }
  
  public int getCorrespondingAnnotationsCount()
  { return annotationMatches.size(); }

  protected Document getDocument()
  { return xmlDocument;  }
  
  public void reinit()
  {
    annotations = new ArrayList();
    annotationMatches.clear();
    filename = "";
  }
  
  //-- private members ---
  private void init(File file)
  throws Exception
  {
    try
    {
      fis = new FileInputStream(file);
      fis2 = new FileInputStream(file);
      init();
      filename = file.getName();
    }
    catch(FileNotFoundException fne)
    { throw new Exception(fne); }
  }
  
  private void init()
  throws Exception
  {
    try
    {
      // could be two different formats - XML or plain text
      filename = "";
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      xmlDocument = db.parse(fis);
    }
    catch(ParserConfigurationException pce)
    { throw new Exception(pce); }  // XmlRepresentationException
    catch(IOException ie)
    { throw new Exception(ie); }  // XmlRepresentationException
  }
  

  

  private void initArrays()
  {
    annotations = new ArrayList();
    annotationMatches = new ArrayList();
  }

  
  
  //-- protected data --
  protected InputStream fis;
  protected InputStream fis2;
  protected Document xmlDocument;
  protected List annotations;
  
  protected List annotationMatches; 
  
  private String filename = "";
  
  public String getFilename() 
  {
	  return filename;
  }
}
