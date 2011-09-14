package org.mitre.medfacts.uima.assertion;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.cas.FSArray;
import org.mitre.medfacts.i2b2.annotation.ConceptType;

import edu.mayo.bmi.uima.core.type.OntologyConcept;
import edu.mayo.bmi.uima.core.type.UmlsConcept;

public class ConceptLookup
{
  protected static HashSet<String> problemSet = new HashSet<String>();
  protected static HashSet<String> testSet = new HashSet<String>();
  protected static HashSet<String> treatmentSet = new HashSet<String>();
  protected static HashSet<String> ignoredSet = new HashSet<String>();

  static
  {
    String diseasesAndDisordersTuis[] =
    { "T019", "T020", "T037", "T046", "T047", "T048", "T049", "T050", 
      "T190", "T191", "T033" };
    String signAndSymptomTuis[] = { "T184" };
    String anatomicalSitesTuis[] =
      { "T017", "T029", "T023", "T030", "T031", "T022", "T025", "T026",
        "T018", "T021", "T024" };
    String medicationsAndDrugsTuis[] =
      { "T116", "T195", "T123", "T122", "T118", "T103", "T120", "T104",
        "T200", "T111", "T196", "T126", "T131", "T125", "T129", "T130",
        "T197", "T119", "T124", "T114", "T109", "T115", "T121", "T192",
        "T110", "T127"};
    String proceduresTuis[] =
      { "T060", "T065", "T058", "T059", "T063", "T062", "T061" };
    String deviceTuis[] = { "T074", "T075" };
    String laboratoryTuis[] = { "T059" };
    
    HashSet<String> problemSet = new HashSet<String>();
    HashSet<String> testSet = new HashSet<String>();
    HashSet<String> treatmentSet = new HashSet<String>();
    HashSet<String> ignoredSet = new HashSet<String>();
    
    problemSet.addAll(Arrays.asList(diseasesAndDisordersTuis));
    problemSet.addAll(Arrays.asList(signAndSymptomTuis));
    ignoredSet.addAll(Arrays.asList(anatomicalSitesTuis));
    treatmentSet.addAll(Arrays.asList(medicationsAndDrugsTuis));
    testSet.addAll(Arrays.asList(proceduresTuis));
    treatmentSet.addAll(Arrays.asList(deviceTuis));
    testSet.addAll(Arrays.asList(laboratoryTuis));
  }
  
  public static ConceptType lookupConceptType(FSArray ontologyConceptArray)
  {
    FeatureStructure firstConceptFS = null;
    boolean hasConcept =
      (ontologyConceptArray != null &&
       ontologyConceptArray.size() >= 1 &&
       ontologyConceptArray.get(0) instanceof OntologyConcept
      );
    if (hasConcept)
    {
      firstConceptFS = ontologyConceptArray.get(0);
    }
    OntologyConcept ontologyConcept = (OntologyConcept)firstConceptFS;
    boolean isRxnorm = false;
    boolean isUmls = false;

    UmlsConcept umlsConcept = null;
    if (ontologyConcept instanceof UmlsConcept)
    {
      isUmls = true;
      umlsConcept = (UmlsConcept)firstConceptFS;
    } else
    {
      isRxnorm = "RXNORM".equalsIgnoreCase(ontologyConcept.getCodingScheme());
    }
    
    if (isRxnorm)
    {
      return ConceptType.TREATMENT;
    } else if (!isUmls) // is not umls and is not rxnorm
    {
      return null;
    }
    
    // if we're continuing, this means we are umls (and we are not rxnorm)
    
    String tui = umlsConcept.getTui();
    
    if (problemSet.contains(tui))
    {
      return ConceptType.PROBLEM;
    } else if (testSet.contains(tui))
    {
      return ConceptType.TEST;
    } else if (treatmentSet.contains(tui))
    {
      return ConceptType.TREATMENT;
    } else
    {
      return null;
    }
  }

}
