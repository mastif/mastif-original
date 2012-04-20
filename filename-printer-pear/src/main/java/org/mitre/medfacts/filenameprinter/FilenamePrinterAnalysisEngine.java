package org.mitre.medfacts.filenameprinter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.examples.SourceDocumentInformation_Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

public class FilenamePrinterAnalysisEngine extends JCasAnnotator_ImplBase
{
  public static final Logger logger = Logger.getLogger(FilenamePrinterAnalysisEngine.class.getName());


  String outputPrefix;
  String outputSuffix;
  
  @Override
  public void initialize(UimaContext context)
      throws ResourceInitializationException
  {
    super.initialize(context);
    
    outputPrefix = (String)context.getConfigParameterValue("outputPrefix");
    outputSuffix = (String)context.getConfigParameterValue("outputSuffix");
  }

  @Override
  public void process(JCas jcas)
      throws AnalysisEngineProcessException
  {
    logger.severe("BEGINNING OF FilenamePrinterAnalysisEngine");
    System.out.println("BEGINNING OF FilenamePrinterAnalysisEngine");
    String currentFilename = null;
    
    int type = SourceDocumentInformation.type;
    
    AnnotationIndex<Annotation> annotationIndex = jcas.getAnnotationIndex(type);
    if (annotationIndex == null)
    {
      logger.info("cas returned null AnnotationIndex. continuing...");
      return;
    }
    
    FSIterator<Annotation> iterator = annotationIndex.iterator();
    if (iterator == null)
    {
      logger.info("AnnotationIndex returned null iterator. continuing...");
      return;
    }
    
    if (!iterator.hasNext())
    {
      logger.info("AnnotationIndex's iterator returned empty iterator. continuing...");
      return;
    }
    
    SourceDocumentInformation sdi = (SourceDocumentInformation)iterator.next();
    String fullUriString = sdi.getUri();
    URI fullUri;
    URL fullUrl;
    try
    {
      fullUri = new URI(fullUriString);
      fullUrl = fullUri.toURL();
    } catch (URISyntaxException e)
    {
      String message = "URISyntaxException while retrieving SourceDocumentInformation's URI (to get the filename).";
      logger.log(Level.SEVERE, message, e);
      return;
    } catch (MalformedURLException e)
    {
      String message = "MalformedURLException while retrieving SourceDocumentInformation's URI (to get the filename).";
      logger.log(Level.SEVERE, message, e);
      return;
    }
    String filename = fullUrl.getFile();
    
    if (iterator.hasNext())
    {
      logger.info("document has more than one SourceDocumentInformation annotations. only looking at first one. continuing...");
    }
    
    String message = String.format("%s%s%s", outputPrefix, filename, outputSuffix);
    System.out.println(message);
    logger.severe(message);
  }

}
