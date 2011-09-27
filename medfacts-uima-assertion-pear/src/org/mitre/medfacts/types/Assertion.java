

/* First created by JCasGen Mon May 23 12:04:33 EDT 2011 */
package org.mitre.medfacts.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Sep 27 09:25:17 EDT 2011
 * XML source: /work/medfacts/eclipse-medfacts-uima/medfacts-uima-assertion-pear/desc/conceptConverterAnalysisEngine.xml
 * @generated */
public class Assertion extends Concept {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Assertion.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Assertion() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Assertion(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Assertion(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Assertion(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: assertionType

  /** getter for assertionType - gets assertion type -- one of present, absent, possible, conditional, hypothetical, or assocated_with_someone_else
   * @generated */
  public String getAssertionType() {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_assertionType == null)
      jcasType.jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Assertion_Type)jcasType).casFeatCode_assertionType);}
    
  /** setter for assertionType - sets assertion type -- one of present, absent, possible, conditional, hypothetical, or assocated_with_someone_else 
   * @generated */
  public void setAssertionType(String v) {
    if (Assertion_Type.featOkTst && ((Assertion_Type)jcasType).casFeat_assertionType == null)
      jcasType.jcas.throwFeatMissing("assertionType", "org.mitre.medfacts.types.Assertion");
    jcasType.ll_cas.ll_setStringValue(addr, ((Assertion_Type)jcasType).casFeatCode_assertionType, v);}    
  }

    