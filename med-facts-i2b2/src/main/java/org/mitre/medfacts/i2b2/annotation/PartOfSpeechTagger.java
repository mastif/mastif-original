package org.mitre.medfacts.i2b2.annotation;

import org.mitre.jcarafe.jarafe.JarafeTagger;
import org.mitre.jcarafe.jarafe.Jarafe;
import org.mitre.jcarafe.jarafe.Jarafe.LightAnnot;
import java.util.ArrayList;
import java.util.List;
import org.mitre.medfacts.i2b2.util.Location;

public class PartOfSpeechTagger {
  
  JarafeTagger posTagger = null;

  public PartOfSpeechTagger(String posModel) {
    posTagger = new JarafeTagger();
    posTagger.initialize(new String[] {"--model",posModel,"--no-pre-proc","--mode","json"});
  }

  public List<PartOfSpeechAnnotation> posTagDocument(String [][] toks) {
    List<PartOfSpeechAnnotation> posList = new ArrayList<PartOfSpeechAnnotation>();
    for (int i = 0; i < toks.length; i++) {
      String [] line = toks[i];
      ArrayList lineStr = new ArrayList();
      for (int j = 0; j < line.length; j++) {
	lineStr.add(line[j]);
      }
      List<String> posTags = posTagger.processStringList(lineStr);
      int c = 0;
      for (String tag : posTags) {
	PartOfSpeechAnnotation posAn = new PartOfSpeechAnnotation();
	posAn.setBegin(new Location(i, c));
	posAn.setEnd(new Location(i, c));
	posAn.setPartOfSpeech(tag);
	posList.add(posAn);
	c += 1;
      }
    }
    return posList;
  }
}