package org.mitre.medfacts.uimaloader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

import javax.xml.xpath.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: MCOARR
 * Date: May 3, 2010
 * Time: 9:30:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestXpath
{
    public static final String ALL_TEXT_XPATH_EXPRESSION = "//text()";
    public static final String CUE_XPATH_EXPRESSION = "//cue";

    public static void main(String args[])
    {
        TestXpath testXpath = new TestXpath();
        String filename = args[0];
        testXpath.setFilename(filename);
        testXpath.execute();
    }

    String filename;

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public void execute()
    {
        System.out.println("=== BEGIN ===");
        try
        {
            File inputFile = new File(filename);
            FileReader fileReader = new FileReader(inputFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            DOMImplementationRegistry registry =
                 DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl =
                (DOMImplementationLS)registry.getDOMImplementation("LS");

            LSParser builder = impl.createLSParser(
                             DOMImplementationLS.MODE_SYNCHRONOUS, null);


            LSInput input = impl.createLSInput();
            input.setCharacterStream(bufferedReader);
            Document document = builder.parse(input);

            bufferedReader.close();
            fileReader.close();


            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            XPathExpression cueExpression = xpath.compile(CUE_XPATH_EXPRESSION);
            XPathExpression allTextExpression = xpath.compile(ALL_TEXT_XPATH_EXPRESSION);

            NodeList result = (NodeList)allTextExpression.evaluate(document, XPathConstants.NODESET);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i < result.getLength(); i++)
            {
                Node currentNode = result.item(i);
                String text = currentNode.getTextContent();
                stringBuilder.append(text);
            }

            String concatenatedText = stringBuilder.toString();
            System.out.format(">>>%s<<<%n", concatenatedText);

        } catch(XPathExpressionException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        } catch (InstantiationException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("problem during processing xpath test", e);
        }

        System.out.println("=== END ===");
    }
}
