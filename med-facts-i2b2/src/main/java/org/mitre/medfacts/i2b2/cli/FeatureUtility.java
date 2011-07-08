package org.mitre.medfacts.i2b2.cli;

import java.util.ArrayList;
import java.util.List;
import org.mitre.medfacts.i2b2.util.StringHandling;

public class FeatureUtility
{
  public final static int MAX_WINDOW_LEFT = 12;
  public final static int MAX_WINDOW_RIGHT = 12;

  public static String constructFeatureOfAnnot(int conceptBeginToken, int conceptEndToken, String[] currentLine) {
      StringBuilder sb = new StringBuilder();
      int count = 0;
      for (int i=conceptBeginToken - 1; i >= 0 && i < currentLine.length && ++count <= MAX_WINDOW_LEFT; i--) {
          sb.append(StringHandling.escapeStringForFeatureName(currentLine[i]));
      }
      return sb.toString();
  }

  public static List<String> constructWordLeftFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i=conceptBeginCharacter - 1; i >= 0 && i < currentLine.length && ++count <= MAX_WINDOW_LEFT; i--)
    {
      String currentToken = currentLine[i];
      if (!currentToken.matches("[,.]+")) {
          String featureName1 = "";
	  if (currentToken.matches("[?]+") && ((conceptBeginCharacter - i) < 4)) {
	      featureList.add("word_left_exact_" + (conceptBeginCharacter - i) + "_?");
	  }
          featureName1 = "word_left_" + StringHandling.escapeStringForFeatureName(currentToken);
          featureList.add(featureName1);
          if ((conceptBeginCharacter - i) < 4) {
              featureList.add("word_left_3_" + StringHandling.escapeStringForFeatureName(currentToken));
          }
      }
    }
    return featureList;
  }

  public static List<String> constructWordRightFeatureList(int conceptBeginCharacter, int conceptEndCharacter, String[] currentLine)
  {
    List<String> featureList = new ArrayList<String>();
    int count = 0;
    for (int i=conceptEndCharacter + 1; i < currentLine.length && i < currentLine.length && ++count <= MAX_WINDOW_RIGHT; i++)
    {
      String currentToken = currentLine[i];
      if (!currentToken.matches("[,.]+")) {
          String featureName1 = "";
	  if (currentToken.matches("[?]+") && ((i - conceptBeginCharacter) < 4)) {
	      featureList.add("word_right_exact_" + (i - conceptBeginCharacter) + "_?");
	  }
          featureName1 = "word_right_" + StringHandling.escapeStringForFeatureName(currentToken);
          featureList.add(featureName1);
          if ((i - conceptEndCharacter) < 4) {
              featureList.add("word_right_3_"+ StringHandling.escapeStringForFeatureName(currentToken));
          }
      }
    }
    return featureList;
  }

}
