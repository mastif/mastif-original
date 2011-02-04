/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.scanners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueWordAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueWordType;
import org.mitre.medfacts.i2b2.util.Location;

/**
 *
 * @author MCOARR
 */
public class CueListScanner
{
  private static final Logger logger = Logger.getLogger(CueListScanner.class.getName());
  public static Pattern SINGLE_TAB_PATTERN = Pattern.compile("\\t");
  public static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  protected CueWordType cueWordType;

  public CueListScanner(BufferedReader scannerTermsReader, CueWordType cueWordType)
  {
    loadScannerTermsFromReader(scannerTermsReader);
    this.cueWordType = cueWordType;
  }

  public CueListScanner(File scannerTermsFile, CueWordType cueWordType)
  {
    loadScannerTermsFile(scannerTermsFile);
    this.cueWordType = cueWordType;
  }

  protected String[][] textLookup;
  protected List<CueItem> cueItemList = new ArrayList<CueItem>();
  protected List<Annotation> annotationList = new ArrayList<Annotation>();

  public void execute()
  {
    for (CueItem cueItem : cueItemList)
    {
      int size = cueItem.getSize();
      for (int lineNumberOffset=0; lineNumberOffset < textLookup.length; lineNumberOffset++)
      {
        String currentLine[] = textLookup[lineNumberOffset];
        for (int tokenOffset=0; tokenOffset + size - 1 < currentLine.length; tokenOffset++)
        {
          boolean matched = compareForCueMatch(cueItem, currentLine, tokenOffset);
          if (matched)
          {
            logger.finest(String.format("MATCHES!!!%n  BEGIN MATCH%n  cue: %s%n  inputLine: %s%n  position: %d%n  END MATCH%n%n", cueItem.toString(), convertLineToString(currentLine), tokenOffset));
            CueWordAnnotation a = new CueWordAnnotation();
            int beginOffset = tokenOffset;
            int endOffset = tokenOffset + size - 1;
            Location beginLocation = new Location();
            beginLocation.setLine(lineNumberOffset + 1);
            beginLocation.setTokenOffset(beginOffset);
            Location endLocation = new Location();
            endLocation.setLine(lineNumberOffset + 1);
            endLocation.setTokenOffset(endOffset);
            a.setBegin(beginLocation);
            a.setEnd(endLocation);
            a.setCueWordType(getCueWordType());
            a.setCueWordClass(cueItem.getCueWordClass());
            a.setCueWordText(constructWordSequenceText(currentLine, beginOffset, endOffset));
            annotationList.add(a);
          }
        }

        // generate alternative sentences for the current line with any
        // combinationthe of the PROBLEM/TREATMENT/TEST annotations substituted
        // out to create new sentences
//        List<Annotation> annotationsForCurrentLine =
//            findAnnotationsForCurrentLine(annotationList, lineNumberOffset + 1);
//
//        String alternativeLines[][] = generateAlternatives(currentLine, annotationsForCurrentLine);

      }
    }
  }

  public String constructWordSequenceText(String line[], int begin, int endOffset)
  {
    StringBuilder sb = new StringBuilder();
    int last = Math.min(line.length - 1, endOffset);
    for (int i = begin; i <= last; i++)
    {
      boolean isLast = (i == line.length - 1);
      String currentToken = line[i];
      sb.append(currentToken);
      if (!isLast) { sb.append(" "); }
    }
    return sb.toString();
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

  public final void loadScannerTermsFile(File scannerTermsFile)
  {
    try
    {
      FileReader fileReader = new FileReader(scannerTermsFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      loadScannerTermsFromReader(bufferedReader);

      bufferedReader.close();
      fileReader.close();
    } catch (IOException ex)
    {
      Logger.getLogger(CueListScanner.class.getName()).log(Level.SEVERE, String.format("problem reading scanner terms file \"%s\"", scannerTermsFile.getAbsolutePath()), ex);
      throw new RuntimeException(String.format("problem reading scanner terms file \"%s\""));
    }

  }

  public final void loadScannerTermsFromReader(BufferedReader bufferedReader)
  {
    try
    {
      for(String input = null; (input = bufferedReader.readLine()) != null; )
      {
        String tabSeparatedInput[] = SINGLE_TAB_PATTERN.split(input);
        String cueWordClass = null;
        if (tabSeparatedInput.length == 0)
        {
          throw new RuntimeException("cue terms file should have one or two tab separated items on each line, but had zero");
        } else if (tabSeparatedInput.length == 1)
        {
          input = tabSeparatedInput[0];
        } else if (tabSeparatedInput.length == 2)
        {
          input = tabSeparatedInput[0];
          cueWordClass = tabSeparatedInput[1];
        } else if (tabSeparatedInput.length > 2)
        {
          throw new RuntimeException("cue terms file should have one or two tab separated items on each line, but had more than two");
        }
        //System.out.format(" - INPUT LINE: %s%n", input);
        String tokens[] = WHITESPACE_PATTERN.split(input);
        CueItem cueItem = new CueItem();
        cueItem.setCueWordClass(cueWordClass);

        for (String t : tokens)
        {
          cueItem.addToken(t);
        }
        cueItemList.add(cueItem);
      }
    } catch (IOException ex)
    {
      Logger.getLogger(CueListScanner.class.getName()).log(Level.SEVERE, String.format("problem reading scanner terms from buffered reader"));
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
      matches = cueItemToken.equalsIgnoreCase(sourceToken);
    }
    return matches;
  }

  /**
   * @return the cueWordType
   */
  public CueWordType getCueWordType()
  {
    return cueWordType;
  }

  /**
   * @param cueWordType the cueWordType to set
   */
  public void setCueWordType(CueWordType cueWordType)
  {
    this.cueWordType = cueWordType;
  }

  public List<Annotation> findAnnotationsForCurrentLine(List<Annotation> allAnnotationList, int lineNumber)
  {
    List<Annotation> filteredAnnotationList = new ArrayList<Annotation>();
    for (Annotation candidateAnnotation : allAnnotationList)
    {
      if (candidateAnnotation.getBegin().getLine() == lineNumber)
      {
        filteredAnnotationList.add(candidateAnnotation);
      }
    }
    return filteredAnnotationList;
  }

}
