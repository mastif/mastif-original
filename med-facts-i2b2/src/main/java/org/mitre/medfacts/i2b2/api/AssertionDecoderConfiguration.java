package org.mitre.medfacts.i2b2.api;

import java.util.Set;
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.medfacts.i2b2.annotation.PartOfSpeechTagger;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;

/**
 *
 * @author MCOARR
 */
public class AssertionDecoderConfiguration
{
  protected JarafeMEDecoder assertionDecoder;
  protected Set<String> enabledFeatureIdSet;
  protected ScopeParser scopeParser;
  private PartOfSpeechTagger posTagger;

  public JarafeMEDecoder getAssertionDecoder()
  {
    return assertionDecoder;
  }

  public void setAssertionDecoder(JarafeMEDecoder assertionDecoder)
  {
    this.assertionDecoder = assertionDecoder;
  }

  public Set<String> getEnabledFeatureIdSet()
  {
    return enabledFeatureIdSet;
  }

  public void setEnabledFeatureIdSet(Set<String> enabledFeatureIdSet)
  {
    this.enabledFeatureIdSet = enabledFeatureIdSet;
  }

  public ScopeParser getScopeParser()
  {
    return scopeParser;
  }

  public void setScopeParser(ScopeParser scopeParser)
  {
    this.scopeParser = scopeParser;
  }

  public PartOfSpeechTagger getPosTagger()
  {
    return posTagger;
  }

  public void setPosTagger(PartOfSpeechTagger posTagger)
  {
    this.posTagger = posTagger;
  }
}
