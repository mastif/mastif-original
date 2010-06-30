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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mitre.itc.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.itc.jcarafe.jarafe.JarafeMETrainer;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.processors.RelationFileProcessor;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.RandomAssignmentSystem;

/**
 *
 * @author MCOARR
 */
public class BatchRunner
{
  public static final float TRAINING_RATIO = 0.8f;
  //protected String baseDirectoryString;
  protected String trainingDirectory;
  protected String decodeDirectory;

  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();
  protected FileProcessor assertionFileProcessor = new AssertionFileProcessor();
  protected FileProcessor relationFileProcessor = new RelationFileProcessor();
  protected FileProcessor scopeFileProcessor = new ScopeFileProcessor();


  protected List<TrainingInstance> masterTrainingInstanceListTraining = new ArrayList<TrainingInstance>();
  protected List<TrainingInstance> masterTrainingInstanceListEvaluation = new ArrayList<TrainingInstance>();
  protected List<TrainingInstance> trainingSplitList;
  protected List<TrainingInstance> testSplitList;

  public static void main(String args[])
  {

    Options options = new Options();
//    options.addOption("t", "train", false, "train the model");
//    options.addOption("r", "run", false, "run the model");
    options.addOption("b", "base-dir", true, "base directory from which train and decode directories are located");
    options.addOption("t", "train", true, "train the model using the given parameter as the training data directory");
    options.addOption("d", "decode", true, "run the model using the given parameter as the data directory");
    
    CommandLineParser parser = new GnuParser();
    CommandLine cmd = null;
    try
    {
      cmd = parser.parse(options, args);
    } catch (ParseException ex)
    {
      Logger.getLogger(BatchRunner.class.getName()).log(Level.SEVERE, "problem parsing command-line options", ex);
      throw new RuntimeException("problem parsing command-line options", ex);
    }

    String baseDir = null;
    boolean isTrain = cmd.hasOption("train");
    boolean isDecode = cmd.hasOption("decode");

    if (cmd.hasOption("base-dir"))
    {
      baseDir = cmd.getOptionValue("base-dir");
      System.out.format("using base directory: \"%s\"%n", baseDir);
    }
    String trainDir = null;
    if (isTrain)
    {
      System.out.println("running training...");
      String trainDirRelative = cmd.getOptionValue("train");
      System.out.format("trainDirRelative: %s%n", trainDirRelative);
      if (trainDirRelative == null)
      {
        trainDir = baseDir;
      } else
      {
        File trainDirFile = new File(baseDir, trainDirRelative);
        trainDir = trainDirFile.getAbsolutePath();
      }
      System.out.format("using training dir: \"%s\"%n", trainDir);
      System.out.println("finished running training.");
    }

    String decodeDir = null;
    if (isDecode)
    {
      System.out.println("running decode...");
      String decodeDirRelative = cmd.getOptionValue("decode");
      System.out.format("decodeDirRelative: %s%n", decodeDirRelative);
      if (decodeDirRelative == null)
      {
        decodeDir = baseDir;
      } else
      {
        File decodeDirFile = new File(baseDir, decodeDirRelative);
        decodeDir = decodeDirFile.getAbsolutePath();
      }
      System.out.format("using decode dir: \"%s\"%n", decodeDir);
      System.out.println("finished running decode.");
    }

//    String baseDirectory = args[0];
//    System.out.format("base directory: %s%n", baseDirectory);

    BatchRunner batchRunner = new BatchRunner();
    //batchRunner.setBaseDirectoryString(baseDirectory);
    batchRunner.setTrainingDirectory(trainDir);
    batchRunner.setDecodeDirectory(decodeDir);
    batchRunner.execute();
  }

//  /**
//   * @return the baseDirectoryString
//   */
//  public String getBaseDirectoryString()
//  {
//    return baseDirectoryString;
//  }

  public List<TrainingInstance> processFile(File currentTextFile)
  {
    System.out.format(" * %s%n", currentTextFile);
    String currentTextFilename = currentTextFile.getAbsolutePath();
    String baseFilename = Constants.TEXT_FILE_EXTENSTION_PATTERN.matcher(currentTextFilename).replaceFirst("");
    System.out.format("    - base filename: %s%n", baseFilename);

    String conceptFilename = baseFilename + Constants.FILE_EXTENSION_CONCEPT_FILE;
    File conceptFile = new File(conceptFilename);
    boolean conceptFileExists = conceptFile.exists();
    System.out.format("    - concept filename: %s (%s)%n", conceptFilename, conceptFileExists ? "EXISTS" : "not present");

    String assertionFilename = baseFilename + Constants.FILE_EXTENSION_ASSERTION_FILE;
    File assertionFile = new File(assertionFilename);
    boolean assertionFileExists = assertionFile.exists();
    System.out.format("    - assertion filename: %s (%s)%n", assertionFilename, assertionFileExists ? "EXISTS" : "not present");

    String relationFilename = baseFilename + Constants.FILE_EXTENSION_RELATION_FILE;
    File relationFile = new File(relationFilename);
    boolean relationFileExists = relationFile.exists();
    System.out.format("    - relation filename: %s (%s)%n", relationFilename, relationFileExists ? "EXISTS" : "not present");

    String scopeFilename = baseFilename + Constants.FILE_EXTENSION_SCOPE_FILE;
    File scopeFile = new File(scopeFilename);
    boolean scopeFileExists = scopeFile.exists();
    System.out.format("    - scope filename: %s (%s)%n", scopeFilename, scopeFileExists ? "EXISTS" : "not present");

    MedFactsRunner runner = new MedFactsRunner();

    runner.setConceptFileProcessor(conceptFileProcessor);
    runner.setAssertionFileProcessor(assertionFileProcessor);
    runner.setRelationFileProcessor(relationFileProcessor);
    runner.setScopeFileProcessor(scopeFileProcessor);

    runner.setTextFilename(currentTextFile.getAbsolutePath());
    if (conceptFileExists)
    {
      runner.addAnnotationFilename(conceptFilename);
    }
    if (assertionFileExists)
    {
      runner.addAnnotationFilename(assertionFilename);
    }
//    if (relationFileExists)
//    {
//      runner.addAnnotationFilename(relationFilename);
//    }
    // todo add back in the scope processing step
    if (scopeFileExists)
    {
      runner.addAnnotationFilename(scopeFilename);
    }
    runner.execute();
    List<TrainingInstance> trainingInstanceList = runner.getMapOfTrainingInstanceLists().get(AnnotationType.ASSERTION);
    return trainingInstanceList;
  }

  public void processFileSet(File baseDirectory, List<TrainingInstance> masterList)
  {
    File[] textFiles = baseDirectory.listFiles(new FilenameFilter()
    {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    });
    System.out.println("=== TEXT FILE LIST BEGIN ===");
    for (File currentTextFile : textFiles)
    {
      List<TrainingInstance> trainingInstanceList = processFile(currentTextFile);
      masterList.addAll(trainingInstanceList);
    }
  }

//  /**
//   * @param baseDirectoryString the baseDirectoryString to set
//   */
//  public void setBaseDirectoryString(String baseDirectoryString)
//  {
//    this.baseDirectoryString = baseDirectoryString;
//  }

  private void execute()
  {
    //File baseDirectory = new File(baseDirectoryString);
    if (trainingDirectory != null)
    {
      File trainingDirectoryFile = new File(trainingDirectory);
      processFileSet(trainingDirectoryFile, masterTrainingInstanceListTraining);
    }

    if (decodeDirectory != null)
    {
      File decodeDirectoryFile = new File(decodeDirectory);
      processFileSet(decodeDirectoryFile, masterTrainingInstanceListEvaluation);
    }

    // train and evaluate the classifier
    //createTrainingSplit();
    trainAndEval();

    System.out.println("=== TEXT FILE LIST END ===");
  }

  /**
   * @return the masterTrainingInstanceList
   */
  public List<TrainingInstance> getMasterTrainingInstanceListTraining()
  {
    return masterTrainingInstanceListTraining;
  }

  /**
   * @param masterTrainingInstanceList the masterTrainingInstanceList to set
   */
  public void setMasterTrainingInstanceListTraining(List<TrainingInstance> masterTrainingInstanceList)
  {
    this.masterTrainingInstanceListTraining = masterTrainingInstanceList;
  }


  public List<TrainingInstance> getMasterTrainingInstanceListEvaluation()
  {
    return masterTrainingInstanceListEvaluation;
  }

  public void setMasterTrainingInstanceListEvaluation(List<TrainingInstance> masterTrainingInstanceListEvaluation)
  {
    this.masterTrainingInstanceListEvaluation = masterTrainingInstanceListEvaluation;
  }

  public void trainAndEval()
  {
    JarafeMETrainer trainer = new JarafeMETrainer();

    Collection<TrainingInstance> trainingInstanceSet =
        //getTrainingSplitList();
        getMasterTrainingInstanceListTraining();

    for (TrainingInstance currentTrainingInstance : trainingInstanceSet)
    {
      trainer.addTrainingInstance(currentTrainingInstance.getExpectedValue(), currentTrainingInstance.getFeatureList());
    }
    String model = trainer.train();

    // decoding
    JarafeMEDecoder decoder = new JarafeMEDecoder(model);
    int matchCount = 0;
    int notMatchCount = 0;
    Collection<TrainingInstance> evaluationInstanceSet =
        getMasterTrainingInstanceListEvaluation();
    for (TrainingInstance currentEvalInstance : evaluationInstanceSet)
    {
      String expectedValue = currentEvalInstance.getExpectedValue();
      List<String> featureList = currentEvalInstance.getFeatureList();
      String actualValue = decoder.classifyInstance(featureList);

      boolean actualMatchesExpected = actualValue.equalsIgnoreCase(expectedValue);
      if (actualMatchesExpected)
      {
        System.out.format("MATCHES (actual/expected) %s/%s [%s:%d]%n", actualValue, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber());
        matchCount++;
      } else
      {
        System.err.format("DOES NOT MATCH (actual/expected) %s/%s [%s:%d]%n", actualValue, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber());
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
    List<TrainingInstance> masterTrainingList = getMasterTrainingInstanceListTraining();
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

  /**
   * @return the trainingDirectory
   */
  public String getTrainingDirectory()
  {
    return trainingDirectory;
  }

  /**
   * @param trainingDirectory the trainingDirectory to set
   */
  public void setTrainingDirectory(String trainingDirectory)
  {
    this.trainingDirectory = trainingDirectory;
  }

  /**
   * @return the decodeDirectory
   */
  public String getDecodeDirectory()
  {
    return decodeDirectory;
  }

  /**
   * @param decodeDirectory the decodeDirectory to set
   */
  public void setDecodeDirectory(String decodeDirectory)
  {
    this.decodeDirectory = decodeDirectory;
  }


}

