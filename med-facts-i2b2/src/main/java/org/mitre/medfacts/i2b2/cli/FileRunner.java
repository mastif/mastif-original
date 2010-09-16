/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.cli;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueSubType;
import org.mitre.medfacts.i2b2.annotation.CueWordAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueWordType;
import org.mitre.medfacts.i2b2.annotation.ScopeAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.annotation.ZoneAnnotation;
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
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.ZonerCli;
import org.mitre.medfacts.zoner.ZonerCli.Range;

/**
 *
 * @author MCOARR
 */
public class FileRunner extends AbstractRunner
{

  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();
  protected FileProcessor assertionFileProcessor = new AssertionFileProcessor();
  protected FileProcessor relationFileProcessor = new RelationFileProcessor();
  protected FileProcessor scopeFileProcessor = new ScopeFileProcessor();

  public FileRunner()
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

    FileRunner runner = new FileRunner();

    String filename = args[0];

    runner.setTextFilename(filename);
    for (int i = 1; i < count; i++)
    {
      String current = args[i];
      runner.addAnnotationFilename(current);
    }

    runner.execute();
  }

  protected String textFilename;
  protected List<String> annotationFilenameList;

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

    StringWriter writer = new StringWriter();
    PrintWriter printer = new PrintWriter(writer);

    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 0;
    while ((currentLine = br.readLine()) != null)
    {
      printer.println(currentLine);
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

    setEntireContents(writer.toString());

    printer.close();
    writer.close();

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
          if (mode == Mode.DECODE)
          {
            System.out.println(">>>processed concept file in decode mode; building assertions from concepts...");
            List<Annotation> assertionAnnotationList =
              buildAssertionsFromConcepts(currentAnnotationList);
            annotationsByType.put(AnnotationType.ASSERTION, assertionAnnotationList);
            // currentAnnotationList is added to the full list after this switch
            // statement, so all the assertions are also added to the
            // currentAnnotationList
            currentAnnotationList.addAll(assertionAnnotationList);
          }
          break;
        case ASSERTION:
          if (mode == Mode.EVAL || mode == Mode.TRAIN)
          {
            System.out.println(">>>in eval mode, reading assertions file");
            currentAnnotationList = getAssertionFileProcessor().processConceptAnnotationFile(currentFilename);
            annotationsByType.put(AnnotationType.ASSERTION, currentAnnotationList);
          }
          break;
        case RELATION:
          currentAnnotationList = getRelationFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.RELATION, currentAnnotationList);
          break;
        case SCOPE:
          currentAnnotationList = getScopeFileProcessor().processConceptAnnotationFile(currentFilename);
          annotationsByType.put(AnnotationType.SCOPE, currentAnnotationList);

          List<Annotation> scopeOrCueAnnotationList = annotationsByType.get(AnnotationType.SCOPE);

          System.out.format("=== BEGIN SCOPE AND CUE ANNOTATIONS === %n");
          for (Annotation a : scopeOrCueAnnotationList)
          {
            System.out.format(" - scope or cue annotation: (%s) %s%n", a.getClass().getName().toString(), a.toString());
          }
          System.out.format("=== END SCOPE AND CUE ANNOTATIONS === %n");
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

  @Override
  public void loadTextAndAnnotation()
  {
    try
    {
      processTextFile();
      processAnnotationFiles();
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

}
