package org.mitre.medfacts.i2b2.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.RelationAnnotation;
import org.mitre.medfacts.i2b2.annotation.RelationType;
import org.mitre.medfacts.i2b2.util.Location;

public class RelationFileProcessor extends FileProcessor
{

  public RelationFileProcessor()
  {
    super();
    setPatternString(ANNOTATION_FILE_REGEX_RELATION);
  }

  public static final String ANNOTATION_FILE_REGEX_RELATION = "^c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)\\|\\|r=\"(.*)\"\\|\\|c=\"(.*)\" (\\d+):(\\d+) (\\d+):(\\d+)$";
  @Override
  public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern)
  {
    System.out.format("RELATION PROCESSING: %s%n", currentLine);
    Matcher matcher = conceptPattern.matcher(currentLine);
    System.out.format("    matches? %b%n", matcher.matches());

    String conceptText = matcher.group(1);
    String beginLine = matcher.group(2);
    String beginCharacter = matcher.group(3);
    String endLine = matcher.group(4);
    String endCharacter = matcher.group(5);
    String relationTypeText = matcher.group(6);
    String otherConceptText = matcher.group(7);
    String otherBeginLine = matcher.group(8);
    String otherBeginCharacter = matcher.group(9);
    String otherEndLine = matcher.group(10);
    String otherEndCharacter = matcher.group(11);

    RelationAnnotation a = new RelationAnnotation();
    a.setConceptText(conceptText);
    a.setBegin(new Location(beginLine, beginCharacter));
    a.setEnd(new Location(endLine, endCharacter));
    a.setRelationType(RelationType.valueOf(relationTypeText.toUpperCase()));
    a.setOtherConceptText(otherConceptText);
    a.setOtherConceptBegin(new Location(otherBeginLine, otherBeginCharacter));
    a.setOtherConceptEnd(new Location(otherEndLine, otherEndCharacter));

    System.out.format("    RELATION ANNOTATION OBJECT: %s%n", a);
    System.out.format("    RELATION ANNOTATION OBJECT i2b2: %s%n", a.toI2B2String());
    return a;
  }

}
