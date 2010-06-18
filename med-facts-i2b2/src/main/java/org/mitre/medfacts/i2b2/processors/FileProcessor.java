package org.mitre.medfacts.i2b2.processors;

import java.io.BufferedReader;
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

  public String getPatternString()
  {
    return patternString;
  }

  public void setPatternString(String patternString)
  {
    this.patternString = patternString;
  }

  public List<Annotation> processConceptAnnotationFile(String currentFilename)
          throws FileNotFoundException, IOException
  {
    FileReader fr = new FileReader(currentFilename);
    BufferedReader br = new BufferedReader(fr);

    List<Annotation> annotationList = new ArrayList<Annotation>();

    Pattern conceptPattern = Pattern.compile(getPatternString());

    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 0;
    while ((currentLine = br.readLine()) != null)
    {
      Annotation c = processAnnotationLine(currentLine, conceptPattern);
      annotationList.add(c);
    }

    br.close();
    fr.close();

    return annotationList;
  }

  abstract public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern);
}

