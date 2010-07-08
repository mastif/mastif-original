/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.scanners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author MCOARR
 */
public class CueListScanner
{
  public static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  public CueListScanner(File scannerTermsFile)
  {
    loadScannerTermsFile(scannerTermsFile);
  }

  protected String[][] textLookup;
  protected List<CueItem> cueItemList = new ArrayList<CueItem>();


  public void execute()
  {
    for (CueItem cueItem : cueItemList)
    {
      int size = cueItem.getSize();
      for (int lineNumber=0; lineNumber < textLookup.length; lineNumber++)
      {
        String currentLine[] = textLookup[lineNumber];
        for (int tokenOffset=0; tokenOffset + size - 1 < currentLine.length; tokenOffset++)
        {
          boolean matched = compareForCueMatch(cueItem, currentLine, tokenOffset);
          if (matched)
          {
            System.out.format("MATCHES!!!%n  BEGIN MATCH%n  cue: %s%n  inputLine: %s%n  position: %d%n  END MATCH%n%n", cueItem.toString(), convertLineToString(currentLine), tokenOffset);

          }
        }
      }
    }
  }

  public String convertLineToString(String currentLine[])
  {
    StringBuilder sb = new StringBuilder();
    sb.append("{ ");
    for (int i = 0; i < currentLine.length; i++)
    {
      boolean isLast = (i == currentLine.length - 1);
      String currentToken = currentLine[i];
      sb.append("\"");
      sb.append(currentToken);
      sb.append("\"");
      if (!isLast) { sb.append(", "); }
    }
    sb.append("}");
    return sb.toString();
  }

  /**
   * Get the value of textLookup
   *
   * @return the value of textLookup
   */
  public String[][] getTextLookup()
  {
    return textLookup;
  }

  /**
   * Set the value of textLookup
   *
   * @param textLookup new value of textLookup
   */
  public void setTextLookup(String[][] textLookup)
  {
    this.textLookup = textLookup;
  }
  protected List<Annotation> annotationList;

  /**
   * Get the value of annotationList
   *
   * @return the value of annotationList
   */
  public List<Annotation> getAnnotationList()
  {
    return annotationList;
  }

  /**
   * Set the value of annotationList
   *
   * @param annotationList new value of annotationList
   */
  public void setAnnotationList(List<Annotation> annotationList)
  {
    this.annotationList = annotationList;
  }

  public void loadScannerTermsFile(File scannerTermsFile)
  {
    try
    {
      FileReader fileReader = new FileReader(scannerTermsFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      for(String input = null; (input = bufferedReader.readLine()) != null; )
      {
        System.out.format(" - INPUT LINE: %s%n", input);
        String tokens[] = WHITESPACE_PATTERN.split(input);
        CueItem cueItem = new CueItem();

        for (String t : tokens)
        {
          cueItem.addToken(t);
        }
        cueItemList.add(cueItem);
      }

      bufferedReader.close();
      fileReader.close();
    } catch (IOException ex)
    {
      Logger.getLogger(CueListScanner.class.getName()).log(Level.SEVERE, String.format("problem reading scanner terms file \"%s\"", scannerTermsFile.getAbsolutePath()), ex);
      throw new RuntimeException(String.format("problem reading scanner terms file \"%s\""));
    }

  }

  private boolean compareForCueMatch(CueItem cueItem, String[] currentLine, int beginTokenOffset)
  {
    boolean matches = true;
    for (int i = 0; i < cueItem.getSize() && matches; i++)
    {
      String cueItemToken = cueItem.tokenAtPosition(i);
      String sourceToken = currentLine[i + beginTokenOffset];
      matches = cueItemToken.equals(sourceToken);
    }
    return matches;
  }

}
