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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;
import org.xml.sax.InputSource;

/**
 * Hello world!
 *
 */
public class ZonerCli {

  private static final Logger logger = Logger.getLogger(ZonerCli.class.getName());
  public static final String EOL = System.getProperty("line.separator");
  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
  protected String inputFilename;
  protected List<SectionRegexDefinition> sectionRegexDefinitionList;
  protected Map<String,Node> fragmentMap;
  // This will include all the Ranges, including those we will eventuall mark
  // isIgnore because of overlaps
  protected List<Range> fullRangeList = new ArrayList<Range>();
  // This will be the trimmed down list of Ranges, excluding all the
  // overlapping isIgnore Ranges
  protected List<Range> rangeList = new ArrayList<Range>();
  protected List<Range> fullRangeListAdjusted = new ArrayList<Range>();
  protected List<HeadingRange> headings = new ArrayList<HeadingRange>();
  protected String entireContents;
  public static final int expansionThreshold = 5;

  public ZonerCli() {
    try {
      String regexFilename = "org/mitre/medfacts/zoner/section_regex.xml";
      URI regexFileUri = this.getClass().getClassLoader().getResource(regexFilename).toURI();

      Document input = parseDocument(regexFileUri.toString());
    
      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression sectionExpression = xpath.compile("/root/sections/section");
      XPathExpression regexExpression = xpath.compile("./regex");
      XPathExpression regexIgnoreCaseExpression = xpath.compile("./regex/@ignore-case");
      XPathExpression regexFindAllExpression = xpath.compile("./regex/@find-all");
      XPathExpression labelExpression = xpath.compile("./label/text()");
      XPathExpression fragmentExpression = xpath.compile("/root/fragments/fragment");
      XPathExpression fragmentNameExpression = xpath.compile("./name/text()");
      XPathExpression fragmentExpansionExpression = xpath.compile("./expansion/text()");
      XPathExpression fragmentExpansionNode = xpath.compile("./expansion");
      XPathExpression embeddedFragmentExpression = xpath.compile("./fragment-ref");
      XPathExpression embeddedFragmentName = xpath.compile("./@name");


// get all the fragment elements out of the xml file and has name/expansion pairs
      fragmentMap = new LinkedHashMap<String,Node>();
      NodeList fragmentNodeList =
                (NodeList) fragmentExpression.evaluate(input, XPathConstants.NODESET);
      for (int i=0; i<fragmentNodeList.getLength(); i++) {
        Element fragmentElement = (Element) fragmentNodeList.item(i);
        String nameString = fragmentNameExpression.evaluate(fragmentElement);
        String expansionString = fragmentExpansionExpression.evaluate(fragmentElement);
        Node expansionNode = (Node)fragmentExpansionNode.evaluate(fragmentElement, XPathConstants.NODE);
        fragmentMap.put(nameString, expansionNode);
        logger.log(Level.FINEST, "found fragment: {0} -> {1}",
                   new Object[]{nameString, nodeToString(expansionNode)});
      }

// get all the section (regular expression) elements from the xml file,
//        and expand as needed and create a regex Pattern for each     
      
      sectionRegexDefinitionList = new ArrayList<SectionRegexDefinition>();
      NodeList sectionNodeList =
                (NodeList) sectionExpression.evaluate(input, XPathConstants.NODESET);
      for (int i = 0; i < sectionNodeList.getLength(); i++) {
        Element sectionElement = (Element) sectionNodeList.item(i);
        // logger.finest("found section element");

        Node regexNode = 
                 (Node) regexExpression.evaluate(sectionElement, XPathConstants.NODE);
        String regexString = expandFragments(regexNode, embeddedFragmentExpression,
                embeddedFragmentName);
        // if the fragment nesting is too deep, expandFragments will return null
        // in that case, skip this regex
        if (regexString == null)
          continue;

        String regexIgnoreCaseString = regexIgnoreCaseExpression.evaluate(sectionElement);
        if (regexIgnoreCaseString == null || regexIgnoreCaseString.isEmpty()) {
          regexIgnoreCaseString = "true";
        }
        boolean regexIgnoreCase = regexIgnoreCaseString.equalsIgnoreCase("true");
        String regexFindAllString = regexFindAllExpression.evaluate(sectionElement);
        if (regexFindAllString == null || regexFindAllString.isEmpty()) {
          regexFindAllString = "true";
        }
        boolean regexFindAll = regexFindAllString.equalsIgnoreCase("true");
        String labelString = labelExpression.evaluate(sectionElement);
        logger.finest(String.format(" - section -- label: \"%s\"; regex: \"%s\"; ignore case: \"%s\"; match all: \"%s\"",
                labelString, regexString, regexIgnoreCaseString, regexFindAllString));

        int flags = 0;
        if (regexIgnoreCase) {
          flags += Pattern.CASE_INSENSITIVE;
        }
        flags += Pattern.MULTILINE;

        Pattern currentRegex = Pattern.compile(regexString, flags);

        SectionRegexDefinition definition = new SectionRegexDefinition();
        definition.setLabel(labelString);
        definition.setRegex(currentRegex);
        definition.setFindAll(regexFindAll);
        sectionRegexDefinitionList.add(definition);
      }

    } catch (URISyntaxException ex) {
      String message = "problem (URISyntaxException) reading regex from xml file";
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } catch (XPathExpressionException ex) {
      String message = "problem (XPathExpressionException) reading regex from xml file";
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    }
  }

  public static Document parseDocument(String inputUri) {
    DOMImplementationRegistry registry;
    try {
      registry = DOMImplementationRegistry.newInstance();
    } catch (ClassNotFoundException ex) {
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
      throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
    } catch (InstantiationException ex) {
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
      throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, "problem before attempting to parse xml (registry problem)", ex);
      throw new RuntimeException("problem before attempting to parse xml (registry problem)", ex);
    } catch (ClassCastException ex) {
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

  private String expandFragments(Node regexNode, XPathExpression embeddedFragmentExpression,
          XPathExpression embeddedFragmentName){
    int levels = 0;
    Element parentRegexElement = (Element) regexNode;
    Document ownerDocument = parentRegexElement.getOwnerDocument();
    //logger.log(Level.FINEST, "expandFragments on textContent: {0}", parentRegexElement.getTextContent());
    logger.log(Level.FINEST, "expandFragments on Node: {0}", nodeToString(regexNode));

   /*** old way 
    try {
      NodeList fragmentList =
              (NodeList) embeddedFragmentExpression.evaluate(regexNode, XPathConstants.NODESET);
      while (fragmentList.getLength() > 0) {
        for (int i = 0; i < fragmentList.getLength(); i++) {
          Element fragmentRefElement = (Element) fragmentList.item(i);
          String fragName = embeddedFragmentName.evaluate(fragmentRefElement);
          Node fragExpansion = fragmentMap.get(fragName);
          // todo only do this once?
          //Element parentRegexElement = (Element)fragmentRefElement.getParentNode();
          // todo only do this once?
          //Document ownerDocument = fragmentRefElement.getOwnerDocument();
          Text replacementTextNode = ownerDocument.createTextNode(fragExpansion.getTextContent());
          parentRegexElement.replaceChild(replacementTextNode, fragmentRefElement);
        }
        // now that one level of fragments has been replaced, increment levels counter
        // and check element for any new fragment references that may have been added
        levels++;
        fragmentList =
                (NodeList) embeddedFragmentExpression.evaluate(parentRegexElement,
                XPathConstants.NODESET);
      } *****/
    try {
      NodeList fragmentList =
              (NodeList) embeddedFragmentExpression.evaluate(regexNode, XPathConstants.NODESET);
      while (levels < expansionThreshold && fragmentList.getLength() > 0) {
        for (int i = 0; i < fragmentList.getLength(); i++) {
          Element fragmentRefElement = (Element) fragmentList.item(i);
          String fragName = embeddedFragmentName.evaluate(fragmentRefElement);
          Node fragExpansionNode = fragmentMap.get(fragName);
          // replace the fragmentRef with its expansion
          Node parentNode = fragmentRefElement.getParentNode();
          parentNode.replaceChild(fragExpansionNode, fragmentRefElement);
          logger.log(Level.FINEST, "Level {0} fragment {1} expansion: {2}", 
                  new Object[]{levels, i, nodeToString((Node)parentRegexElement)});
        }
        // now that we've handled all the fragments, increment levels and get a 
        // new fragment list from fragments that were in the replacement nodes
        levels++;
        logger.log(Level.FINEST, "checking for any level {0} embedded fragments in {1}", 
                new Object[]{levels, nodeToString((Node)parentRegexElement)});
        // deepen the xpath search expression
        StringBuffer nestedFragmentBuf = new StringBuffer("./");
        for (int j=0; j<levels; j++) {
          nestedFragmentBuf.append("expansion/");
        }
        nestedFragmentBuf.append("fragment-ref");
        // todo not happy about doing this again in here
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression nestedFragmentExpression = xpath.compile(nestedFragmentBuf.toString());

        fragmentList =
                (NodeList) nestedFragmentExpression.evaluate(parentRegexElement,
                XPathConstants.NODESET);
        logger.log(Level.FINEST, "found {0} embedded fragments", fragmentList.getLength());
      }
    } catch (XPathExpressionException ex) {
      String message = "problem (XPathExpressionException) expanding regex fragment";
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    }


    if (levels == expansionThreshold) {
      // nesting of fragments is too deep
      return null;
    }

    logger.log(Level.FINEST, "\texpanded to {0}", parentRegexElement.getTextContent());
    return parentRegexElement.getTextContent();
  }

  private static String nodeToString(Node node) {
    TransformerFactory transFactory = TransformerFactory.newInstance();
    Transformer transformer;
    String str=null;
    try {
      transformer = transFactory.newTransformer();
      StringWriter buffer = new StringWriter();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.transform(new DOMSource(node), new StreamResult(buffer));
      str = buffer.toString();
    } catch (TransformerConfigurationException ex) {
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, null, ex);
    } catch (TransformerException ex) {
      Logger.getLogger(ZonerCli.class.getName()).log(Level.SEVERE, null, ex);
    }
    return str;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      logger.severe("Usage:  " + ZonerCli.class.getName() + " <input file name>");
      return;
    }

    logger.finest ("finest logging");
    logger.severe ("severe logging");
    System.out.println("runnning stdout...");
    String inputFile = args[0];

    //Pattern filenamePattern = Pattern.compile("(([a-z,A-Z]:\\)?[");

    ZonerCli zonerCli = new ZonerCli();
    zonerCli.setInputFilename(inputFile);
    zonerCli.readFile(inputFile);
    zonerCli.execute();
    zonerCli.logRangesAndHeadings();
  }

  public void execute() throws IOException {
    // First pass through the input: formatting adjustments
//        String outputFile = inputFilename + ".out1";
//        String outputFile2 = inputFilename + ".out2";

//        passZero(inputFilename, outputFile);
//        passOneB(outputFile, outputFile2);
    clearRangeLists();
    findHeadings();
    pruneRanges();
  }

  /**
   * @return the inputFilename
   */
  public String getInputFilename() {
    return inputFilename;
  }

  public void readFile(String inputFilename) throws IOException, FileNotFoundException {
    logger.finest(String.format("input: %s", inputFilename));
    File inputFile = new File(inputFilename);
    FileReader inputFileReader = new FileReader(inputFile);
    BufferedReader inputBufferedReader = new BufferedReader(inputFileReader);
    StringWriter stringWriter = new StringWriter();
    PrintWriter stringPrinter = new PrintWriter(stringWriter);
    for (String currentInputLine = inputBufferedReader.readLine(); currentInputLine != null; currentInputLine = inputBufferedReader.readLine()) {
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
  public void setInputFilename(String inputFilename) {
    this.inputFilename = inputFilename;
  }

  public String buildString(String[] allLines) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < allLines.length; i++) {
      boolean isLast = (i == allLines.length - 1);
      String current = allLines[i];
      sb.append(current);
      if (!isLast) {
        sb.append(EOL);
      }
    }
    String returnValue = sb.toString();
    return returnValue;
  }

  @SuppressWarnings("LoggerStringConcat")
  public void findHeadings() {


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

//    Check each section regex for matches and create a new Range object for each
    for (SectionRegexDefinition currentDefinition : sectionRegexDefinitionList) {
      Matcher currentMatcher = currentDefinition.regex.matcher(getEntireContents());
      boolean findAll = currentDefinition.isFindAll();
      boolean findFirstOnly = !findAll;
      int i = 0;
      logger.finest(String.format(" trying %s ...", currentDefinition.getLabel()));
      while (currentMatcher.find()) {
        int start = currentMatcher.start();
        int end = currentMatcher.end();
        logger.finest(String.format(" ** " + currentDefinition.getLabel() + " match found: %d-%d", start, end));

        Range currentRange = new Range();
        currentRange.setLabel(currentDefinition.getLabel());
        currentRange.setBegin(start);
        currentRange.setEnd(end);
        currentRange.setIgnore(false); //the default, may be changed later if overlap found
        getFullRangeList().add(currentRange);

        if (findFirstOnly) {
          break;
        }
      }
    }

//    Sort the list of Ranges
    Collections.sort(getFullRangeList());

//    For each heading found, compute range end as token before next range begins
    for (Range currentRange : getFullRangeList()) {
      logger.finest(String.format(" - %s", currentRange));
    }
    logger.finest("===");
    
    List<Range> fullRangeListAdjusted = new ArrayList<Range>();

    int rangeListSize = getFullRangeList().size();
    for (int i = 0; i < rangeListSize; i++) {
      boolean isLast = (i == rangeListSize - 1);
      Range currentRange = getFullRangeList().get(i);
      if (currentRange.isIgnore()) {
        continue;
      }
      int begin = currentRange.getBegin();
      int end = currentRange.getEnd();
      int oneBeforeNextRange = 0;
      HeadingRange currentHeading = new HeadingRange();
      headings.add(currentHeading);
      currentHeading.setHeadingEnd(end);
      currentHeading.setHeadingBegin(begin);
      currentHeading.setLabel(currentRange.getLabel());
      currentHeading.setHeadingText(entireContents.substring(begin, end));
      if (!isLast) {
        int j = i + 1; // index of next range, may increase if there are overlaps
        Range nextRange = getFullRangeList().get(j);
        int nextRangeBegin = nextRange.getBegin();
        //oneBeforeNextRange = nextRangeBegin - 1;


//        check for overlapping headings
        if (nextRangeBegin < end) {
          logger.finest("*** overlap found: \"" + currentHeading.getHeadingText() + "\" "
                  + currentRange + " *** \""
                  + entireContents.substring(nextRangeBegin, nextRange.getEnd()) + "\" " + nextRange);
          // Since there is an overlap, mark to ignore the Range corresponding to the
          // shorter of the two headings -- it will eventually be removed but cannot be
          // removed in the middle of this loop.  If NextRange is the shorter one,
          // grab a new NextRange to use to compute the real extent of this heading's range.
          // if no more next headings, use isLast logic
          int curRangeLen = end - begin;
          int nextRangeEnd = nextRange.getEnd();
          int nextRangeLen = nextRangeEnd - nextRangeBegin;
          if (curRangeLen < nextRangeLen) {
            logger.finest("\ttruncating current: " + currentRange);
            currentRange.setTruncated(true);
            currentRange.setEnd(nextRangeBegin-2);
          } else {
            while (++j < rangeListSize && nextRangeBegin < end) {
              logger.finest("\tignoring next: " + nextRange);
              nextRange.setIgnore(true);
              nextRange = getFullRangeList().get(j);
              nextRangeBegin = nextRange.getBegin();
            }

            if (j == rangeListSize && nextRangeBegin < end) {
              // nextRange is the last range and it still overlaps
              nextRange.setIgnore(true);
              oneBeforeNextRange = getEntireContents().length() - 1;
              isLast = true;
            }
          }
        }
        /************ This doesn't seem to be useful, so removing it
        //          else {
        //        check for headings separated only by non-alpha characters
        //              Pattern alphanum = Pattern.compile("[a-zA-Z1-9]");
        logger.finest("checking for alphanum in intervening range: " + end + " - " + nextRangeBegin);
        boolean noGap = false;
        String betweenHeaders = "";
        Matcher m = null;
        if (end == nextRangeBegin) {
        noGap = true;
        } else {
        betweenHeaders = entireContents.substring((end + 1), nextRangeBegin);
        m = alphanum.matcher(betweenHeaders);
        }
        if (noGap || !m.find()) {
        logger.finest("~~~ consecutive headings found: " + currentRange + " ~~~ " + nextRange);
        }
        }
         ******/
        // todo is (nextRangeBegin - 2) the right place to start searching for the previous token?
        // it makes sense, because if the previous char is whitespace (which
        // it would have to be), you'd want to go back atleast two chars
        if (!currentRange.isIgnore() && !isLast) {
          oneBeforeNextRange = findLastCharOffsetOfPreviousWord(entireContents, nextRangeBegin - 2);
        }
      } else { // isLast
        oneBeforeNextRange = getEntireContents().length() - 1;
      }

      if (!currentRange.isIgnore()) {

        int realSectionEnd = oneBeforeNextRange;

//        update range to reflect section end
        CharacterOffsetToLineTokenConverter c =
                new CharacterOffsetToLineTokenConverter(getEntireContents());
        LineAndTokenPosition beginLineAndTokenPosition = c.convert(begin);
        LineAndTokenPosition endLineAndTokenPosition = c.convert(realSectionEnd);

        logger.finest(String.format(" - %s: %s (%d-%d) (section end: %d) %s to %s ",
                currentRange, getEntireContents().substring(begin, end),
                begin, end, realSectionEnd,
                beginLineAndTokenPosition.toString(), endLineAndTokenPosition.toString()));


        currentRange.setEnd(oneBeforeNextRange);
        currentRange.setBeginLineAndToken(beginLineAndTokenPosition);
        currentRange.setEndLineAndToken(endLineAndTokenPosition);
        
        fullRangeListAdjusted.add(currentRange);

        //SectionAnnotation a = new SectionAnnotation();
      }
      
    }
    
    this.fullRangeListAdjusted = fullRangeListAdjusted;

//      while (admissionDateMatcher.find())
//      {
//        int start = admissionDateMatcher.start();
//        int end = admissionDateMatcher.end();
//        System.out.format("admission date match found: %d-%d%n", start, end);
//      }
//
  }

  public String getEntireContents() {
    return entireContents;
  }

  public void setEntireContents(String entireContents) {
    this.entireContents = entireContents;
  }

  public void pruneRanges() {
    for (Iterator<Range> i = getFullRangeList().iterator(); i.hasNext(); ) {
      Range checkRange = i.next();
      if (!checkRange.isIgnore()) {
        getRangeList().add(checkRange);
      }
    }
  }

  public void clearRangeLists() {
    getRangeList().clear();
    getFullRangeList().clear();
  }
  
  /**
   * @return the rangeList
   */
  public List<Range> getFullRangeList() {
    return fullRangeList;
  }

 public List<Range> getRangeList() {
    return rangeList;
  }

  /**
   * @param rangeList the rangeList to set
   */
  public void setRangeList(List<Range> rangeList) {
    this.fullRangeList = rangeList;
  }

  public List<HeadingRange> getHeadings() {
    return headings;
  }

  public void setHeadings(List<HeadingRange> headings) {
    this.headings = headings;
  }

  public void logRangesAndHeadings() {
    logger.finest("================== RangeList ======================");
    for (Iterator i = getRangeList().iterator(); i.hasNext(); ) {
      logger.finest(i.next().toString());
    }
    logger.finest("================== FullRangeList ======================");
    for (Iterator i = getFullRangeList().iterator(); i.hasNext(); ) {
      logger.finest(i.next().toString());
    }
    logger.finest("================== Headings ======================");
    for (Iterator i = getHeadings().iterator(); i.hasNext(); ) {
      logger.finest(i.next().toString());
    }
  }

  private int findLastCharOffsetOfPreviousWord(String entireContents, int initialPosition) {
    boolean found = false;
    int i = initialPosition;
    while (!found && i >= 0) {
      char currentChar = entireContents.charAt(i);
      if (i != ' ' && i != '\r' && i != '\n') {
        found = true;
      }
    }
    if (i < 0) {
      i = 0;
    }
    return i;
  }

  public class Range implements Comparable<Range> {

    public Range() {
    }
    protected int begin;
    protected int end;
    protected LineAndTokenPosition beginLineAndToken;
    protected LineAndTokenPosition endLineAndToken;
    protected String label;
    protected boolean ignore;
    protected boolean truncated;

    public String toString() {
      return String.format("RANGE \"%s\" [%d-%d]", label, begin, end);
    }

    public int getBegin() {
      return begin;
    }

    public void setBegin(int begin) {
      this.begin = begin;
    }

    public int getEnd() {
      return end;
    }

    public void setEnd(int end) {
      this.end = end;
    }

    public boolean isIgnore() {
      return ignore;
    }

    public void setIgnore(boolean ignore) {
      this.ignore = ignore;
    }

    public boolean isTruncated() {
      return truncated;
    }

    public void setTruncated(boolean truncated) {
      this.truncated = truncated;
    }


    @Override
    public int compareTo(Range other) {
      if (this.begin < other.begin) {
        return -1;
      } else if (this.begin > other.begin) {
        return 1;
      } else if (this.end == other.end) {
        return 0;
      } else if (this.end < other.end) {
        return -1;
      } else {
        return 1;
      }
    }

    /**
     * @return the label
     */
    public String getLabel() {
      return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
      this.label = label;
    }

    public LineAndTokenPosition getBeginLineAndToken() {
      return beginLineAndToken;
    }

    public void setBeginLineAndToken(LineAndTokenPosition beginLineAndToken) {
      this.beginLineAndToken = beginLineAndToken;
    }

    public LineAndTokenPosition getEndLineAndToken() {
      return endLineAndToken;
    }

    public void setEndLineAndToken(LineAndTokenPosition endLineAndToken) {
      this.endLineAndToken = endLineAndToken;
    }
  }

  public class HeadingRange {

    protected int headingBegin;
    protected int headingEnd;
    protected String label;
    protected String headingText;

    @Override 
    public String toString() {
      return String.format("HEADING \"%s\" (%s)", headingText, label);
    }

    public int getHeadingBegin() {
      return headingBegin;
    }

    public void setHeadingBegin(int begin) {
      this.headingBegin = begin;
    }

    public int getHeadingEnd() {
      return headingEnd;
    }

    public void setHeadingEnd(int end) {
      this.headingEnd = end;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }

    public void setHeadingText(String headingText) {
      this.headingText = headingText;
    }

    public String getHeadingText() {
      return headingText;
    }
  }

  public class SectionRegexDefinition {

    protected Pattern regex;
    protected String label;
    protected boolean findAll;

    public Pattern getRegex() {
      return regex;
    }

    public void setRegex(Pattern regex) {
      this.regex = regex;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public boolean isFindAll() {
      return findAll;
    }

    public void setFindAll(boolean findAll) {
      this.findAll = findAll;
    }
  }

  public static ParsedTextFile processTextFile(File inputFile) throws FileNotFoundException, IOException {
    System.out.format("processing text file \"%s\"...%n", inputFile.getAbsolutePath());

    FileReader fr = new FileReader(inputFile);
    BufferedReader br = new BufferedReader(fr);

    ParsedTextFile parsedTextFile = ZonerCli.processTextBufferedReader(br);
    String[][] text2dArray = parsedTextFile.getTokens();
    //this.textLookup = text2dArray;

    br.close();
    fr.close();

    System.out.println("=====");

    System.out.format("done processing text file \"%s\".%n", inputFile.getAbsolutePath());

    return parsedTextFile;
  }

  public static ParsedTextFile processTextBufferedReader(BufferedReader br) throws FileNotFoundException, IOException {
    StringWriter writer = new StringWriter();
    PrintWriter printer = new PrintWriter(writer);

    String currentLine = null;
    //ArrayList<ArrayList<String>> textLookup = new ArrayList<ArrayList<String>>();
    ArrayList<String[]> textLookupTemp = new ArrayList<String[]>();
    int lineNumber = 0;
    while ((currentLine = br.readLine()) != null) {
      printer.println(currentLine);
//      System.out.format("CURRENT LINE (pre) [%d]: %s%n", lineNumber, currentLine);
      //ArrayList<String> currentTextLookupLine = new ArrayList<String>();
      //textLookup.add(currentTextLookupLine);

      String tokenArray[] = WHITESPACE_PATTERN.split(currentLine);

//      Pattern pattern = Pattern.compile("\\s+");
//      String tokenArray[] = pattern.split(currentLine);


      //logger.finest(String.format("before split: %s; %nafter split: %s", currentLine, printOutLineOfTokens(tokenArray)));
//      for (String currentToken : tokenArray)
//      {
//        System.out.format("    CURRENT token (pre): %s%n", currentToken);
//      }

      textLookupTemp.add(tokenArray);

      lineNumber++;
    }

    ParsedTextFile parsedTextFile = new ParsedTextFile();
    parsedTextFile.setEverything(writer.toString());

    printer.close();
    writer.close();

    String twoDimensionalStringArray[][] = new String[1][];
    //String textLookup[][] = null;
    String textLookup2dArray[][] = textLookupTemp.toArray(twoDimensionalStringArray);

    parsedTextFile.setTokens(textLookup2dArray);

    return parsedTextFile;
  }

  public static String printOutLineOfTokens(String[] string) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < string.length; i++) {
      boolean isLast = (i == (string.length - 1));
      sb.append(i);
      sb.append(":");
      sb.append('"');
      //String text = string[i].replace("\\", "\\\\").replace("\"", "\\\"");
      String text = string[i];
      sb.append(text);
      sb.append('"');
      if (!isLast) {
        sb.append(", ");
      }
    }
    sb.append("]");

    return sb.toString();
  }

  public static String printOutFileOfLinesOfTokens(String[][] arrayOfLines) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < arrayOfLines.length; i++) {
      boolean isLast = (i == (arrayOfLines.length - 1));
      sb.append("line_");
      sb.append(i);
      sb.append(":::");
      //String text = string[i].replace("\\", "\\\\").replace("\"", "\\\"");
      String text = printOutLineOfTokens(arrayOfLines[i]);
      sb.append(text);
      if (!isLast) {
        sb.append(", ");
      }
      sb.append("\n");
    }
    sb.append("]");

    return sb.toString();
  }

    public List<Range> getFullRangeListAdjusted() {
        return fullRangeListAdjusted;
    }

    public void setFullRangeListAdjusted(List<Range> fullRangeListAdjusted) {
        this.fullRangeListAdjusted = fullRangeListAdjusted;
    }
}
