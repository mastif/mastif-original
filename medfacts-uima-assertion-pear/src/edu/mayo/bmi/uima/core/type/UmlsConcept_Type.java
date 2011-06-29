
/* First created by JCasGen Wed May 25 14:13:06 EDT 2011 */
package edu.mayo.bmi.uima.core.type;

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
 * Updated by JCasGen Wed Jun 01 11:04:20 EDT 2011
 * @generated */
public class UmlsConcept_Type extends OntologyConcept_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (UmlsConcept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = UmlsConcept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new UmlsConcept(addr, UmlsConcept_Type.this);
  			   UmlsConcept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new UmlsConcept(addr, UmlsConcept_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = UmlsConcept.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.mayo.bmi.uima.core.type.UmlsConcept");
 
  /** @generated */
  final Feature casFeat_cui;
  /** @generated */
  final int     casFeatCode_cui;
  /** @generated */ 
  public String getCui(int addr) {
        if (featOkTst && casFeat_cui == null)
      jcas.throwFeatMissing("cui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_cui);
  }
  /** @generated */    
  public void setCui(int addr, String v) {
        if (featOkTst && casFeat_cui == null)
      jcas.throwFeatMissing("cui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_cui, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tui;
  /** @generated */
  final int     casFeatCode_tui;
  /** @generated */ 
  public String getTui(int addr) {
        if (featOkTst && casFeat_tui == null)
      jcas.throwFeatMissing("tui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_tui);
  }
  /** @generated */    
  public void setTui(int addr, String v) {
        if (featOkTst && casFeat_tui == null)
      jcas.throwFeatMissing("tui", "edu.mayo.bmi.uima.core.type.UmlsConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_tui, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public UmlsConcept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_cui = jcas.getRequiredFeatureDE(casType, "cui", "uima.cas.String", featOkTst);
    casFeatCode_cui  = (null == casFeat_cui) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cui).getCode();

 
    casFeat_tui = jcas.getRequiredFeatureDE(casType, "tui", "uima.cas.String", featOkTst);
    casFeatCode_tui  = (null == casFeat_tui) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tui).getCode();

  }
}



    