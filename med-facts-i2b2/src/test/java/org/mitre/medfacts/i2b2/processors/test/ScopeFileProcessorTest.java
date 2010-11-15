package org.mitre.medfacts.i2b2.processors.test;

import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;

public class ScopeFileProcessorTest {

    public ScopeFileProcessorTest() {
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testSingleScopeLine()
    {
      String inputText = "c=\"\" 10:4 10:7||t=\"xcope\"||id=\"1\"";

      ScopeFileProcessor processor = new ScopeFileProcessor();

      Pattern pattern = Pattern.compile(processor.getPatternString());
      Annotation a = processor.processAnnotationLine(inputText, pattern);
    }

    @Test
    public void testSingleCueLine()
    {
      String inputText = "c=\"\" 10:4 10:4||t=\"cue\"||sub_t=\"negation\" ref=\"1\"";

      ScopeFileProcessor processor = new ScopeFileProcessor();

      Pattern pattern = Pattern.compile(processor.getPatternString());
      Annotation a = processor.processAnnotationLine(inputText, pattern);
    }

}