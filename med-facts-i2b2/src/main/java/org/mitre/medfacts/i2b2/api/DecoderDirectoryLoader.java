package org.mitre.medfacts.i2b2.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
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

    List<ApiConcept> apiConceptList = parseConceptFile(conceptFile, contents);


    DecoderSingleFileProcessor p = new DecoderSingleFileProcessor();
    p.setContents(contents);
    for (ApiConcept apiConcept : apiConceptList)
    {
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

  private List<ApiConcept> parseConceptFile(File conceptFile, String contents)
  {

    LineTokenToCharacterOffsetConverter c =
        new LineTokenToCharacterOffsetConverter(contents);

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

        Integer beginCharacter = c.convert(convertPositionToZonerLineAndTokenPosition(beginLineToken)).getBegin();
        Integer endCharacter = c.convert(convertPositionToZonerLineAndTokenPosition(endLineToken)).getEnd();

        logger.info(String.format("      - character offsets: %d-%d", beginCharacter, endCharacter));

        String conceptType = currentAnnotation.getConceptText();

        ApiConcept apiConcept = new ApiConcept(beginCharacter, endCharacter, conceptType);
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

}
