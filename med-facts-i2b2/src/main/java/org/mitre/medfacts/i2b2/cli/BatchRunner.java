/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.mitre.itc.jcarafe.jarafe.JarafeMETrainer;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.ItemType;
import org.mitre.medfacts.i2b2.util.RandomAssignmentItem;
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

      MedFactsRunner runner = new MedFactsRunner();
      runner.setTextFilename(currentTextFile.getAbsolutePath());
      runner.addAnnotationFilename(conceptFilename);
      runner.addAnnotationFilename(assertionFilename);
      runner.addAnnotationFilename(relationFilename);

      runner.execute();

      List<TrainingInstance> trainingInstanceList =
          runner.getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION);
      masterTrainingInstanceList.addAll(trainingInstanceList);
    }

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

    List<TrainingInstance> trainingInstanceList =
        getMasterTrainingInstanceList();

    for (TrainingInstance currentTrainingInstance : trainingInstanceList)
    {
      trainer.addTrainingInstance(currentTrainingInstance.getExpectedValue(), currentTrainingInstance.getFeatureList());
    }
    String model = trainer.train();

    // decoding
//    JarafeMEDecoder decoder = new JarafeMEDecoder(model);
//    String classification1 = decoder.classifyInstance(l1);
//    assert(classification1.equals("absent"));

  }

  public void createTrainingSplit()
  {
    List<TrainingInstance> trainingInstanceList =
    getMasterTrainingInstanceList();
    int inputListSize = trainingInstanceList.size();

    int trainingCutoff = Math.round(TRAINING_RATIO * (inputListSize-1));

    int randomSlots[] = new int[inputListSize];

    RandomAssignmentSystem system = new RandomAssignmentSystem();
    system.setTrainingRatio(TRAINING_RATIO);

    List<TrainingInstance> master = getMasterTrainingInstanceList();
    for (int i=0; i < master.size(); i++)
    {
      system.addItem(i);
    }

    system.createSets();
    final ItemType[] arrayOfItemTypes = system.getArrayOfItemsTypes();
    Set<TrainingInstance> trainingSet = new TreeSet<TrainingInstance>();
    Set<TrainingInstance> testSet = new TreeSet<TrainingInstance>();

    for (int i=0; i < arrayOfItemTypes.length; i++)
    {
      ItemType currentItemType = arrayOfItemTypes[i];
      TrainingInstance currentTrainingInstance = trainingInstanceList.get(i);
      switch (currentItemType)
      {
        case TRAINING:
          trainingSet.add(currentTrainingInstance);
          break;
        case TEST:
          testSet.add(currentTrainingInstance);
          break;
      }
    }

  }


}
