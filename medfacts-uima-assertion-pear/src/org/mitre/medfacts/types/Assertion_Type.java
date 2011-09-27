
/* First created by JCasGen Mon May 23 12:04:34 EDT 2011 */
package org.mitre.medfacts.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Tue Sep 27 09:25:17 EDT 2011
 * @generated */
public class Assertion_Type extends Concept_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Assertion_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Assertion_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Assertion(addr, Assertion_Type.this);
  			   Assertion_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Assertion(addr, Assertion_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Assertion.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mitre.medfacts.types.Assertion");
 
  /** @generated */
  final Feature casFeat_assertionType;
  /** @generated */
  final int     casFeatCode_assertionType;
  /** @generated */ 
  public String getAssertionType(int addr) {
        if (featOkTst && casFeat_assertionType == null)
      jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    return ll_cas.ll_getStringValue(addr, casFeatCode_assertionType);
  }
  /** @generated */    
  public void setAssertionType(int addr, String v) {
        if (featOkTst && casFeat_assertionType == null)
      jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    ll_cas.ll_setStringValue(addr, casFeatCode_assertionType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Assertion_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_assertionType = jcas.getRequiredFeatureDE(casType, "assertionType", "uima.cas.String", featOkTst);
    casFeatCode_assertionType  = (null == casFeat_assertionType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_assertionType).getCode();

  }
}



    