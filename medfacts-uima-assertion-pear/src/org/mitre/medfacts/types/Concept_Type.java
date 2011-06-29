
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Jun 01 11:04:20 EDT 2011
 * @generated */
public class Concept_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Concept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Concept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Concept(addr, Concept_Type.this);
  			   Concept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Concept(addr, Concept_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Concept.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.mitre.medfacts.types.Concept");
 
  /** @generated */
  final Feature casFeat_conceptType;
  /** @generated */
  final int     casFeatCode_conceptType;
  /** @generated */ 
  public String getConceptType(int addr) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptType);
  }
  /** @generated */    
  public void setConceptType(int addr, String v) {
        if (featOkTst && casFeat_conceptType == null)
      jcas.throwFeatMissing("conceptType", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_conceptText;
  /** @generated */
  final int     casFeatCode_conceptText;
  /** @generated */ 
  public String getConceptText(int addr) {
        if (featOkTst && casFeat_conceptText == null)
      jcas.throwFeatMissing("conceptText", "org.mitre.medfacts.types.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptText);
  }
  /** @generated */    
  public void setConceptText(int addr, String v) {
        if (featOkTst && casFeat_conceptText == null)
      jcas.throwFeatMissing("conceptText", "org.mitre.medfacts.types.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptText, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Concept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_conceptType = jcas.getRequiredFeatureDE(casType, "conceptType", "uima.cas.String", featOkTst);
    casFeatCode_conceptType  = (null == casFeat_conceptType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptType).getCode();

 
    casFeat_conceptText = jcas.getRequiredFeatureDE(casType, "conceptText", "uima.cas.String", featOkTst);
    casFeatCode_conceptText  = (null == casFeat_conceptText) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptText).getCode();

  }
}



    