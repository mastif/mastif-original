package org.mitre.medfacts.i2b2.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.util.Location;

public class AssertionFileProcessor extends FileProcessor
{

  public AssertionFileProcessor()
  {
    super();
    setPatternString(ANNOTATION_FILE_REGEX_ASSERTION);
  }

  public static final String ANNOTATION_FILE_REGEX_ASSERTION = "^c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|t=\"(.*)\"\\|\\|a=\"(.*)\"$";
  @Override
  public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern)
  {
//    System.out.format("ASSERTION  PROCESSING: %s%n", currentLine);
    Matcher matcher = getPattern().matcher(currentLine);
    if (!matcher.matches())
    {
      System.err.format("ERROR!! current assertion line does not match concept pattern!  current line: %s%n", currentLine);
      throw new RuntimeException(String.format("ERROR!! currentline does not match assertion pattern! current line %s", currentLine));
    }
//    System.out.format("    matches? %b%n", matcher.matches());
    String conceptText = matcher.group(1);
    String beginLine = matcher.group(2);
    String beginCharacter = matcher.group(3);
    String endLine = matcher.group(4);
    String endCharacter = matcher.group(5);
    String conceptTypeText = matcher.group(6);
    String assertionValue = matcher.group(7);
//    System.out.format("    concept text: %s%n", conceptText);
//    System.out.format("    concept type text: %s%n", conceptTypeText);
    AssertionAnnotation a = new AssertionAnnotation();
    a.setConceptText(conceptText);
    a.setBegin(new Location(beginLine, beginCharacter));
    a.setEnd(new Location(endLine, endCharacter));
    a.setConceptType(ConceptType.valueOf(conceptTypeText.toUpperCase()));
    a.setAssertionValue(AssertionValue.valueOf(assertionValue.toUpperCase()));
//    System.out.format("    ASSERTION ANNOTATION OBJECT: %s%n", a);
//    System.out.format("    ASSERTION ANNOTATION OBJECT i2b2: %s%n", a.toI2B2String());
    return a;
  }

}
