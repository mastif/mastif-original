package org.mitre.medfacts.i2b2.api;

public class ApiAssertion extends ApiConcept
{
  private Integer conceptExternalId;

  public ApiAssertion()
  {
  }

  public ApiAssertion(int begin, int end, String type, String text,
      Integer externalId)
  {
    super(begin, end, type, text, externalId);
  }

  public ApiAssertion(ApiConcept apiConcept)
  {
    super(apiConcept.begin, apiConcept.end, apiConcept.type, apiConcept.text, null);
    this.conceptExternalId = apiConcept.getExternalId();
  }

  public Integer getConceptExternalId()
  {
    return conceptExternalId;
  }

  public void setConceptExternalId(Integer conceptExternalId)
  {
    this.conceptExternalId = conceptExternalId;
  }

}
