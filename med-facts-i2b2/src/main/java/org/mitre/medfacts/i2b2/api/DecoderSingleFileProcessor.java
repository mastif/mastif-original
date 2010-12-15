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
import org.mitre.itc.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.cli.FeatureUtility;
import org.mitre.medfacts.i2b2.cli.MedFactsRunner;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

/**
 *
 * @author MCOARR
 */
public class DecoderSingleFileProcessor
{
  private static final Logger logger = Logger.getLogger(DecoderSingleFileProcessor.class.getName());
  
  protected String contents;
  protected List<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
  protected LineTokenToCharacterOffsetConverter converter;
  protected Set<String> enabledFeatureIdSet;
  protected JarafeMEDecoder namedEntityDecoder;

  protected String arrayOfArrayOfTokens[][] = null;
  protected List<Annotation> allAnnotationList = new ArrayList<Annotation>();
  protected Map<AnnotationType,List<Annotation>> annotationsByType =
      new EnumMap<AnnotationType, List<Annotation>>(AnnotationType.class);

  public DecoderSingleFileProcessor(LineTokenToCharacterOffsetConverter converter)
  {
    this.converter = converter;
  }
  
  public void processSingleFile()
  {
    preprocess();
    generateAnnotations();
    Map<Integer, TrainingInstance> trainingInstanceMap = generateFeatures();
    Map<Integer, String> assertionTypeMap = decode(trainingInstanceMap);
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
    ApiConcept apiConcept = new ApiConcept(begin, end, conceptType, text);
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

  private void preprocess()
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

  private void generateAnnotations()
  {
    MedFactsRunner.postProcessForCueWords(arrayOfArrayOfTokens, allAnnotationList, annotationsByType);
    //throw new UnsupportedOperationException("Not yet implemented");
  }

  private Map<Integer, TrainingInstance> generateFeatures()
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

      logger.info(String.format("<<PROB>> %s", problem));
      LineAndTokenPosition problemBegin = converter.convertReverse(problem.getBegin());
      LineAndTokenPosition problemEnd = converter.convertReverse(problem.getEnd());
      int lineNumber = problemBegin.getLine();
      String currentLine[] = arrayOfArrayOfTokens[lineNumber - 1];

      TrainingInstance trainingInstance = new TrainingInstance();

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



      logger.info(String.format("TRAINING INSTANCE: %s", trainingInstance.toString()));
    }

    return trainingInstanceMap;
  }

  private Map<Integer, String> decode(Map<Integer, TrainingInstance> trainingInstanceMap)
  {
    Map<Integer, String> assertionMap = new TreeMap<Integer, String>();

    for (Entry<Integer, TrainingInstance> currentEntrySet : trainingInstanceMap.entrySet())
    {
      Integer index = currentEntrySet.getKey();
      TrainingInstance trainingInstance = currentEntrySet.getValue();

      Set<String> featureSet = trainingInstance.getFeatureSet();
      List<String> featureList = new ArrayList<String>(featureSet);

      String assertionType = namedEntityDecoder.classifyInstance(featureList);
      logger.info(String.format("ASSERTION OUTPUT: %d/%s", index, assertionType));

      assertionMap.put(index, assertionType);
    }

    return assertionMap;
  }

  private boolean checkForEnabledFeature(String featureId)
  {
    return (enabledFeatureIdSet == null) || enabledFeatureIdSet.contains(featureId);
  }

  /**
   * @return the namedEntityDecoder
   */
  public JarafeMEDecoder getNamedEntityDecoder()
  {
    return namedEntityDecoder;
  }

  /**
   * @param namedEntityDecoder the namedEntityDecoder to set
   */
  public void setNamedEntityDecoder(JarafeMEDecoder namedEntityDecoder)
  {
    this.namedEntityDecoder = namedEntityDecoder;
  }

  public Set<String> getEnabledFeatureIdSet()
  {
    return enabledFeatureIdSet;
  }

  public void setEnabledFeatureIdSet(Set<String> enabledFeatureIdSet)
  {
    this.enabledFeatureIdSet = enabledFeatureIdSet;
  }

}
