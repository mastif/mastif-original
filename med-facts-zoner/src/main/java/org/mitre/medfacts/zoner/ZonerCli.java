package org.mitre.medfacts.zoner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * Hello world!
 *
 */
public class ZonerCli
{
    public static final String EOL = System.getProperty("line.separator");
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\w+");
    protected String inputFilename;
    protected List<SectionRegexDefinition> sectionRegexDefinitionList;
    protected List<Range> rangeList = new ArrayList<Range>();
    protected String entireContents;

    public ZonerCli()
    {
      try
      {
        String regexFilename = "org/mitre/medfacts/zoner/section_regex.xml";
        URI regexFileUri = this.getClass().getClassLoader().getResource(regexFilename).toURI();

        Document input = parseDocument(regexFileUri.toString());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression sectionExpression = xpath.compile("/sections/section");
        XPathExpression regexExpression = xpath.compile("./regex/text()");
        XPathExpression labelExpression = xpath.compile("./label/text()");

        sectionRegexDefinitionList = new ArrayList<SectionRegexDefinition>();

        NodeList sectionNodeList =
          (NodeList)
          sectionExpression.evaluate(input, XPathConstants.NODESET);
        for (int i=0; i < sectionNodeList.getLength(); i++)
        {
          Element sectionElement = (Element)sectionNodeList.item(i);
          System.out.println("found section element");

          String regexString = regexExpression.evaluate(sectionElement);
          String labelString = labelExpression.evaluate(sectionElement);
          System.out.format(" - section -- label: \"%s\"; regex: \"%s\"%n",
                  labelString, regexString);

          Pattern currentRegex = Pattern.compile(regexString, Pattern.CASE_INSENSITIVE);

          SectionRegexDefinition definition = new SectionRegexDefinition();
          definition.setLabel(labelString);
          definition.setRegex(currentRegex);
          sectionRegexDefinitionList.add(definition);
        }

    } catch (URISyntaxException ex)
    {
      String message = "problem (URISyntaxException) reading regex from xml file";
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } catch (XPathExpressionException ex)
    {
      String message = "problem (XPathExpressionException) reading regex from xml file";
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    }
  }

    public static Document parseDocument(String inputUri)
    {
      DOMImplementationRegistry registry;
      try
      {
        registry = DOMImplementationRegistry.newInstance();
      } catch (ClassNotFoundException ex)
      {
        Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
        throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
      } catch (InstantiationException ex)
      {
        Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
        throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
      } catch (IllegalAccessException ex)
      {
        Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
        throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
      } catch (ClassCastException ex)
      {
        Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
        throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
      }
      DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");

      LSParser parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
      DOMConfiguration domConfig = parser.getDomConfig();

      LSInput lsInput = impl.createLSInput();
      lsInput.setSystemId(inputUri);

      Document document = parser.parse(lsInput);
      return document;
    }

    public static void main( String[] args ) throws IOException
    {
        if (args.length != 1)
        {
          System.out.println("Usage:  " + ZonerCli.class.getName() + " <input file name>");
          return;
        }

        String inputFile = args[0];

        //Pattern filenamePattern = Pattern.compile("(([a-z,A-Z]:\\)?[");

        ZonerCli zonerCli = new ZonerCli();
        zonerCli.setInputFilename(inputFile);
        zonerCli.readFile(inputFile);
        zonerCli.execute();
    }

    public void execute() throws IOException
    {
        // First pass through the input: formatting adjustments
//        String outputFile = inputFilename + ".out1";
//        String outputFile2 = inputFilename + ".out2";

//        passZero(inputFilename, outputFile);
//        passOneB(outputFile, outputFile2);
        findHeadings();
    }


  /**
   * @return the inputFilename
   */
  public String getInputFilename()
  {
    return inputFilename;
  }

  public void readFile(String inputFilename) throws IOException, FileNotFoundException
  {
    System.out.format("input: %s%n", inputFilename);
    File inputFile = new File(inputFilename);
    FileReader inputFileReader = new FileReader(inputFile);
    BufferedReader inputBufferedReader = new BufferedReader(inputFileReader);
    StringWriter stringWriter = new StringWriter();
    PrintWriter stringPrinter = new PrintWriter(stringWriter);
    for (String currentInputLine = inputBufferedReader.readLine(); currentInputLine != null; currentInputLine = inputBufferedReader.readLine())
    {
      stringPrinter.println(currentInputLine);
      //System.out.format("  ONE LINE: %s%n", currentInputLine);
    }
    inputBufferedReader.close();
    inputFileReader.close();
    String wholeThing = stringWriter.toString();
    //System.out.format("### THE WHOLE THING - BEGIN ###%n%s%n### THE WHOLE THING - END ###%n%n", wholeThing);
    setEntireContents(wholeThing);
  }

  /**
   * @param inputFilename the inputFilename to set
   */
  public void setInputFilename(String inputFilename)
  {
    this.inputFilename = inputFilename;
  }

  public String buildString(String[] allLines)
  {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i < allLines.length; i++)
    {
      boolean isLast = (i == allLines.length - 1);
      String current = allLines[i];
      sb.append(current);
      if (!isLast)
      {
        sb.append(EOL);
      }
    }
    String returnValue = sb.toString();
    return returnValue;
  }
  
    public void findHeadings()
    {


      //System.out.format("### THE WHOLE THING - BEGIN ###%n%s%n### THE WHOLE THING - END ###%n%n", wholeThing);

//      Pattern headerUpmcDeidProgressNotePattern =
//          Pattern.compile("^(?:PROGRESS\\s*)?\\*+INSTITUTION(\\s+(?:GENERAL|CRITICAL\\s+CARE)\\s+MEDICINE\\s+ATTENDING\\s+PHYSICIAN\\s+PROGRESS\\s+NOTE)?", Pattern.MULTILINE);
//      Matcher headerUpmcDeidProgressNoteMatcher =
//          headerUpmcDeidProgressNotePattern.matcher(wholeThing);
//      if ()

//      String allLines[] = entireContents.split("\\r?\\n");

//      Pattern admissionDatePattern = Pattern.compile("((?:admission\\s+date)|(?:date\\s+of\\s+admission))", Pattern.CASE_INSENSITIVE);
//      Pattern dischargeDatePattern = Pattern.compile("((?:discharge\\s+date)|(?:date\\s+of\\s+discharge))", Pattern.CASE_INSENSITIVE);
//
//      Matcher admissionDateMatcher = admissionDatePattern.matcher(wholeThing);

      for (SectionRegexDefinition currentDefinition: sectionRegexDefinitionList)
      {
        Matcher currentMatcher = currentDefinition.regex.matcher(getEntireContents());
        while (currentMatcher.find())
        {
          System.out.format(" trying %s ...%n", currentDefinition.getLabel());
          int start = currentMatcher.start();
          int end = currentMatcher.end();
          System.out.format(" ** " + currentDefinition.getLabel() + " match found: %d-%d%n", start, end);

          Range currentRange = new Range();
          currentRange.setLabel(currentDefinition.getLabel());
          currentRange.setBegin(start);
          currentRange.setEnd(end);
          getRangeList().add(currentRange);
        }
      }

      Collections.sort(getRangeList());

      for (Range currentRange : getRangeList())
      {
        System.out.format(" - %s%n", currentRange);
      }
      System.out.println("===");

      int rangeListSize = getRangeList().size();
      for (int i=0; i < rangeListSize; i++)
      {
        boolean isLast = (i == rangeListSize - 1);
        Range currentRange = getRangeList().get(i);
        int begin = currentRange.getBegin();
        int end = currentRange.getEnd();
        int oneBeforeNextRange = 0;
        if (!isLast)
        {
          Range nextRange = getRangeList().get(i+1);
          int nextRangeBegin = nextRange.getBegin();
          //oneBeforeNextRange = nextRangeBegin - 1;

          // todo is (nextRangeBegin - 2) the right place to start searching for the previous token?
          // it makes sense, because if the previous char is whitespace (which
          // it would have to be), you'd want to go back atleast two chars
          oneBeforeNextRange = findLastCharOffsetOfPreviousWord(entireContents, nextRangeBegin - 2);
        } else
        {
          oneBeforeNextRange = getEntireContents().length() - 1;
        }
        int realSectionEnd = oneBeforeNextRange;

        CharacterOffsetToLineTokenConverter c =
          new CharacterOffsetToLineTokenConverter(getEntireContents());
        LineAndTokenPosition beginLineAndTokenPosition = c.convert(begin);
        LineAndTokenPosition endLineAndTokenPosition = c.convert(realSectionEnd);

        System.out.format(" - %s (%d) %s to %s %n", currentRange, realSectionEnd,
                beginLineAndTokenPosition.toString(), endLineAndTokenPosition.toString());

        currentRange.setEnd(oneBeforeNextRange);
        currentRange.setBeginLineAndToken(beginLineAndTokenPosition);
        currentRange.setEndLineAndToken(endLineAndTokenPosition);
        //SectionAnnotation a = new SectionAnnotation();

      }

//      while (admissionDateMatcher.find())
//      {
//        int start = admissionDateMatcher.start();
//        int end = admissionDateMatcher.end();
//        System.out.format("admission date match found: %d-%d%n", start, end);
//      }
//
    }

    public String getEntireContents()
    {
      return entireContents;
    }

    public void setEntireContents(String entireContents)
    {
      this.entireContents = entireContents;
    }

    /**
     * @return the rangeList
     */
    public List<Range> getRangeList()
    {
      return rangeList;
    }

    /**
     * @param rangeList the rangeList to set
     */
    public void setRangeList(List<Range> rangeList)
    {
      this.rangeList = rangeList;
    }

    private int findLastCharOffsetOfPreviousWord(String entireContents, int initialPosition)
    {
      boolean found = false;
      int i = initialPosition;
      while (!found && i >= 0)
      {
        char currentChar = entireContents.charAt(i);
        if (i != ' ' && i != '\r' && i != '\n')
        {
          found = true;
        }
      }
      if (i < 0) { i = 0; }
      return i;
    }

    public class Range implements Comparable<Range>
    {
      public Range()
      {
      }

      protected int begin;
      protected int end;
      protected LineAndTokenPosition beginLineAndToken;
      protected LineAndTokenPosition endLineAndToken;
      protected String label;

      public String toString()
      {
        return String.format("RANGE \"%s\" [%d-%d]", label, begin, end);
      }

      public int getBegin()
      {
        return begin;
      }

      public void setBegin(int begin)
      {
        this.begin = begin;
      }

      public int getEnd()
      {
        return end;
      }

      public void setEnd(int end)
      {
        this.end = end;
      }

      @Override
      public int compareTo(Range other)
      {
        if (this.begin < other.begin)
        {
          return -1;
        } else if (this.begin > other.begin)
        {
          return 1;
        } else if (this.end == other.end)
        {
          return 0;
        } else if (this.end < other.end)
        {
          return -1;
        } else
        {
          return 1;
        }
      }

      /**
       * @return the label
       */
      public String getLabel()
      {
        return label;
      }

      /**
       * @param label the label to set
       */
      public void setLabel(String label)
      {
        this.label = label;
      }

      public LineAndTokenPosition getBeginLineAndToken()
      {
        return beginLineAndToken;
      }

      public void setBeginLineAndToken(LineAndTokenPosition beginLineAndToken)
      {
        this.beginLineAndToken = beginLineAndToken;
      }

      public LineAndTokenPosition getEndLineAndToken()
      {
        return endLineAndToken;
      }

      public void setEndLineAndToken(LineAndTokenPosition endLineAndToken)
      {
        this.endLineAndToken = endLineAndToken;
      }
    }

    public class SectionRegexDefinition
    {

      protected Pattern regex;
      protected String label;

      public Pattern getRegex()
      {
        return regex;
      }

      public void setRegex(Pattern regex)
      {
        this.regex = regex;
      }

      public String getLabel()
      {
        return label;
      }

      public void setLabel(String label)
      {
        this.label = label;
      }

    }

}
