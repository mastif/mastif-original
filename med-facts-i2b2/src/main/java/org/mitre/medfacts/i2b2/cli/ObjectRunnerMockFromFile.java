/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.util.List;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.processors.RelationFileProcessor;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;

/**
 *
 * @author MCOARR
 */
public class ObjectRunnerMockFromFile extends ObjectRunner
{
  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();
  protected FileProcessor assertionFileProcessor = new AssertionFileProcessor();
  protected FileProcessor relationFileProcessor = new RelationFileProcessor();
  protected FileProcessor scopeFileProcessor = new ScopeFileProcessor();
  protected List<String> annotationFilenameList;

  public FileProcessor getConceptFileProcessor()
  {
    return conceptFileProcessor;
  }

  public void setConceptFileProcessor(FileProcessor conceptFileProcessor)
  {
    this.conceptFileProcessor = conceptFileProcessor;
  }

  public FileProcessor getAssertionFileProcessor()
  {
    return assertionFileProcessor;
  }

  public void setAssertionFileProcessor(FileProcessor assertionFileProcessor)
  {
    this.assertionFileProcessor = assertionFileProcessor;
  }

  public FileProcessor getRelationFileProcessor()
  {
    return relationFileProcessor;
  }

  public void setRelationFileProcessor(FileProcessor relationFileProcessor)
  {
    this.relationFileProcessor = relationFileProcessor;
  }

  public FileProcessor getScopeFileProcessor()
  {
    return scopeFileProcessor;
  }

  public void setScopeFileProcessor(FileProcessor scopeFileProcessor)
  {
    this.scopeFileProcessor = scopeFileProcessor;
  }

  public List<String> getAnnotationFilenameList()
  {
    return annotationFilenameList;
  }

  public void setAnnotationFilenameList(List<String> annotationFilenameList)
  {
    this.annotationFilenameList = annotationFilenameList;
  }

  public void addAnnotationFilename(String filename)
  {
    this.annotationFilenameList.add(filename);
  }

}
