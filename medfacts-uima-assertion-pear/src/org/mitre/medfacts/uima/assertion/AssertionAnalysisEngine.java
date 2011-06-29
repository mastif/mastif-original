package org.mitre.medfacts.uima.assertion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceAccessException;
import org.mitre.itc.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.i2b2.api.AssertionDecoderConfiguration;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.types.Assertion;
import org.mitre.medfacts.types.Concept;
import org.mitre.medfacts.types.Concept_Type;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

public class AssertionAnalysisEngine extends JCasAnnotator_ImplBase {
	Logger logger = Logger.getLogger(AssertionAnalysisEngine.class.getName());

	public AssertionAnalysisEngine()
	{
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException
	{
		String contents = jcas.getDocumentText();
		
		int conceptType = Concept.type;
		AnnotationIndex<Annotation> conceptAnnotationIndex =
			jcas.getAnnotationIndex(conceptType);
		
		ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
		for (Annotation annotation : conceptAnnotationIndex)
		{
			Concept conceptAnnotation = (Concept)annotation;
			
			ApiConcept apiConcept = new ApiConcept();
			int begin = conceptAnnotation.getBegin();
			int end = conceptAnnotation.getEnd();
			String conceptText = contents.substring(begin, begin + end + 1);
			
			apiConcept.setBegin(begin);
			apiConcept.setEnd(end);
			apiConcept.setText(conceptText);
			apiConcept.setType(conceptAnnotation.getConceptType());
			
			apiConceptList.add(apiConcept);
		}
		
		String assertionModelContents;
		String scopeModelFilePath;
		String cueModelFilePath;
		File enabledFeaturesFile;
		
		try {
			String assertionModelResourceKey = "assertionModelResource";
			String assertionModelFilePath = getContext().getResourceFilePath(
					assertionModelResourceKey);
			File assertionModelFile = new File(assertionModelFilePath);
			assertionModelContents = StringHandling
					.readEntireContents(assertionModelFile);
			String scopeModelResourceKey = "scopeModelResource";
			scopeModelFilePath = getContext().getResourceFilePath(
					scopeModelResourceKey);
			String cueModelResourceKey = "cueModelResource";
			cueModelFilePath = getContext().getResourceFilePath(
					cueModelResourceKey);
			String enabledFeaturesResourceKey = "enabledFeaturesResource";
			String enabledFeaturesFilePath = getContext().getResourceFilePath(
					enabledFeaturesResourceKey);
			enabledFeaturesFile = new File(enabledFeaturesFilePath);
		} catch (ResourceAccessException e) {
			String message = String.format("problem accessing resource");
			throw new RuntimeException(message, e);
		}

//	    String conceptFilePath = currentTextFile.getAbsolutePath().replaceFirst("\\.txt$", ".con");
//	    File conceptFile = new File(conceptFilePath);
//	    logger.info(String.format("    - using concept file \"%s\"...", conceptFile.getName()));
//	    String conceptFileContents = StringHandling.readEntireContents(conceptFile);
//	    //List<Concept> parseConceptFileContents(conceptFileContents);
//
//	    LineTokenToCharacterOffsetConverter converter =
//	        new LineTokenToCharacterOffsetConverter(contents);
//
//	    List<ApiConcept> apiConceptList = parseConceptFile(conceptFile, contents, converter);

	    LineTokenToCharacterOffsetConverter converter =
          new LineTokenToCharacterOffsetConverter(contents);
	    
	    AssertionDecoderConfiguration assertionDecoderConfiguration =
	        new AssertionDecoderConfiguration();

	    ScopeParser scopeParser = new ScopeParser(scopeModelFilePath, cueModelFilePath);
	    assertionDecoderConfiguration.setScopeParser(scopeParser);
	    
	    Set<String> enabledFeatureIdSet = null;
	    enabledFeatureIdSet = BatchRunner.loadEnabledFeaturesFromFile(enabledFeaturesFile);
	    assertionDecoderConfiguration.setEnabledFeatureIdSet(enabledFeatureIdSet);

	    JarafeMEDecoder assertionDecoder = null;
	    assertionDecoder = new JarafeMEDecoder(assertionModelContents);
	    assertionDecoderConfiguration.setAssertionDecoder(assertionDecoder);

	    SingleDocumentProcessor p = new SingleDocumentProcessor(converter);
	    p.setAssertionDecoderConfiguration(assertionDecoderConfiguration);
	    p.setContents(contents);
	    for (ApiConcept apiConcept : apiConceptList)
	    {
	      logger.info(String.format("dir loader concept: %s", apiConcept.toString()));
	      p.addConcept(apiConcept);
	    }
	    p.processSingleDocument();
	    Map<Integer, String> assertionTypeMap = p.getAssertionTypeMap();
	    logger.info(String.format("    - done processing \"%s\"."));
	    
	    for (Entry<Integer, String>  current : assertionTypeMap.entrySet())
	    {
	    	String currentAssertionType = current.getValue();
	    	Integer currentIndex = current.getKey();
	    	ApiConcept originalConcept = apiConceptList.get(currentIndex);
	    	
	    	Assertion assertion = new Assertion(jcas, originalConcept.getBegin(), originalConcept.getEnd());
	    	assertion.setAssertionType(currentAssertionType);
	    	assertion.addToIndexes();
	    }
	}

}
