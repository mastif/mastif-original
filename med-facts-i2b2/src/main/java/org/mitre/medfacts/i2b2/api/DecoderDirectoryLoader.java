package org.mitre.medfacts.i2b2.api;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mitre.itc.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.util.Location;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

/**
 *
 * @author MCOARR
 */
public class DecoderDirectoryLoader
{
  static final Logger logger = Logger.getLogger(DecoderDirectoryLoader.class.getName());

  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();

  protected File directory;
  protected String model;
  protected Set<String> enabledFeatureIdSet;
  JarafeMEDecoder namedEntityDecoder;
  protected static ScopeParser scopeParser = null;

  public void processDirectory()
  {
    logger.info(String.format("  - processing directory \"%s\"...", directory.getAbsolutePath()));
    File[] textFiles = directory.listFiles(new FilenameFilter()
    {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    });
    logger.info("=== TEXT FILE LIST BEGIN ===");
    List<File> textFileList = new ArrayList<File>();
    Collections.addAll(textFileList, textFiles);

    for (File currentTextFile : textFileList)
    {
      processFile(currentTextFile);
    }

    logger.info(String.format("  - done processing directory \"%s\".", directory.getAbsolutePath()));
  }

  public File getDirectory()
  {
    return directory;
  }

  public void setDirectory(File directory)
  {
    this.directory = directory;
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
    namedEntityDecoder = new JarafeMEDecoder(model);
  }

  private void processFile(File currentTextFile)
  {
    logger.info(String.format("    - processing \"%s\"...", currentTextFile.getName()));
    String contents = StringHandling.readEntireContents(currentTextFile);

    String conceptFilePath = currentTextFile.getAbsolutePath().replaceFirst("\\.txt$", ".con");
    File conceptFile = new File(conceptFilePath);
    logger.info(String.format("    - using concept file \"%s\"...", conceptFile.getName()));
    String conceptFileContents = StringHandling.readEntireContents(conceptFile);
    //List<Concept> parseConceptFileContents(conceptFileContents);

    LineTokenToCharacterOffsetConverter converter =
        new LineTokenToCharacterOffsetConverter(contents);

    List<ApiConcept> apiConceptList = parseConceptFile(conceptFile, contents, converter);


    DecoderSingleFileProcessor p = new DecoderSingleFileProcessor(converter);
    p.setContents(contents);
    p.setAssertionDecoder(namedEntityDecoder);
    p.setEnabledFeatureIdSet(enabledFeatureIdSet);
    p.setScopeParser(scopeParser);
    for (ApiConcept apiConcept : apiConceptList)
    {
      logger.info(String.format("dir loader concept: %s", apiConcept.toString()));
      p.addConcept(apiConcept);
    }
    p.processSingleFile();
    logger.info(String.format("    - done processing \"%s\".", currentTextFile.getName()));
  }

  private static LineAndTokenPosition convertPositionToZonerLineAndTokenPosition(Location input)
  {
    LineAndTokenPosition output = new LineAndTokenPosition();
    output.setLine(input.getLine());
    output.setTokenOffset(input.getTokenOffset());
    return output;
  }

  private List<ApiConcept> parseConceptFile(File conceptFile, String contents, LineTokenToCharacterOffsetConverter converter)
  {

    try
    {
      List<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
      List<Annotation> conceptAnnotations = conceptFileProcessor.processAnnotationFile(conceptFile);

      logger.info("    BEGIN CONCEPTS");
      for (Annotation currentAnnotation : conceptAnnotations)
      {
        logger.info(String.format("    CONCEPT: %s", currentAnnotation));
        Location beginLineToken = currentAnnotation.getBegin();
        Location endLineToken = currentAnnotation.getEnd();

        Integer beginCharacter = converter.convert(convertPositionToZonerLineAndTokenPosition(beginLineToken)).getBegin();
        Integer endCharacter = converter.convert(convertPositionToZonerLineAndTokenPosition(endLineToken)).getEnd();

        String text =  contents.substring(beginCharacter, endCharacter + 1);

        logger.info(String.format("      - character offsets: %d-%d", beginCharacter, endCharacter));

        String conceptType = ((ConceptAnnotation)currentAnnotation).getConceptType().toString();

        ApiConcept apiConcept = new ApiConcept(beginCharacter, endCharacter, conceptType, text);
        apiConceptList.add(apiConcept);

      }
      logger.info("    END CONCEPTS");
      return apiConceptList;

    } catch (IOException ex)
    {
      String message = "IOException during parsing of concept file";
      Logger.getLogger(DecoderDirectoryLoader.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    }
  }

  public Set<String> getEnabledFeatureIdSet()
  {
    return enabledFeatureIdSet;
  }

  public void setEnabledFeatureIdSet(Set<String> enabledFeatureIdSet)
  {
    this.enabledFeatureIdSet = enabledFeatureIdSet;
  }

  public static ScopeParser getScopeParser()
  {
    return scopeParser;
  }

  public static void setScopeParser(ScopeParser aScopeParser)
  {
    scopeParser = aScopeParser;
  }

}
