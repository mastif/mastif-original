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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * Hello world!
 *
 */
public class ZonerCli {

    private static final Logger logger = Logger.getLogger(ZonerCli.class.getName());
    public static final String EOL = System.getProperty("line.separator");
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    public static final int FULL_RANGE_LIST = 0;
    public static final int TEMPLATE_LIST = 1;
    public static final int SUBSECTION_LIST = 2;
    protected String inputFilename;
    protected List<SectionRegexDefinition> sectionRegexDefinitionList;
    protected List<SectionRegexDefinition> templateRegexDefinitionList;
    protected List<SectionRegexDefinition> subsectionRegexDefinitionList;
    protected Map<String, Node> fragmentMap;
    protected Map<String, AttributesHolder> fragmentAttrsMap;
    // This will include all the Ranges, including those we will eventually mark
    // isIgnore because of overlaps
    protected List<Range> fullRangeList = new ArrayList<Range>();
    // This will be the trimmed down list of Ranges, excluding all the
    // overlapping isIgnore Ranges
    protected List<Range> rangeList = new ArrayList<Range>();
    protected List<Range> fullRangeListAdjusted = new ArrayList<Range>();
    protected List<HeadingRange> headings = new ArrayList<HeadingRange>();
    protected List<Range> templates = new ArrayList<Range>();
    protected List<Range> subsections = new ArrayList<Range>();
    // maps a section name to a list of ranges that represent sections of that type
    protected Map<String, List<Range>> sectionMap = new HashMap<String, List<Range>>();
    // maps a section range to a list of subsection ranges within it
    protected Map<Range, List<Range>> subsectionMap = new HashMap<Range, List<Range>>();
    // maps a section range to a list of template ranges within it
    // TODO what if a range has both subsections and templates within it?
    // TODO maybe templates will be terminated differently than sections/subsections?
    protected Map<Range, List<Range>> templateMap = new HashMap<Range, List<Range>>();
    protected String entireContents;
    public static final int expansionThreshold = 5;
    private static String defaultRegexFilename = "org/mitre/medfacts/zoner/section_regex_with_attributes.xml";
    private CharacterOffsetToLineTokenConverter converter;
    private XPathExpression sectionExpression;
    private XPathExpression regexExpression;
    private XPathExpression regexIgnoreCaseExpression;
    private XPathExpression regexFindAllExpression;
    private XPathExpression labelExpression;
    private XPathExpression subjectExpression;
    private XPathExpression medicalExpression;
    private XPathExpression temporalExpression;
    private XPathExpression uncertainExpression;
    private XPathExpression negatedExpression;
    private XPathExpression subsectionOfExpression;
    private XPathExpression fragmentExpression;
    private XPathExpression fragmentNameExpression;
    private XPathExpression fragmentExpansionExpression;
    private XPathExpression fragmentExpansionNode;
    private XPathExpression embeddedFragmentExpression;
    private XPathExpression embeddedFragmentName;
    private XPathExpression templateExpression;
    private XPathExpression subsectionExpression;
    private boolean doTemplates;
    private boolean doSubsections;
    private boolean convertOffsets;

    public ZonerCli() {
        this(null, false);
    }
    
    public ZonerCli(URI regexFileUri) {
        this (regexFileUri, false);
    }

    /**
     * 
     * @param regexFileUri - the patterns file that defines the section headings
     * @param convertOffsets - if true will add i2b2 style line/token offsets to 
     *                         the heading and section structures
     */
    public ZonerCli(URI regexFileUri, boolean convertOffsets) {
        try {
            if (regexFileUri == null) {
                regexFileUri = this.getClass().getClassLoader().getResource(defaultRegexFilename).toURI();
            }
            this.convertOffsets = convertOffsets;
            
            Document input = parseDocument(regexFileUri.toString());

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            sectionExpression = xpath.compile("/root/sections/section");
            regexExpression = xpath.compile("./regex");
            regexIgnoreCaseExpression = xpath.compile("./regex/@ignore-case");
            regexFindAllExpression = xpath.compile("./regex/@find-all");
            labelExpression = xpath.compile("./label/text()");
            medicalExpression = xpath.compile("./medical/text()");
            temporalExpression = xpath.compile("./temporal/text()");
            subjectExpression = xpath.compile("./subject/text()");
            uncertainExpression = xpath.compile("./uncertain/text()");
            negatedExpression = xpath.compile("./negated/text()");
            subsectionOfExpression = xpath.compile("./subsection_of/text()");
            fragmentExpression = xpath.compile("/root/fragments/fragment");
            fragmentNameExpression = xpath.compile("./name/text()");
            fragmentExpansionExpression = xpath.compile("./expansion/text()");
            fragmentExpansionNode = xpath.compile("./expansion");
            embeddedFragmentExpression = xpath.compile("./fragment-ref");
            embeddedFragmentName = xpath.compile("./@name");
            templateExpression = xpath.compile("/root/templates/template");
            subsectionExpression = xpath.compile("/root/subsections/subsection");
            doTemplates = true; // default
            doSubsections = true; // default

            // get all the fragment elements out of the xml file and hash name/expansion pairs
            // also hash name/attrs pairs
            fragmentMap = new LinkedHashMap<String, Node>();
            fragmentAttrsMap = new LinkedHashMap<String, AttributesHolder>();
            NodeList fragmentNodeList =
                    (NodeList) fragmentExpression.evaluate(input, XPathConstants.NODESET);
            for (int i = 0; i < fragmentNodeList.getLength(); i++) {
                Element fragmentElement = (Element) fragmentNodeList.item(i);
                String nameString = fragmentNameExpression.evaluate(fragmentElement);
                String expansionString = fragmentExpansionExpression.evaluate(fragmentElement);
                Node expansionNode = (Node) fragmentExpansionNode.evaluate(fragmentElement, XPathConstants.NODE);
                fragmentMap.put(nameString, expansionNode);
                fragmentAttrsMap.put(nameString,
                        new AttributesHolder(medicalExpression.evaluate(fragmentElement),
                            temporalExpression.evaluate(fragmentElement),
                            subjectExpression.evaluate(fragmentElement),
                            uncertainExpression.evaluate(fragmentElement),
                            negatedExpression.evaluate(fragmentElement)));
                logger.log(Level.FINEST, "found fragment: {0} -> {1}",
                        new Object[]{nameString, nodeToString(expansionNode)});
            }

// get all the section (regular expression) elements from the xml file,
//        and expand as needed and create a regex Pattern for each     

            sectionRegexDefinitionList = new ArrayList<SectionRegexDefinition>();
            NodeList sectionNodeList =
                    (NodeList) sectionExpression.evaluate(input, XPathConstants.NODESET);
            parseNodeList(sectionNodeList, sectionRegexDefinitionList);

// get all the subsection (regular expression) elements from the xml file,
//       and create a regex Pattern for each     

            subsectionRegexDefinitionList = new ArrayList<SectionRegexDefinition>();
            NodeList subsectionNodeList =
                    (NodeList) subsectionExpression.evaluate(input, XPathConstants.NODESET);
            parseNodeList(subsectionNodeList, subsectionRegexDefinitionList);

// get all the template (regular expression) elements from the xml file,
//       and create a regex Pattern for each     

            templateRegexDefinitionList = new ArrayList<SectionRegexDefinition>();
            NodeList templateNodeList =
                    (NodeList) templateExpression.evaluate(input, XPathConstants.NODESET);
            parseNodeList(templateNodeList, templateRegexDefinitionList);

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

    private void parseNodeList(NodeList theNodeList, List<SectionRegexDefinition> theRegexDefinitionList)
            throws XPathExpressionException {
        for (int i = 0; i < theNodeList.getLength(); i++) {
            Element theElement = (Element) theNodeList.item(i);
            // logger.finest("found the element");

            Node regexNode =
                    (Node) regexExpression.evaluate(theElement, XPathConstants.NODE);
            AttributesHolder attrs = new AttributesHolder();
            Map<Integer,List<String>> fragmentLevelsMap = new HashMap<Integer,List<String>>();
            String regexString = expandFragments(regexNode, embeddedFragmentExpression,
                    embeddedFragmentName, fragmentLevelsMap /*attrs*/);
            // if the fragment nesting is too deep, expandFragments will return null
            // in that case, skip this regex
            if (regexString == null) {
                continue;
            }

            String regexIgnoreCaseString = regexIgnoreCaseExpression.evaluate(theElement);
            if (regexIgnoreCaseString == null || regexIgnoreCaseString.isEmpty()) {
                regexIgnoreCaseString = "true";
            }
            boolean regexIgnoreCase = regexIgnoreCaseString.equalsIgnoreCase("true");
            String regexFindAllString = regexFindAllExpression.evaluate(theElement);
            if (regexFindAllString == null || regexFindAllString.isEmpty()) {
                regexFindAllString = "true";
            }
            boolean regexFindAll = regexFindAllString.equalsIgnoreCase("true");
            String labelString = labelExpression.evaluate(theElement);
            logger.finest(String.format(" - section -- label: \"%s\"; regex: \"%s\"; ignore case: \"%s\"; match all: \"%s\"",
                    labelString, regexString, regexIgnoreCaseString, regexFindAllString));
            attrs.maybeSetMedical(medicalExpression.evaluate(theElement));
            attrs.maybeSetSubject(subjectExpression.evaluate(theElement));
            attrs.maybeSetTemporal(temporalExpression.evaluate(theElement));
            attrs.maybeSetUncertain(uncertainExpression.evaluate(theElement));
            attrs.maybeSetNegated(negatedExpression.evaluate(theElement));
            // subsectionOfString may be a comma-separated string of multiple section names, or just a single section name
            String subsectionOfString = subsectionOfExpression.evaluate(theElement);
            if (subsectionOfString.isEmpty()) {
                subsectionOfString = null;
            }

            int flags = 0;
            if (regexIgnoreCase) {
                flags += Pattern.CASE_INSENSITIVE;
            }
            flags += Pattern.MULTILINE;

            Pattern currentRegex = Pattern.compile(regexString, flags);

            SectionRegexDefinition definition = new SectionRegexDefinition();
            definition.setLabel(labelString);
            definition.setAttributes(attrs);
            definition.setRegex(currentRegex);
            definition.setFindAll(regexFindAll);
            definition.setSubsectionOf(subsectionOfString);
            definition.setFragmentsMap(fragmentLevelsMap);
            theRegexDefinitionList.add(definition);
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
            XPathExpression embeddedFragmentName, Map<Integer,List<String>> fragmentLevelsMap) {
        int levels = 0;
        Element parentRegexElement = (Element) regexNode;
        // Document ownerDocument = parentRegexElement.getOwnerDocument();
        //logger.log(Level.FINEST, "expandFragments on textContent: {0}", parentRegexElement.getTextContent());
        logger.log(Level.FINEST, "expandFragments on Node: {0}", nodeToString(regexNode));

        try {
            NodeList fragmentList =
                    (NodeList) embeddedFragmentExpression.evaluate(regexNode, XPathConstants.NODESET);
            while (levels < expansionThreshold && fragmentList.getLength() > 0) {
                for (int i = 0; i < fragmentList.getLength(); i++) {
                    Element fragmentRefElement = (Element) fragmentList.item(i);
                    String fragName = embeddedFragmentName.evaluate(fragmentRefElement);
                    Node fragExpansionNode = fragmentMap.get(fragName).cloneNode(true);
                    // attrs.merge(fragmentAttrsMap.get(fragName));
                    // add to the fragment levels map (passed in)
                    Integer l = new Integer(levels);
                    List<String> levelList = fragmentLevelsMap.get(l);
                    if (levelList == null) {
                        levelList = new ArrayList<String>();
                        fragmentLevelsMap.put(l, levelList);
                    }
                    levelList.add(fragName);
                    // replace the fragmentRef with its expansion
                    Node parentNode = fragmentRefElement.getParentNode();
                    /* logger.finest(String.format("In Node %s replace\n\t%s\nwith\n\t%s", nodeToString(parentNode),
                            nodeToString((Node)fragmentRefElement), nodeToString(fragExpansionNode))); */
                    parentNode.replaceChild(fragExpansionNode, fragmentRefElement);
                    logger.log(Level.FINEST, "Level {0} fragment {1} expansion: {2}",
                            new Object[]{levels, i, nodeToString((Node) parentRegexElement)});
                }
                // now that we've handled all the fragments, increment levels and get a 
                // new fragment list from fragments that were in the replacement nodes
                levels++;
                logger.log(Level.FINEST, "checking for any level {0} embedded fragments in {1}",
                        new Object[]{levels, nodeToString((Node) parentRegexElement)});
                // deepen the xpath search expression
                StringBuffer nestedFragmentBuf = new StringBuffer("./");
                for (int j = 0; j < levels; j++) {
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
        String str = null;
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

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length != 1 && args.length != 2) {
            logger.severe("Usage:  " + ZonerCli.class.getName() + " <input file name> [<pattern file name>]");
            logger.severe("args passed were: (" + args.length + ")");
            for (int i = 0; i < args.length; i++) {
                logger.severe(args[i]);
            }
            return;
        }

        logger.finest("finest logging");
        logger.severe("severe logging");
        System.out.println("runnning stdout...");
        String inputFile = args[0];
        String patternFile = null;
        if (args.length == 2) {
            patternFile = args[1];
        }
        System.out.println("inputFile: " + inputFile);

        //Pattern filenamePattern = Pattern.compile("(([a-z,A-Z]:\\)?[");

        ZonerCli zonerCli;
        if (patternFile == null) {
            zonerCli = new ZonerCli();
        } else {
            zonerCli = new ZonerCli((new File(patternFile)).toURI());
        }

        zonerCli.setInputFilename(inputFile);
        zonerCli.readFile(inputFile);
        // note that at this time it is not possible to have convertOffsets
        // be true when running from the command line -- leaving this here
        // just in case we want to eventually support that functionality
        if (zonerCli.convertOffsets) {
            // initializeConverter converter
            zonerCli.initializeConverter();
        }
        zonerCli.setDoTemplates(true);
        zonerCli.execute();
        zonerCli.logRangesAndHeadings();
    }

    public void execute() throws IOException {
        // First pass through the input: formatting adjustments
//        String outputFile = inputFilename + ".out1";
//        String outputFile2 = inputFilename + ".out2";

//        passZero(inputFilename, outputFile);
//        passOneB(outputFile, outputFile2);
        logger.finest("ZonerCli.execute()");
        System.out.println("running the newest version");
        clearRangeLists();
        clearHeadings();
        findHeadings();
        if (doSubsections) {
            logger.finest("~~~~~~~~~~~~ Subsections ~~~~~~~~~~~~~");
            clearSubsections();
            findSubsections();
        }
        if (doTemplates) {
            logger.finest("~~~~~~~~~~~~~ Templates ~~~~~~~~~~~~~~~");
            clearTemplates();
            findTemplates();
        }
        pruneRanges(); // only for full range list right now
    }

    public void setDoTemplates(boolean doit) {
        doTemplates = doit;
    }

    public void setDoSubsections(boolean doSubsections) {
        this.doSubsections = doSubsections;
    }

    public void setConvertOffsets(boolean convert) {
        this.convertOffsets = convert;
    }
    
    public void doConvertOffsets() {
        this.convertOffsets = true;
        this.initializeConverter();
    }
    
    public void initialize() {
        this.initializeConverter();
    }
    
    public void initializeConverter() {
        if (converter == null) {
            converter = new CharacterOffsetToLineTokenConverterDefaultImpl(getEntireContents());
        }

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

        /***************** replace with call to matchRegexes
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
         * **********/
        matchRegexes(sectionRegexDefinitionList, FULL_RANGE_LIST);
        Collections.sort(getFullRangeList());
        adjustRangeEnds(getFullRangeList(), true, getEntireContents().length() - 1);


//      while (admissionDateMatcher.find())
//      {
//        int start = admissionDateMatcher.start();
//        int end = admissionDateMatcher.end();
//        System.out.format("admission date match found: %d-%d%n", start, end);
//      }
//
    }

    public void findTemplates() {
        matchRegexes(templateRegexDefinitionList, TEMPLATE_LIST);
        // Collections.sort(getTemplates());
        // adjustSubsectionRangeEnds(templateMap);
    }

    public void findSubsections() {
        initSectionMap();
        clearSubsectionMap();
        matchRegexes(subsectionRegexDefinitionList, SUBSECTION_LIST);
        Collections.sort(getSubsections());
        adjustSubsectionRangeEnds(subsectionMap);
    }

    //TODO version of findTemplates for given sub-section of document
    public void matchRegexes(List<SectionRegexDefinition> theRegexList, int listID) {
        //    Check each section regex for matches and create a new Range object for each
        for (SectionRegexDefinition currentDefinition : theRegexList) {
            // for subsections and maybe templates, search only in certain sections
            // so loop through those sections and instead of doing getEntireContents()
            // we'll need to send a substring and keep track of offsets
            //subsectionOf may be a comma-separated list; loop through those too
            String subsectionOf = currentDefinition.getSubsectionOf();
            if (subsectionOf == null) {
                matchRegexInSection(currentDefinition, getEntireContents(), 0, listID, null);
            } else {
                List<String> parentSections = Arrays.asList(subsectionOf.split(","));
                for (String parentSection : parentSections) {
                    List<Range> ranges = sectionMap.get(parentSection);
                    // loop through ranges
                    if (ranges != null) {
                        for (Range curRange : ranges) {
                            matchRegexInSection(currentDefinition, getEntireContents().substring(curRange.getBegin(), curRange.getEnd()),
                                    curRange.getBegin(), listID, curRange);
                        }
                    }
                }
            }
        }
    }

    private void matchRegexInSection(SectionRegexDefinition currentDefinition,
            String docString, int offset, int listID, Range parentRange) {
        Matcher currentMatcher = currentDefinition.regex.matcher(docString);
        boolean findAll = currentDefinition.isFindAll();
        boolean findFirstOnly = !findAll;
        int i = 0;
        // logger.finest(String.format(" trying %s ...", currentDefinition.getLabel()));
        while (currentMatcher.find()) {
            int start = currentMatcher.start();
            int end = currentMatcher.end();
            logger.finest(String.format(" ** " + currentDefinition.getLabel() + " match found: %d-%d", start, end));

            Range currentRange = new Range();
            currentRange.setLabel(currentDefinition.getLabel());
            AttributesHolder attrs = new AttributesHolder(currentDefinition.getMedical(),
                    currentDefinition.getTemporal(), currentDefinition.getSubject(),
                    currentDefinition.getUncertain(), currentDefinition.getNegated());
            currentRange.setBegin(start + offset);
            currentRange.setEnd(end + offset);
            currentRange.setIgnore(false); //the default, may be changed later if overlap found
            // check for named captures from fragments that contribute attributes
            List<Integer> keyList = new ArrayList<Integer>(currentDefinition.getFragmentsMap().keySet());
            Collections.sort(keyList);
            for (Integer level : keyList) {
                List<String> fragList = currentDefinition.getFragmentsMap().get(level);
                for (String fragName : fragList) {
                    logger.finest(String.format("checking for match with fragment named %s", fragName));
                    try {
                        if (currentMatcher.group(fragName) != null) {
                            logger.finest(String.format(">>> found match for fragment %s with attrs %s", 
                                    fragName, fragmentAttrsMap.get(fragName).toString()));
                            attrs.merge(fragmentAttrsMap.get(fragName));
                        }
                    } catch (IllegalArgumentException x) {
                        // there wasn't a matching group with that name at all, skip it
                    }
                }
            }
            currentRange.setAttrs(attrs);
            getList(listID).add(currentRange);
            if (parentRange != null) {
                List<Range> rangeSubsections = subsectionMap.get(parentRange);
                if (rangeSubsections == null) {
                    rangeSubsections = new ArrayList<Range>();
                    subsectionMap.put(parentRange, rangeSubsections);
                }
                rangeSubsections.add(currentRange);
            }

            if (findFirstOnly) {
                break;
            }
        }
    }

    // TODO when dealing with subsections, perhaps we need to call this once for each section?
    // something needs to be done so we can treat the last subsection in a section as a "last" range
    private void adjustRangeEnds(List<Range> theRangeList, boolean checkOverlaps, int sectionEndOffset) {
        //    Sort the list of Ranges -- instead require a sorted list to be passed in
        //     Collections.sort(getList(listID));

//    For each heading found, compute range end as token before next range begins
        for (Range currentRange : theRangeList) {
            logger.finest(String.format(" - %s", currentRange));
        }
        logger.finest("===");

        fullRangeListAdjusted = new ArrayList<Range>();

        int rangeListSize = theRangeList.size();
        for (int i = 0; i < rangeListSize; i++) {
            boolean isLast = (i == rangeListSize - 1);
            Range currentRange = theRangeList.get(i);
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
            currentHeading.setMedical(currentRange.getMedical());
            currentHeading.setTemporal(currentRange.getTemporal());
            currentHeading.setSubject(currentRange.getSubject());
            currentHeading.setUncertain(currentRange.getUncertain());
            currentHeading.setNegated(currentRange.getNegated());
            currentHeading.setHeadingText(entireContents.substring(begin, end));
            if (!isLast) {
                int j = i + 1; // index of next range, may increase if there are overlaps
                Range nextRange = theRangeList.get(j);
                int nextRangeBegin = nextRange.getBegin();
                //oneBeforeNextRange = nextRangeBegin - 1;


                // check for overlapping headings
                if (checkOverlaps && nextRangeBegin < end) {
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
                        currentRange.setEnd(nextRangeBegin - 1);
                    } else {
                        while (++j < rangeListSize && nextRangeBegin < end) {
                            logger.finest("\tignoring next: " + nextRange);
                            nextRange.setIgnore(true);
                            nextRange = theRangeList.get(j);
                            nextRangeBegin = nextRange.getBegin();
                        }

                        if (j == rangeListSize && nextRangeBegin < end) {
                            // theRange is the last range and it still overlaps
                            nextRange.setIgnore(true);
                            oneBeforeNextRange = sectionEndOffset; // TODO not always using entireContents
                            isLast = true;
                        }
                    }
                }
                // todo is (nextRangeBegin - 2) the right place to start searching for the previous token?
                // it makes sense, because if the previous char is whitespace (which
                // it would have to be), you'd want to go back atleast two chars
                // Actually we just need -1 because end offsets are different than begin offsets 
                if (!currentRange.isIgnore() && !isLast) {
                    oneBeforeNextRange = findLastCharOffsetOfPreviousWord(entireContents, nextRangeBegin - 1) + 1; // +1 because we want range end to be after last char, not on it
                }
            } else { // isLast
                oneBeforeNextRange = sectionEndOffset;
            }

            if (!currentRange.isIgnore()) {
                // TODO this is where it's breaking!!! Should find not just one
                // character back, but rather previous non-whitespace character that
                // is at least one character back
                // int realSectionEnd = oneBeforeNextRange;
                int realSectionEnd = oneBeforeNextRange;

                if (convertOffsets) {
                    logger.fine("ZonerCli: calling converter on 'begin': " + begin);
                    LineAndTokenPosition beginLineAndTokenPosition = converter.convert(begin);
                    logger.fine("ZonerCli: calling converter on 'realSectionEnd': " + realSectionEnd);
                    LineAndTokenPosition endLineAndTokenPosition = converter.convert(realSectionEnd);
                    
                    logger.finest(String.format(" - %s: %s (%d-%d) (section end: %d) %s to %s ",
                            currentRange, getEntireContents().substring(begin, end),
                            begin, end, realSectionEnd,
                            beginLineAndTokenPosition.toString(), endLineAndTokenPosition.toString()));
                    currentRange.setBeginLineAndToken(beginLineAndTokenPosition);
                    currentRange.setEndLineAndToken(endLineAndTokenPosition);
                } else {
                    logger.finest(String.format(" - %s: %s (%d-%d) (section end: %d) ",
                            currentRange, getEntireContents().substring(begin, end),
                            begin, end, realSectionEnd));
                }

                currentRange.setEnd(oneBeforeNextRange);
                fullRangeListAdjusted.add(currentRange);

                //SectionAnnotation a = new SectionAnnotation();
            }
        }
    }

    //TODO must actually call adjustRangeEnds once per parent range
    // expects theRanges to be sorted 
    private void adjustSubsectionRangeEnds(Map<Range, List<Range>> theMap) {
        for (Iterator<Range> i = theMap.keySet().iterator(); i.hasNext();) {
            Range curRange = i.next();
            List<Range> theSubranges = theMap.get(curRange);
            Collections.sort(theSubranges);
            adjustRangeEnds(theSubranges, false, curRange.getEnd()); // for now don't worry about overlap?
        }
    }

    public String getEntireContents() {
        return entireContents;
    }

    public void setEntireContents(String entireContents) {
        this.entireContents = entireContents;
    }

    public void pruneRanges() {
        for (Iterator<Range> i = getFullRangeList().iterator(); i.hasNext();) {
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

    public void clearHeadings() {
        getHeadings().clear();
    }

    public void clearTemplates() {
        getTemplates().clear();
    }

    public void clearSubsections() {
        getSubsections().clear();
    }

    public void clearSubsectionMap() {
        subsectionMap.clear();
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

    public List<Range> getTemplates() {
        return templates;
    }

    public List<Range> getSubsections() {
        return subsections;
    }

    public List<Range> getList(int listID) {
        switch (listID) {
            case FULL_RANGE_LIST:
                return getFullRangeList();
            case TEMPLATE_LIST:
                return getTemplates();
            case SUBSECTION_LIST:
                return getSubsections();
            default:
                return null;
        }
    }

    public void setHeadings(List<HeadingRange> headings) {
        this.headings = headings;
    }

    public void logRangesAndHeadings() {
        logger.finest("================== RangeList ======================");
        for (Iterator i = getRangeList().iterator(); i.hasNext();) {
            // logger.finest(i.next().toString());
            Range t = (Range) i.next();
            logger.finest(t.toString());
            logger.finest(getEntireContents().substring(t.getBegin(), t.getEnd()));
            logger.finest("char at " + t.getEnd() + ": " + getEntireContents().charAt(t.getEnd()));
        }
        logger.finest("================== FullRangeList ======================");
        for (Iterator i = getFullRangeList().iterator(); i.hasNext();) {
            logger.finest(i.next().toString());
        }
        logger.finest("================== TemplatesList ======================");
        for (Iterator i = getTemplates().iterator(); i.hasNext();) {
            Range t = (Range) i.next();
            logger.finest(t.toString());
            logger.finest(getEntireContents().substring(t.getBegin(), t.getEnd()));
        }
        logger.finest("================== SubsectionsList ======================");
        for (Iterator i = getSubsections().iterator(); i.hasNext();) {
            logger.finest(i.next().toString());
        }
        logger.finest("================== Headings ======================");
        for (Iterator i = getHeadings().iterator(); i.hasNext();) {
            logger.finest(i.next().toString());
        }
    }

    private int findLastCharOffsetOfPreviousWord(String entireContents, int initialPosition) {
        boolean found = false;
        int i = initialPosition;
        while (!found && i >= 0) {
            char currentChar = entireContents.charAt(i);
            //if (i != ' ' && i != '\r' && i != '\n')
            if (currentChar != ' ' && currentChar != '\r' && currentChar != '\n') {
                found = true;
            } else {
                i--;
            }
        }
        if (i < 0) {
            i = 0;
        }
        return i;
    }

    private void initSectionMap() {
        sectionMap = new HashMap<String, List<Range>>();
        for (Iterator<Range> i = this.getFullRangeListAdjusted().iterator(); i.hasNext();) {
            Range theRange = i.next();
            String theLabel = theRange.getLabel();
            List<Range> theList = sectionMap.get(theLabel);
            if (theList == null) {
                theList = new ArrayList<Range>();
                sectionMap.put(theLabel, theList);
            }
            theList.add(theRange);
        }
    }

    protected static class AttributesHolder {

        private String medical;
        private String temporal;
        private String subject;
        private String uncertain;
        private String negated;

        public AttributesHolder() {
            this.medical = null;
            this.temporal = null;
            this.subject = null;
            this.uncertain = null;
            this.negated = null;
        }

        public AttributesHolder(String medical, String temporal, String subject, String uncertain, String negated) {
            this.medical = medical;
            this.temporal = temporal;
            this.subject = subject;
            this.uncertain = uncertain;
            this.negated = negated;
        }

        public AttributesHolder copy (AttributesHolder origAttrs) {
            return new AttributesHolder(origAttrs.medical, origAttrs.temporal, origAttrs.subject, origAttrs.uncertain, origAttrs.negated);
        }
        
        @Override
        public String toString() {
            return String.format("AttributesHolder Med: %s, Temp: %s, Subj: %s, Unc: %s, Neg: %s",
                    ((this.medical==null)?"null":this.medical),
                    ((this.temporal==null)?"null":this.temporal),
                    ((this.subject==null)?"null":this.subject),
                    ((this.uncertain==null)?"null":this.uncertain),
                    ((this.negated==null)?"null":this.negated));
        }
        
        /**
         * 
         * @param newAttrs another AttributesHolder object whose values should
         * be merged into this AttributeHolder, with its values taking precedence
         * over those already set for this object
         */
        public void merge(AttributesHolder newAttrs) {
            logger.finest(String.format("Merging %s into %s", newAttrs.toString(), this.toString()));
            this.medical = ((newAttrs.medical==null || newAttrs.medical.isEmpty())?this.medical:newAttrs.medical);
            this.temporal = ((newAttrs.temporal==null || newAttrs.temporal.isEmpty())?this.temporal:newAttrs.temporal);
            this.subject = ((newAttrs.subject==null || newAttrs.subject.isEmpty())?this.subject:newAttrs.subject);
            this.uncertain = ((newAttrs.uncertain ==null || newAttrs.uncertain.isEmpty())?this.uncertain :newAttrs.uncertain );
            this.negated = ((newAttrs.negated==null || newAttrs.negated.isEmpty())?this.negated:newAttrs.negated);
        }
        
        public String getMedical() {
            return medical;
        }
        // maybeSetters only change the value if there wasn't one there
        // already -- in this way the earliest value set takes precedence
        // over later ones
        // as we drill down through fragments, we get the most specific
        // attribute information earlier, and only fill in the less
        // specific default if a more specific value was not found/set
        
        public void maybeSetMedical(String medical) {
            if (this.medical == null) {
                this.medical = medical;
            }
        }

        public String getNegated() {
            return negated;
        }


        public void maybeSetNegated(String negated) {
            if (this.negated == null) {
                this.negated = negated;
            }
        }

        public String getSubject() {
            return subject;
        }

        public void maybeSetSubject(String subject) {
            if (this.subject == null) {
                this.subject = subject;
            }
        }

        public String getTemporal() {
            return temporal;
        }

        public void maybeSetTemporal(String temporal) {
            if (this.temporal == null) {
                this.temporal = temporal;
            }
        }

        public String getUncertain() {
            return uncertain;
        }

        public void maybeSetUncertain(String uncertain) {
            if (this.uncertain == null) {
                this.uncertain = uncertain;
            }
        }
    }

    public class Range implements Comparable<Range> {

        public Range() {
        }
        protected int begin;
        protected int end;
        // these default to null and are only set if convertOffsets is true
        // in the ZonerCli
        protected LineAndTokenPosition beginLineAndToken = null;
        protected LineAndTokenPosition endLineAndToken = null;
        protected AttributesHolder attrs;
        protected String label;
        /**
        protected String medical;
        protected String temporal;
        protected String subject;
        protected String uncertain;
        protected String negated;
         * */
        
        protected boolean ignore;
        protected boolean truncated;

        @Override
        public String toString() {
            return String.format("RANGE \"%s\" [%d-%d] Med: %s, Temp: %s, Subj: %s, Unc: %s, Neg: %s",
                    label, begin, end, attrs.getMedical(), attrs.getTemporal(), 
                    attrs.getSubject(), attrs.getUncertain(), attrs.getNegated());
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

        public String getMedical() {
            return attrs.getMedical();
        }
/**
        public void setMedical(String medical) {
            this.medical = medical;
        } */

        public String getNegated() {
            return attrs.getNegated();
        }
/**
        public void setNegated(String negated) {
            this.negated = negated;
        } */

        public String getSubject() {
            return attrs.getSubject();
        }
/**
        public void setSubject(String subject) {
            this.subject = subject;
        } */

        public String getTemporal() {
            return attrs.getTemporal();
        }
/**
        public void setTemporal(String temporal) {
            this.temporal = temporal;
        } */

        public String getUncertain() {
            return attrs.getUncertain();
        }

        
    /**
        public void setUncertain(String uncertain) {
            this.uncertain = uncertain;
        } */
        
        public AttributesHolder getAttrs() {
            return attrs;
        }

        public void setAttrs(AttributesHolder attrs) {
            this.attrs = attrs;
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
        protected String medical;
        protected String temporal;
        protected String subject;
        protected String uncertain;
        protected String negated;
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

        public String getMedical() {
            return medical;
        }

        public void setMedical(String medical) {
            this.medical = medical;
        }

        public String getNegated() {
            return negated;
        }

        public void setNegated(String negated) {
            this.negated = negated;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTemporal() {
            return temporal;
        }

        public void setTemporal(String temporal) {
            this.temporal = temporal;
        }

        public String getUncertain() {
            return uncertain;
        }

        public void setUncertain(String uncertain) {
            this.uncertain = uncertain;
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
        /**
        protected String medical;
        protected String temporal;
        protected String subject;
        protected String uncertain;
        protected String negated;
         * **/
        protected AttributesHolder attrs;
        protected boolean findAll;
        protected String subsectionOf = null;
        // a map from fragment level to labels at that level
        protected Map<Integer,List<String>> fragmentLevelsMap = null;
        // a map from fragment labels to additional attributes the fragment carries
        // let's keep this global?
        // protected Map<String, AttributesHolder> fragmentAttrsMap = null;
        
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

        public String getMedical() {
            return attrs.getMedical();
        }

        /*
        public void setMedical(String medical) {
            this.medical = medical;
        } */

        public String getNegated() {
            return attrs.getNegated();
        }

        /*
        public void setNegated(String negated) {
            this.negated = negated;
        } */

        public String getSubject() {
            return attrs.getSubject();
        }

        /*
        public void setSubject(String subject) {
            this.subject = subject;
        } */

        public String getTemporal() {
            return attrs.getTemporal();
        }

        /*
        public void setTemporal(String temporal) {
            this.temporal = temporal;
        } */

        public String getUncertain() {
            return attrs.getUncertain();
        }

        /*
        public void setUncertain(String uncertain) {
            this.uncertain = uncertain;
        } */
        
        public void setAttributes(AttributesHolder attrs) {
            this.attrs = attrs;
          
        }

        public boolean isFindAll() {
            return findAll;
        }

        public void setFindAll(boolean findAll) {
            this.findAll = findAll;
        }

        public String getSubsectionOf() {
            return subsectionOf;
        }

        public void setSubsectionOf(String subsectionOf) {
            this.subsectionOf = subsectionOf;
        }

        /**
        public Map<String, AttributesHolder> getFragmentAttrsMap() {
            return fragmentAttrsMap;
        }

        public void setFragmentAttrsMap(Map<String, AttributesHolder> fragmentAttrsMap) {
            this.fragmentAttrsMap = fragmentAttrsMap;
        }
         * **/

        public Map<Integer, List<String>> getFragmentsMap() {
            return fragmentLevelsMap;
        }

        public void setFragmentsMap(Map<Integer, List<String>> fragmentsMap) {
            this.fragmentLevelsMap = fragmentsMap;
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

    public CharacterOffsetToLineTokenConverter getConverter() {
        return converter;
    }

    public void setConverter(CharacterOffsetToLineTokenConverter converter) {
        this.converter = converter;
    }
}
