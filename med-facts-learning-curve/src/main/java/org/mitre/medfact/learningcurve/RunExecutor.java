package org.mitre.medfact.learningcurve;

import org.mitre.medfacts.i2b2.cli.RunConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.cli.Mode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class RunExecutor
{
  Logger logger = Logger.getLogger(RunExecutor.class.getName());
  // --base-dir="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22" --cue-model="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/cue.model" --scope-model="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/scope.model" --train=train --decode=eval --features-file="featureFile11d_no_class" --gaussian-prior=1.0 --mode=eval

  public static void main(String args[])
  {
    RunExecutor runExecutor = new RunExecutor();

    runExecutor.execute();
  }

  public void execute()
  {
    /*
    String arguments[] = 
      {
        "--base-dir=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22\"",
        "--cue-model=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/cue.model\"",
        "--scope-model=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/scope.model\"",
        "--train=train",
        "--decode=eval",
        "--features-file=\"featureFile11d_no_class\"",
        "--gaussian-prior=1.0",
        "--mode=eval"
      };
    BatchRunner.main(arguments);
    */

    //String baseDirectoryString = "C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22";
    String baseDirectoryString = "/afs/rcf/project/medfact/dev/mcoarr/learning_curve";
    File baseDirectory = new File(baseDirectoryString);

    String runsConfigurationString = "runs.xml";
    File runsConfigurationFile = new File(baseDirectory, runsConfigurationString);

    File dataDirectory = new File(baseDirectory, "data");

    File trainDirectory = new File(dataDirectory, "train");
    File evalDirectory = new File(dataDirectory, "eval");

    String scopeFileString = "scope.model";
    File scopeFile = new File(baseDirectory, scopeFileString);

    String cueFileString = "cue.model";
    File cueFile = new File(baseDirectory, cueFileString);

    ArrayList<RunConfiguration> runConfigurationList = loadRunsConfiguration(runsConfigurationFile);

    int i = 0;
    for (RunConfiguration currentRunConfiguration : runConfigurationList)
    {
      int size = currentRunConfiguration.getFileList().size();
      logger.info(String.format("====== BEGIN RUN # %d WITH %d FILES IN TRAINING SET =====", i, size));

      BatchRunner b = new BatchRunner();
      b.setMode(Mode.EVAL);

      ScopeParser scopeParser = new ScopeParser(scopeFile.getAbsolutePath(), cueFile.getAbsolutePath());
      b.setScopeParser(scopeParser);

      b.setBaseDirectoryString(baseDirectoryString);
      b.setTrainingDirectory(trainDirectory.getAbsolutePath());
      b.setDecodeDirectory(evalDirectory.getAbsolutePath());

      b.setFileNameSuffix(String.format("%03d", currentRunConfiguration.getFileList().size()));

      String featuresFileString = "featureFile11d_with_class";
      File featuresFile = new File(baseDirectory, featuresFileString);

      b.processEnabledFeaturesFile(featuresFile);

      b.setRunConfiguration(currentRunConfiguration);

      b.execute();

      logger.info(String.format("====== END RUN # %d WITH %d FILES IN TRAINING SET =====", i, size));
      i++;
    }


  }

  public ArrayList<RunConfiguration> loadRunsConfiguration(File runsConfigurationFile)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(runsConfigurationFile);

      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      XPathExpression runXpathExpression = xpath.compile("//run");
      
      XPathExpression runNumberXpathExpression = xpath.compile("@run-number");
      XPathExpression runFileXpathExpression = xpath.compile("file");
      XPathExpression fileNameExpression = xpath.compile("text()");

      logger.info("=== RUNS begin ===");
      NodeList runsElementResult = (NodeList) runXpathExpression.evaluate(doc, XPathConstants.NODESET);
      logger.info(String.format("runsElementResult size: %d", runsElementResult.getLength()));

      ArrayList<RunConfiguration> runConfigurationList = new ArrayList<RunConfiguration>();

      for (int i = 0; i < runsElementResult.getLength(); i++)
      {
        Element currentRunsElement = (Element)runsElementResult.item(i);

        RunConfiguration runConfiguration = new RunConfiguration();
        runConfigurationList.add(runConfiguration);

        int runNumber = ((Double)runNumberXpathExpression.evaluate(currentRunsElement, XPathConstants.NUMBER)).intValue();
        logger.info(String.format("  (%d) RUN -- run number: %d", i, runNumber));

        NodeList fileElementNodeList = (NodeList)runFileXpathExpression.evaluate(currentRunsElement, XPathConstants.NODESET);
        for (int j=0; j < fileElementNodeList.getLength(); j++)
        {
          Element fileElement = (Element)fileElementNodeList.item(j);

          String filename = fileNameExpression.evaluate(fileElement);
          File file = new File(filename);
          //logger.info(String.format("    - %s", filename));

          runConfiguration.addFile(file);
        }
      }
      logger.info("=== RUNS end ===");

      return runConfigurationList;

    } catch (XPathExpressionException ex)
    {
      String message = "XPathExpressionException during loading xml runs configuration file";
      Logger.getLogger(RunExecutor.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } catch (SAXException ex)
    {
      String message = "SAXException during loading xml runs configuration file";
      Logger.getLogger(RunExecutor.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } catch (IOException ex)
    {
      String message = "IOException during loading xml runs configuration file";
      Logger.getLogger(RunExecutor.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } catch (ParserConfigurationException ex)
    {
      String message = "ParserConfigurationException during loading xml runs configuration file";
      Logger.getLogger(RunExecutor.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    }
  }
}
