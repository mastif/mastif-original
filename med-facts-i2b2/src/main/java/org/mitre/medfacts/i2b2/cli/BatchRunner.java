/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.mitre.itc.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.itc.jcarafe.jarafe.JarafeMETrainer;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.ItemType;
import org.mitre.medfacts.i2b2.util.RandomAssignmentSystem;

/**
 *
 * @author MCOARR
 */
public class BatchRunner
{
  public static final float TRAINING_RATIO = 0.8f;
  protected String baseDirectoryString;

  protected List<TrainingInstance> masterTrainingInstanceList = new ArrayList<TrainingInstance>();
  protected List<TrainingInstance> trainingSplitList;
  protected List<TrainingInstance> testSplitList;

  public static void main(String args[])
  {
    String baseDirectory = args[0];
    System.out.format("base directory: %s%n", baseDirectory);

    BatchRunner batchRunner = new BatchRunner();
    batchRunner.setBaseDirectoryString(baseDirectory);

    batchRunner.execute();
  }

  /**
   * @return the baseDirectoryString
   */
  public String getBaseDirectoryString()
  {
    return baseDirectoryString;
  }

  /**
   * @param baseDirectoryString the baseDirectoryString to set
   */
  public void setBaseDirectoryString(String baseDirectoryString)
  {
    this.baseDirectoryString = baseDirectoryString;
  }

  private void execute()
  {
    File baseDirectory = new File(baseDirectoryString);
    File textFiles[] = baseDirectory.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    });

    System.out.println("=== TEXT FILE LIST BEGIN ===");
    for (File currentTextFile : textFiles)
    {
      System.out.format(" * %s%n", currentTextFile);

      String currentTextFilename = currentTextFile.getAbsolutePath();
      String baseFilename = Constants.TEXT_FILE_EXTENSTION_PATTERN.matcher(currentTextFilename).replaceFirst("");
      System.out.format("    - base filename: %s%n", baseFilename);

      String conceptFilename = baseFilename + Constants.FILE_EXTENSION_CONCEPT_FILE;
      File conceptFile = new File(conceptFilename);
      boolean conceptFileExists = conceptFile.exists();
      System.out.format("    - concept filename: %s (%s)%n", conceptFilename, (conceptFileExists ? "EXISTS" : "not present"));

      String assertionFilename = baseFilename + Constants.FILE_EXTENSION_ASSERTION_FILE;
      File assertionFile = new File(assertionFilename);
      boolean assertionFileExists = assertionFile.exists();
      System.out.format("    - assertion filename: %s (%s)%n", assertionFilename, (assertionFileExists ? "EXISTS" : "not present"));

      String relationFilename = baseFilename + Constants.FILE_EXTENSION_RELATION_FILE;
      File relationFile = new File(relationFilename);
      boolean relationFileExists = relationFile.exists();
      System.out.format("    - relation filename: %s (%s)%n", relationFilename, (relationFileExists ? "EXISTS" : "not present"));

      String scopeFilename = baseFilename + Constants.FILE_EXTENSION_SCOPE_FILE;
      File scopeFile = new File(scopeFilename);
      boolean scopeFileExists = scopeFile.exists();
      System.out.format("    - scope filename: %s (%s)%n", scopeFilename, (scopeFileExists ? "EXISTS" : "not present"));

      MedFactsRunner runner = new MedFactsRunner();
      runner.setTextFilename(currentTextFile.getAbsolutePath());
      if (conceptFileExists)    runner.addAnnotationFilename(conceptFilename);
      if (assertionFileExists)  runner.addAnnotationFilename(assertionFilename);
      if (relationFileExists)   runner.addAnnotationFilename(relationFilename);
      if (scopeFileExists)      runner.addAnnotationFilename(scopeFilename);

      runner.execute();

      List<TrainingInstance> trainingInstanceList =
          runner.getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION);
      masterTrainingInstanceList.addAll(trainingInstanceList);
    }

    createTrainingSplit();
    trainAndEval();

    System.out.println("=== TEXT FILE LIST END ===");
  }

  /**
   * @return the masterTrainingInstanceList
   */
  public List<TrainingInstance> getMasterTrainingInstanceList()
  {
    return masterTrainingInstanceList;
  }

  /**
   * @param masterTrainingInstanceList the masterTrainingInstanceList to set
   */
  public void setMasterTrainingInstanceList(List<TrainingInstance> masterTrainingInstanceList)
  {
    this.masterTrainingInstanceList = masterTrainingInstanceList;
  }

  public void trainAndEval()
  {
    JarafeMETrainer trainer = new JarafeMETrainer();

    Collection<TrainingInstance> trainingInstanceSet =
        getTrainingSplitList();

    for (TrainingInstance currentTrainingInstance : trainingInstanceSet)
    {
      trainer.addTrainingInstance(currentTrainingInstance.getExpectedValue(), currentTrainingInstance.getFeatureList());
    }
    String model = trainer.train();

    // decoding
    JarafeMEDecoder decoder = new JarafeMEDecoder(model);
    int matchCount = 0;
    int notMatchCount = 0;
    for (TrainingInstance currentEvalInstance : getTestSplitList())
    {
      String expectedValue = currentEvalInstance.getExpectedValue();
      List<String> featureList = currentEvalInstance.getFeatureList();
      String actualValue = decoder.classifyInstance(featureList);

      boolean actualMatchesExpected = actualValue.equalsIgnoreCase(expectedValue);
      if (actualMatchesExpected)
      {
        System.out.format("MATCHES (actual/expected) %s/%s%n", actualValue, expectedValue);
        matchCount++;
      } else
      {
        System.err.format("DOES NOT MATCH (actual/expected) %s/%s%n", actualValue, expectedValue);
        notMatchCount++;
      }
    }
    System.out.format("matches: %d%n", matchCount);
    System.out.format("not matches: %d%n", notMatchCount);
//    String classification1 = decoder.classifyInstance(l1);
//    assert(classification1.equals("absent"));

  }

  public void createTrainingSplit()
  {
    List<TrainingInstance> masterTrainingList = getMasterTrainingInstanceList();
    RandomAssignmentSystem system = new RandomAssignmentSystem();
    system.setTrainingRatio(0.8f);

    system.addZeroInclusiveToNExclusive(masterTrainingList.size());

    for (int i=0; i < 10; i++)
    {
      system.addItem(i);
    }

    system.createSets();

    Set<Integer> trainingSet = system.getTrainingPositions();
    Set<Integer> testSet = system.getTestPositions();

    trainingSplitList = new ArrayList<TrainingInstance>();
    testSplitList = new ArrayList<TrainingInstance>();

    for (int i : trainingSet)
    {
      trainingSplitList.add(masterTrainingList.get(i));
    }

    for (int i : testSet)
    {
      testSplitList.add(masterTrainingList.get(i));
    }

  }

  /**
   * @return the trainingSplitSet
   */
  public List<TrainingInstance> getTrainingSplitList()
  {
    return trainingSplitList;
  }

  /**
   * @param trainingSplitList the trainingSplitSet to set
   */
  public void setTrainingSplitList(List<TrainingInstance> trainingSplitList)
  {
    this.trainingSplitList = trainingSplitList;
  }

  /**
   * @return the testSplitSet
   */
  public List<TrainingInstance> getTestSplitList()
  {
    return testSplitList;
  }

  /**
   * @param testSplitList the testSplitSet to set
   */
  public void setTestSplitList(List<TrainingInstance> testSplitList)
  {
    this.testSplitList = testSplitList;
  }


}
