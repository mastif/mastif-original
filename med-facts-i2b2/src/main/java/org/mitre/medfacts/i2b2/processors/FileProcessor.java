package org.mitre.medfacts.i2b2.processors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;

public abstract class FileProcessor
{
  protected String patternString;
  protected Pattern pattern;

  public String getPatternString()
  {
    return patternString;
  }

  public void setPatternString(String patternString)
  {
    this.patternString = patternString;
    this.pattern = Pattern.compile(patternString);
  }

  public List<Annotation> processAnnotationFile(String currentFilename)
          throws FileNotFoundException, IOException
  {
    File currentFile = new File(currentFilename);
    return processAnnotationFile(currentFile);
  }
  public List<Annotation> processAnnotationFile(File currentFile)
          throws FileNotFoundException, IOException
  {
    FileReader fr = new FileReader(currentFile);
    BufferedReader br = new BufferedReader(fr);

    List<Annotation> annotationList = new ArrayList<Annotation>();

    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 1;
    while ((currentLine = br.readLine()) != null)
    {
      Annotation c = processAnnotationLine(currentLine, pattern);
      c.setAnnotationFileLineNumber(lineNumber);
      annotationList.add(c);
      lineNumber++;
    }

    br.close();
    fr.close();

    return annotationList;
  }

  abstract public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern);

  /**
   * @return the pattern
   */
  public Pattern getPattern()
  {
    return pattern;
  }

  /**
   * @param pattern the pattern to set
   */
  public void setPattern(Pattern pattern)
  {
    this.pattern = pattern;
  }
}

