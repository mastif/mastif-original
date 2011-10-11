package org.mitre.medfacts.i2b2.api.ctakes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSBooleanConstraint;
import org.apache.uima.cas.FSIntConstraint;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.i2b2.api.SingleDocumentProcessor;
import org.mitre.medfacts.zoner.LineAndTokenPosition;
import org.mitre.medfacts.zoner.LineTokenToCharacterOffsetConverter;

import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.PunctuationToken;
import edu.mayo.bmi.uima.core.type.Sentence;
import edu.mayo.bmi.uima.core.type.Sentence_Type;
import edu.mayo.bmi.uima.core.type.WordToken;

public class SingleDocumentProcessorCtakes extends SingleDocumentProcessor
{
  
  protected JCas jcas;

  public SingleDocumentProcessorCtakes()
  {
    super();
  }

//  public SingleDocumentProcessorCtakes(
//      LineTokenToCharacterOffsetConverter converter)
//  {
//    super(converter);
//  }

  public JCas getJcas()
  {
    return jcas;
  }

  public void setJcas(JCas jcas)
  {
    this.jcas = jcas;
  }
  
  @Override
  protected void preExecutionTest()
  {
    // do not construct converter (since we don't have the full text in tokenized format)
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
  
  public void postprocess()
  {
    
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
      FSIterator<Annotation> tokensInThisSentenceIterator = tokenAnnotationIndex.subiterator(sentence);
      ArrayList<String> arrayOfTokens = new ArrayList<String>();
      //for (Annotation baseTokenAnnotationUntyped : tokenAnnotationIndex)
      while (tokensInThisSentenceIterator.hasNext())
      {
        Annotation baseTokenAnnotationUntyped = tokensInThisSentenceIterator.next();
//        // ignore tokens that are outside of the sentence.
//        // there has to be a better way to do this with Constraints, but this
//        // should work for now...
//        if (baseTokenAnnotationUntyped.getBegin() < sentenceBegin ||
//            baseTokenAnnotationUntyped.getEnd() > sentenceEnd)
//        {
//          continue;
//        }
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
  
  public LineAndTokenPosition convertCharacterOffsetToLineToken(int characterOffset)
  {
    int baseTokenTypeId = BaseToken.type;
    
    ConstraintConstructorFindContainedBy constraintConstructorFindContainedBy = new ConstraintConstructorFindContainedBy(jcas);
    ConstraintConstructorFindContainedWithin constraintConstructorFindContainedWithin = new ConstraintConstructorFindContainedWithin(jcas);
    
    Type sentenceType = jcas.getTypeSystem().getType(Sentence.class.getName());
    Type baseTokenType = jcas.getTypeSystem().getType(BaseToken.class.getName());

    FSIterator<Annotation> filteredIterator =
        constraintConstructorFindContainedBy.createFilteredIterator(
          characterOffset, characterOffset, sentenceType);

    if (!filteredIterator.hasNext())
    {
      throw new RuntimeException("Surrounding sentence annotation not found!!");
    }
    Annotation sentenceAnnotation = filteredIterator.next();
    Sentence sentence = (Sentence)sentenceAnnotation;
    int lineNumber = sentence.getSentenceNumber() + 1;
    
    
    FSIterator<Annotation> tokensInSentenceIterator =
        jcas.getAnnotationIndex(baseTokenTypeId).subiterator(sentence);
    
    if (!tokensInSentenceIterator.hasNext())
    {
      throw new RuntimeException("First token in sentence not found!!");
    }
    Annotation firstTokenAnnotation = tokensInSentenceIterator.next();
    BaseToken firstToken = (BaseToken)firstTokenAnnotation;
    int firstTokenInSentenceNumber = firstToken.getTokenNumber();
    
    
    FSIterator<Annotation> beginTokenInSentenceIterator =
        constraintConstructorFindContainedBy.createFilteredIterator(
          characterOffset, characterOffset, baseTokenType);
    
    if (!beginTokenInSentenceIterator.hasNext())
    {
      throw new RuntimeException("First token in sentence not found!!");
    }
    Annotation beginTokenAnnotation = beginTokenInSentenceIterator.next();
    BaseToken beginToken = (BaseToken)beginTokenAnnotation;
    int beginTokenNumber = beginToken.getTokenNumber();
    int beginTokenWordNumber = beginTokenNumber - firstTokenInSentenceNumber;
    
    LineAndTokenPosition b = new LineAndTokenPosition();
    b.setLine(lineNumber);
    b.setTokenOffset(beginTokenWordNumber);

    return b;
  }

  public List<LineAndTokenPosition> calculateBeginAndEndOfConcept
    (ApiConcept problem)
  {
    return calculateBeginAndEndOfConcept(problem.getBegin(), problem.getEnd());
  }
  
  public List<LineAndTokenPosition> calculateBeginAndEndOfConcept(
      int problemBegin, int problemEnd)
  {
    //int externalId = problem.getExternalId();
    //int sentenceTypeId = Sentence.type;
    int baseTokenTypeId = BaseToken.type;
    //jcas.getAnnotationIndex(sentenceTypeId);
    
    ConstraintConstructorFindContainedBy constraintConstructorFindContainedBy = new ConstraintConstructorFindContainedBy(jcas);
    ConstraintConstructorFindContainedWithin constraintConstructorFindContainedWithin = new ConstraintConstructorFindContainedWithin(jcas);
    
    //AnnotationIndex<Annotation> sentenceAnnotationIndex = jcas.getAnnotationIndex(sentenceTypeId);
    Type sentenceType = jcas.getTypeSystem().getType(Sentence.class.getName());
    Type baseTokenType = jcas.getTypeSystem().getType(BaseToken.class.getName());
    ///
    FSIterator<Annotation> filteredIterator =
        constraintConstructorFindContainedBy.createFilteredIterator(
          problemBegin, problemEnd, sentenceType);
    ///
    if (!filteredIterator.hasNext())
    {
      throw new RuntimeException("Surrounding sentence annotation not found!!");
    }
    Annotation sentenceAnnotation = filteredIterator.next();
    Sentence sentence = (Sentence)sentenceAnnotation;
    int lineNumber = sentence.getSentenceNumber() + 1;
    
    
    FSIterator<Annotation> tokensInSentenceIterator =
        jcas.getAnnotationIndex(baseTokenTypeId).subiterator(sentence);
    
    if (!tokensInSentenceIterator.hasNext())
    {
      throw new RuntimeException("First token in sentence not found!!");
    }
    Annotation firstTokenAnnotation = tokensInSentenceIterator.next();
    BaseToken firstToken = (BaseToken)firstTokenAnnotation;
    int firstTokenInSentenceNumber = firstToken.getTokenNumber();
    
    
    FSIterator<Annotation> beginTokenInSentenceIterator =
        constraintConstructorFindContainedWithin.createFilteredIterator(
          problemBegin, problemEnd, baseTokenType);
    
    if (!beginTokenInSentenceIterator.hasNext())
    {
      throw new RuntimeException("First token in sentence not found!!");
    }
    Annotation beginTokenAnnotation = beginTokenInSentenceIterator.next();
    BaseToken beginToken = (BaseToken)beginTokenAnnotation;
    int beginTokenNumber = beginToken.getTokenNumber();
    int beginTokenWordNumber = beginTokenNumber - firstTokenInSentenceNumber;
    
    
    beginTokenInSentenceIterator.moveToLast();
    if (!beginTokenInSentenceIterator.hasNext())
    {
      throw new RuntimeException("First token in sentence not found!!");
    }
    Annotation endTokenAnnotation = beginTokenInSentenceIterator.next();
    BaseToken endToken = (BaseToken)endTokenAnnotation;
    int endTokenNumber = endToken.getTokenNumber();
    int endTokenWordNumber = endTokenNumber - firstTokenInSentenceNumber;
    

    ArrayList<LineAndTokenPosition> list = new ArrayList<LineAndTokenPosition>();
    LineAndTokenPosition b = new LineAndTokenPosition();
    b.setLine(lineNumber);
    b.setTokenOffset(beginTokenWordNumber);
    list.add(b);
    LineAndTokenPosition e = new LineAndTokenPosition();
    e.setLine(lineNumber);
    e.setTokenOffset(endTokenWordNumber);
    list.add(e);
    return list;
  }

//  /**
//   * @param problemBegin
//   * @param problemEnd
//   * @param sentenceType
//   * @return
//   */
//  public FSIterator<Annotation> createFilteredIteratorByBeginEndAndType(
//      int problemBegin, int problemEnd, Type sentenceType)
//  {
//    ConstraintFactory cf = jcas.getConstraintFactory();
//    TypeSystem ts = jcas.getTypeSystem();
//    Type annotationType = ts.getType(Annotation.class.getName());
//    Feature sentenceBeginFeature = annotationType.getFeatureByBaseName("begin");
//    FeaturePath sentenceBeginFeaturePath = jcas.createFeaturePath();
//    sentenceBeginFeaturePath.addFeature(sentenceBeginFeature);
//    
//    Feature sentenceEndFeature = annotationType.getFeatureByBaseName("end");
//    FeaturePath sentenceEndFeaturePath = jcas.createFeaturePath();
//    sentenceEndFeaturePath.addFeature(sentenceEndFeature);
//    
//    FSMatchConstraint beginAndEnd = constructContainedByConstraint(
//        problemBegin, problemEnd, cf, sentenceBeginFeaturePath,
//        sentenceEndFeaturePath);
//    
//    
//    FSTypeConstraint sentenceTypeConstraint = cf.createTypeConstraint();
//    sentenceTypeConstraint.add(sentenceType);
//    
//    FSMatchConstraint beginAndEndAndType = cf.and(beginAndEnd, sentenceTypeConstraint);
//    
//    FSIterator<Annotation> filteredIterator =
//        jcas.createFilteredIterator(jcas.getAnnotationIndex().iterator(),  beginAndEndAndType);
//    return filteredIterator;
//  }
//
//  /**
//   * @param problemBegin
//   * @param problemEnd
//   * @param cf
//   * @param sentenceBeginFeaturePath
//   * @param sentenceEndFeaturePath
//   * @return
//   */
//  public FSMatchConstraint constructContainedByConstraint(int problemBegin,
//      int problemEnd, ConstraintFactory cf,
//      FeaturePath sentenceBeginFeaturePath, FeaturePath sentenceEndFeaturePath)
//  {
//    FSIntConstraint sentenceBeginIntConstraint = cf.createIntConstraint();
//    sentenceBeginIntConstraint.leq(problemBegin);
//    
//    FSIntConstraint sentenceEndIntConstraint = cf.createIntConstraint();
//    sentenceEndIntConstraint.geq(problemEnd);
//    
//    
//    FSMatchConstraint begin = cf.embedConstraint(sentenceBeginFeaturePath, sentenceBeginIntConstraint);
//    FSMatchConstraint end = cf.embedConstraint(sentenceEndFeaturePath, sentenceEndIntConstraint);
//    
//    FSMatchConstraint beginAndEnd = cf.and(begin, end);
//    return beginAndEnd;
//  }

}

