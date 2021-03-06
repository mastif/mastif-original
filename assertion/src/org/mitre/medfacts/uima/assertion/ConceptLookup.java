/*
 * Copyright: (c) 2012   The MITRE Corporation. All rights reserved.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MITRE  as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.mitre.medfacts.uima.assertion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.cas.FSArray;
import org.mitre.medfacts.i2b2.annotation.ConceptType;

import edu.mayo.bmi.uima.core.type.refsem.OntologyConcept;
import edu.mayo.bmi.uima.core.type.refsem.UmlsConcept;

public class ConceptLookup
{
  public static final Logger logger = Logger.getLogger(ConceptConverterAnalysisEngine.class.getName());

  protected static HashSet<String> problemSet = new HashSet<String>();
  protected static HashSet<String> testSet = new HashSet<String>();
  protected static HashSet<String> treatmentSet = new HashSet<String>();
  protected static HashSet<String> ignoredSet = new HashSet<String>();

  static
  {
    String diseasesAndDisordersTuis[] =
    { "T019", "T020", "T037", "T046", "T047", "T048", "T049", "T050", 
      "T190", "T191", "T033",
      // and for testing
      "T_DD"
    };
    String signAndSymptomTuis[] =
      { "T184",
    	// and for testing
    	"T_SS"
      };
    String anatomicalSitesTuis[] =
      { "T017", "T029", "T023", "T030", "T031", "T022", "T025", "T026",
        "T018", "T021", "T024",
        // and for testing
        "T_AS"
      };
    String medicationsAndDrugsTuis[] =
      { "T116", "T195", "T123", "T122", "T118", "T103", "T120", "T104",
        "T200", "T111", "T196", "T126", "T131", "T125", "T129", "T130",
        "T197", "T119", "T124", "T114", "T109", "T115", "T121", "T192",
        "T110", "T127"};
    String proceduresTuis[] =
      { "T060", "T065", "T058", "T059", "T063", "T062", "T061",
    	// and for testing
    	"T_PR"
      };
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
    
    ConceptLookup.problemSet = problemSet;
    ConceptLookup.testSet = testSet;
    ConceptLookup.treatmentSet = treatmentSet;
    ConceptLookup.ignoredSet = ignoredSet;
  }
  
  public static ConceptType lookupConceptType(FSArray ontologyConceptArray)
  {
    //logger.info("begin ConceptLookup.lookupConceptType");
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
    //logger.info("hasConcept: " + hasConcept);
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
    
    //logger.info(String.format("isUmls: %b; isRxnorm: %b", isUmls, isRxnorm));
    
    if (isRxnorm)
    {
      return ConceptType.TREATMENT;
    } else if (!isUmls) // is not umls and is not rxnorm
    {
      return null;
    }
    
    // if we're continuing, this means we are umls (and we are not rxnorm)
    
    String tui = umlsConcept.getTui();
    //logger.info(String.format("tui: %s", tui));
    
    ConceptType conceptType = null;
    if (problemSet.contains(tui))
    {
      conceptType = ConceptType.PROBLEM;
    } else if (testSet.contains(tui))
    {
      conceptType = ConceptType.TEST;
    } else if (treatmentSet.contains(tui))
    {
      conceptType = ConceptType.TREATMENT;
    } else
    {
      conceptType = null;
    }
    
    //logger.info(String.format("ConceptLookup.lookupConceptType() END -- conceptType is %s", (conceptType == null ? null : conceptType.toString())));
    
    return conceptType;
  }

}
