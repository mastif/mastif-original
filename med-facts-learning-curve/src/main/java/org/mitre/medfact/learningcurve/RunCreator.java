package org.mitre.medfact.learningcurve;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Hello world!
 *
 */
public class RunCreator
{
  public static final Logger logger = Logger.getLogger(RunCreator.class.getName());
  double percentages[] = {2.5, 5.0, 7.5, 10.0, 15.0, 20.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0 };

  protected File inputDirectory;

  public static void main(String[] args)
    throws IOException
  {
    String inputDirectoryString = args[0];
    File inputDirectory = new File(inputDirectoryString);

    RunCreator lc = new RunCreator();
    lc.setInputDirectory(inputDirectory);
    lc.execute();
  }

  public void execute() throws IOException
  {
    FilenameFilter filenameFilter = new FilenameFilter() {

      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    };
    File fileArray[] = getInputDirectory().listFiles(filenameFilter);

    TreeMap<Double,File> fileMap = new TreeMap<Double,File>();
    long seed = (new Date()).getTime();
    logger.info(String.format("random seed: %d", seed));
    Random random = new Random(seed);

    //logger.info("BEGIN FILE LIST");
    for (File f : fileArray)
    {
      double key = random.nextDouble();
      fileMap.put(key, f);
      //logger.info(String.format("FILE: %s", f.getCanonicalPath()));
    }
    //logger.info("END FILE LIST");

    //logger.info("BEGIN FILE MAP");
    int i = 0;
    ArrayList<File> fileList = new ArrayList<File>();
    for (Entry<Double, File> entry : fileMap.entrySet())
    {
      double key = entry.getKey();
      File value = entry.getValue();
      fileList.add(value);
      //logger.info(String.format("[%d] %7.5f %s", i, key, value.getCanonicalPath()));
      
      i++;
    }
    //logger.info("END FILE MAP");

    //logger.info("BEGIN SORTED FILE LIST");
    i = 0;
    for (File f : fileList)
    {
      //logger.info(String.format("%3d: %s", i, f.getCanonicalPath()));
      i++;
    }
    //logger.info("END SORTED FILE LIST");

    Document fileListXml = writeFileListToXml(fileMap, fileList);
    String fileListXmlString = writeXmlToString(fileListXml);

    logger.info(String.format("file xml: %s", fileListXmlString));

  }

  public File getInputDirectory()
  {
    return inputDirectory;
  }

  public void setInputDirectory(File inputDirectory)
  {
    this.inputDirectory = inputDirectory;
  }

  public Document writeFileListToXml(TreeMap<Double, File> fileMap, ArrayList<File> fileList)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

      Document doc = documentBuilder.newDocument();

      Element rootElement = doc.createElement("root");
      doc.appendChild(rootElement);

      Element allFilesElement = doc.createElement("all-files");
      rootElement.appendChild(allFilesElement);

      int i = 0;
      for (Entry<Double, File> entry : fileMap.entrySet())
      {
        double key = entry.getKey();
        File value = entry.getValue();

        Element fileElement = doc.createElement("file");
        allFilesElement.appendChild(fileElement);

        fileElement.setAttribute("number", Integer.toString(i));
        fileElement.setAttribute("random", Double.toString(key));

        Text fileTextNode = doc.createTextNode(value.getCanonicalPath());
        fileElement.appendChild(fileTextNode);

        i++;
      }
      createRuns(doc, fileMap, fileList, percentages);

      return doc;

    } catch(ParserConfigurationException e)
    {
      String message = "Parser Config Exception while building document";
      logger.log(Level.SEVERE, message, e);
      throw new RuntimeException(message, e);
    } catch(IOException e)
    {
      String message = "IO Exception while building document";
      logger.log(Level.SEVERE, message, e);
      throw new RuntimeException(message, e);
    }
  }

  public String writeXmlToString(Document doc)
  {
      DOMImplementationRegistry registry = null;
      try
      {
        registry = DOMImplementationRegistry.newInstance();
      } catch (ClassNotFoundException ex)
      {
        String message = "ClassNotFoundException during DOM LS";
        Logger.getLogger(RunCreator.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      } catch (InstantiationException ex)
      {
        String message = "InstantiationException during DOM LS";
        Logger.getLogger(RunCreator.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      } catch (IllegalAccessException ex)
      {
        String message = "IllegalAccessException during DOM LS";
        Logger.getLogger(RunCreator.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      } catch (ClassCastException ex)
      {
        String message = "ClassCastException during DOM LS";
        Logger.getLogger(RunCreator.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      }
      DOMImplementationLS impl =
          (DOMImplementationLS)registry.getDOMImplementation("LS");

      LSSerializer serializer = impl.createLSSerializer();

      DOMConfiguration domConfig = serializer.getDomConfig();

      if (domConfig.canSetParameter("format-pretty-print", Boolean.TRUE))
      {
         domConfig.setParameter("format-pretty-print", Boolean.TRUE);
      }

      String outputXmlString = serializer.writeToString(doc);
      return outputXmlString;
  }

  private void createRuns(Document doc, TreeMap<Double, File> fileMap, ArrayList<File> fileList, double[] percentages)
  {
    Element allRunsElement = doc.createElement("all-runs");
    doc.getDocumentElement().appendChild(allRunsElement);

    int fullSize = fileMap.size();

    int i = 0;
    for (double currentPercentage : percentages)
    {
      Element runElement = doc.createElement("run");
      allRunsElement.appendChild(runElement);

      runElement.setAttribute("run-number", Integer.toString(i));
      runElement.setAttribute("percentage", String.format("%.2f", currentPercentage));

      int max = (int)Math.floor(currentPercentage / 100.0 * fullSize);

      for (int j = 0; j < max; j++)
      {
        Element fileElement = doc.createElement("file");
        runElement.appendChild(fileElement);
        fileElement.setAttribute("file-number", Integer.toString(j));

        String fileName = fileList.get(j).getAbsolutePath();
        Text fileTextNode = doc.createTextNode(fileName);
        fileElement.appendChild(fileTextNode);
      }

      i++;
    }
  }
}
