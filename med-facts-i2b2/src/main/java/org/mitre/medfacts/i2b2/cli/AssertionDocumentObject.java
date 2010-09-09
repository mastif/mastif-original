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
/**
 *
 * @author WELLNER
 */
public class AssertionDocumentObject<ConceptType> {

    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    public class AssertionStatusPair {
        ConceptType concept;
        String assertionStatus;
        public AssertionStatusPair(ConceptType c, String aS) {
            concept = c;
            assertionStatus = aS;
        }
        public String getAssertionStatus() {
            return assertionStatus;
        }

        public ConceptType getConcept() {
            return concept;
        }
    }

    private HashSet<AssertionStatusPair> assertionStatusPairs = new HashSet<AssertionStatusPair>();


    private String textLookup[][];

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
        BufferedReader br = new BufferedReader(reader);

        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);

        String currentLine = null;
        ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
        int lineNumber = 0;
        try {
            while ((currentLine = br.readLine()) != null)
          {
            printer.println(currentLine);
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
    }

    public void addConceptAnnotation(int startCharOffset, int endCharOffset, String typ, ConceptType concept) {

    }

    public ArrayList<AssertionStatusPair> getAssertions() {
        return new ArrayList(assertionStatusPairs);
    }
}
