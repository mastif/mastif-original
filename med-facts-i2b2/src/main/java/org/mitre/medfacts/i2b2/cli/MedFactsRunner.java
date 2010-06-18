/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.cli;

import java.util.Map;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.processors.RelationFileProcessor;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
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

  private String textLookup[][];

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
        validateAnnotations();
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

    Pattern whitespacePattern = Pattern.compile("\\s");
    
    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 0;
    while ((currentLine = br.readLine()) != null)
    {
      System.out.format("CURRENT LINE (pre) [%d]: %s%n", lineNumber, currentLine);
      //ArrayList<String> currentTextLookupLine = new ArrayList<String>();
      //textLookup.add(currentTextLookupLine);
      
      String tokenArray[] = whitespacePattern.split(currentLine);
      for (String currentToken : tokenArray)
      {
        System.out.format("    CURRENT token (pre): %s%n", currentToken);
      }
      textLookupTemp.add(tokenArray);

      lineNumber++;
    }

    br.close();
    fr.close();

    System.out.println("=====");

    String twoDimensionalStringArray[][] = new String[1][];
    //String textLookup[][] = null;
    textLookup = textLookupTemp.toArray(twoDimensionalStringArray);

    int lineNumber2 = 0;
    for (String[] currentTextLookupLine : textLookup)
    {
      int tokenNumber2 = 0;
      for (String currentToken : currentTextLookupLine)
      {
        System.out.format("    CURRENT token (post): [%d:%d] \"%s\"%n", lineNumber2, tokenNumber2, currentToken);
        tokenNumber2++;
      }
      lineNumber2++;
    }

    System.out.format("done processing text file \"%s\".%n", textFilename);
  }

  private void processAnnotationFiles() throws IOException
  {
    List<Annotation> allAnnotationList = new ArrayList<Annotation>();
    Map<AnnotationType,List<Annotation>> annotationsByType = new TreeMap<AnnotationType,List<Annotation>>();

    FileProcessor conceptFileProcessor = new ConceptFileProcessor();
    FileProcessor assertionFileProcessor = new AssertionFileProcessor();
    FileProcessor relationFileProcessor = new RelationFileProcessor();
    FileProcessor scopeFileProcessor = new ScopeFileProcessor();

    for (String currentFilename : getAnnotationFilenameList())
    {
      System.out.format("processing annotation file \"%s\"...%n", currentFilename);
      AnnotationType currentAnnotationType = getAnnotationTypeFromFilename(currentFilename);
      System.out.format(" - annotationType of file \"%s\" is %s%n", currentFilename, currentAnnotationType);

      List<Annotation> currentAnnotationList = null;
      switch (currentAnnotationType)
      {
        case CONCEPT:
          //currentAnnotationList = processConceptAnnotationFile(currentFilename);
          currentAnnotationList = conceptFileProcessor.processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.CONCEPT, currentAnnotationList);
          break;
        case ASSERTION:
          currentAnnotationList = assertionFileProcessor.processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.ASSERTION, currentAnnotationList);
          break;
        case RELATION:
          currentAnnotationList = relationFileProcessor.processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.RELATION, currentAnnotationList);
          break;
        case SCOPE:
          currentAnnotationList = scopeFileProcessor.processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.SCOPE, currentAnnotationList);
          break;
      }

      if (currentAnnotationList != null)
      {
        allAnnotationList.addAll(currentAnnotationList);
      }

      System.out.format("done processing annotation file \"%s\".%n", currentFilename);
    }

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
            begin.getCharacter() >= 0 &&
            begin.getCharacter() < lineLength &&
            end.getCharacter() >= 0 &&
            end.getCharacter() < lineLength;

    System.err.format("STATUS: checking offset(s): %s to %s%n", begin, end);
    if (!isOffsetLegit)
    {
      System.err.format("ERROR!: invalid line offset(s): %s to %s (%s) TEXT: >>%s<< %n", begin, end, getTextFilename(), ArrayPrinter.toString(textLookup[lineOffset]));
      return null;
    }
    for (int i = begin.getCharacter(); i <= end.getCharacter(); i++)
    {
      String currentToken = textLookup[lineOffset][i];
      b.append(currentToken);
      boolean isLast = (i == end.getCharacter());
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
    System.out.println("$$$$$");
    System.out.println("$$$$$");

    for (Annotation currentAnnotation : getAnnotationsByType().get(AnnotationType.ASSERTION))
    {
      AssertionAnnotation currentAssertionAnnotation = (AssertionAnnotation)currentAnnotation;
      if (!currentAssertionAnnotation.getConceptType().equals(ConceptType.PROBLEM))
      {
        // skip this one
        continue;
      }

      StringBuilder sb = new StringBuilder();
      TrainingInstance trainingInstance = new TrainingInstance();

      AssertionValue assertionValue = currentAssertionAnnotation.getAssertionValue();
      String assertionValueString = assertionValue.toString().toLowerCase();
      sb.append(assertionValueString);
      trainingInstance.setExpectedValue(assertionValueString);

      String conceptText = currentAssertionAnnotation.getConceptText();
      String conceptTextFeature = constructConceptPhraseFeature(conceptText);
      sb.append(" ");
      sb.append(conceptTextFeature);
      trainingInstance.addFeature(conceptTextFeature);

      Matcher conceptHeadMatcher = conceptHeadPattern.matcher(conceptText);
      if (conceptHeadMatcher.find())
      {
        String conceptHeadText = conceptHeadMatcher.group(1);
        String conceptHeadFeature = constructConceptHeadFeature(conceptHeadText);
        sb.append(" ");
        sb.append(conceptHeadFeature);
        trainingInstance.addFeature(conceptHeadFeature);
      }

      Location conceptBeginLocation = currentAssertionAnnotation.getBegin();
      int conceptBeginLine = conceptBeginLocation.getLine();
      int conceptBeginCharacter = conceptBeginLocation.getCharacter();
      Location conceptEndLocation = currentAssertionAnnotation.getEnd();
      int conceptEndCharacter = conceptEndLocation.getCharacter();
      String currentLine[] = textLookup[conceptBeginLine-1];

      List<String> wordLeftFeatureList = constructWordLeftFeatureList(conceptBeginCharacter, conceptEndCharacter, currentLine);
      for (String currentFeature : wordLeftFeatureList)
      {
        sb.append(" ");
        sb.append(currentFeature);
        trainingInstance.addFeature(currentFeature);
      }

      List<String> wordRightFeatureList = constructWordRightFeatureList(conceptBeginCharacter, conceptEndCharacter, currentLine);
      for (String currentFeature : wordRightFeatureList)
      {
        sb.append(" ");
        sb.append(currentFeature);
        trainingInstance.addFeature(currentFeature);
      }

      String featureLine = sb.toString();
      System.out.println(featureLine);
      featuresPrinter.println(featureLine);
      getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION).add(trainingInstance);
    }
    System.out.println("$$$$$");
    System.out.println("$$$$$");

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