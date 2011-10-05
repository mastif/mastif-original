package org.mitre.medfacts.i2b2.api.ctakes;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.WordToken;

public class SingleDocumentProcessorCtakes extends SingleDocumentProcessor
{
  
  protected JCas jcas;

  public SingleDocumentProcessorCtakes()
  {
    super();
  }

  public SingleDocumentProcessorCtakes(
      LineTokenToCharacterOffsetConverter converter)
  {
    super(converter);
  }

  public JCas getJcas()
  {
    return jcas;
  }

  public void setJcas(JCas jcas)
  {
    this.jcas = jcas;
  }
  
  public void preprocess()
  {
    String arrayOfArrayOfTokens[][] = null;
    
    ArrayList<ArrayList<String>> returnedObject = construct2DTokenArray(jcas);
    arrayOfArrayOfTokens = new String[returnedObject.size()][];
    String template[] = new String[0];
    for (int i=0; i < returnedObject.size(); i++)
    {
      ArrayList<String> current = returnedObject.get(i);
      String temp[] = current.toArray(template);
      arrayOfArrayOfTokens[i] = temp;
    }
    
    this.arrayOfArrayOfTokens = arrayOfArrayOfTokens;
  }

  public ArrayList<ArrayList<String>> construct2DTokenArray(JCas jcas)
  {
    int sentenceType = Sentence.type;
    AnnotationIndex<Annotation> sentenceAnnotationIndex =
      jcas.getAnnotationIndex(sentenceType);
    ArrayList<ArrayList<String>> arrayOfLines = new ArrayList<ArrayList<String>>();
    
    //ArrayList<ApiConcept> apiConceptList = new ArrayList<ApiConcept>();
    for (Annotation annotation : sentenceAnnotationIndex)
    {
      Sentence sentence = (Sentence)annotation;
      int sentenceBegin = sentence.getBegin();
      int sentenceEnd = sentence.getEnd();
      
      AnnotationIndex<Annotation> tokenAnnotationIndex = jcas.getAnnotationIndex(BaseToken.type);
      ArrayList<String> arrayOfTokens = new ArrayList<String>();
      for (Annotation baseTokenAnnotationUntyped : tokenAnnotationIndex)
      {
        // ignore tokens that are outside of the sentence.
        // there has to be a better way to do this with Constraints, but this
        // should work for now...
        if (baseTokenAnnotationUntyped.getBegin() < sentenceBegin ||
            baseTokenAnnotationUntyped.getEnd() > sentenceEnd)
        {
          continue;
        }
        BaseToken baseToken = (BaseToken)baseTokenAnnotationUntyped;
        if (baseToken instanceof WordToken ||
            baseToken instanceof PunctuationToken)
        {
          String currentTokenText = baseToken.getCoveredText();
          arrayOfTokens.add(currentTokenText);
        }
      }
      arrayOfLines.add(arrayOfTokens);
      
    }
    return arrayOfLines;
  }

}

