package org.mitre.medfacts.i2b2.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.CueAnnotation;
import org.mitre.medfacts.i2b2.annotation.CueSubType;
import org.mitre.medfacts.i2b2.annotation.ScopeAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeOrCueAnnotation;
import org.mitre.medfacts.i2b2.annotation.ScopeType;
import org.mitre.medfacts.i2b2.util.Location;

public class ScopeFileProcessor extends FileProcessor
{

  public ScopeFileProcessor()
  {
    super();
    setPatternString(ANNOTATION_FILE_REGEX_SCOPE);
  }

  public static final String ANNOTATION_FILE_REGEX_SCOPE = "^c=\"(.*)\" (-?\\d+):(-?\\d+) (-?\\d+):(-?\\d+)\\|\\|t=\"(.*)\"\\|\\|((id=\"(-?\\d+)\")|(sub_t=\"([a-z]+)\" ref=\"(-?\\d+)\"))$";
  @Override
  public Annotation processAnnotationLine(String currentLine, Pattern conceptPattern)
  {
//    System.out.format("SCOPE FILE PROCESSING: %s%n", currentLine);
    Matcher matcher = getPattern().matcher(currentLine);
    if (!matcher.matches())
    {
      System.err.format("ERROR!! currentline does not match scope pattern!  current line: %s%n", currentLine);
      throw new RuntimeException(String.format("ERROR!! currentline does not match scope pattern! current line %s", currentLine));
    }
//    System.out.format("    matches? %b%n", matcher.matches());

    String conceptText = matcher.group(1);
    String beginLine = matcher.group(2);
    String beginCharacter = matcher.group(3);
    String endLine = matcher.group(4);
    String endCharacter = matcher.group(5);
    String scopeTypeText = matcher.group(6);
    ScopeType scopeType = ScopeType.valueOf(scopeTypeText.toUpperCase());

    ScopeOrCueAnnotation annotation = null;

    switch (scopeType)
    {
      case XCOPE:
        String scopeIdString = matcher.group(9);
        int scopeId = Integer.parseInt(scopeIdString);

        ScopeAnnotation a1 = new ScopeAnnotation();
        a1.setScopeId(scopeId);

        annotation = a1;
        break;
      case CUE:
        String cueSubTypeString = matcher.group(11);
        CueSubType cueSubType = CueSubType.valueOf(cueSubTypeString.toUpperCase());
        String scopeRefString = matcher.group(12);
        int scopeRef = Integer.parseInt(scopeRefString);

        CueAnnotation a2 = new CueAnnotation();
        a2.setCueSubType(cueSubType);
        a2.setScopeIdReference(scopeRef);
        annotation = a2;
        break;
    }
    annotation.setConceptText(conceptText);
    annotation.setBegin(new Location(beginLine, beginCharacter));
    annotation.setEnd(new Location(endLine, endCharacter));
    annotation.setScopeType(scopeType);


//    System.out.format("    SCOPE ANNOTATION OBJECT: %s%n", annotation);
//    System.out.format("    SCOPE ANNOTATION OBJECT i2b2: %s%n", annotation.toI2B2String());
    return annotation;
  }

}
