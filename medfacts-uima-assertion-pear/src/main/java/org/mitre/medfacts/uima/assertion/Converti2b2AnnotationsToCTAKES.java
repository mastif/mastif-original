package org.mitre.medfacts.uima.assertion;

import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverterDefaultImpl;
import java.io.File;
import java.io.FileInputStream;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.xml.sax.SAXException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription; 
import org.apache.uima.cas.CAS; 
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.cas.TypeSystem; 
import java.util.Collections; 
import org.apache.uima.util.XMLParser;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.jcas.cas.Sofa; 
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.resource.ResourceInitializationException;
import org.mitre.medfacts.types.Assertion;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.apache.uima.cas.text.AnnotationIndex;
//import org.apache.uima.jcas.tcas.Annotation;

import org.mitre.medfacts.zoner.LineAndTokenPosition;
import java.io.PrintWriter;
import java.io.IOException;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import java.util.List;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.WordToken;
import org.apache.uima.jcas.JCas;
import java.io.FileOutputStream;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.util.XMLSerializer;
import org.mitre.medfacts.types.Concept;



/*
 * Comments on using this class:
 * This provides a utility to convert i2b2 format sentence and tokens to cTAKES Sentence and WordToken
 * annotations.  It takes as input the input text file, as well as the assertion annotations (the .ast file).
 * It also takes the pipeline descriptor (the top-level descriptor used for the entire pipeline) and an output
 * file path.  The potentially tricky part of this process involves setting up the descriptor so that 
 * it refers to the correct sub-descriptors, etc.  This probably won't be a problem with a full install
 * of the pipeline, but could be an issue in a development environment - some paths may need to be manually
 * (and temporarily) adjusted).
 */

public class Converti2b2AnnotationsToCTAKES {
	
	private static CAS getTypeSystemFromDescriptor(String descriptor) throws InvalidXMLException, IOException, ResourceInitializationException, CASException {
		XMLParser xmlParser = UIMAFramework.getXMLParser();
		AnalysisEngineDescription tsDesc = xmlParser.parseAnalysisEngineDescription(new XMLInputSource(descriptor));
		return CasCreationUtils.createCas(tsDesc);
	}
	
	/**
	   * Serialize a CAS to a file in XMI format
	   * 
	   * @param aCas
	   *          CAS to serialize
	   * @param name
	   *          output file
	   * @throws SAXException
	   * @throws Exception
	   * 
	   * @throws ResourceProcessException
	   */
	  private static void writeXmi(CAS aCas, File name) throws IOException, SAXException {
	    FileOutputStream out = null;

	    try {
	      // write XMI
	      out = new FileOutputStream(name);
	      XmiCasSerializer ser = new XmiCasSerializer(aCas.getTypeSystem());
	      XMLSerializer xmlSer = new XMLSerializer(out, false);
	      ser.serialize(aCas, xmlSer.getContentHandler());
	    } finally {
	      if (out != null) {
	        out.close();
	      }
	    }
	  }
	
	public static void main(String [] args) throws IOException, InvalidXMLException, CASException, SAXException {
		File currentTextFile = new File(args[0]);
		File assertionFile = new File(args[1]);
		String desc = args[2];
		File xmiOutFile = new File(args[3]);
		String contents = StringHandling.readEntireContents(currentTextFile);
		LineTokenToCharacterOffsetConverter converter = new LineTokenToCharacterOffsetConverter(contents);
		AssertionFileProcessor assertionProcessor = new AssertionFileProcessor();
		List<Annotation> assertions = assertionProcessor.processAnnotationFile(assertionFile);
		
		CAS cas = null;
		try {
			cas = getTypeSystemFromDescriptor(desc);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		JCas jcas = cas.getJCas();
		SingleDocumentProcessor p = new SingleDocumentProcessor();
		p.setContents(contents);
		p.preprocess();
		String [][] tokenArrays = p.getTokenArrays();
		jcas.setSofaDataString(contents, "");
		for (int i=0; i < tokenArrays.length; i++) {
			Sentence sent = new Sentence(jcas);
			LineAndTokenPosition sentStart = new LineAndTokenPosition();
			sentStart.setLine(i+1);
			sentStart.setTokenOffset(0);
			LineAndTokenPosition sentEnd = new LineAndTokenPosition();
			sentEnd.setLine(i+1);
			sentEnd.setTokenOffset(tokenArrays[i].length-1);
			sent.setBegin(converter.convert(sentStart).getBegin()); // get begin of first token
			sent.setEnd(converter.convert(sentEnd).getEnd()); // get end of last token
			sent.addToIndexes();
			for (int j=0; j < tokenArrays[i].length; j++) {
				WordToken tok = new WordToken(jcas);
				LineAndTokenPosition word = new LineAndTokenPosition();
				word.setLine(i+1);
				word.setTokenOffset(j);
				tok.setBegin(converter.convert(word).getBegin());
				tok.setEnd(converter.convert(word).getEnd());
				tok.addToIndexes();
			}
		}
		for (Annotation a : assertions) {
			Concept assertion = new Concept(jcas);
			LineAndTokenPosition assertionStart = new LineAndTokenPosition();
			LineAndTokenPosition assertionEnd = new LineAndTokenPosition();
			assertionStart.setLine(a.getBegin().getLine());
			assertionStart.setTokenOffset(a.getBegin().getTokenOffset());
			assertionEnd.setLine(a.getEnd().getLine());
			assertionEnd.setTokenOffset(a.getBegin().getTokenOffset());
			assertion.setBegin(converter.convert(assertionStart).getBegin());
			assertion.setEnd(converter.convert(assertionEnd).getEnd());
			assertion.addToIndexes();
		}
		writeXmi(cas,xmiOutFile);
	}
	

}
