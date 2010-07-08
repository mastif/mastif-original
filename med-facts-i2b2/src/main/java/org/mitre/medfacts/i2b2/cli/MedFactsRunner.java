/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.cli;

import java.net.URISyntaxException;
import java.util.Map;
import org.mitre.medfacts.i2b2.annotation.ScopeOrCueAnnotation;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.Location;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.annotation.CueAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueWordType;
import org.mitre.medfacts.i2b2.annotation.ScopeAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.processors.RelationFileProcessor;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;
import org.mitre.medfacts.i2b2.scanners.CueListScanner;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.AnnotationIndexer;
import org.mitre.medfacts.i2b2.util.ArrayPrinter;
import org.mitre.medfacts.i2b2.util.StringHandling;

/**
 *
 * @author MCOARR
 */
public class MedFactsRunner
{
  public final static int MAX_WINDOW_LEFT = 12;
  public final static int MAX_WINDOW_RIGHT = 12;

  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

  private String textLookup[][];

  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();
  protected FileProcessor assertionFileProcessor = new AssertionFileProcessor();
  protected FileProcessor relationFileProcessor = new RelationFileProcessor();
  protected FileProcessor scopeFileProcessor = new ScopeFileProcessor();

  AnnotationIndexer indexer = new AnnotationIndexer();

  public MedFactsRunner()
  {
    annotationFilenameList = new ArrayList<String>();
    mapOfTrainingInstanceLists = new TreeMap<AnnotationType, List<TrainingInstance>>();
    List<TrainingInstance> thisList = mapOfTrainingInstanceLists.put(AnnotationType.ASSERTION, new ArrayList<TrainingInstance>());
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args)
  {
    int count = args.length;
    if (count < 2)
    {
      System.out.println("syntax: MedFactRunner text_file annotation_file_1 annotation_file_2 ...");
    }

    MedFactsRunner runner = new MedFactsRunner();

    String filename = args[0];

    runner.setTextFilename(filename);
    for (int i = 1; i < count; i++)
    {
      String current = args[i];
      runner.addAnnotationFilename(current);
    }

    runner.execute();
  }

  private String textFilename;
  private List<String> annotationFilenameList;
  protected List<Annotation> allAnnotationList;
  private Map<AnnotationType,List<Annotation>> annotationsByType;
  protected Map<AnnotationType, List<TrainingInstance>> mapOfTrainingInstanceLists;

  public void execute()
  {
    try
      {
        processTextFile();
        processAnnotationFiles();
        postProcess();
        //validateAnnotations();
        indexAnnotations();
        printOutFeatures();
      } catch (FileNotFoundException ex)
      {
        Logger.getLogger(MedFactsRunner.class.getName()).log(Level.SEVERE, null, ex);
        throw new RuntimeException("problem processing files; FileNotFoundException", ex);
      } catch (IOException ex)
      {
        Logger.getLogger(MedFactsRunner.class.getName()).log(Level.SEVERE, null, ex);
        throw new RuntimeException("problem processing files; IOException", ex);
      }
  }

  public AnnotationType getAnnotationTypeFromFilename(String currentFilename)
  {
    AnnotationType currentAnnotationType = null;
    if (currentFilename.endsWith(Constants.FILE_EXTENSION_CONCEPT_FILE))
    {
      currentAnnotationType = AnnotationType.CONCEPT;
    } else if (currentFilename.endsWith(Constants.FILE_EXTENSION_ASSERTION_FILE))
    {
      currentAnnotationType = AnnotationType.ASSERTION;
    } else if (currentFilename.endsWith(Constants.FILE_EXTENSION_RELATION_FILE))
    {
      currentAnnotationType = AnnotationType.RELATION;
    } else if (currentFilename.endsWith(Constants.FILE_EXTENSION_SCOPE_FILE))
    {
      currentAnnotationType = AnnotationType.SCOPE;
    }
    return currentAnnotationType;
  }

  public void processCueList(String cueListFilename, CueWordType cueWordType) throws URISyntaxException
  {
    ClassLoader classLoader = getClass().getClassLoader();
    URL cueFileUrl = classLoader.getResource(cueListFilename);
    System.out.format("cue list url: %s%n", cueFileUrl);
    URI cueFileUri = cueFileUrl.toURI();
    System.out.format("cue list uri: %s%n", cueFileUri);
    File cueFile = new File(cueFileUri);
    System.out.format("cue list url: %s%n", cueFile);
    CueListScanner scanner = new CueListScanner(cueFile, cueWordType);
    scanner.setTextLookup(textLookup);
    scanner.execute();
    List<Annotation> annotationList = scanner.getAnnotationList();
    allAnnotationList.addAll(annotationList);
    List<Annotation> cueWordAnnotationList = annotationsByType.get(AnnotationType.CUEWORD);
    if (cueWordAnnotationList == null)
    {
      cueWordAnnotationList = new ArrayList<Annotation>();
      annotationsByType.put(AnnotationType.CUEWORD, cueWordAnnotationList);
    }
    cueWordAnnotationList.addAll(annotationList);
  }

  public void processScopeInProcess(Map<AnnotationType, List<Annotation>> annotationsByType, List<Annotation> allAnnotationList) throws RuntimeException
  {
    String cueModelFileName = "cue.model";
    String scopeModelFileName = "scope.model";
    ClassLoader cl = this.getClass().getClassLoader();
    URL cueModelFileUrl = cl.getResource(cueModelFileName);
    URL scopeModelFileUrl = cl.getResource(scopeModelFileName);
    File cueModelFile = null;
    File scopeModelFile = null;
    try
    {
      cueModelFile = new File(cueModelFileUrl.toURI());
      scopeModelFile = new File(scopeModelFileUrl.toURI());
    } catch (URISyntaxException ex)
    {
      Logger.getLogger(MedFactsRunner.class.getName()).log(Level.SEVERE, "problem getting scope or model file URIs", ex);
      throw new RuntimeException("problem getting scope or model file URIs");
    }
    String cueModelFileNameFullPath = cueModelFile.getAbsolutePath();
    String scopeModelFileNameFullPath = scopeModelFile.getAbsolutePath();
    System.out.format("cue model: %s%n", cueModelFileNameFullPath);
    System.out.format("scope model: %s%n", scopeModelFileNameFullPath);
    ScopeParser scopeParser = new ScopeParser(scopeModelFileNameFullPath, cueModelFileNameFullPath);
    List<ScopeOrCueAnnotation> scopeOrCueAnnotationList = scopeParser.decodeDocument(textLookup);
    //    Map<AnnotationType,List<Annotation>> map2 = new TreeMap<AnnotationType,List<Annotation>>();
    //    map2.put(AnnotationType.SCOPE, allAnnotationList);
    List<Annotation> newList = new ArrayList<Annotation>();
    newList.addAll(scopeOrCueAnnotationList);
    annotationsByType.put(AnnotationType.SCOPE, newList);
    allAnnotationList.addAll(scopeOrCueAnnotationList);
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

  public String getTextFilename()
  {
    return textFilename;
  }

  public void setTextFilename(String textFilename)
  {
    this.textFilename = textFilename;
  }

  public List<String> getAnnotationFilenameList()
  {
    return annotationFilenameList;
  }

  public void setAnnotationFilenameList(List<String> annotationFilenameList)
  {
    this.annotationFilenameList = annotationFilenameList;
  }

  public void addAnnotationFilename(String filename)
  {
    this.annotationFilenameList.add(filename);
  }

  private void processTextFile() throws FileNotFoundException, IOException
  {
    System.out.format("processing text file \"%s\"...%n", textFilename);
    
    FileReader fr = new FileReader(getTextFilename());
    BufferedReader br = new BufferedReader(fr);

    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 0;
    while ((currentLine = br.readLine()) != null)
    {
//      System.out.format("CURRENT LINE (pre) [%d]: %s%n", lineNumber, currentLine);
      //ArrayList<String> currentTextLookupLine = new ArrayList<String>();
      //textLookup.add(currentTextLookupLine);
      
      String tokenArray[] = WHITESPACE_PATTERN.split(currentLine);
//      for (String currentToken : tokenArray)
//      {
//        System.out.format("    CURRENT token (pre): %s%n", currentToken);
//      }
      textLookupTemp.add(tokenArray);

      lineNumber++;
    }

    br.close();
    fr.close();

    System.out.println("=====");

    String twoDimensionalStringArray[][] = new String[1][];
    //String textLookup[][] = null;
    textLookup = textLookupTemp.toArray(twoDimensionalStringArray);

//    int lineNumber2 = 0;
//    for (String[] currentTextLookupLine : textLookup)
//    {
//      int tokenNumber2 = 0;
//      for (String currentToken : currentTextLookupLine)
//      {
//        System.out.format("    CURRENT token (post): [%d:%d] \"%s\"%n", lineNumber2, tokenNumber2, currentToken);
//        tokenNumber2++;
//      }
//      lineNumber2++;
//    }

    System.out.format("done processing text file \"%s\".%n", textFilename);
  }

  private void processAnnotationFiles() throws IOException
  {
    List<Annotation> allAnnotationList = new ArrayList<Annotation>();
    Map<AnnotationType,List<Annotation>> annotationsByType = new TreeMap<AnnotationType,List<Annotation>>();

//    FileProcessor conceptFileProcessor = new ConceptFileProcessor();
//    FileProcessor assertionFileProcessor = new AssertionFileProcessor();
//    FileProcessor relationFileProcessor = new RelationFileProcessor();
//    FileProcessor scopeFileProcessor = new ScopeFileProcessor();

    for (String currentFilename : getAnnotationFilenameList())
    {
      System.out.format("processing annotation file \"%s\"...%n", currentFilename);
      AnnotationType currentAnnotationType = getAnnotationTypeFromFilename(currentFilename);
      //System.out.format(" - annotationType of file \"%s\" is %s%n", currentFilename, currentAnnotationType);

      List<Annotation> currentAnnotationList = null;
      switch (currentAnnotationType)
      {
        case CONCEPT:
          //currentAnnotationList = processConceptAnnotationFile(currentFilename);
          currentAnnotationList = getConceptFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.CONCEPT, currentAnnotationList);
          break;
        case ASSERTION:
          currentAnnotationList = getAssertionFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.ASSERTION, currentAnnotationList);
          break;
        case RELATION:
          currentAnnotationList = getRelationFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.RELATION, currentAnnotationList);
          break;
        case SCOPE:
          currentAnnotationList = getScopeFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.SCOPE, currentAnnotationList);
          break;
      }

      if (currentAnnotationList != null)
      {
        allAnnotationList.addAll(currentAnnotationList);
      }

      System.out.format("done processing annotation file \"%s\".%n", currentFilename);
    }
    processScopeInProcess(annotationsByType, allAnnotationList);

    setAllAnnotationList(allAnnotationList);
    setAnnotationsByType(annotationsByType);

  }

  /**
   * @return the textLookup
   */
  public String[][] getTextLookup()
  {
    return textLookup;
  }

  /**
   * @param textLookup the textLookup to set
   */
  public void setTextLookup(String[][] textLookup)
  {
    this.textLookup = textLookup;
  }

  /**
   * @return the allAnnotationList
   */
  public List<Annotation> getAllAnnotationList()
  {
    return allAnnotationList;
  }

  /**
   * @param allAnnotationList the allAnnotationList to set
   */
  public void setAllAnnotationList(List<Annotation> allAnnotationList)
  {
    this.allAnnotationList = allAnnotationList;
  }

  private String extractTargetText(String[][] textLookup, Location begin, Location end)
  {
    if (begin.getLine() != end.getLine())
    {
      throw new RuntimeException("cannot have a range across multiple lines");
    }
    int line = begin.getLine();
    int lineOffset = line - 1;
    StringBuilder b = new StringBuilder();
    int lineLength = textLookup[lineOffset].length;
    boolean isOffsetLegit =
            begin.getTokenOffset() >= 0 &&
            begin.getTokenOffset() < lineLength &&
            end.getTokenOffset() >= 0 &&
            end.getTokenOffset() < lineLength;

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
      boolean isLast = (i == end.getTokenOffset());
      if (!isLast) { b.append(" "); }
    }
    return b.toString();
  }

  private void printOutFeatures() throws IOException
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
      AssertionAnnotation currentAssertionAnnotation = (AssertionAnnotation)currentAnnotation;
      if (!currentAssertionAnnotation.getConceptType().equals(ConceptType.PROBLEM))
      {
        // skip this one
        continue;
      }

      TrainingInstance trainingInstance = new TrainingInstance();

      trainingInstance.setFilename(getTextFilename());
      trainingInstance.setLineNumber(lineNumber);

      AssertionValue assertionValue = currentAssertionAnnotation.getAssertionValue();
      String assertionValueString = assertionValue.toString().toLowerCase();
      trainingInstance.setExpectedValue(assertionValueString);

      String conceptText = currentAssertionAnnotation.getConceptText();
      String conceptTextFeature = constructConceptPhraseFeature(conceptText);
      trainingInstance.addFeature(conceptTextFeature);

      Matcher conceptHeadMatcher = conceptHeadPattern.matcher(conceptText);
      if (conceptHeadMatcher.find())
      {
        String conceptHeadText = conceptHeadMatcher.group(1);
        String conceptHeadFeature = constructConceptHeadFeature(conceptHeadText);
        trainingInstance.addFeature(conceptHeadFeature);
      }

      Location conceptBeginLocation = currentAssertionAnnotation.getBegin();
      int conceptBeginLine = conceptBeginLocation.getLine();
      int conceptBeginTokenOffset = conceptBeginLocation.getTokenOffset();
      Location conceptEndLocation = currentAssertionAnnotation.getEnd();
      int conceptEndTokenOffset = conceptEndLocation.getTokenOffset();
      String currentLine[] = textLookup[conceptBeginLine-1];

      List<String> wordLeftFeatureList = constructWordLeftFeatureList(conceptBeginTokenOffset, conceptEndTokenOffset, currentLine);
      for (String currentFeature : wordLeftFeatureList)
      {
        trainingInstance.addFeature(currentFeature);
      }

      List<String> wordRightFeatureList = constructWordRightFeatureList(conceptBeginTokenOffset, conceptEndTokenOffset, currentLine);
      for (String currentFeature : wordRightFeatureList)
      {
        trainingInstance.addFeature(currentFeature);
      }
      System.out.format("lineNumber: %d%n", lineNumber);
      String tokensOnCurrentLine[] = textLookup[lineNumber-1];
      for (int currentTokenOffset=0; currentTokenOffset < tokensOnCurrentLine.length; currentTokenOffset++)
      {
        String currentToken = tokensOnCurrentLine[currentTokenOffset];
        List<Annotation> annotationsAtCurrentPosition = indexer.findAnnotationsForPosition(lineNumber, currentTokenOffset);

        int scopeCount = 0;
        if (annotationsAtCurrentPosition != null)
        for (Annotation a : annotationsAtCurrentPosition)
        {
          if (a instanceof ScopeAnnotation)
          {
            ScopeAnnotation scope = (ScopeAnnotation)a;
            scopeCount++;
            trainingInstance.addFeature("scope");
            trainingInstance.addFeature("in_scope_" + currentToken);
            trainingInstance.addFeature("in_scope_id_" + scope.getScopeId() + "_" + currentToken);
          }

          if (a instanceof CueAnnotation)
          {
            CueAnnotation cue = (CueAnnotation)a;
            trainingInstance.addFeature("cue");
            trainingInstance.addFeature("in_cue_" + currentToken);
            trainingInstance.addFeature("in_cue_for_scope_id_" + cue.getScopeIdReference() + "_" + currentToken);
          }
        }
        if (scopeCount > 0)
        {
          trainingInstance.addFeature("scope_count_" + scopeCount);
          boolean scopeCountIsEven = (scopeCount % 2) == 0;
          trainingInstance.addFeature("scope_count_" + (scopeCountIsEven ? "even" : "odd"));
        }
      }

      String featureLine = trainingInstance.toStringWithExpectedValue();
      featuresPrinter.println(featureLine);
      getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION).add(trainingInstance);

      //lineNumber++;
    }
    //System.out.println("$$$$$");
    //System.out.println("$$$$$");

    featuresPrinter.close();
    featuresBufferedWriter.close();
    featuresFileWriter.close();
  }
  
  public String constructConceptPhraseFeature(String input)
  {
    return "concept_phrase_is_" + StringHandling.escapeStringForFeatureName(input);
  }
  
  public String constructConceptHeadFeature(String input)
  {
    return "concept_head_is_" + StringHandling.escapeStringForFeatureName(input);
  }

//  public String constructFeatureSuffix(String input)
//  {
//    return StringHandling.escapeStringForFeatureName(input);
//  }

  /**
   * @return the annotationsByType
   */
  public Map<AnnotationType, List<Annotation>> getAnnotationsByType()
  {
    return annotationsByType;
  }

  /**
   * @param annotationsByType the annotationsByType to set
   */
  public void setAnnotationsByType(Map<AnnotationType, List<Annotation>> annotationsByType)
  {
    this.annotationsByType = annotationsByType;
  }

  private List<String> constructWordLeftFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i=conceptBeginCharacter - 1; i >= 0 && ++count <= MAX_WINDOW_LEFT; i--)
    {
      String currentToken = currentLine[i];
      String featureName = "";
      featureName = "word_left_" + StringHandling.escapeStringForFeatureName(currentToken);
      featureList.add(featureName);
    }
    return featureList;
  }

  private List<String> constructWordRightFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i=conceptEndCharacter + 1; i < currentLine.length && ++count <= MAX_WINDOW_RIGHT; i++)
    {
      String currentToken = currentLine[i];
      String featureName = "";
      featureName = "word_right_" + StringHandling.escapeStringForFeatureName(currentToken);
      featureList.add(featureName);
    }
    return featureList;
  }

  /**
   * @return the mapOfTrainingInstanceLists
   */
  public Map<AnnotationType, List<TrainingInstance>> getMapOfTrainingInstanceLists()
  {
    return mapOfTrainingInstanceLists;
  }

  /**
   * @param mapOfTrainingInstanceLists the mapOfTrainingInstanceLists to set
   */
  public void setMapOfTrainingInstanceLists(Map<AnnotationType, List<TrainingInstance>> mapOfTrainingInstanceLists)
  {
    this.mapOfTrainingInstanceLists = mapOfTrainingInstanceLists;
  }

  /**
   * @return the conceptFileProcessor
   */
  public FileProcessor getConceptFileProcessor()
  {
    return conceptFileProcessor;
  }

  /**
   * @param conceptFileProcessor the conceptFileProcessor to set
   */
  public void setConceptFileProcessor(FileProcessor conceptFileProcessor)
  {
    this.conceptFileProcessor = conceptFileProcessor;
  }

  /**
   * @return the assertionFileProcessor
   */
  public FileProcessor getAssertionFileProcessor()
  {
    return assertionFileProcessor;
  }

  /**
   * @param assertionFileProcessor the assertionFileProcessor to set
   */
  public void setAssertionFileProcessor(FileProcessor assertionFileProcessor)
  {
    this.assertionFileProcessor = assertionFileProcessor;
  }

  /**
   * @return the relationFileProcessor
   */
  public FileProcessor getRelationFileProcessor()
  {
    return relationFileProcessor;
  }

  /**
   * @param relationFileProcessor the relationFileProcessor to set
   */
  public void setRelationFileProcessor(FileProcessor relationFileProcessor)
  {
    this.relationFileProcessor = relationFileProcessor;
  }

  /**
   * @return the scopeFileProcessor
   */
  public FileProcessor getScopeFileProcessor()
  {
    return scopeFileProcessor;
  }

  /**
   * @param scopeFileProcessor the scopeFileProcessor to set
   */
  public void setScopeFileProcessor(FileProcessor scopeFileProcessor)
  {
    this.scopeFileProcessor = scopeFileProcessor;
  }

  private void indexAnnotations()
  {
    indexer.indexAnnotations(getAllAnnotationList());
  }

  public void postProcess()
  {
    try
    {
      processCueList("org/mitre/medfacts/i2b2/cuefiles/updated_negation_cue_list.txt", CueWordType.NEGATION);
      processCueList("org/mitre/medfacts/i2b2/cuefiles/updated_speculation_cue_list.txt", CueWordType.SPECULATION);

    }
    catch (URISyntaxException e)
    {
      Logger.getLogger(MedFactsRunner.class.getName()).log(Level.SEVERE, "URISyntaxException when trying to load cue word", e);
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

}
