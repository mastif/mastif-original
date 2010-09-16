/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.util.Location;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;
/**
 *
 * @author WELLNER
 */
public class ObjectRunner<ConceptTypeParam> extends AbstractRunner
{

    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public class AssertionStatusPair {
        ConceptTypeParam concept;
        String assertionStatus;
        public AssertionStatusPair(ConceptTypeParam c, String aS) {
            concept = c;
            assertionStatus = aS;
        }
        public String getAssertionStatus() {
            return assertionStatus;
        }

        public ConceptTypeParam getConcept() {
            return concept;
        }
    }

    private HashSet<AssertionStatusPair> assertionStatusPairs = new HashSet<AssertionStatusPair>();

    private List<ConceptAnnotation> conceptAnnotationList =
        new ArrayList<ConceptAnnotation>();


    private String textLookup[][];
    private String wholeText;

    public void setDocument(java.io.File f) {
        try {
            Reader reader = new FileReader(f);
            processText(reader);
        } catch (Exception e) {}
    }

    public void setDocument(String docStr) {
        processText(new StringReader(docStr));
    }

    private void processText(Reader reader) {
        String eol = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(reader);

        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);

        String currentLine = null;
        StringBuilder builder = new StringBuilder();
        ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
        int lineNumber = 0;
        try {
            while ((currentLine = br.readLine()) != null)
          {
            printer.println(currentLine);
            builder.append(currentLine);
            builder.append(eol);

            String tokenArray[] = WHITESPACE_PATTERN.split(currentLine);
            textLookupTemp.add(tokenArray);

            lineNumber++;
          }

          br.close();
          reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Read error " + e.toString());
        }

        System.out.println("=====");

        String twoDimensionalStringArray[][] = new String[1][];
        //String textLookup[][] = null;
        textLookup = textLookupTemp.toArray(twoDimensionalStringArray);
        wholeText = builder.toString();
    }

    public void addConceptAnnotation(int startCharOffset, int endCharOffset, /*String type,*/ ConceptTypeParam conceptType) {
      CharacterOffsetToLineTokenConverter converter = new CharacterOffsetToLineTokenConverter(wholeText);

      LineAndTokenPosition startLineAndTokenPosition = converter.convert(startCharOffset);
      LineAndTokenPosition endLineAndTokenPosition = converter.convert(endCharOffset);

      ConceptAnnotation conceptAnnotation = new ConceptAnnotation();

      Location beginLocation = new Location();
      beginLocation.setLine(startLineAndTokenPosition.getLine());
      beginLocation.setTokenOffset(startLineAndTokenPosition.getTokenOffset());
      conceptAnnotation.setBegin(beginLocation);

      Location endLocation = new Location();
      endLocation.setLine(endLineAndTokenPosition.getLine());
      endLocation.setTokenOffset(endLineAndTokenPosition.getTokenOffset());
      conceptAnnotation.setEnd(endLocation);

      String conceptText = wholeText.substring(startCharOffset, endCharOffset+1);
      conceptAnnotation.setConceptText(conceptText);

      // todo: what should be used as the parameter to setConceptType???
      conceptAnnotation.setConceptType(ConceptType.valueOf(conceptType.toString()));

      conceptAnnotationList.add(conceptAnnotation);
    }

    public ArrayList<AssertionStatusPair> getAssertions() {
        return new ArrayList(assertionStatusPairs);
    }

    @Override
    public String getTextFilename()
    {
      throw new UnsupportedOperationException("Not supported on object api (no files).");
    }

    @Override
    public void setTextFilename(String textFilename)
    {
      throw new UnsupportedOperationException("Not supported on object api (no files).");
    }

}
