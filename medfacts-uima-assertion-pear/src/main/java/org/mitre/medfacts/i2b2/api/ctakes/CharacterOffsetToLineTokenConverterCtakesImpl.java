package org.mitre.medfacts.i2b2.api.ctakes;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.mitre.medfacts.i2b2.api.ApiConcept;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.LineAndTokenPosition;

import edu.mayo.bmi.uima.core.type.BaseToken;
import edu.mayo.bmi.uima.core.type.Sentence;

public class CharacterOffsetToLineTokenConverterCtakesImpl implements CharacterOffsetToLineTokenConverter
{
  protected Logger logger = Logger.getLogger(CharacterOffsetToLineTokenConverterCtakesImpl.class.getName());
  protected JCas jcas;
  
  protected TreeMap<Integer, Sentence> beginTreeMap;
  
  public CharacterOffsetToLineTokenConverterCtakesImpl()
  {
    
  }
  
  public CharacterOffsetToLineTokenConverterCtakesImpl(JCas jcas)
  {
    this.jcas = jcas;
    buildSentenceBoundaryMap();
  }
  
  public void buildSentenceBoundaryMap()
  {
	  beginTreeMap = new TreeMap<Integer, Sentence>();
	  
	  AnnotationIndex<Annotation> annotationIndex = jcas.getAnnotationIndex(Sentence.type);
	  for (Annotation current : annotationIndex)
	  {
		  Sentence currentSentence = (Sentence)current;
		  
		  int begin = currentSentence.getBegin();
		  beginTreeMap.put(begin, currentSentence);
	  }
  }
  
  public Sentence findPreviousOrCurrentSentence(int characterOffset)
  {
	  Integer floorKey = beginTreeMap.floorKey(characterOffset);
	  if (floorKey == null)
	  {
		  return null;
	  }
	  Sentence floorEntry = beginTreeMap.get(floorKey);
	  
	  return floorEntry;
  }
  
  public LineAndTokenPosition convert(int characterOffset)
  {
    return convertCharacterOffsetToLineToken(characterOffset);
  }
  
  public LineAndTokenPosition convertCharacterOffsetToLineToken(int characterOffset)
  {
    logger.info("entering CharacterOffsetToLineTokenConverterCtakesImpl.convertCharacterOffsetToLineToken() with a characterOffset of: " + characterOffset);
    int baseTokenTypeId = BaseToken.type;
    
    ConstraintConstructorFindContainedBy constraintConstructorFindContainedBy = new ConstraintConstructorFindContainedBy(jcas);
    ConstraintConstructorFindContainedWithin constraintConstructorFindContainedWithin = new ConstraintConstructorFindContainedWithin(jcas);
    
    Type sentenceType = jcas.getTypeSystem().getType(Sentence.class.getName());
    Type baseTokenType = jcas.getTypeSystem().getType(BaseToken.class.getName());

//    FSIterator<Annotation> filteredIterator =
//        constraintConstructorFindContainedBy.createFilteredIterator(
//          characterOffset, characterOffset, sentenceType);
//
//    if (!filteredIterator.hasNext())
//    {
//      throw new RuntimeException("Surrounding sentence annotation not found[" + characterOffset + "]!!");
//    }
//    Annotation sentenceAnnotation = filteredIterator.next();
//    Sentence sentence = (Sentence)sentenceAnnotation;
    
    logger.info("finding current or previous sentence for character offset " + characterOffset);
    Sentence sentence = findPreviousOrCurrentSentence(characterOffset);
    if (sentence == null)
    {
    	logger.info("current or previous sentence IS NULL!");
    } else
    {
    	logger.info("current or previous sentence -- id: " + sentence.getAddress() +
    			"; begin: " + sentence.getBegin() + 
    			"; end: " + sentence.getEnd());
    }
    
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

}








