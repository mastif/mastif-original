package org.mitre.medfacts.i2b2.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.ConceptAnnotation;
import org.mitre.medfacts.i2b2.annotation.ConceptType;
import org.mitre.medfacts.i2b2.util.Location;

public class ConceptFileProcessor extends FileProcessor
{

  public ConceptFileProcessor()
  {
    super();
    setPatternString(ANNOTATION_FILE_REGEX_CONCEPT);
  }

  public static final String ANNOTATION_FILE_REGEX_CONCEPT = "^c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|t=\"(.*)\"$";
  @Override
  public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern)
  {
    System.out.format("CONCEPT PROCESSING: %s%n", currentLine);
    Matcher matcher = conceptPattern.matcher(currentLine);
    System.out.format("    matches? %b%n", matcher.matches());
    String conceptText = matcher.group(1);
    String beginLine = matcher.group(2);
    String beginCharacter = matcher.group(3);
    String endLine = matcher.group(4);
    String endCharacter = matcher.group(5);
    String conceptTypeText = matcher.group(6);
    System.out.format("    concept text: %s%n", conceptText);
    System.out.format("    concept type text: %s%n", conceptTypeText);
    ConceptAnnotation a = new ConceptAnnotation();
    a.setConceptText(conceptText);
    a.setBegin(new Location(beginLine, beginCharacter));
    a.setEnd(new Location(endLine, endCharacter));
    a.setConceptType(ConceptType.valueOf(conceptTypeText.toUpperCase()));
    System.out.format("    CONCEPT ANNOTATION OBJECT: %s%n", a);
    System.out.format("    CONCEPT ANNOTATION OBJECT i2b2: %s%n", a.toI2B2String());
    return a;
  }

}
