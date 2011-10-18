package org.mitre.medfacts.uima.assertion;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.i2b2.api.AssertionDecoderConfiguration;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.i2b2.api.ctakes.CharacterOffsetToLineTokenConverterCtakesImpl;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.types.Assertion;
import org.mitre.medfacts.types.Concept;
import org.mitre.medfacts.types.Concept_Type;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.WordToken;

public class AssertionAnalysisEngine extends JCasAnnotator_ImplBase {
	Logger logger = Logger.getLogger(AssertionAnalysisEngine.class.getName());

	public AssertionAnalysisEngine()
	{
	}

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException
	{
		String contents = jcas.getDocumentText();
		
		//String tokenizedContents = tokenizeCasDocumentText(jcas);
		
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
			apiConcept.setExternalId(conceptAnnotation.getAddress());
			
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

//	    LineTokenToCharacterOffsetConverter converter =
//          new LineTokenToCharacterOffsetConverter(contents);
	    
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

	    SingleDocumentProcessor p = new SingleDocumentProcessor();
	    p.setAssertionDecoderConfiguration(assertionDecoderConfiguration);
	    //p.setContents(tokenizedContents);
      p.setContents(contents);
      CharacterOffsetToLineTokenConverter converter = new CharacterOffsetToLineTokenConverterCtakesImpl();
      p.setConverter2(converter);
	    for (ApiConcept apiConcept : apiConceptList)
	    {
	      logger.info(String.format("dir loader concept: %s", apiConcept.toString()));
	      p.addConcept(apiConcept);
	    }
	    p.processSingleDocument();
	    Map<Integer, String> assertionTypeMap = p.getAssertionTypeMap();
	    logger.info(String.format("    - done processing \"%s\"."));
	    
	    Map<Integer, Annotation> annotationMap = generateAnnotationMap(jcas, Concept.type);
	    
	    for (Entry<Integer, String>  current : assertionTypeMap.entrySet())
	    {
	    	String currentAssertionType = current.getValue();
	    	Integer currentIndex = current.getKey();
	    	ApiConcept originalConcept = apiConceptList.get(currentIndex);
	    	
	    	Assertion assertion = new Assertion(jcas, originalConcept.getBegin(), originalConcept.getEnd());
	    	assertion.setAssertionType(currentAssertionType);
	    	Concept associatedConcept = (Concept) annotationMap.get(originalConcept.getExternalId());
	      assertion.setAssociatedConcept(associatedConcept);
	    	assertion.addToIndexes();
	    	
	    	
	    }
	}

  public Map<Integer, Annotation> generateAnnotationMap(JCas jcas)
  {
    return generateAnnotationMap(jcas, null);
  }
  
	public Map<Integer, Annotation> generateAnnotationMap(JCas jcas, Integer typeId)
  {
	  Map<Integer, Annotation> annotationMap = new HashMap<Integer, Annotation>();
	  
	  AnnotationIndex<Annotation> index = null;
	  if (typeId == null)
	  {
	    index = jcas.getAnnotationIndex();
	  } else
	  {
	    index = jcas.getAnnotationIndex(typeId);
	  }
	  FSIterator<Annotation> iterator = index.iterator();
	  while (iterator.hasNext())
	  {
	    Annotation current = iterator.next();
	    int address = current.getAddress();
	    annotationMap.put(address, current);
	  }
	  
	  return annotationMap;
  }

//  public String tokenizeCasDocumentText(JCas jcas)
//  {
//    ArrayList<ArrayList<String>> arrayOfLines = construct2DTokenArray(jcas);
//    
//    String spaceSeparatedTokensInput = convert2DTokenArrayToText(arrayOfLines);
//
//    return spaceSeparatedTokensInput;
//  }
//
//  public ArrayList<ArrayList<String>> construct2DTokenArray(JCas jcas)
//  {
//    int sentenceType = Sentence.type;
//    AnnotationIndex<Annotation> sentenceAnnotationIndex =
//      jcas.getAnnotationIndex(sentenceType);
//    ArrayList<ArrayList<String>> arrayOfLines = new ArrayList<ArrayList<String>>();
//    
//    //ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
//    for (Annotation annotation : sentenceAnnotationIndex)
//    {
//      Sentence sentence = (Sentence)annotation;
//      int sentenceBegin = sentence.getBegin();
//      int sentenceEnd = sentence.getEnd();
//      
//      AnnotationIndex<Annotation> tokenAnnotationIndex = jcas.getAnnotationIndex(BaseToken.type);
//      ArrayList<String> arrayOfTokens = new ArrayList<String>();
//      for (Annotation baseTokenAnnotationUntyped : tokenAnnotationIndex)
//      {
//        // ignore tokens that are outside of the sentence.
//        // there has to be a better way to do this with Constraints, but this
//        // should work for now...
//        if (baseTokenAnnotationUntyped.getBegin() < sentenceBegin ||
//            baseTokenAnnotationUntyped.getEnd() > sentenceEnd)
//        {
//          continue;
//        }
//        BaseToken baseToken = (BaseToken)baseTokenAnnotationUntyped;
//        if (baseToken instanceof WordToken ||
//            baseToken instanceof PunctuationToken)
//        {
//          String currentTokenText = baseToken.getCoveredText();
//          arrayOfTokens.add(currentTokenText);
//        }
//      }
//      arrayOfLines.add(arrayOfTokens);
//      
//    }
//    return arrayOfLines;
//  }
//
  public String convert2DTokenArrayToText(ArrayList<ArrayList<String>> arrayOfLines)
  {
    final String DELIM = " ";
    StringWriter writer = new StringWriter();
    PrintWriter printer = new PrintWriter(writer);
    
    boolean isFirstLine = true;
    for (ArrayList<String> line : arrayOfLines)
    {
      if (!isFirstLine)
      {
        printer.println();
      }
      
      boolean isFirstTokenOnLine = true;
      for (String currentToken : line)
      {
        if (!isFirstTokenOnLine)
        {
          printer.print(DELIM);
        }
        printer.print(currentToken);
        isFirstTokenOnLine = false;
      }
      
      isFirstLine = false;
    }
    
    printer.close();
    
    String output = writer.toString();
    return output;
  }

}



