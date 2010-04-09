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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.mayo.bmi.util.AnnotationType;
import edu.mayo.bmi.util.ArrayMap;
import edu.mayo.bmi.util.UmlsConcept;

/**
 * @author M039575
 * @version 1.01
 * 
 * Reads UIMA XCAS file to load annotations
 * 
 */
public class UimaXmlRepresentation extends XmlRepresentation {
	//public static String TAG_TCAS_DOC = "uima.tcas.Document";
	public static String TAG_TCAS_DOC = "uima.tcas.Document";
	public static String TAG_CAS_SOFA = "uima.cas.Sofa";
	public static String TAG_TCAS_SOFA_STRING_ATTRIBUTE = "sofaString";
	public static String TAG_I = "i";
	//public static String TAG_UMLS_CONCEPT = "edu.mayo.bmi.uima.common.types.UmlsConcept";
	// changed "common.types" to "core.ae.type"
	public static String TAG_UMLS_CONCEPT = "edu.mayo.bmi.uima.core.ae.type.UmlsConcept";
	public static String TAG_UIMA_FS_ARR = "uima.cas.FSArray";

	public static String ATTRIBUTE_CERTAINITY = "certainty";
    public static String ATTRIBUTE_FOCUS_TEXT = "FocusText";
	public static String ATTRIBUTE_STATUS = "status";

	public static String ATTRIBUTE_ID = "_id";
	public static String ATTRIBITE_CODING_SCHEME = "codingScheme";
	public static String ATTRIBITE_CODE = "code";
	public static String ATTRIBITE_OID = "oid";
	public static String ATTRIBITE_CUI = "cui";
	public static String ATTRIBITE_TUI = "tui";
	public static String ATTRIBUTE_ONTOLOGY_CONCEPT_ARR = "_ref_ontologyConceptArr";

	public static String VALUE_NEGATED = "-1";
	public static ArrayList<Annot> annotList = new ArrayList<Annot>();

	public UimaXmlRepresentation(File file) throws Exception {
		super(file);
		init();
	}

	public UimaXmlRepresentation(InputStream fis) throws Exception {
		super(fis);
		init();
	}

	/**
	 * constructor
	 * 
	 * @param aUimaXmlRepresentation
	 * @throws Exception
	 */

	public UimaXmlRepresentation(UimaXmlRepresentation aUimaXmlRepresentation) throws Exception {
		super(aUimaXmlRepresentation); // ideally this instance should not use
		// any file operations

		init();

		fsArray.putAll(aUimaXmlRepresentation.fsArray);
		umlsConcepts.addAll(aUimaXmlRepresentation.umlsConcepts);
	}

	private void init() {
		fsArray = new ArrayMap();
		umlsConcepts = new ArrayList();
	}

	/**
	 * <edu.mayo.bmi.uima.common.types.NamedEntityAnnotation _indexed="1"
	 * _id="13096" sofa="1" begin="1750" end="1754" discoveryTechnique="1"
	 * _ref_ontologyConceptArr="13092" status="0" certainty="0" typeID="3"
	 * confidence="0.0" segmentID="SIMPLE_SEGMENT" uid="59"/>
	 * 
	 * <uima.cas.FSArray _id="13092" size="2"> <i>13080</i> <i>13086</i>
	 * </uima.cas.FSArray>
	 * 
	 * <edu.mayo.bmi.uima.common.types.UmlsConcept _id="13080"
	 * codingScheme="SNOMED" code="63448001" oid="63448001#SNOMED" cui="C0016928"
	 * tui="T033"/>
	 */

	public void processXml() {
		loadFSArrays();
		loadUmlsConcept();
		loadAnnotations();
	}

	/**
	 * 
	 * @param iOntConceptId -
	 *          _id attribute of the annotation
	 * @return - int array of ids representing umls concept Ids
	 */
	public int[] getUmlsIds(int iOntConceptId) {
		int[] umlsIdArr = null;
		Iterator itr = fsArray.keySet().iterator();
		while (itr.hasNext()) {
			String keyId = (String) itr.next();
			int iKey = Integer.parseInt(keyId);

			if (iKey == iOntConceptId) {
				umlsIdArr = (int[]) fsArray.get(keyId);
			}
		}

		return umlsIdArr;
	}

	/**
	 * @param umlsId -
	 *          retrieved using getUmlsIds methods for a given annotation
	 * @return
	 */

	public UmlsConcept getUmlsConceptObj(String umlsId) {
		for (int i = 0; i < umlsConcepts.size(); i++) {
			UmlsConcept umls = (UmlsConcept) umlsConcepts.get(i);
			if (umls.getId().equals(umlsId))
				return umls;
		}
		return null;
	}

	/**
	 * 
	 * @param umlsIds
	 * @return
	 */
	public List getAllUmlsConceptObjs(int[] umlsIds) {
		List objs = new ArrayList();

		for (int i = 0; i < umlsConcepts.size(); i++) {
			UmlsConcept umls = (UmlsConcept) umlsConcepts.get(i);
			for (int j = 0; j < umlsIds.length; j++) {
				if (umls.getIntId() == umlsIds[j])
					objs.add(umls);
			}
		}
		return objs;
	}

	public List getAllUmlsConceptObjs(int iOntConceptId) {
		int[] arr = getUmlsIds(iOntConceptId);
		return getAllUmlsConceptObjs(arr);
	}

	// ---- private methods ---------
	private void loadFSArrays() {
		NodeList nodeList = xmlDocument.getElementsByTagName(TAG_UIMA_FS_ARR);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);

			if (!isElementType(n))
				continue;

			Element e = (Element) n;
			String _id = e.getAttribute(ATTRIBUTE_ID);

			NodeList children = e.getElementsByTagName(TAG_I);
			int[] umlsIds = getUmlsIds(children);

			fsArray.put(_id, umlsIds);
		}
	}

	private int[] getUmlsIds(NodeList children) {
		int[] intarr = new int[children.getLength()];
		for (int i = 0, j = 0; i < children.getLength(); i++) {
			Node n = children.item(i);

			if (!isElementType(n))
				continue;

			String id = getNonNullChildContent(n);
			if (id == null)
				continue;
			// System.out.println("Found id with value '" + id + "'");
			if (id.equals("")) {
				continue;
			}
			int val = Integer.parseInt(id);

			intarr[j++] = val;

		}

		return intarr;
	}

	private String getNonNullChildContent(Node n) {
		NodeList nl = n.getChildNodes();
		String data = "";
		for (int i = 0; i < nl.getLength(); i++) {
			String s = nl.item(i).getNodeValue();
			if (s != null && s.length() > 0)
				data += s;
		}

		return data.trim();
	}

	private boolean isElementType(Node n) {
		return (n.getNodeType() == Node.ELEMENT_NODE);
	}

	private int getIntVal(List l, int i) {
		return ((Integer) l.get(i)).intValue();
	}

	private void loadUmlsConcept() {
		NodeList nodeList = xmlDocument.getElementsByTagName(TAG_UMLS_CONCEPT);

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);

			if (!isElementType(n))
				continue;

			Element e = ((Element) n);
			String _id = e.getAttribute(ATTRIBUTE_ID);
			String codingSchema = e.getAttribute(ATTRIBITE_CODING_SCHEME);
			String code = e.getAttribute(ATTRIBITE_CODE);
			String oid = e.getAttribute(ATTRIBITE_OID);
			String cui = e.getAttribute(ATTRIBITE_CUI);
			String tui = e.getAttribute(ATTRIBITE_TUI);

			UmlsConcept umlsConcept = new UmlsConcept(_id, codingSchema, code, oid, cui, tui);

			umlsConcepts.add(umlsConcept);
		}
	}

	private void loadAnnotations() {
		Element e;
		StringBuffer docText = new StringBuffer();

		/**
		 * load document text
		 */
		NodeList textNodeList = xmlDocument.getElementsByTagName(TAG_CAS_SOFA);
		if (textNodeList.getLength() > 0) { // if not a plain text file
			Node n = textNodeList.item(0);
			if (isElementType(n)) {
				e = (Element) n;
				String sofaString = e.getAttribute(TAG_TCAS_SOFA_STRING_ATTRIBUTE);
				docText.append(sofaString);
			}
		}
		
		System.out.format("docText: \"%s\"%n", docText);
		
		
//		NodeList textNodeList = xmlDocument.getElementsByTagName(TAG_TCAS_DOC);
//		if (textNodeList.getLength() > 0) { // if not a plain text file
//			Node n = textNodeList.item(0);
//			if (isElementType(n)) {
//				e = (Element) n;
//				NodeList nl = e.getChildNodes();
//				for (int i = 0; i < nl.getLength(); i++)
//					docText.append((nl.item(i) == null ? "" : nl.item(i).toString()));
//
//			}
//		}

		/**
		 * Use _ref_ontologyConceptArr to get umlsIds
		 */

		int tagTypeCount = 9;
		String[] tagTypes = new String[tagTypeCount];
//		tagTypes[0] = "edu.mayo.bmi.uima.common.types.NamedEntityAnnotation";
//		tagTypes[1] = "edu.mayo.bmi.uima.common.types.SentenceAnnotation";
//		tagTypes[2] = "edu.mayo.bmi.uima.common.types.WordTokenAnnotation";
//		tagTypes[3] = "edu.mayo.bmi.uima.common.types.PunctTokenAnnotation";
//		tagTypes[4] = "edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation";
//		tagTypes[5] = "edu.mayo.bmi.uima.common.types.NewlineTokenAnnotation";
//		tagTypes[6] = "edu.mayo.bmi.uima.common.types.NumTokenAnnotation";
		tagTypes[0] = "edu.mayo.bmi.uima.core.ae.type.NamedEntity";
		tagTypes[1] = "edu.mayo.bmi.uima.core.sentence.type.Sentence";
		tagTypes[2] = "edu.mayo.bmi.uima.core.ae.type.WordToken";
		tagTypes[3] = "edu.mayo.bmi.uima.core.ae.type.PunctuationToken";
		tagTypes[4] = "edu.mayo.bmi.uima.core.ae.type.ContractionToken";
		tagTypes[5] = "edu.mayo.bmi.uima.core.ae.type.NewlineToken";
		tagTypes[6] = "edu.mayo.bmi.uima.core.ae.type.NumToken";
		tagTypes[7] = "gov.va.maveric.uima.ctakes.NENegation";
        tagTypes[8] = "edu.mayo.bmi.uima.context.type.NEContext";
		//System.out.format("$$$%ninside for loop over t = 0 .. tagTypeCount BEFORE LOOP%n$$$%n");
		for (int t = 0; t < tagTypeCount; t++) {
			//System.out.format("$$$%ninside for loop over t = 0 .. tagTypeCount; t == %d BEGIN INSIDE LOOP%n$$$%n", t);
			//System.out.format("tag type: %s%n", tagTypes[t]);
			NodeList neNodeList = xmlDocument.getElementsByTagName(tagTypes[t]);

			String start = null;
			String end = null;
			String coveredText = null;
			String typeId = null;
			String ontologyConceptArrId = null;
			String strNegated = null;
			String status = null;
			String pennTag = null;
			String umlsObjsString = null;

			int iTypeId = 0;
			int istart = 0;
			int iend = 0;
			int iOntConceptArrId = 0;
			boolean isNegated = false;
			List umlsObjs = null;

			for (int i = 0; i < neNodeList.getLength(); i++) {
                String focusText = null;
				Element annotation = (Element) neNodeList.item(i);
				typeId = annotation.getAttribute(ATTRIBUTE_TYPE_ID);
				start = annotation.getAttribute(ATTRIBUTE_BEGIN);
				end = annotation.getAttribute(ATTRIBUTE_END);
				// named entity-specific attrs:
				//if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.NamedEntityAnnotation")) {
				if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.core.ae.type.NamedEntity")) {
					strNegated = annotation.getAttribute(ATTRIBUTE_CERTAINITY);
					status = annotation.getAttribute(ATTRIBUTE_STATUS);

					ontologyConceptArrId = annotation.getAttribute(ATTRIBUTE_ONTOLOGY_CONCEPT_ARR);

					isNegated = (strNegated != null && strNegated.equals(VALUE_NEGATED));

					try {
						iOntConceptArrId = Integer.parseInt(ontologyConceptArrId);
					} catch (NumberFormatException nfe) {
					}
					umlsObjs = getAllUmlsConceptObjs(iOntConceptArrId);
					umlsObjsString = umlsObjs.toString();
					
//				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.WordTokenAnnotation")) {
//					pennTag = annotation.getAttribute("pennTag");
//				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.ContractionTokenAnnotation")) {
//					pennTag = new String("contr");
//				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.PunctTokenAnnotation")) {
//					pennTag = new String("punct");
//				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.NumTokenAnnotation")) {
//					pennTag = new String("number");
//				}
				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.core.ae.type.WordToken")) {
					pennTag = annotation.getAttribute("partOfSpeech");
					//pennTag = annotation.getAttribute("pennTag");
				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.core.ae.type.ContractionToken")) {
					pennTag = new String("partOfSpeech");
					//pennTag = new String("contr");
				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.core.ae.type.PunctToken")) {
					pennTag = new String("partOfSpeech");
					//pennTag = new String("punct");
				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.core.ae.type.NumToken")) {
					pennTag = new String("partOfSpeech");
					//pennTag = new String("number");
				} else if (tagTypes[t].equalsIgnoreCase("gov.va.maveric.uima.ctakes.NENegation")) {
					pennTag = new String("negation");
				} else if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.context.type.NEContext")) {
                    pennTag = new String("negationContext");
                    focusText = annotation.getAttribute(ATTRIBUTE_FOCUS_TEXT);
                }
				try {
					System.out.format("start: %s; end: %s; typeId: %s%n", start, end, typeId);
					istart = Integer.parseInt(start);
					System.out.format("istart: %d; ", istart);
					// istart = Integer.parseInt(start) + 8;
					iend = Integer.parseInt(end);
					System.out.format("iend: %d; ", iend);
					// iend = Integer.parseInt(end) + 8;
					iTypeId = Integer.parseInt(typeId);
					System.out.format("iTypeId: %d; ", iTypeId);
				} catch (NumberFormatException nfe) {
					System.out.println();
					System.out.println("NumberFormatException!");
				}
				System.out.println();

				String typeName = AnnotationType.getTypeName(typeId);
				if (docText == null || docText.length() == 0) {
					System.err.println("empty docText");
					System.out.println("empty docText");
				} else {
					coveredText = docText.substring(istart, iend);
					System.out.format("coveredText: %s%n", coveredText);
					// coveredText = docText.substring(istart+8, iend+8); //
					// sometimes things are off by 8, apparently depending on
					// which JRE?
				}

				// Just output to stdout as example ....
				// System.out.println("Start=" + istart + "  End=" + iend + "  Text = '" + coveredText + "'");
				// System.out.println("Type=" + typeName + "  status=" + status + "  Negated=" + isNegated);
				// if (tagTypes[t].equalsIgnoreCase("edu.mayo.bmi.uima.common.types.NamedEntityAnnotation")) {
				// 	System.out.println("umlsObj[] = " + umlsObjs.toString());
				// }
				// System.out.println("");

				System.out.println("creating new Annot...");
				Annot annot = new Annot();
				annot.start = istart;
				annot.end = iend;
				annot.type = typeName;
				if (isNegated) {
					annot.neg = "neg";
				} else {
					annot.neg = "pos";
				}
				annot.status = status;
				annot.umlsObjsString = umlsObjsString;
				annot.pennTag = pennTag;
				annot.text = coveredText;
				annot.localName = annotation.getTagName();
                annot.focusText = focusText;
				// System.out.println("localName in Mayo code:" + annot.localName);
				// System.out.println("Adding annot...");
				annotList.add(annot);
				//System.out.println("finished creating annot.");
				//System.out.format("new annot: %s%n", annot.toString());

				// Reset
				typeName = null;
				coveredText = null;
				status = null;
				pennTag = null;
				umlsObjsString = null;
				//System.out.format("$$$%ninside for loop over i = 0 .. neNodeList.length; i == %d END INSIDE LOOP%n$$$%n", i);
			}
			//System.out.format("$$$%nafter for loop over i = 0 .. neNodeList.length; AFTER INSIDE LOOP%n$$$%n");

			//System.out.format("$$$%ninside for loop over t = 0 .. tagTypeCount; 5 == %d AFTER LOOP%n$$$%n", t);
		}
		//System.out.format("$$$%nafter for loop over t = 0 .. tagTypeCount AFTER LOOP%n$$$%n");
		
		////
		////
		System.out.println("Starting to process CHUNK annotations...");
		tagTypeCount = 6;
		tagTypes = new String[tagTypeCount];
		tagTypes[0] = "edu.mayo.bmi.uima.chunker.type.ADJP";
		tagTypes[1] = "edu.mayo.bmi.uima.chunker.type.ADVP";
		tagTypes[2] = "edu.mayo.bmi.uima.chunker.type.NP";
		tagTypes[3] = "edu.mayo.bmi.uima.chunker.type.PP";
		tagTypes[4] = "edu.mayo.bmi.uima.chunker.type.SBAR";
		tagTypes[5] = "edu.mayo.bmi.uima.chunker.type.VP";
		
		for (int t = 0; t < tagTypeCount; t++) {
			System.out.format("$$$%ninside 2nd for loop over t = 0 .. tagTypeCount; t == %d BEGIN INSIDE LOOP%n$$$%n", t);
			System.out.format("tag type: %s%n", tagTypes[t]);
			NodeList neNodeList = xmlDocument.getElementsByTagName(tagTypes[t]);

			String start = null;
			String end = null;
			String coveredText = null;
			String typeId = null;
			String ontologyConceptArrId = null;
			String strNegated = null;
			String status = null;
			String pennTag = null;
			String umlsObjsString = null;

			int iTypeId = 0;
			int istart = 0;
			int iend = 0;
			int iOntConceptArrId = 0;
			boolean isNegated = false;
			List umlsObjs = null;

			for (int i = 0; i < neNodeList.getLength(); i++) {
				Element annotation = (Element) neNodeList.item(i);
				//typeId = annotation.getAttribute(ATTRIBUTE_TYPE_ID);
				start = annotation.getAttribute(ATTRIBUTE_BEGIN);
				end = annotation.getAttribute(ATTRIBUTE_END);
				String chunkType = annotation.getAttribute(ATTRIBUTE_CHUNK_TYPE);

				try {
					System.out.format("(reminder: tag type: %s)%n", tagTypes[t]);
					System.out.format("start: %s; end: %s; typeId: %s%n", start, end, typeId);
					istart = Integer.parseInt(start);
					System.out.format("istart: %d; ", istart);
					// istart = Integer.parseInt(start) + 8;
					iend = Integer.parseInt(end);
					System.out.format("iend: %d; ", iend);
					// iend = Integer.parseInt(end) + 8;
					//iTypeId = Integer.parseInt(typeId);
					//System.out.format("iTypeId: %d; ", iTypeId);
				} catch (NumberFormatException nfe) {
					System.out.println();
					System.out.println("NumberFormatException!");
				}
				//String typeName = AnnotationType.getTypeName(typeId);
				String typeName = "chunk";
				pennTag = chunkType;
				if (docText == null || docText.length() == 0) {
					System.err.println("empty docText");
					System.out.println("empty docText");
				} else {
					coveredText = docText.substring(istart, iend);
					System.out.format("coveredText: %s%n", coveredText);
					// coveredText = docText.substring(istart+8, iend+8); //
					// sometimes things are off by 8, apparently depending on
					// which JRE?
				}

				System.out.println("creating new Annot...");
				Annot annot = new Annot();
				annot.start = istart;
				annot.end = iend;
				annot.type = typeName;
				if (isNegated) {
					annot.neg = "neg";
				} else {
					annot.neg = "pos";
				}
				annot.status = status;
				annot.umlsObjsString = umlsObjsString;
				annot.pennTag = pennTag;
				annot.text = coveredText;
				annot.localName = annotation.getTagName();
				// System.out.println("localName in Mayo code:" + annot.localName);
				// System.out.println("Adding annot...");
				annotList.add(annot);
				System.out.println("finished creating annot.");
				System.out.format("new annot: %s%n", annot.toString());
				
				// Reset
				typeName = null;
				coveredText = null;
				status = null;
				pennTag = null;
				umlsObjsString = null;
			}
		}
		
	}

	public static void main(String args[]) {
		File f = new File("src/data/sampleXcas.xml");
		System.out.println("Looking for data in file '" + f.getAbsolutePath() + "'");
		System.out.println("");
		UimaXmlRepresentation uxr;
		try {
			uxr = new UimaXmlRepresentation(f);
			uxr.processXml();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// -- private data members -----
	private Map fsArray;
	private List umlsConcepts;
}
