/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.scanners.test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mitre.medfacts.i2b2.scanners.CueListScanner;

/**
 *
 * @author MCOARR
 */
public class CueListScannerTest {

    public CueListScannerTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

     @Test
     public void testCueScannerNegation() throws URISyntaxException
     {
       String textLookup[][] =
       {
         { "The", "patient", "is", "to", "call", "787-984-9608", "if", "any", "fevers", ",", "chills", ",", "nausea", "and", "vomiting", ",", "abdominal", "pain", ",", "inability", "to", "take", "medications", ",", "inability", "to", "eat", "or", "drink", "." },
         { "Given", "his", "occasional", "sinus", "bradycardia", "his", "Atenolol", "was", "not", "advanced", "but", "instead", "his", "Zestril", "dose", "was", "increased", "from", "10-20", "mg", "bid", "and", "his", "blood", "pressure", "responded", "appropriately", "." },
         { "The", "differential", "diagnosis", "includes", "mesenteric", "ischemia", ",", "a", "mesenteric", "venous", "thrombosis", ",", "or", "possible", "reperfusion", "syndrome", "." }
       };

       ClassLoader classLoader = getClass().getClassLoader();
       URL negationCueFileUrl = classLoader.getResource("org/mitre/medfacts/i2b2/cuefiles/updated_negation_cue_list.txt");
       System.out.format("negation cue list url: %s%n", negationCueFileUrl);
       URI negationCueFileUri = negationCueFileUrl.toURI();
       System.out.format("negation cue list uri: %s%n", negationCueFileUri);
       File negationCueFile = new File(negationCueFileUri);
       System.out.format("negation cue list url: %s%n", negationCueFile);

       CueListScanner scanner = new CueListScanner(negationCueFile);
       scanner.setTextLookup(textLookup);
       scanner.execute();
       List<Annotation> annotationList = scanner.getAnnotationList();
       System.out.println("ANNOTATIONS:");
       if (annotationList == null)
       {
         System.out.println("no annotations returned.");
       } else
       for (Annotation currentAnnotation : annotationList)
       {
         System.out.format(" - annotation: %s%n", currentAnnotation);
       }
       System.out.println("END OF ANNOTATIONS.");
     }

     @Test
     public void testCueScannerSpeculation() throws URISyntaxException
     {
       String textLookup[][] =
       {
         { "The", "patient", "is", "to", "call", "787-984-9608", "if", "any", "fevers", ",", "chills", ",", "nausea", "and", "vomiting", ",", "abdominal", "pain", ",", "inability", "to", "take", "medications", ",", "inability", "to", "eat", "or", "drink", "." },
         { "Given", "his", "occasional", "sinus", "bradycardia", "his", "Atenolol", "was", "not", "advanced", "but", "instead", "his", "Zestril", "dose", "was", "increased", "from", "10-20", "mg", "bid", "and", "his", "blood", "pressure", "responded", "appropriately", "." },
         { "The", "differential", "diagnosis", "includes", "mesenteric", "ischemia", ",", "a", "mesenteric", "venous", "thrombosis", ",", "or", "possible", "reperfusion", "syndrome", "." }
       };

       ClassLoader classLoader = getClass().getClassLoader();
       URL negationCueFileUrl = classLoader.getResource("org/mitre/medfacts/i2b2/cuefiles/updated_speculation_cue_list.txt");
       System.out.format("negation cue list url: %s%n", negationCueFileUrl);
       URI negationCueFileUri = negationCueFileUrl.toURI();
       System.out.format("negation cue list uri: %s%n", negationCueFileUri);
       File negationCueFile = new File(negationCueFileUri);
       System.out.format("negation cue list url: %s%n", negationCueFile);

       CueListScanner scanner = new CueListScanner(negationCueFile);
       scanner.setTextLookup(textLookup);
       scanner.execute();
       List<Annotation> annotationList = scanner.getAnnotationList();
       System.out.println("ANNOTATIONS:");
       if (annotationList == null)
       {
         System.out.println("no annotations returned.");
       } else
       for (Annotation currentAnnotation : annotationList)
       {
         System.out.format(" - annotation: %s%n", currentAnnotation);
       }
       System.out.println("END OF ANNOTATIONS.");
     }
}