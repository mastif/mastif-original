package org.mitre.medfacts.uimaloader;

import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: MCOARR
 * Date: Apr 12, 2010
 * Time: 9:42:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class UimaLoaderCli
{
    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    protected String filename;

    public void execute()
    {
        System.out.format(" - input file: %s%n", filename);

        try
        {

            File inputFile = new File(filename);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document inputDom = builder.parse(inputFile);

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            XPathExpression sectionExpression = xpath.compile("//section");
            XPathExpression subsectionExpression = xpath.compile("//subsection");
            XPathExpression xcopeExpression = xpath.compile("//xcope");
            XPathExpression cueExpression = xpath.compile("//cue");
            XPathExpression allTextExpression = xpath.compile("//text()");

            //String allTextString = (String)allTextExpression.evaluate(inputDom, XPathConstants.STRING);

            NodeList allTextResult = (NodeList)allTextExpression.evaluate(inputDom, XPathConstants.NODESET);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i < allTextResult.getLength(); i++)
            {
                Node currentNode = allTextResult.item(i);
                String text = currentNode.getTextContent();
                stringBuilder.append(text);
            }
            String allTextString = stringBuilder.toString();

            System.out.format("###%n%s%n###", allTextString);

            NodeList sectionNodeList = (NodeList)sectionExpression.evaluate(inputDom, XPathConstants.NODESET);

            int sectionNodeListLength = sectionNodeList.getLength();
            System.out.format("sectionNodeList size: %d%n", sectionNodeListLength);

            for (int i = 0; i < sectionNodeListLength; i++)
            {
                Node currentSectionNode = sectionNodeList.item(i);
                String temp = (currentSectionNode == null ? "NULL" : currentSectionNode.getNodeName());
                System.out.format(" - current node: %s%n", temp);
            }

            NodeSubtreeCrawler crawler = new NodeSubtreeCrawler();
            crawler.traverseNode(inputDom);

            List<TextSegment> result = crawler.getTextSegmentList();
            List<Segment> allSegmentsList = crawler.getAllSegmentList();

            System.out.format("=== Strings BEGIN ===%n");
            for (int i=0; i < result.size(); i++)
            {
                TextSegment currentSegment = result.get(i);

                //System.out.format(" - %d) [%d/%d] \"%s\"%n", i, currentSegment.getBegin(), currentSegment.getEnd(), currentSegment.getText());
                System.out.format(" - %d) %s%n", i, currentSegment);
            }
            System.out.format("=== Strings END ===%n");

            System.out.format("=== Segments BEGIN ===%n");
            for (int i=0; i < allSegmentsList.size(); i++)
            {
                Segment currentSegment = allSegmentsList.get(i);

                System.out.format(" - %d) %s%n", i, currentSegment);
            }
            System.out.format("=== Segments END ===%n");

            NodeAndSegmentsList nodeAndSegmentsList = new NodeAndSegmentsList();

            nodeAndSegmentsList.processNodes(allSegmentsList);
            nodeAndSegmentsList.postProcess();

            // todo print out node and segment list...

            List<NodeAndSegments> returnedList  = nodeAndSegmentsList.getList();
            for (NodeAndSegments currentNodeAndSegments : returnedList)
            {
                Node currentNode = currentNodeAndSegments.getNode();
                Segment  beginSegment = currentNodeAndSegments.getBeginSegment();
                Segment  endSegment = currentNodeAndSegments.getEndSegment();
                System.out.format("  ***%n");
                System.out.format("    - node: %s%n", currentNode);
                System.out.format("    - begin: %s%n", beginSegment);
                System.out.format("    - end: %s%n", endSegment);
            }

            Document xcasDocument = buildXcasXml(returnedList, allTextString);

            printoutXml(xcasDocument);

            System.out.format("%n===%nfinished%n===%n");
        } catch(IOException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("IOException error processing xml", e);
        } catch (ParserConfigurationException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("ParserConfigurationException error processing xml", e);
        } catch (SAXException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("SAXException error processing xml", e);
        } catch (XPathExpressionException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("XPathExpressionException error processing xml", e);
        } catch (ClassNotFoundException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("ClassNotFoundException error processing xml", e);
        } catch (InstantiationException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("InstantiationException error processing xml", e);
        } catch (IllegalAccessException e)
        {
            System.err.format("ERROR: %s%n", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("IllegalAccessException error processing xml", e);
        }
    }

    private void printoutXml(Document xcasDocument)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        DOMImplementationRegistry registry =
             DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl =
            (DOMImplementationLS)registry.getDOMImplementation("LS");

        LSParser builder = impl.createLSParser(
                         DOMImplementationLS.MODE_SYNCHRONOUS, null);



        LSSerializer serializer = impl.createLSSerializer();
        LSOutput output = impl.createLSOutput();
        //output.setSystemId("file:///personalout.xml");

        StringWriter writer = new StringWriter();
        output.setCharacterStream(writer);


        serializer.write(xcasDocument, output);

        String outputString = writer.toString();
        System.out.format("=== OUTPUT XML BEGIN ===%n");
        System.out.println(outputString);
        System.out.format("=== OUTPUT XML END ===%n");
    }

    private Document buildXcasXml(List<NodeAndSegments> list, String documentText)
            throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document inputDom = builder.newDocument();

        Element rootCasElement = inputDom.createElement("CAS");
        rootCasElement.setAttribute("version", "2");

        Element sofaElement = inputDom.createElement("uima.cas.Sofa");
        sofaElement.setAttribute("_indexed", "0");
        sofaElement.setAttribute("_id", "1");
        sofaElement.setAttribute("sofaNum", "1");
        sofaElement.setAttribute("sofaID", "_InitialView");
        sofaElement.setAttribute("mimeType", "text");
        sofaElement.setAttribute("sofaString", documentText);
        rootCasElement.appendChild(sofaElement);

        inputDom.appendChild(rootCasElement);

        Element documentAnnotationElement = inputDom.createElement("uima.tcas.DocumentAnnotation");
        documentAnnotationElement.setAttribute("_indexed", "1");
        documentAnnotationElement.setAttribute("_id", "8");
        documentAnnotationElement.setAttribute("_ref_sofa", "1");
        documentAnnotationElement.setAttribute("begin", "0");
        documentAnnotationElement.setAttribute("end", documentText.length()+"");
        documentAnnotationElement.setAttribute("language", "x-unspecified");
        rootCasElement.appendChild(documentAnnotationElement);

        Set<String> interestedAnnotationSet = new TreeSet<String>();
        interestedAnnotationSet.add("section");
        interestedAnnotationSet.add("subsection");
        interestedAnnotationSet.add("xcope");
        interestedAnnotationSet.add("cue");

        int annotationId = 10;
        nodeAndSegmentsLoop:
        for (NodeAndSegments currentNodeAndSegments : list)
        {
            String annotationType = currentNodeAndSegments.getNode().getNodeName();
            if (!interestedAnnotationSet.contains(annotationType))
            {
                continue nodeAndSegmentsLoop;
            }
            Element currentAnnotationElement = inputDom.createElement("org.mitre.medfact.type." + annotationType);
            currentAnnotationElement.setAttribute("_indexed", "1");
            currentAnnotationElement.setAttribute("_ref_sofa", "1");
            //currentAnnotationElement.setAttribute("id", "" + annotationId);
            currentAnnotationElement.setAttribute("id", "" + currentNodeAndSegments.getId());
            Integer parentId = currentNodeAndSegments.getParentId();
            if (parentId != null)
            {
                currentAnnotationElement.setAttribute("parentId", "" + parentId);
            }

            currentAnnotationElement.setAttribute("begin", "" + currentNodeAndSegments.getBeginSegment().getLocation());
            currentAnnotationElement.setAttribute("end", "" + currentNodeAndSegments.getEndSegment().getLocation());
            rootCasElement.appendChild(currentAnnotationElement);
            annotationId++;
        }

        return inputDom;

//        for (NodeAndSegments currentNodeAndSegments : returnedList)
//        {
//            Node currentNode = currentNodeAndSegments.getNode();
//            Segment  beginSegment = currentNodeAndSegments.getBeginSegment();
//            Segment  endSegment = currentNodeAndSegments.getEndSegment();
//        }
    }


    public static void main(String args[])
    {
        UimaLoaderCli cli = new UimaLoaderCli();
        String filename = args[0];
        cli.setFilename(filename);
        cli.execute();
    }

    public class NodeSubtreeCrawler
    {
        List<TextSegment> textSegmentList = new ArrayList<TextSegment>();
        List<Segment> allSegmentList = new ArrayList<Segment>();
        int currentPointerOffset = 0;

        public void traverseNode(Node context)
        {
            NodeList childNodeList = context.getChildNodes();
            for (int i = 0; i < childNodeList.getLength(); i++)
            {
                Node currentNode = childNodeList.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element currentElement = (Element)currentNode;
                    int begin = currentPointerOffset;
                    ElementBeginSegment newBeginSegment = new ElementBeginSegment(begin, currentElement);
                    allSegmentList.add(newBeginSegment);

                    traverseNode(currentNode);

                    int end = currentPointerOffset;
                    ElementEndSegment newEndSegment = new ElementEndSegment(end, currentElement);
                    allSegmentList.add(newEndSegment);
                } else if(currentNode.getNodeType() == Node.TEXT_NODE)
                {
                    Text currentTextNode = (Text)currentNode;
                    String textString = currentTextNode.getTextContent();
                    int begin = currentPointerOffset;
                    int length = textString.length();
                    int end = begin + length;
                    TextSegment newSegment = new TextSegment(textString, begin, end, currentTextNode);
                    textSegmentList.add(newSegment);
                    allSegmentList.add(newSegment);
                    currentPointerOffset = end;
                }  else if (currentNode.getNodeType() == Node.CDATA_SECTION_NODE)
                {
                    Text currentTextNode = (Text)currentNode;
                    String textString = currentTextNode.getTextContent();
                    int begin = currentPointerOffset;
                    int length = textString.length();
                    int end = begin + length;
                    TextSegment newSegment = new TextSegment(textString, begin, end, currentTextNode);
                    textSegmentList.add(newSegment);
                    allSegmentList.add(newSegment);
                    currentPointerOffset = end;
                }
            }
        }

        public List<TextSegment> getTextSegmentList()
        {
            return textSegmentList;
        }

        public List<Segment> getAllSegmentList()
        {
            return allSegmentList;
        }

    }

    public class Segment
    {
        protected Node node;

        public Segment(Node node)
        {
            this.node = node;
        }

        public Node getNode()
        {
            return node;
        }

        public void setNode(Node node)
        {
            this.node = node;
        }
    }

    public class SpecificLocationSegment extends Segment
    {
        public SpecificLocationSegment(int location, Node node)
        {
            super(node);
            this.location = location;
        }

        protected int location;

        public int getLocation()
        {
            return location;
        }

        public void setLocation(int location)
        {
            this.location = location;
        }
    }

    public class ElementBeginSegment extends SpecificLocationSegment
    {
        public ElementBeginSegment(int location, Node node)
        {
            super(location, node);
        }
        public String toString()
        {
            StringBuilder b = new StringBuilder();
            b.append("[");
            b.append(getLocation());
            b.append("] ");
            Element currentElement = (Element)getNode();
            b.append(currentElement.getTagName());
            b.append(" BEGIN");
            return b.toString();
        }
    }

    public class ElementEndSegment extends SpecificLocationSegment
    {
        public ElementEndSegment(int location, Node node)
        {
            super(location, node);
        }
        public String toString()
        {
            StringBuilder b = new StringBuilder();
            b.append("[");
            b.append(getLocation());
            b.append("] ");
            Element currentElement = (Element)getNode();
            b.append(currentElement.getTagName());
            b.append(" END");
            return b.toString();
        }
    }

    public class RangeSegment extends Segment
    {
        protected int begin;
        protected int end;

        public RangeSegment(int begin, int end, Node node)
        {
            super(node);
            this.begin = begin;
            this.end = end;
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
    }

    public class TextSegment extends RangeSegment
    {
        protected String text;

        public TextSegment (String text, int begin, int end, Text textNode)
        {
            super(begin, end, textNode);
            this.text = text;
        }

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public String toString()
        {
            StringBuilder b = new StringBuilder();
            b.append("[");
            b.append(getBegin());
            b.append("/");
            b.append(getEnd());
            b.append("] \"");
            b.append(getText());
            b.append("\"");
            return b.toString();
        }

    }

    public class NodeAndSegments
    {
        protected Node node;
        protected ElementBeginSegment beginSegment;
        protected ElementEndSegment endSegment;
        protected int id;
        protected Integer parentId;

        public Node getNode()
        {
            return node;
        }

        public void setNode(Node node)
        {
            this.node = node;
        }

        public ElementBeginSegment getBeginSegment()
        {
            return beginSegment;
        }

        public void setBeginSegment(ElementBeginSegment beginSegment)
        {
            this.beginSegment = beginSegment;
        }

        public ElementEndSegment getEndSegment()
        {
            return endSegment;
        }

        public void setEndSegment(ElementEndSegment endSegment)
        {
            this.endSegment = endSegment;
        }

        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public Integer getParentId()
        {
            return parentId;
        }

        public void setParentId(Integer parentId)
        {
            this.parentId = parentId;
        }
    }

    public class NodeAndSegmentsList
    {
        List<NodeAndSegments> myList = new ArrayList<NodeAndSegments>();

        public List<NodeAndSegments> getList()
        {
            return myList;
        }

        public void add(NodeAndSegments nodeAndSegments)
        {
            myList.add(nodeAndSegments);
        }

        public NodeAndSegments retrieveByNode(Node node)
        {
            boolean found = false;
            int foundLocation = -1;
            NodeAndSegments foundNodeAndSegments = null;

            for (int i = 0; i < myList.size() && !found; i++)
            {
                NodeAndSegments currentNodeAndSegments = myList.get(i);
                Node currentNode = currentNodeAndSegments.getNode();
                if (node.equals(currentNode))
                {
                    found = true;
                    foundLocation = i;
                    foundNodeAndSegments = currentNodeAndSegments;
                }
            }
            return foundNodeAndSegments;
        }

        public void processNodes(List<Segment> allSegmentsList)
        {
            int currentIdValue = 10;
            for (Segment currentSegment : allSegmentsList)
            {
                if (currentSegment instanceof ElementBeginSegment)
                {
                    ElementBeginSegment beginSegment = (ElementBeginSegment)currentSegment;
                    Node node = beginSegment.getNode();

                    // find end node
                    ElementEndSegment endSegment = null;
                    boolean isEndFound = false;
                    for (int i = 0; i < allSegmentsList.size() && !isEndFound; i++)
                    {
                        Segment otherSegment = allSegmentsList.get(i);
                        if (otherSegment instanceof ElementEndSegment && !beginSegment.equals(otherSegment) && node.equals(otherSegment.getNode()))
                        {
                            isEndFound = true;
                            endSegment = (ElementEndSegment)otherSegment;
                        }
                    }
                    //todo
                    NodeAndSegments newNodeAndSegments = new NodeAndSegments();
                    newNodeAndSegments.setNode(node);
                    newNodeAndSegments.setBeginSegment(beginSegment);
                    newNodeAndSegments.setEndSegment(endSegment);
                    newNodeAndSegments.setId(currentIdValue);
                    add(newNodeAndSegments);

                    currentIdValue++;
                }
            }
        }

        public void postProcess()
        {
            Map<String, NodeAndSegments> xcopeMap = new TreeMap<String, NodeAndSegments>();

            // construct map of xcope ids to NodeAndSegment objects (which will
            // be used to find the parent xcopes for the cue annotations)
            for (NodeAndSegments current : getList())
            {
                Node currentNode = current.getNode();
                Element currentElement = (Element)currentNode;
                if (currentElement.getTagName().equals("xcope"))
                {
                    String id = currentElement.getAttribute("id");
                    xcopeMap.put(id, current);
                }
            }

            // iterate over the cue annotations and find their parent xcopes
            for (NodeAndSegments current : getList())
            {
                Node currentNode = current.getNode();
                Element currentElement = (Element)currentNode;
                if (currentElement.getTagName().equals("cue"))
                {
                    String ref = currentElement.getAttribute("ref");
                    NodeAndSegments parentXcopeNodeAndSegments = xcopeMap.get(ref);
                    if (parentXcopeNodeAndSegments != null)
                    {
                        int parentXcopeId = parentXcopeNodeAndSegments.getId();
                        current.setParentId(parentXcopeId);
                    }
                }
            }
        }
    }
}
