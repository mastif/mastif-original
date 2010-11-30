package org.mitre.medfacts.i2b2.api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MCOARR
 */
public class DecoderSingleFileProcessor
{
  protected String contents;
  protected List<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
  
  public void processSingleFile()
  {
    
  }

  public String getContents()
  {
    return contents;
  }

  public void setContents(String contents)
  {
    this.contents = contents;
  }

  public void addConcept(int begin, int end, String conceptType)
  {
    ApiConcept apiConcept = new ApiConcept(begin, end, conceptType);
    apiConceptList.add(apiConcept);
  }

  public void addConcept(ApiConcept apiConcept)
  {
    apiConceptList.add(apiConcept);
  }

  public List<ApiConcept> getApiConceptList()
  {
    return apiConceptList;
  }

  public void setApiConceptList(List<ApiConcept> apiConceptList)
  {
    this.apiConceptList = apiConceptList;
  }

}
