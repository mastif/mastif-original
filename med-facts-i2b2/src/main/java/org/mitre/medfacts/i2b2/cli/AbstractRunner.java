/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.annotation.CueAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueSubType;
import org.mitre.medfacts.i2b2.annotation.CueWordAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueWordType;
import org.mitre.medfacts.i2b2.annotation.ScopeAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeOrCueAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.annotation.ZoneAnnotation;
import org.mitre.medfacts.i2b2.scanners.CueListScanner;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.AnnotationIndexer;
import org.mitre.medfacts.i2b2.util.ArrayPrinter;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.Location;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.ZonerCli;
import org.mitre.medfacts.zoner.ZonerCli.Range;

/**
 *
 * @author MCOARR
 */
public abstract class AbstractRunner {

  public AbstractRunner()
  {
  }

  protected String textLookup[][];


  public static final int MAX_WINDOW_LEFT = 12;
  public static final int MAX_WINDOW_RIGHT = 12;

  protected static final Pattern SPACE_PATTERN = Pattern.compile(" ");
  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

  protected List<Annotation> allAnnotationList;
  protected Set<String> enabledFeatureIdSet = null;
  protected String entireContents;
  AnnotationIndexer indexer = new AnnotationIndexer();
  protected Map<AnnotationType, List<TrainingInstance>> mapOfTrainingInstanceLists;
  protected Mode mode;
  protected ScopeParser scopeParser;
  protected Map<AnnotationType,List<Annotation>> annotationsByType;


  public List<Annotation> buildAssertionsFromConcepts(List<Annotation> conceptList)
  {
    List<Annotation> assertionList = new ArrayList<Annotation>();
    for (Annotation a : conceptList)
    {
      ConceptAnnotation concept = (ConceptAnnotation) a;
      if (concept.getConceptType() == ConceptType.PROBLEM)
      {
        AssertionAnnotation assertion = new AssertionAnnotation();
        assertion.setAssertionValue(null);
        assertion.setBegin(concept.getBegin());
        assertion.setEnd(concept.getEnd());
        assertion.setConceptText(concept.getConceptText());
        assertion.setConceptType(concept.getConceptType());
        assertionList.add(assertion);
      }
    }
    return assertionList;
  }

  public boolean checkForEnabledFeature(String featureId)
  {
    return (enabledFeatureIdSet == null) || enabledFeatureIdSet.contains(featureId);
  }

  public String constructConceptHeadFeature(String input)
  {
    return "concept_head_is_" + StringHandling.escapeStringForFeatureName(input);
  }

  public String constructConceptPhraseFeature(String input)
  {
    return "concept_phrase_is_" + StringHandling.escapeStringForFeatureName(input);
  }

  public String escapeFeatureName(String originalFeatureName)
  {
    Matcher m = FileRunner.SPACE_PATTERN.matcher(originalFeatureName);
    String cleanFeatureName = m.replaceAll("_");
    return cleanFeatureName;
  }

  public void execute()
  {
    loadTextAndAnnotation();
    postProcess();
  }

  public List<Annotation> getAllAnnotationList()
  {
    return allAnnotationList;
  }

  public Map<AnnotationType, List<Annotation>> getAnnotationsByType()
  {
    return annotationsByType;
  }

  public Set<String> getEnabledFeatureIdSet()
  {
    return enabledFeatureIdSet;
  }

  public String getEntireContents()
  {
    return entireContents;
  }

  public Map<AnnotationType, List<TrainingInstance>> getMapOfTrainingInstanceLists()
  {
    return mapOfTrainingInstanceLists;
  }

  public Mode getMode()
  {
    return mode;
  }

  public String[][] getTextLookup()
  {
    return textLookup;
  }

  public void loadTextAndAnnotation()
  {
  }

  public void postProcess()
  {
    try
    {
      processCueWordLists();
      processZones();
      //validateAnnotations();
      indexAnnotations();
      linkAnnotations();
      printOutFeatures();
    } catch (FileNotFoundException ex)
    {
      Logger.getLogger(FileRunner.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException("problem processing files; FileNotFoundException", ex);
    } catch (IOException ex)
    {
      Logger.getLogger(FileRunner.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException("problem processing files; IOException", ex);
    }
  }

  public void printOutFeatures() throws IOException
  {
    String featuresFilename = Constants.TEXT_FILE_EXTENSTION_PATTERN.matcher(getTextFilename()).replaceFirst(".features");
    File featuresFile = new File(featuresFilename);
    System.out.format("output filename: %s%n", featuresFilename);
    Writer featuresFileWriter = new FileWriter(featuresFile);
    BufferedWriter featuresBufferedWriter = new BufferedWriter(featuresFileWriter);
    PrintWriter featuresPrinter = new PrintWriter(featuresBufferedWriter);
    Pattern conceptHeadPattern = Pattern.compile(" ([^ ]+)$");
    //System.out.println("$$$$$");
    //System.out.println("$$$$$");
    //int lineNumber = 1;
    for (Annotation currentAnnotation : getAnnotationsByType().get(AnnotationType.ASSERTION))
    {
      final int lineNumber = currentAnnotation.getBegin().getLine();
      AssertionAnnotation currentAssertionAnnotation = (AssertionAnnotation) currentAnnotation;
      if (!currentAssertionAnnotation.getConceptType().equals(ConceptType.PROBLEM))
      {
        // skip this one
        continue;
      }
      TrainingInstance trainingInstance = new TrainingInstance();
      List<Annotation> allLineAnnotations = indexer.getAnnotationByLine().get((long) lineNumber);
      trainingInstance.setFilename(getTextFilename());
      trainingInstance.setLineNumber(lineNumber);
      trainingInstance.setAssertAnnotateForTI(currentAssertionAnnotation); //link training instance to corresponding assertion
      trainingInstance.setAnnotationsForLine(allLineAnnotations); //list of annotations for the line this training instance is on
      trainingInstance.setTokensForLine(textLookup[lineNumber - 1]); //token string for the line this training instance is on
      AssertionValue assertionValue = currentAssertionAnnotation.getAssertionValue();
      String assertionValueString = (assertionValue == null) ? "" : assertionValue.toString().toLowerCase();
      trainingInstance.setExpectedValue(assertionValueString);
      String conceptText = currentAssertionAnnotation.getConceptText();
      if (checkForEnabledFeature("conceptTextFeature"))
      {
        String conceptTextFeature = constructConceptPhraseFeature(conceptText);
        trainingInstance.addFeature(conceptTextFeature);
      }
      if (checkForEnabledFeature("conceptPseudoHeadFeature"))
      {
        int ln = currentAssertionAnnotation.getEnd().getLine();
        int pos = currentAssertionAnnotation.getEnd().getTokenOffset();
        String conceptHead = textLookup[ln - 1][pos];
        trainingInstance.addFeature(constructConceptHeadFeature(conceptHead));
        //Matcher conceptHeadMatcher = conceptHeadPattern.matcher(conceptText);
        //if (conceptHeadMatcher.find())
        //{
        //  String conceptHeadText = conceptHeadMatcher.group(1);
        //  String conceptHeadFeature = constructConceptHeadFeature(conceptHeadText);
        //  trainingInstance.addFeature(conceptHeadFeature);
        //}
      }
      Location conceptBeginLocation = currentAssertionAnnotation.getBegin();
      int conceptBeginLine = conceptBeginLocation.getLine();
      int conceptBeginTokenOffset = conceptBeginLocation.getTokenOffset();
      Location conceptEndLocation = currentAssertionAnnotation.getEnd();
      int conceptEndTokenOffset = conceptEndLocation.getTokenOffset();
      String[] currentLine = textLookup[conceptBeginLine - 1];
      if (checkForEnabledFeature("conceptUnigrams"))
      {
        for (int k = conceptBeginTokenOffset; k <= conceptEndTokenOffset; k++)
        {
          trainingInstance.addFeature("concept_unigram_" + StringHandling.escapeStringForFeatureName(currentLine[k]));
        }
      }
      if (checkForEnabledFeature("wordLeftFeature"))
      {
        List<String> wordLeftFeatureList = constructWordLeftFeatureList(conceptBeginTokenOffset, conceptEndTokenOffset, currentLine);
        for (String currentFeature : wordLeftFeatureList)
        {
          trainingInstance.addFeature(currentFeature);
        }
      }
      if (checkForEnabledFeature("wordRightFeature"))
      {
        List<String> wordRightFeatureList = constructWordRightFeatureList(conceptBeginTokenOffset, conceptEndTokenOffset, currentLine);
        for (String currentFeature : wordRightFeatureList)
        {
          trainingInstance.addFeature(currentFeature);
        }
      }
      if (checkForEnabledFeature("cueWordOrderingsLeft"))
      {
        List<CueWordAnnotation> annots = new ArrayList<CueWordAnnotation>();
        for (Annotation a : allLineAnnotations)
        {
          if ((a instanceof CueWordAnnotation) && (a.getBegin().getTokenOffset() < conceptBeginTokenOffset))
          {
            annots.add((CueWordAnnotation) a);
          }
        }
        Collections.sort(annots);
        StringBuilder str = new StringBuilder("CWS_left");
        for (CueWordAnnotation a : annots)
        {
          str.append("_");
          str.append(a.getCueWordType());
        }
        trainingInstance.addFeature(str.toString());
      }
      String[] tokensOnCurrentLine = textLookup[lineNumber - 1];
      for (int currentTokenOffset = 0; currentTokenOffset < tokensOnCurrentLine.length; currentTokenOffset++)
      {
        String currentToken = tokensOnCurrentLine[currentTokenOffset];
        List<Annotation> annotationsAtCurrentPosition = indexer.findAnnotationsForPosition(lineNumber, currentTokenOffset);
        int scopeCount = 0;
        if (annotationsAtCurrentPosition != null)
        {
          for (Annotation a : annotationsAtCurrentPosition)
          {
            if (checkForEnabledFeature("concepts"))
            {
              if (a instanceof ConceptAnnotation)
              {
                ConceptAnnotation concept = (ConceptAnnotation) a;
                String conceptType = concept.getConceptType().toString();
                int thisConceptBegin = concept.getBegin().getTokenOffset();
                int thisConceptEnd = concept.getEnd().getTokenOffset();
                if (concept.getBegin().getTokenOffset() < conceptBeginTokenOffset)
                {
                  trainingInstance.addFeature("concept_" + conceptType + "_left");
                  if ((conceptBeginTokenOffset - thisConceptEnd) < 4)
                  {
                    trainingInstance.addFeature("concept_" + conceptType + "_left_3");
                  }
                } else
                {
                  if ((thisConceptBegin - conceptEndTokenOffset) < 4)
                  {
                    trainingInstance.addFeature("concept_" + conceptType + "_right_3");
                  }
                  trainingInstance.addFeature("concept_" + conceptType + "_right");
                }
              }
            }
            if (a instanceof ScopeAnnotation)
            {
              ScopeAnnotation scope = (ScopeAnnotation) a;
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
              CueAnnotation cue = (CueAnnotation) a;
              if (checkForEnabledFeature("cue"))
              {
                String cueType = cue.getCueSubType().toString();
                int cueBegin = cue.getBegin().getTokenOffset();
                if (cueBegin < conceptBeginTokenOffset)
                {
                  trainingInstance.addFeature("cue_" + cueType + "_left");
                  if ((conceptBeginTokenOffset - cueBegin) < 4)
                  {
                    trainingInstance.addFeature("cue_" + cueType + "_left_3");
                  }
                } else
                {
                  int cueEnd = cue.getEnd().getTokenOffset();
                  trainingInstance.addFeature("cue_" + cueType + "_right");
                  if ((cueEnd - conceptEndTokenOffset) < 4)
                  {
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
            if (a instanceof CueWordAnnotation)
            {
              CueWordAnnotation cueWord = (CueWordAnnotation) a;
              String cueWordType = cueWord.getCueWordType().toString();
              if (checkForEnabledFeature("cueWord"))
              {
                int cueWordBegin = cueWord.getBegin().getTokenOffset();
                int cueWordEnd = cueWord.getEnd().getTokenOffset();
                if (cueWordBegin < conceptBeginTokenOffset)
                {
                  trainingInstance.addFeature("cueWord_" + cueWordType + "_left");
                  if ((conceptBeginTokenOffset - cueWordBegin) < 4)
                  {
                    trainingInstance.addFeature("cueWord_" + cueWordType + "_left_3");
                  }
                } else if (cueWordBegin > conceptEndTokenOffset)
                {
                  trainingInstance.addFeature("cueWord_" + cueWordType + "_right");
                  if ((cueWordEnd - conceptEndTokenOffset) < 4)
                  {
                    trainingInstance.addFeature("cueWord_" + cueWordType + "_right_3");
                  }
                } else
                {
                  trainingInstance.addFeature("cueWord_" + cueWordType + "_within");
                }
              }
              if (checkForEnabledFeature("cueWordValue"))
              {
                trainingInstance.addFeature("cueword_" + cueWord.getCueWordText());
              }
            }
            if (checkForEnabledFeature("zone"))
            {
              if (a instanceof ZoneAnnotation)
              {
                ZoneAnnotation zone = (ZoneAnnotation) a;
                trainingInstance.addFeature("zone_" + escapeFeatureName(zone.getZoneName()));
              }
            }
          }
        }
        if (scopeCount > 0)
        {
          if (checkForEnabledFeature("scopeCountNumber"))
          {
            trainingInstance.addFeature("scope_count_" + scopeCount);
          }
          if (checkForEnabledFeature("scopeCountEvenOrOdd"))
          {
            boolean scopeCountIsEven = (scopeCount % 2) == 0;
            trainingInstance.addFeature("scope_count_" + (scopeCountIsEven ? "even" : "odd"));
          }
        }
      }
      //Features based on negation and speculation scopes enclosing the text of the entire training instance -Alex Yeh
      int enclosingNegationScopeCnt = 0;
      int enclosingSpeculationScopeCnt = 0;
      AssertionAnnotation assertForTI = trainingInstance.getAssertAnnotateForTI();
      //Count number of enclosing negation and speculation scopes
      for (ScopeAnnotation enclosingScope : assertForTI.getEnclosingScopes())
      {
        CueAnnotation cueForScope = enclosingScope.getCueForScope();
        CueSubType scopeType = cueForScope.getCueSubType();
        if (scopeType == CueSubType.NEGATION)
        {
          enclosingNegationScopeCnt++;
        } else if (scopeType == CueSubType.SPECULATION)
        {
          enclosingSpeculationScopeCnt++;
        } else
        {
          System.out.format("WARNING: CUE %s%n  FOR SCOPE %s%n ENCLOSING %s%n is neither a negation nor speculation cue%n", cueForScope, enclosingScope, assertForTI);
        }
      }
      if (checkForEnabledFeature("statusRuleMixNMatchFeature"))
      {
        //Write out status rule features for this instance that are meant to be mixed with non status rule features
        trainingInstance.addFeature("status_rule_mix_n_match_" + enclosingNegationScopeCnt + "negation_" + enclosingSpeculationScopeCnt + "spec_enclosing_scopes");
      }
      if (checkForEnabledFeature("statusRuleStandAloneFeature"))
      {
        //Write out status rule features for this instance that are meant to stand by themselves
        switch (enclosingNegationScopeCnt)
        {
          case 0:
            {
              if (enclosingSpeculationScopeCnt == 0)
              {
                trainingInstance.addFeature("status_rule_standAlone_present");
              } else if (enclosingSpeculationScopeCnt == 1)
              {
                trainingInstance.addFeature("status_rule_standAlone_possible");
              } else
              {
                trainingInstance.addFeature("status_rule_standAlone_unhandled_case");
              }
              break;
            }
          case 1:
            {
              if (enclosingSpeculationScopeCnt == 0)
              {
                trainingInstance.addFeature("status_rule_standAlone_absent");
              } else
              {
                trainingInstance.addFeature("status_rule_standAlone_unhandled_case");
              }
              break;
            }
          case 2:
            {
              if (enclosingSpeculationScopeCnt == 0)
              {
                trainingInstance.addFeature("status_rule_standAlone_present");
              } else
              {
                trainingInstance.addFeature("status_rule_standAlone_unhandled_case");
              }
              break;
            }
          default:
            trainingInstance.addFeature("status_rule_standAlone_unhandled_case");
        }
      }
      //      System.out.format("TI on line %s with value %s%n  => %s%n     has %s neg and %s spec enclosing scopes%n", trainingInstance.getLineNumber(), trainingInstance.toStringWithExpectedValue(), assertForTI.toString(), enclosingNegationScopeCnt, enclosingSpeculationScopeCnt); //For testing
      String featureLine = trainingInstance.toStringWithExpectedValue();
      featuresPrinter.println(featureLine);
      getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION).add(trainingInstance);
      //lineNumber++;
    }
    //System.out.println("$$$$$");
    //System.out.println("$$$$$");
    //System.out.println("$$$$$");
    //System.out.println("$$$$$");
    featuresPrinter.close();
    featuresBufferedWriter.close();
    featuresFileWriter.close();
  }

  public void processCueList(String cueListFilename, CueWordType cueWordType) throws URISyntaxException
  {
    ClassLoader classLoader = getClass().getClassLoader();
    //    URL cueFileUrl = classLoader.getResource(cueListFilename);
    //    System.out.format("cue list url: %s%n", cueFileUrl);
    //    URI cueFileUri = cueFileUrl.toURI();
    //    System.out.format("cue list uri: %s%n", cueFileUri);
    //    File cueFile = new File(cueFileUri);
    //    System.out.format("cue file: %s%n", cueFile);
    List<Annotation> annotationList = null;
    try
    {
      InputStream cueFileInputStream = classLoader.getResourceAsStream(cueListFilename);
      InputStreamReader inputStreamReader = new InputStreamReader(cueFileInputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      CueListScanner scanner = new CueListScanner(bufferedReader, cueWordType);
      scanner.setTextLookup(textLookup);
      scanner.execute();
      annotationList = scanner.getAnnotationList();
      bufferedReader.close();
      inputStreamReader.close();
      cueFileInputStream.close();
    } catch (IOException ex)
    {
      Logger.getLogger(CueListScanner.class.getName()).log(Level.SEVERE, String.format("problem reading scanner terms file "), ex);
      throw new RuntimeException(String.format("problem reading scanner terms file \"%s\""));
    }
    allAnnotationList.addAll(annotationList);
    List<Annotation> cueWordAnnotationList = annotationsByType.get(AnnotationType.CUEWORD);
    if (cueWordAnnotationList == null)
    {
      cueWordAnnotationList = new ArrayList<Annotation>();
      annotationsByType.put(AnnotationType.CUEWORD, cueWordAnnotationList);
    }
    cueWordAnnotationList.addAll(annotationList);
  }

  public void processCueWordLists()
  {
    try
    {
      processCueList("org/mitre/medfacts/i2b2/cuefiles/updated_negation_cue_list.txt", CueWordType.NEGATION);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/updated_speculation_cue_list.txt", CueWordType.SPECULATION);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/conditional_cue_list.txt", CueWordType.CONDITIONAL);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/hypothetical_cue_list.txt", CueWordType.HYPOTHETICAL);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/not_patient_cue_list.txt", CueWordType.NOT_PATIENT);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/NegEx_pseudoneg.txt", CueWordType.NEGEX_PSEUDONEG);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/NegEx_scope_terminators.txt", CueWordType.NEGEX_SCOPEEND);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/activity_cue.txt", CueWordType.ACTIVITY);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/cause.txt", CueWordType.CAUSE);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/clause_boundary_cue_list.txt", CueWordType.CLAUSE_BOUNDARY);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/conditional_concept.txt", CueWordType.COND_CONCEPT);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/negated_concept.txt", CueWordType.NEG_CONCEPT);
    } catch (URISyntaxException e)
    {
      Logger.getLogger(FileRunner.class.getName()).log(Level.SEVERE, "URISyntaxException when trying to load cue word", e);
      throw new RuntimeException("URISyntaxException when trying to load cue word", e);
    }
    //  private List<Annotation> processConceptAnnotationFile(String currentFilename)
    //          throws FileNotFoundException, IOException
    //  {
    //    FileReader fr = new FileReader(currentFilename);
    //    BufferedReader br = new BufferedReader(fr);
    //
    //    List<Annotation> annotationList = new ArrayList<Annotation>();
    //
    //    Pattern conceptPattern = Pattern.compile("^c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|t=\"(.*)\"$");
    //
    //    String currentLine = null;
    //    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    //    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    //    int lineNumber = 0;
    //    while ((currentLine = br.readLine()) != null)
    //    {
    //      ConceptAnnotation c = processConceptAnnotationLine(currentLine, conceptPattern);
    //      annotationList.add(c);
    //    }
    //
    //    br.close();
    //    fr.close();
    //
    //    return annotationList;
    //
    //  }
    //
    //  public ConceptAnnotation processConceptAnnotationLine(String currentLine, Pattern conceptPattern)
    //  {
    //    System.out.format("CONCEPT PROCESSING: %s%n", currentLine);
    //    Matcher matcher = conceptPattern.matcher(currentLine);
    //    System.out.format("    matches? %b%n", matcher.matches());
    //    String conceptText = matcher.group(1);
    //    String beginLine = matcher.group(2);
    //    String beginCharacter = matcher.group(3);
    //    String endLine = matcher.group(4);
    //    String endCharacter = matcher.group(5);
    //    String conceptTypeText = matcher.group(6);
    //    System.out.format("    concept text: %s%n", conceptText);
    //    System.out.format("    concept type text: %s%n", conceptTypeText);
    //    ConceptAnnotation c = new ConceptAnnotation();
    //    c.setConceptText(conceptText);
    //    c.setBegin(new Location(beginLine, beginCharacter));
    //    c.setEnd(new Location(endLine, endCharacter));
    //    c.setConceptType(ConceptType.valueOf(conceptTypeText.toUpperCase()));
    //    System.out.format("    CONCEPT ANNOTATION OBJECT: %s%n", c);
    //    System.out.format("    CONCEPT ANNOTATION OBJECT i2b2: %s%n", c.toI2B2String());
    //    return c;
    //  }
    //  private List<Annotation> processConceptAnnotationFile(String currentFilename)
    //          throws FileNotFoundException, IOException
    //  {
    //    FileReader fr = new FileReader(currentFilename);
    //    BufferedReader br = new BufferedReader(fr);
    //
    //    List<Annotation> annotationList = new ArrayList<Annotation>();
    //
    //    Pattern conceptPattern = Pattern.compile("^c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|t=\"(.*)\"$");
    //
    //    String currentLine = null;
    //    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    //    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    //    int lineNumber = 0;
    //    while ((currentLine = br.readLine()) != null)
    //    {
    //      ConceptAnnotation c = processConceptAnnotationLine(currentLine, conceptPattern);
    //      annotationList.add(c);
    //    }
    //
    //    br.close();
    //    fr.close();
    //
    //    return annotationList;
    //
    //  }
    //
    //  public ConceptAnnotation processConceptAnnotationLine(String currentLine, Pattern conceptPattern)
    //  {
    //    System.out.format("CONCEPT PROCESSING: %s%n", currentLine);
    //    Matcher matcher = conceptPattern.matcher(currentLine);
    //    System.out.format("    matches? %b%n", matcher.matches());
    //    String conceptText = matcher.group(1);
    //    String beginLine = matcher.group(2);
    //    String beginCharacter = matcher.group(3);
    //    String endLine = matcher.group(4);
    //    String endCharacter = matcher.group(5);
    //    String conceptTypeText = matcher.group(6);
    //    System.out.format("    concept text: %s%n", conceptText);
    //    System.out.format("    concept type text: %s%n", conceptTypeText);
    //    ConceptAnnotation c = new ConceptAnnotation();
    //    c.setConceptText(conceptText);
    //    c.setBegin(new Location(beginLine, beginCharacter));
    //    c.setEnd(new Location(endLine, endCharacter));
    //    c.setConceptType(ConceptType.valueOf(conceptTypeText.toUpperCase()));
    //    System.out.format("    CONCEPT ANNOTATION OBJECT: %s%n", c);
    //    System.out.format("    CONCEPT ANNOTATION OBJECT i2b2: %s%n", c.toI2B2String());
    //    return c;
    //  }
  }

  public void processScopeInProcess(Map<AnnotationType, List<Annotation>> annotationsByType, List<Annotation> allAnnotationList) throws RuntimeException
  {
    List<ScopeOrCueAnnotation> scopeOrCueAnnotationList = scopeParser.decodeDocument(textLookup);
    //    Map<AnnotationType,List<Annotation>> map2 = new TreeMap<AnnotationType,List<Annotation>>();
    //    map2.put(AnnotationType.SCOPE, allAnnotationList);
    List<Annotation> newList = new ArrayList<Annotation>();
    newList.addAll(scopeOrCueAnnotationList);
    annotationsByType.put(AnnotationType.SCOPE, newList);
    allAnnotationList.addAll(scopeOrCueAnnotationList);
  }

  public void processZones()
  {
    ZonerCli zoner = new ZonerCli();
    zoner.setEntireContents(getEntireContents());
    zoner.findHeadings();
    List<Range> zonerRangeList = zoner.getRangeList();
    for (Range currentRange : zonerRangeList)
    {
      LineAndTokenPosition rangeBegin = currentRange.getBeginLineAndToken();
      LineAndTokenPosition rangeEnd = currentRange.getEndLineAndToken();
      int firstLine = rangeBegin.getLine();
      int lastLine = rangeEnd.getLine();
      for (int i = firstLine; i <= lastLine; i++)
      {
        boolean isFirstLine = i == firstLine;
        boolean isLastLine = i == lastLine;
        int beginToken;
        if (isFirstLine)
        {
          beginToken = rangeBegin.getTokenOffset();
        } else
        {
          beginToken = 0;
        }
        int endToken;
        if (isLastLine)
        {
          endToken = rangeEnd.getTokenOffset();
        } else
        {
          // todo fix logic here
          if (i >= textLookup.length)
          {
            System.out.println("This should not be happening, fix me... (can be ignored for now)");
            continue;
          }
          endToken = textLookup[i].length - 1;
        }
        ZoneAnnotation zone = new ZoneAnnotation();
        zone.setZoneName(currentRange.getLabel());
        Location begin = new Location();
        begin.setLine(i);
        begin.setTokenOffset(beginToken);
        zone.setBegin(begin);
        Location end = new Location();
        end.setLine(i);
        end.setTokenOffset(endToken);
        zone.setEnd(end);
        allAnnotationList.add(zone);
        List<Annotation> zoneAnnotationList = annotationsByType.get(AnnotationType.ZONE);
        if (zoneAnnotationList == null)
        {
          zoneAnnotationList = new ArrayList<Annotation>();
          annotationsByType.put(AnnotationType.ZONE, zoneAnnotationList);
        }
        zoneAnnotationList.add(zone);
      }
    }
  }

  public void setAllAnnotationList(List<Annotation> allAnnotationList)
  {
    this.allAnnotationList = allAnnotationList;
  }

  public void setAnnotationsByType(Map<AnnotationType, List<Annotation>> annotationsByType)
  {
    this.annotationsByType = annotationsByType;
  }

  public void setEnabledFeatureIdSet(Set<String> enabledFeatureIdSet)
  {
    this.enabledFeatureIdSet = enabledFeatureIdSet;
  }

  public void setEntireContents(String entireContents)
  {
    this.entireContents = entireContents;
  }

  public void setMapOfTrainingInstanceLists(Map<AnnotationType, List<TrainingInstance>> mapOfTrainingInstanceLists)
  {
    this.mapOfTrainingInstanceLists = mapOfTrainingInstanceLists;
  }

  public void setMode(Mode mode)
  {
    this.mode = mode;
  }

  public void setScopeParser(ScopeParser parser)
  {
    scopeParser = parser;
  }

  public void setTextLookup(String[][] textLookup)
  {
    this.textLookup = textLookup;
  }

  public void validateAnnotations()
  {
    System.out.println("#####");
    System.out.println("#####");
    for (Annotation currentAnnotation : getAllAnnotationList())
    {
      System.out.format(" - %s%n", currentAnnotation);
      Location begin = currentAnnotation.getBegin();
      Location end = currentAnnotation.getEnd();
      System.out.format("==%n  begin: %s%n  end: %s%n==%n", begin, end);
      String targetText = extractTargetText(getTextLookup(), begin, end);
      System.out.format("    ANNOT: %s%n", currentAnnotation);
      System.out.format("      TARGET: %s%n", targetText);
      System.out.println("---");
    }
    System.out.println("#####");
    System.out.println("#####");
  }

  protected void indexAnnotations()
  {
    indexer.indexAnnotations(getAllAnnotationList());
  }

  //Create links between annotations needed by some status rules
  protected void linkAnnotations()
  {
    //Linking scopes to their cues
    List<Annotation> scopeFileAnnotationList = annotationsByType.get(AnnotationType.SCOPE);
    Map<Integer, ScopeAnnotation> scopeIdMap = new TreeMap<Integer, ScopeAnnotation>();
    Map<Integer, CueAnnotation> cueForScopeIdMap = new TreeMap<Integer, CueAnnotation>();

    //For testing -Alex Yeh
//    System.out.format("SCOPE ANNOTATIONS for FILE %s%n", textFilename);
//    System.out.format("  LIST SIZE: %s%n", scopeFileAnnotationList.size());

    for (Annotation current : scopeFileAnnotationList)
    {
        if (current instanceof ScopeAnnotation)
        {
            ScopeAnnotation scope = (ScopeAnnotation)current;
            int scopeId = scope.getScopeId();
            scopeIdMap.put(scopeId, scope);
        } else if (current instanceof CueAnnotation)
        {
            CueAnnotation cue = (CueAnnotation)current;
            int scopeIdForThisCue = cue.getScopeIdReference();
            cueForScopeIdMap.put(scopeIdForThisCue, cue);
        }
    }

    for (Entry<Integer, ScopeAnnotation> current : scopeIdMap.entrySet())
    {
        int currentId = current.getKey();
        ScopeAnnotation scope = current.getValue();

        CueAnnotation cue = cueForScopeIdMap.get(currentId);
        scope.setCueForScope(cue);
    }
    //For testing -Alex Yeh
//    System.out.format("CUES for SCOPES in FILE %s%n", textFilename);
//    System.out.format("  MAP size: %s.%n", scopeIdMap.entrySet().size());
//    for (Entry<Integer, ScopeAnnotation> current : scopeIdMap.entrySet())
//    {
//        ScopeAnnotation scope = current.getValue();
//        System.out.format("  Scope: %s%n    => Cue: %s%n", scope.toString(), scope.getCueForScope().toString());
//    }

    //Find enclosing scopes for each assertion annotation
    List<Annotation> assertionFileAnnotationList = annotationsByType.get(AnnotationType.ASSERTION);

//    System.out.format("ASSERTIONS for FILE %s%n", textFilename); //For testing
    for (Annotation current : assertionFileAnnotationList)
    {
      Location annotationBegin = current.getBegin();
      int beginLine = annotationBegin.getLine();
      int beginTokenOffset = annotationBegin.getTokenOffset();
      Location annotationEnd = current.getEnd();
      // Assume that 'annotationEnd.getLine()' will return the same line number as 'beginLine'
      int endTokenOffset = annotationEnd.getTokenOffset();
      List<Annotation> annotationsForFirstToken = indexer.findAnnotationsForPosition(beginLine, beginTokenOffset);
      List<ScopeAnnotation> enclosingScopesFound = new ArrayList<ScopeAnnotation>();
      AssertionAnnotation assertion = (AssertionAnnotation)current;

//      System.out.format("   ASRT: %s => %s annotations for 1st token%n", assertion.toString(), annotationsForFirstToken.size()); //for testing
      for (Annotation annotationForFirstToken : annotationsForFirstToken)
      {
        if ((annotationForFirstToken instanceof ScopeAnnotation) &&
                (endTokenOffset <= annotationForFirstToken.getEnd().getTokenOffset()))
        {
          //This annotation containing the first token of the current assertion annotation
          // is a ScopeAnnotation that contains all the tokens of the current assertion annotation.
          //Add it to the list of enclosing scopes.
          ScopeAnnotation scope = (ScopeAnnotation)annotationForFirstToken;
          enclosingScopesFound.add(scope);
        }
      }
      assertion.setEnclosingScopes(enclosingScopesFound);
    }
    //For testing -Alex Yeh
//    for (Annotation current : assertionFileAnnotationList)
//    {
//      AssertionAnnotation assertion = (AssertionAnnotation)current;
//      System.out.format("   ASRT: %s => %s COVERING SCOPES%n", assertion.toString(), assertion.getEnclosingScopes().size());
//      for (ScopeAnnotation scope: assertion.getEnclosingScopes())
//      {
//        System.out.format("      CVR SCP: %s%n", scope.toString());
//        System.out.format("          => CUE IS: %s%n", scope.getCueForScope().toString());
//      }
//    }
  }

  protected String extractTargetText(String[][] textLookup, Location begin, Location end)
  {
    if (begin.getLine() != end.getLine())
    {
      throw new RuntimeException("cannot have a range across multiple lines");
    }
    int line = begin.getLine();
    int lineOffset = line - 1;
    StringBuilder b = new StringBuilder();
    int lineLength = textLookup[lineOffset].length;
    boolean isOffsetLegit = begin.getTokenOffset() >= 0 && begin.getTokenOffset() < lineLength && end.getTokenOffset() >= 0 && end.getTokenOffset() < lineLength;
    //System.err.format("STATUS: checking offset(s): %s to %s%n", begin, end);
    if (!isOffsetLegit)
    {
      System.err.format("ERROR!: invalid line offset(s): %s to %s (%s) TEXT: >>%s<< %n", begin, end, getTextFilename(), ArrayPrinter.toString(textLookup[lineOffset]));
      return null;
    }
    for (int i = begin.getTokenOffset(); i <= end.getTokenOffset(); i++)
    {
      String currentToken = textLookup[lineOffset][i];
      b.append(currentToken);
      boolean isLast = i == end.getTokenOffset();
      if (!isLast)
      {
        b.append(" ");
      }
    }
    return b.toString();
  }

  private List<String> constructWordLeftFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i = conceptBeginCharacter - 1; i >= 0 && ++count <= AbstractRunner.MAX_WINDOW_LEFT; i--)
    {
      String currentToken = currentLine[i];
      if (!currentToken.matches("[,.]+"))
      {
        String featureName1 = "";
        featureName1 = "word_left_" + StringHandling.escapeStringForFeatureName(currentToken);
        featureList.add(featureName1);
        if ((conceptBeginCharacter - i) < 4)
        {
          featureList.add("word_left_3_" + StringHandling.escapeStringForFeatureName(currentToken));
        }
      }
    }
    return featureList;
  }

  private List<String> constructWordRightFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i = conceptEndCharacter + 1; i < currentLine.length && ++count <= AbstractRunner.MAX_WINDOW_RIGHT; i++)
    {
      String currentToken = currentLine[i];
      if (!currentToken.matches("[,.]+"))
      {
        String featureName1 = "";
        featureName1 = "word_right_" + StringHandling.escapeStringForFeatureName(currentToken);
        featureList.add(featureName1);
        if ((i - conceptEndCharacter) < 4)
        {
          featureList.add("word_right_3_" + StringHandling.escapeStringForFeatureName(currentToken));
        }
      }
    }
    return featureList;
  }

  public abstract String getTextFilename();

  public abstract void setTextFilename(String textFilename);

}
