package org.mitre.medfacts.i2b2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.annotation.CueWordAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.annotation.ZoneAnnotation;
import org.mitre.medfacts.i2b2.cli.FeatureUtility;
import org.mitre.medfacts.i2b2.cli.MedFactsRunner;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.AnnotationIndexer;
import org.mitre.medfacts.i2b2.util.Location;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

/**
 *
 * @author MCOARR
 */
public class SingleDocumentProcessor
{
  private static final Logger logger = Logger.getLogger(SingleDocumentProcessor.class.getName());
  
  protected String contents;
  protected List<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
  protected Map<Integer, String> assertionTypeMap;
  protected LineTokenToCharacterOffsetConverter converter;
  //protected Set<String> enabledFeatureIdSet;
  //protected JarafeMEDecoder assertionDecoder;

  protected String arrayOfArrayOfTokens[][] = null;
  protected List<Annotation> allAnnotationList = new ArrayList<Annotation>();
  protected Map<AnnotationType,List<Annotation>> annotationsByType =
      new EnumMap<AnnotationType, List<Annotation>>(AnnotationType.class);

  AnnotationIndexer indexer = new AnnotationIndexer();

  //protected ScopeParser scopeParser;
  protected AssertionDecoderConfiguration assertionDecoderConfiguration;

  public SingleDocumentProcessor()
  {
  }

  public SingleDocumentProcessor(LineTokenToCharacterOffsetConverter converter)
  {
    this.converter = converter;
  }

  protected void preExecutionTest()
  {
    if (converter == null)
    {
      converter =
          new LineTokenToCharacterOffsetConverter(contents);
    }
  }
  
  public void processSingleDocument()
  {
    preExecutionTest();
    preprocess();
    generateAnnotations();
    indexer.indexAnnotations(allAnnotationList);
    //MedFactsRunner.linkAnnotations(annotationsByType, /*annotationsByType.get(AnnotationType.ASSERTION)*/, indexer);
    Map<Integer, TrainingInstance> trainingInstanceMap = generateFeatures();
    assertionTypeMap = decode(trainingInstanceMap);
    postprocess();
  }

  public void postprocess()
  {
  }

  public String getContents()
  {
    return contents;
  }

  public void setContents(String contents)
  {
    this.contents = contents;
  }

  public void addConcept(int begin, int end, String conceptType, String text)
  {
    ApiConcept apiConcept = new ApiConcept(begin, end, conceptType, text, null);
    apiConceptList.add(apiConcept);
  }

  public void addConcept(ApiConcept apiConcept)
  {
    apiConceptList.add(apiConcept);
  }

  public List<ApiConcept> getApiConceptList()
  {
    return apiConceptList;
  }

  public void setApiConceptList(List<ApiConcept> apiConceptList)
  {
    this.apiConceptList = apiConceptList;
  }

  public void preprocess()
  {
    String arrayOfArrayOfTokens[][] = null;
    String arrayOfLines[] = null;

    Pattern endOfLinePattern = LineTokenToCharacterOffsetConverter.endOfLinePattern;
    Pattern spacePattern = LineTokenToCharacterOffsetConverter.spacePattern;

    arrayOfLines = endOfLinePattern.split(contents);

    arrayOfArrayOfTokens = new String[arrayOfLines.length][];
    int i=0;
    for (String currentLine : arrayOfLines)
    {
      String arrayOfTokens[] = spacePattern.split(currentLine);
      arrayOfArrayOfTokens[i] = arrayOfTokens;
      i++;
    }

    this.arrayOfArrayOfTokens = arrayOfArrayOfTokens;
  }

  public void generateAnnotations()
  {
    MedFactsRunner.postProcessForCueWords(arrayOfArrayOfTokens, allAnnotationList, annotationsByType);

    processZones();

    MedFactsRunner.processScopeInProcess(annotationsByType, allAnnotationList, arrayOfArrayOfTokens, assertionDecoderConfiguration.getScopeParser());

    generateConceptAnnotations(apiConceptList, annotationsByType, allAnnotationList);
  }

  public Map<Integer, TrainingInstance> generateFeatures()
  {
    TreeMap<Integer, ApiConcept> problemMap = new TreeMap<Integer, ApiConcept>();
    int i = 0;
    for (ApiConcept currentConcept : apiConceptList)
    {
      if ("PROBLEM".equalsIgnoreCase(currentConcept.getType()))
      {
        problemMap.put(i, currentConcept);
      }
      i++;
    }

    TreeMap<Integer, TrainingInstance> trainingInstanceMap =
        new TreeMap<Integer, TrainingInstance>();

    for (Entry<Integer, ApiConcept> problemEntrySet : problemMap.entrySet())
    {
      Integer index = problemEntrySet.getKey();
      ApiConcept problem = problemEntrySet.getValue();

      // TODO fix this code to either replace/enhance the use of the converter
      // or use some other method
      List<LineAndTokenPosition> beginAndEndOfConcept = calculateBeginAndEndOfConcept(problem);
      LineAndTokenPosition problemBegin = beginAndEndOfConcept.get(0);
      LineAndTokenPosition problemEnd = beginAndEndOfConcept.get(1);
      logger.info(String.format("<<PROB>> %s", problem));
      
//      LineAndTokenPosition problemBegin = converter.convertReverse(problem.getBegin());
//      LineAndTokenPosition problemEnd = converter.convertReverse(problem.getEnd());
      int lineNumber = problemBegin.getLine();

      int conceptBeginTokenOffset = problemBegin.getTokenOffset();
      int conceptEndTokenOffset = problemEnd.getTokenOffset();

      String currentLine[] = arrayOfArrayOfTokens[lineNumber - 1];

      if (assertionDecoderConfiguration.getEnabledFeatureIdSet() == null)
      {
        logger.severe("enabled feature id set is null!!!  using all features");
      } else
      {
        logger.info("enabled feature id set is set; using selected feature set");
      }

      TrainingInstance trainingInstance = new TrainingInstance();

      if (checkForEnabledFeature("conceptUnigrams")) {
          for (int k = conceptBeginTokenOffset; k <= conceptEndTokenOffset; k++) {
              trainingInstance.addFeature("concept_unigram_" + StringHandling.escapeStringForFeatureName(currentLine[k]));
          }
      }

      if (checkForEnabledFeature("wordLeftFeature"))
      {
        List<String> wordLeftFeatureList = FeatureUtility.constructWordLeftFeatureList(problemBegin.getTokenOffset(), problemEnd.getTokenOffset(), currentLine);
        for (String currentFeature : wordLeftFeatureList)
        {
          trainingInstance.addFeature(currentFeature);
        }
      }

      if (checkForEnabledFeature("wordRightFeature"))
      {
        List<String> wordRightFeatureList = FeatureUtility.constructWordRightFeatureList(problemBegin.getTokenOffset(), problemEnd.getTokenOffset(), currentLine);
        for (String currentFeature : wordRightFeatureList)
        {
          trainingInstance.addFeature(currentFeature);
        }
      }

      Set<String> featureSet = trainingInstance.getFeatureSet();
      if (featureSet != null && featureSet.size() > 0)
      {
        trainingInstanceMap.put(index, trainingInstance);
      }

      String conceptText = problem.getText();
      if (checkForEnabledFeature("conceptTextFeature"))
      {
        String conceptTextFeature = MedFactsRunner.constructConceptPhraseFeature(conceptText);
        trainingInstance.addFeature(conceptTextFeature);
      }

      if (checkForEnabledFeature("conceptPseudoHeadFeature"))
      {
          int tokenOffset = problemEnd.getTokenOffset();
          logger.info(String.format("before creating pseudo head; token offset: %d; # tokens on line: %d, tokens: %s", tokenOffset, currentLine.length, Arrays.toString(currentLine)));
          String conceptHead = currentLine[tokenOffset];
          trainingInstance.addFeature(MedFactsRunner.constructConceptHeadFeature(conceptHead));
      }

      /////
      String tokensOnCurrentLine[] = currentLine;
      for (int currentTokenOffset=0; currentTokenOffset < tokensOnCurrentLine.length; currentTokenOffset++)
      {
        String currentToken = tokensOnCurrentLine[currentTokenOffset];
        List<Annotation> annotationsAtCurrentPosition = indexer.findAnnotationsForPosition(lineNumber, currentTokenOffset);

        int scopeCount = 0;
        if (annotationsAtCurrentPosition != null)
        for (Annotation a : annotationsAtCurrentPosition)
        {
          if (a instanceof CueWordAnnotation)
          {
            CueWordAnnotation cueWord = (CueWordAnnotation)a;
            String cueWordType = cueWord.getCueWordType().toString();
            if (checkForEnabledFeature("cueWord"))
            {
              int cueWordBegin = cueWord.getBegin().getTokenOffset();
              int cueWordEnd = cueWord.getEnd().getTokenOffset();
              if (cueWordBegin < conceptBeginTokenOffset) {
                  trainingInstance.addFeature("cueWord_" + cueWordType + "_left");
                  if ((conceptBeginTokenOffset - cueWordBegin) < 4) {
                      trainingInstance.addFeature("cueWord_" + cueWordType + "_left_3");
                  }
              } else if (cueWordBegin > conceptEndTokenOffset) {
                    trainingInstance.addFeature("cueWord_" + cueWordType + "_right");
                    if ((cueWordEnd - conceptEndTokenOffset) < 4) {
                        trainingInstance.addFeature("cueWord_" + cueWordType + "_right_3");
                    }
              } else {
                  trainingInstance.addFeature("cueWord_" + cueWordType + "_within");
              }
            }
            if (checkForEnabledFeature("cueWordValue"))
            {
              trainingInstance.addFeature("cueword_" + cueWord.getCueWordText());
            }
          }
          if (checkForEnabledFeature("concepts")) {
            if (a instanceof ConceptAnnotation) {
                ConceptAnnotation concept = (ConceptAnnotation) a;

                String conceptType = concept.getConceptType().toString();
                int thisConceptBegin = concept.getBegin().getTokenOffset();
                int thisConceptEnd = concept.getEnd().getTokenOffset();
                if (concept.getBegin().getTokenOffset() < conceptBeginTokenOffset) {
                    trainingInstance.addFeature("concept_" + conceptType + "_left");
                    if ((conceptBeginTokenOffset - thisConceptEnd) < 4) {
                        trainingInstance.addFeature("concept_" + conceptType + "_left_3");
                    }
                } else {
                    if ((thisConceptBegin - conceptEndTokenOffset) < 4) {
                        trainingInstance.addFeature("concept_" + conceptType + "_right_3");
                    }
                    trainingInstance.addFeature("concept_" + conceptType + "_right");
                }
            }
            }

          if (a instanceof ScopeAnnotation)
          {
            ScopeAnnotation scope = (ScopeAnnotation)a;
            scopeCount++;
            if (checkForEnabledFeature("scope"))
            {
              trainingInstance.addFeature("scope");
            }
            if (checkForEnabledFeature("inScope"))
            {
              trainingInstance.addFeature("in_scope_" + currentToken);
            }
            if (checkForEnabledFeature("inScopeId"))
            {
              trainingInstance.addFeature("in_scope_id_" + scope.getScopeId() + "_" + currentToken);
            }
          }

          if (a instanceof CueAnnotation)
          {
            CueAnnotation cue = (CueAnnotation)a;
            if (checkForEnabledFeature("cue"))
            {
              String cueType = cue.getCueSubType().toString();
              int cueBegin = cue.getBegin().getTokenOffset();
              if (cueBegin < conceptBeginTokenOffset) {
                  trainingInstance.addFeature("cue_" + cueType + "_left");
                  if ((conceptBeginTokenOffset - cueBegin) < 4) {
                      trainingInstance.addFeature("cue_" + cueType + "_left_3");
                  }
              } else {
                  int cueEnd = cue.getEnd().getTokenOffset();
                  trainingInstance.addFeature("cue_" + cueType + "_right");
                  if ((cueEnd - conceptEndTokenOffset) < 4) {
                      trainingInstance.addFeature("cue_" + cueType + "_right_3");
                  }
              }
            }
            if (checkForEnabledFeature("inCue"))
            {
              trainingInstance.addFeature("in_cue_" + currentToken);
            }
            if (checkForEnabledFeature("inCueForScopeId"))
            {
              trainingInstance.addFeature("in_cue_for_scope_id_" + cue.getScopeIdReference() + "_" + currentToken);
            }
          }

          if (checkForEnabledFeature("zone"))
          {
            if (a instanceof ZoneAnnotation)
            {
              ZoneAnnotation zone = (ZoneAnnotation)a;
              trainingInstance.addFeature("zone_" + MedFactsRunner.escapeFeatureName(zone.getZoneName()));
            }
          }

        } // end of for loop over a : annotationsAtCurrentPosition
      }
      /////

      logger.info(String.format("TRAINING INSTANCE (%d): %s", index, trainingInstance.toString()));
    }

    return trainingInstanceMap;
  }

  public List<LineAndTokenPosition> calculateBeginAndEndOfConcept(
      ApiConcept problem)
  {
    LineAndTokenPosition problemBegin = converter.convertReverse(problem.getBegin());
    LineAndTokenPosition problemEnd = converter.convertReverse(problem.getEnd());
    
    ArrayList<LineAndTokenPosition> list = new ArrayList<LineAndTokenPosition>();
    list.add(problemBegin);
    list.add(problemEnd);
    
    return list;
  }

  public Map<Integer, String> decode(Map<Integer, TrainingInstance> trainingInstanceMap)
  {
    Map<Integer, String> assertionMap = new TreeMap<Integer, String>();

    for (Entry<Integer, TrainingInstance> currentEntrySet : trainingInstanceMap.entrySet())
    {
      Integer index = currentEntrySet.getKey();
      TrainingInstance trainingInstance = currentEntrySet.getValue();

      Set<String> featureSet = trainingInstance.getFeatureSet();
      List<String> featureList = new ArrayList<String>(featureSet);

      JarafeMEDecoder assertionDecoder = assertionDecoderConfiguration.getAssertionDecoder();
      String assertionType = assertionDecoder.classifyInstance(featureList);
      logger.info(String.format("ASSERTION OUTPUT: %d/%s [%s]", index, assertionType, apiConceptList.get(index)));

      assertionMap.put(index, assertionType);
    }

    return assertionMap;
  }

  public void processZones()
  {
    List<ZoneAnnotation> zoneList = MedFactsRunner.findZones(contents, arrayOfArrayOfTokens);

    allAnnotationList.addAll(zoneList);

    List<Annotation> z = annotationsByType.get(AnnotationType.ZONE);
    if (z == null)
    {
      z = new ArrayList<Annotation>();
      annotationsByType.put(AnnotationType.ZONE, z);
    } else
    {
      z.addAll(zoneList);
    }

  }


  public boolean checkForEnabledFeature(String featureId)
  {
    Set<String> enabledFeatureIdSet = assertionDecoderConfiguration.getEnabledFeatureIdSet();
    return (enabledFeatureIdSet == null) || enabledFeatureIdSet.contains(featureId);
  }

  public LineAndTokenPosition convertCharacterOffsetToLineToken(int characterOffset)
  {
    return converter.convertReverse(characterOffset);
  }
  
  public void generateConceptAnnotations(List<ApiConcept> apiConceptList, Map<AnnotationType, List<Annotation>> annotationsByType, List<Annotation> allAnnotationList)
  {
    List<Annotation> i2b2ConceptList = annotationsByType.get(AnnotationType.CONCEPT);
    if (i2b2ConceptList == null)
    {
      i2b2ConceptList = new ArrayList<Annotation>();
      annotationsByType.put(AnnotationType.CONCEPT, i2b2ConceptList);
    }

    for (ApiConcept currentApiConcept : apiConceptList)
    {
      ConceptAnnotation c = new ConceptAnnotation();

      int beginCharacter = currentApiConcept.getBegin();
      int endCharacter = currentApiConcept.getEnd();

      // TODO FIX THIS!! Converter won't work with uima code!!!
      LineAndTokenPosition beginPosition = convertCharacterOffsetToLineToken(beginCharacter);
      LineAndTokenPosition endPosition = convertCharacterOffsetToLineToken(endCharacter);

      Location beginLocation = new Location();
      beginLocation.setLine(beginPosition.getLine());
      beginLocation.setTokenOffset(beginPosition.getTokenOffset());

      Location endLocation = new Location();
      endLocation.setLine(endPosition.getLine());
      endLocation.setTokenOffset(endPosition.getTokenOffset());

      c.setAnnotationFileLineNumber(beginPosition.getLine());
      c.setBegin(beginLocation);
      c.setEnd(endLocation);
      c.setConceptText(currentApiConcept.getText());
      String conceptType = currentApiConcept.getType();
      if (conceptType != null && !conceptType.isEmpty())
      {
        c.setConceptType(ConceptType.valueOf(conceptType));
      } else
      {
        c.setConceptType(null);
      }
      
      c.setEnclosingScopes(null);

      allAnnotationList.add(c);
      annotationsByType.get(AnnotationType.CONCEPT).add(c);
    }
  }

  public AssertionDecoderConfiguration getAssertionDecoderConfiguration()
  {
    return assertionDecoderConfiguration;
  }

  public void setAssertionDecoderConfiguration(AssertionDecoderConfiguration assertionDecoderConfiguration)
  {
    this.assertionDecoderConfiguration = assertionDecoderConfiguration;
  }

  public Map<Integer, String> getAssertionTypeMap()
  {
    return assertionTypeMap;
  }

  public void setAssertionTypeMap(Map<Integer, String> assertionTypeMap)
  {
    this.assertionTypeMap = assertionTypeMap;
  }

}
