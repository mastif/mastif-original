package org.mitre.medfacts.i2b2.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class AssertionXmlOutputLogger
{
  protected XMLOutputFactory outputFactory;
  protected XMLStreamWriter w;

  FileWriter fileWriter = null;
  BufferedWriter bufferedWriter = null;

  protected String baseDirectory;
  protected String fileNameSuffix;

  public AssertionXmlOutputLogger()
  {

  }

  public void init()
  {
    {
      String assertionXmlOutputFilename = null;
      if (fileNameSuffix != null && !fileNameSuffix.isEmpty())
      {
        assertionXmlOutputFilename = "assertion_output_debug." + fileNameSuffix + ".xml";
      } else
      {
        assertionXmlOutputFilename = "assertion_output_debug.xml";
      }
      try
      {
        File baseDirectoryFile = new File(getBaseDirectory());
        File assertionXmlOutputFile = new File(baseDirectoryFile, assertionXmlOutputFilename);

        fileWriter = new FileWriter(assertionXmlOutputFile);
        bufferedWriter = new BufferedWriter(fileWriter);

        outputFactory = XMLOutputFactory.newInstance();
        w = outputFactory.createXMLStreamWriter(bufferedWriter);
      } catch (IOException ex)
      {
        String message = "IOException while creating assertionXmlOutput file";
        Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      } catch (XMLStreamException ex)
      {
        String message = "XMLStreamException while creating assertionXmlOutput file";
        Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, null, ex);
        throw new RuntimeException(message, ex);
      }
    }
  }

  public void startDocument()
  {
    try
    {
      w.writeStartDocument("UTF-8", "1.0");
      w.writeStartElement(("all"));

    } catch (XMLStreamException ex)
    {
      String message = "XMLStreamException during document construction for assertion xml output";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException("message", ex);
    }
  }

  public void finishDocument()
  {
    try
    {
      w.writeEndElement();
      w.writeEndDocument();

    } catch (XMLStreamException ex)
    {
      String message = "XMLStreamException during document construction for assertion xml output";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException("message", ex);
    }
    cleanup();
  }

  public void cleanup()
  {
    try
    {
      w.flush();
    } catch (XMLStreamException ex)
    {
      String message = "XMLStreamException on flushing assertion debug output xml stream";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
    }
    try
    {
      w.close();
    } catch (XMLStreamException ex)
    {
      String message = "XMLStreamException on closing assertion debug output xml stream";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
    }
    try
    {
      bufferedWriter.close();
    } catch (IOException ex)
    {
      String message = "IOException on closing assertion debug output BufferedWriter";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
    }
    try
    {
      fileWriter.close();
    } catch (IOException ex)
    {
      String message = "IOException on closing assertion debug output FileWriter";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
    }
  }

  public void addAssertion
    (
      Boolean matchStatus,
      String actual,
      String expected,
      String fileName,
      Integer lineNumber,
      Integer assertionLineNumber,
      Collection<String> featureList
    )
  {
    try
    {
      w.writeStartElement("assertion-row");

      w.writeStartElement("match-status");
      if (matchStatus != null)
      {
        w.writeCharacters(matchStatus ? "true" : "false");
      }
      w.writeEndElement();

      w.writeStartElement("actual");
      w.writeCharacters(actual);
      w.writeEndElement();

      w.writeStartElement("expected");
      w.writeCharacters(expected);
      w.writeEndElement();

      w.writeStartElement("filename");
      w.writeCharacters(fileName);
      w.writeEndElement();

      w.writeStartElement("line-number");
      if (lineNumber != null)
      {
        w.writeCharacters(Integer.toString(lineNumber));
      }
      w.writeEndElement();

      w.writeStartElement("assertion-line-number");
      if (assertionLineNumber != null)
      {
        w.writeCharacters(Integer.toString(assertionLineNumber));
      }
      w.writeEndElement();

      w.writeStartElement("features");

      for (String currentFeature : featureList)
      {
        w.writeStartElement("feature");
        w.writeCharacters(currentFeature);
        w.writeEndElement();
      }

      // end features
      w.writeEndElement();

      // end assertion-row
      w.writeEndElement();

    } catch (XMLStreamException ex)
    {
      String message = "XMLStreamException during document construction for assertion xml output";
      Logger.getLogger(AssertionXmlOutputLogger.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException("message", ex);
    }

  }

  public String getBaseDirectory()
  {
    return baseDirectory;
  }

  public void setBaseDirectory(String baseDirectory)
  {
    this.baseDirectory = baseDirectory;
  }

  public String getFileNameSuffix()
  {
    return fileNameSuffix;
  }

  public void setFileNameSuffix(String fileNameSuffix)
  {
    this.fileNameSuffix = fileNameSuffix;
  }


}
