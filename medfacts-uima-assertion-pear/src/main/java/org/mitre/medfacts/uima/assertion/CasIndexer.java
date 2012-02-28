package org.mitre.medfacts.uima.assertion;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.mayo.bmi.uima.core.type.textsem.EntityMention;

public class CasIndexer<T extends Annotation>
{
  private Logger logger = Logger.getLogger(CasIndexer.class.getName());
  private JCas jcas;
  protected Map<Integer, T> mapByAddress;
  
  public CasIndexer(JCas jcas)
  {
    this.jcas = jcas;
    initialize();
  }
  
  public CasIndexer()
  {
  }
  
  public void initialize()
  {
    AnnotationIndex<Annotation> annotationIndex = jcas.getAnnotationIndex();
    
    mapByAddress = new HashMap<Integer, T>();
    
    logger.info("    before iterating over all annotations in index...");
    for (Annotation annotation : annotationIndex)
    {
      logger.info("    begin single annotation");
      Integer address = annotation.getAddress();
      logger.info(String.format("      address: %d; type: %s", address, annotation.getClass().getName()));
      T current = (T)annotation;
      
      mapByAddress.put(address,  current);
      logger.info("    end single annotation");
    }
    logger.info("    after iterating over all annotations in index...");
    
  }
  
  public Annotation lookupByAddress(int address)
  {
    return mapByAddress.get(address);
  }

  public JCas getJcas()
  {
    return jcas;
  }

  public void setJcas(JCas jcas)
  {
    this.jcas = jcas;
  }
}
