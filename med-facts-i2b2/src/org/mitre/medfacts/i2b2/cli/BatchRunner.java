/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.util.Constants;

/**
 *
 * @author MCOARR
 */
public class BatchRunner
{
  protected String baseDirectoryString;

  public static void main(String args[])
  {
    String baseDirectory = args[0];
    System.out.format("base directory: %s%n", baseDirectory);

    BatchRunner batchRunner = new BatchRunner();
    batchRunner.setBaseDirectoryString(baseDirectory);

    batchRunner.execute();
  }

  /**
   * @return the baseDirectoryString
   */
  public String getBaseDirectoryString()
  {
    return baseDirectoryString;
  }

  /**
   * @param baseDirectoryString the baseDirectoryString to set
   */
  public void setBaseDirectoryString(String baseDirectoryString)
  {
    this.baseDirectoryString = baseDirectoryString;
  }

  private void execute()
  {
    File baseDirectory = new File(baseDirectoryString);
    File textFiles[] = baseDirectory.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    });

    System.out.println("=== TEXT FILE LIST BEGIN ===");
    for (File currentTextFile : textFiles)
    {
      System.out.format(" * %s%n", currentTextFile);

      String currentTextFilename = currentTextFile.getAbsolutePath();
      String baseFilename = Constants.TEXT_FILE_EXTENSTION_PATTERN.matcher(currentTextFilename).replaceFirst("");
      System.out.format("    - base filename: %s%n", baseFilename);

      String conceptFilename = baseFilename + Constants.FILE_EXTENSION_CONCEPT_FILE;
      File conceptFile = new File(conceptFilename);
      boolean conceptFileExists = conceptFile.exists();
      System.out.format("    - concept filename: %s (%s)%n", conceptFilename, (conceptFileExists ? "EXISTS" : "not present"));

      String assertionFilename = baseFilename + Constants.FILE_EXTENSION_ASSERTION_FILE;
      File assertionFile = new File(assertionFilename);
      boolean assertionFileExists = assertionFile.exists();
      System.out.format("    - assertion filename: %s (%s)%n", assertionFilename, (assertionFileExists ? "EXISTS" : "not present"));

      String relationFilename = baseFilename + Constants.FILE_EXTENSION_RELATION_FILE;
      File relationFile = new File(relationFilename);
      boolean relationFileExists = relationFile.exists();
      System.out.format("    - relation filename: %s (%s)%n", relationFilename, (relationFileExists ? "EXISTS" : "not present"));

      MedFactsRunner runner = new MedFactsRunner();
      runner.setTextFilename(currentTextFile.getAbsolutePath());
      runner.addAnnotationFilename(conceptFilename);
      runner.addAnnotationFilename(assertionFilename);
      runner.addAnnotationFilename(relationFilename);

      runner.execute();
    }
    System.out.println("=== TEXT FILE LIST END ===");
  }

}
