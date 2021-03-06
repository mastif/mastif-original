/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mitre.jcarafe.jarafe.JarafeMEDecoder;
import org.mitre.jcarafe.jarafe.JarafeMETrainer;
import org.mitre.jcarafe.jarafe.JarafeTagger;
import org.mitre.medfacts.i2b2.annotation.Annotation;
import org.mitre.medfacts.i2b2.annotation.AnnotationType;
import org.mitre.medfacts.i2b2.annotation.AssertionAnnotation;
import org.mitre.medfacts.i2b2.annotation.AssertionValue;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.annotation.PartOfSpeechTagger;
import org.mitre.medfacts.i2b2.processors.AssertionFileProcessor;
import org.mitre.medfacts.i2b2.processors.ConceptFileProcessor;
import org.mitre.medfacts.i2b2.processors.FileProcessor;
import org.mitre.medfacts.i2b2.processors.RelationFileProcessor;
import org.mitre.medfacts.i2b2.processors.ScopeFileProcessor;
import org.mitre.medfacts.i2b2.training.TrainingInstance;
import org.mitre.medfacts.i2b2.util.Constants;
import org.mitre.medfacts.i2b2.util.RandomAssignmentSystem;
import org.mitre.medfacts.i2b2.util.StringHandling;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverter;
import org.mitre.medfacts.zoner.CharacterOffsetToLineTokenConverterDefaultImpl;

/**
 *
 * @author MCOARR
 */
public class BatchRunner
{
  static final Logger logger = Logger.getLogger(BatchRunner.class.getName());
  
  public static final float TRAINING_RATIO = 0.8f;
  public static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.[a-zA-Z0-9]*$");
  protected String baseDirectoryString;
  protected String trainingDirectory;
  protected String decodeDirectory;

  protected FileProcessor conceptFileProcessor = new ConceptFileProcessor();
  protected FileProcessor assertionFileProcessor = new AssertionFileProcessor();
  protected FileProcessor relationFileProcessor = new RelationFileProcessor();
  protected FileProcessor scopeFileProcessor = new ScopeFileProcessor();

  protected RunConfiguration runConfiguration;


  protected List<TrainingInstance> masterTrainingInstanceListTraining = new ArrayList<TrainingInstance>();
  protected List<TrainingInstance> masterTrainingInstanceListEvaluation = new ArrayList<TrainingInstance>();
  protected List<TrainingInstance> trainingSplitList;
  protected List<TrainingInstance> testSplitList;
  protected Set<String> enabledFeatureIdSet;
  protected Mode mode;
  protected Map<String, List<AssertionAnnotation>> mapOfResultBySourceFile =
    new HashMap<String, List<AssertionAnnotation>>();

  protected static double gaussianPrior = 10.0;

  protected ScopeParser scopeParser = null;

  protected PartOfSpeechTagger posTagger = null;

  protected String fileNameSuffix;

  public static void main(String args[])
  {

    Options options = new Options();
//    options.addOption("t", "train", false, "train the model");
//    options.addOption("r", "run", false, "run the model");
    options.addOption("b", "base-dir", true, "base directory from which train and decode directories are located");
    options.addOption("t", "train", true, "train the model using the given parameter as the training data directory");
    options.addOption("d", "decode", true, "run the model using the given parameter as the data directory");
    options.addOption("f", "features-file", true, "run the system and read in the 'features file' which lists featureids of features that should be used");
    options.addOption("m", "mode", true, "mode should either be \"eval\" or \"decode\".  eval is used if you have assertion files with expected assertion values.  decode is used if you have no assertion files and thus no known assertion values.");
    options.addOption("g", "gaussian-prior", true, "Gaussian prior to use for MaxEnt model");
    options.addOption("c", "cue-model", true, "Cue identification model");
    options.addOption("s", "scope-model", true, "Scope model");
    options.addOption("p", "pos-model", true, "Part of speech model");
    
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

    Mode mode = Mode.EVAL;
    if (cmd.hasOption("mode"))
    {
      String modeString = cmd.getOptionValue("mode");
      if (modeString != null && !modeString.isEmpty())
      {
        modeString = modeString.toUpperCase();
        mode = Mode.valueOf(modeString);
      }
    }

    String baseDir = null;
    boolean isTrain = cmd.hasOption("train");
    boolean isDecode = cmd.hasOption("decode");
    if (cmd.hasOption("gaussian-prior")) {
        String gpStr = cmd.getOptionValue("gaussian-prior");
        if (gpStr != null && !gpStr.isEmpty()) {
            try {
                gaussianPrior = Double.parseDouble(gpStr);
            } catch (Exception e) {
                Logger.getLogger(BatchRunner.class.getName()).log(Level.SEVERE, "gaussian prior command-line value not parsed properly");
            }
        }
    }

    if (cmd.hasOption("base-dir"))
    {
      baseDir = cmd.getOptionValue("base-dir");
      logger.info(String.format("using base directory: \"%s\"%n", baseDir));
    }

    boolean hasFeaturesFile = false;
    String featuresFileName = null;
    if (cmd.hasOption("features-file"))
    {
      hasFeaturesFile = true;
      featuresFileName = cmd.getOptionValue("features-file");
    }

    String trainDir = null;
    if (isTrain)
    {
      logger.info("running training...");
      String trainDirRelative = cmd.getOptionValue("train");
      logger.info(String.format("trainDirRelative: %s%n", trainDirRelative));
      if (trainDirRelative == null)
      {
        trainDir = baseDir;
      } else
      {
        File trainDirFile = new File(baseDir, trainDirRelative);
        trainDir = trainDirFile.getAbsolutePath();
      }
      logger.info(String.format("using training dir: \"%s\"%n", trainDir));
      logger.info("finished running training.");
    }

    String decodeDir = null;
    if (isDecode)
    {
      logger.info("running decode...");
      String decodeDirRelative = cmd.getOptionValue("decode");
      logger.info(String.format("decodeDirRelative: %s%n", decodeDirRelative));
      if (decodeDirRelative == null)
      {
        decodeDir = baseDir;
      } else
      {
        File decodeDirFile = new File(baseDir, decodeDirRelative);
        decodeDir = decodeDirFile.getAbsolutePath();
      }
      logger.info(String.format("using decode dir: \"%s\"%n", decodeDir));
      logger.info("finished running decode.");
    }

    File featuresFile = null;
    if (hasFeaturesFile)
    {
      featuresFile = new File(baseDir, featuresFileName);
    }
    String scopeModelFileName = cmd.getOptionValue("scope-model");
    String cueModelFileName = cmd.getOptionValue("cue-model");
    String posModelFileName = cmd.getOptionValue("pos-model");
    File scopeModelFile = null;
    File cueModelFile = null;
    File posModelFile = null;
    if (StringHandling.isAbsoluteFileName(scopeModelFileName))
    {
      scopeModelFile = new File(scopeModelFileName);
    } else
    {
      scopeModelFile = new File(baseDir, scopeModelFileName);
    }
    logger.info(String.format("scope model file: %s%n", scopeModelFile.getAbsolutePath()));
    if (StringHandling.isAbsoluteFileName(cueModelFileName))
    {
      cueModelFile = new File(cueModelFileName);
    } else
    {
      cueModelFile = new File(baseDir, cueModelFileName);
    }

    if (StringHandling.isAbsoluteFileName(posModelFileName))
    {
      posModelFile = new File(posModelFileName);
    } else
    {
      posModelFile = new File(baseDir, posModelFileName);
    }

    logger.info(String.format("cue model file: %s%n", cueModelFile.getAbsolutePath()));
    //initialize scope/cue parser
    ScopeParser scopeParser = new ScopeParser(scopeModelFile.getAbsolutePath(), cueModelFile.getAbsolutePath());
    PartOfSpeechTagger posTagger = new PartOfSpeechTagger(posModelFile.getAbsolutePath());

//    String baseDirectory = args[0];
//    logger.info(String.format("base directory: %s%n", baseDirectory);

    BatchRunner batchRunner = new BatchRunner();
    batchRunner.setBaseDirectoryString(baseDir);
    batchRunner.setTrainingDirectory(trainDir);
    batchRunner.setDecodeDirectory(decodeDir);
    batchRunner.setMode(mode);
    batchRunner.setScopeParser(scopeParser);
    batchRunner.setPosTagger(posTagger);
    if (featuresFileName != null)
    {
      batchRunner.processEnabledFeaturesFile(featuresFile);
    }
    batchRunner.execute();

  }

//  /**
//   * @return the baseDirectoryString
//   */
//  public String getBaseDirectoryString()
//  {
//    return baseDirectoryString;
//  }

  public List<TrainingInstance> processFile(File currentTextFile, Mode mode)
  {
    logger.info(String.format(" * %s%n", currentTextFile));
    String currentTextFilename = currentTextFile.getAbsolutePath();
    String baseFilename = Constants.TEXT_FILE_EXTENSTION_PATTERN.matcher(currentTextFilename).replaceFirst("");
    logger.info(String.format("    - base filename: %s%n", baseFilename));

    String conceptFilename = baseFilename + Constants.FILE_EXTENSION_CONCEPT_FILE;
    File conceptFile = new File(conceptFilename);
    boolean conceptFileExists = conceptFile.exists();
    logger.info(String.format("    - concept filename: %s (%s)%n", conceptFilename, conceptFileExists ? "EXISTS" : "not present"));

    String assertionFilename = baseFilename + Constants.FILE_EXTENSION_ASSERTION_FILE;
    File assertionFile = new File(assertionFilename);
    boolean assertionFileExists = assertionFile.exists();
    logger.info(String.format("    - assertion filename: %s (%s)%n", assertionFilename, assertionFileExists ? "EXISTS" : "not present"));

    String relationFilename = baseFilename + Constants.FILE_EXTENSION_RELATION_FILE;
    File relationFile = new File(relationFilename);
    boolean relationFileExists = relationFile.exists();
    logger.info(String.format("    - relation filename: %s (%s)%n", relationFilename, relationFileExists ? "EXISTS" : "not present"));

    String scopeFilename = baseFilename + Constants.FILE_EXTENSION_SCOPE_FILE;
    File scopeFile = new File(scopeFilename);
    boolean scopeFileExists = scopeFile.exists();
    logger.info(String.format("    - scope filename: %s (%s)%n", scopeFilename, scopeFileExists ? "EXISTS" : "not present"));
    
    MedFactsRunner runner = new MedFactsRunner();

    runner.setConceptFileProcessor(conceptFileProcessor);
    runner.setAssertionFileProcessor(assertionFileProcessor);
    runner.setRelationFileProcessor(relationFileProcessor);
    runner.setScopeFileProcessor(scopeFileProcessor);
    runner.setEnabledFeatureIdSet(enabledFeatureIdSet);
    runner.setScopeParser(getScopeParser());
    runner.setPartOfSpeechTagger(getPartOfSpeechTagger());
    runner.setMode(mode);

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

  public void processFileSet(File baseDirectory, List<TrainingInstance> masterList, Mode mode, RunConfiguration runConfiguration)
  {
    File[] textFiles = baseDirectory.listFiles(new FilenameFilter()
    {

      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".txt");
      }
    });
    logger.info("=== TEXT FILE LIST BEGIN ===");
    logger.info(String.format("outside before \"for (File currentExtFile : textFiles)\"%n"));
    for (File currentTextFile : textFiles)
    {
      logger.info(String.format("inside \"for (File currentExtFile : textFiles)\" BEGIN for file \"%s\"%n", currentTextFile.getAbsolutePath()));
      logger.info(String.format("runConfiguration is null? %b%n", (runConfiguration == null)));

      boolean runConfigurationContainsFile = runConfiguration == null || runConfiguration.containsFile(currentTextFile);

      if (runConfiguration != null)
      {
        logger.info(String.format("$$$ INCLUDE OR NOT [\"%s\"]: %b%n", currentTextFile.getAbsolutePath(), runConfigurationContainsFile));
      }
      if (runConfigurationContainsFile)
      {
        List<TrainingInstance> trainingInstanceList = processFile(currentTextFile, mode);
        masterList.addAll(trainingInstanceList);
      } else
      {
        // skip file if not in the current run configuration
      }
      logger.info(String.format("inside \"for (File currentExtFile : textFiles)\" END for file \"%s\"%n", currentTextFile.getAbsolutePath()));
    }
    logger.info(String.format("outside after \"for (File currentExtFile : textFiles)\"%n"));
  }

  /**
   * @param baseDirectoryString the baseDirectoryString to set
   */
  public void setBaseDirectoryString(String baseDirectoryString)
  {
    this.baseDirectoryString = baseDirectoryString;
  }

  public void execute()
  {
    //File baseDirectory = new File(baseDirectoryString);
    if (trainingDirectory != null)
    {
      File trainingDirectoryFile = new File(trainingDirectory);
      processFileSet(trainingDirectoryFile, masterTrainingInstanceListTraining, Mode.TRAIN, runConfiguration);
    }

    if (decodeDirectory != null)
    {
      File decodeDirectoryFile = new File(decodeDirectory);
      processFileSet(decodeDirectoryFile, masterTrainingInstanceListEvaluation, mode, null);
    }

    // train and evaluate the classifier
    //createTrainingSplit();
    trainAndEval();

    logger.info("=== TEXT FILE LIST END ===");
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
    JarafeMETrainer trainer = new JarafeMETrainer(gaussianPrior);

    Collection<TrainingInstance> trainingInstanceSet =
        //getTrainingSplitList();
        getMasterTrainingInstanceListTraining();

    for (TrainingInstance currentTrainingInstance : trainingInstanceSet)
    {
      Set<String> featureSet = currentTrainingInstance.getFeatureSet();
      List<String> featureList = new ArrayList<String>(featureSet);
      trainer.addTrainingInstance(currentTrainingInstance.getExpectedValue(), featureList);
    }
    String model = trainer.train();
    //For testing: print out the model as a string -Alex Yeh

    writeModelToFile(model, trainingDirectory, "i2b2.model");

    // decoding

    AssertionXmlOutputLogger xmlOutputLogger = new AssertionXmlOutputLogger();

    xmlOutputLogger.setFileNameSuffix(fileNameSuffix);

    xmlOutputLogger.setBaseDirectory(baseDirectoryString);
    xmlOutputLogger.init();
    xmlOutputLogger.startDocument();

    JarafeMEDecoder decoder = new JarafeMEDecoder(model);
    int matchCount = 0;
    int notMatchCount = 0;
    Collection<TrainingInstance> evaluationInstanceSet =
        getMasterTrainingInstanceListEvaluation();
    if (mode == Mode.EVAL)
      //Warning about not relying on commas to separate features -Alex Yeh
      System.err.format("%n%nOn 'DOES NOT MATCH' lines, Features are separated by ', ', but commas are also part of some feature names.%n%n");
    for (TrainingInstance currentEvalInstance : evaluationInstanceSet)
    {
      Set<String> featureSet = currentEvalInstance.getFeatureSet();
      List<String> featureList = new ArrayList<String>(featureSet);
      String actualAssertionValueString = decoder.classifyInstance(featureList);

      AssertionAnnotation originalAssertion = currentEvalInstance.getAssertAnnotateForTI();
      AssertionAnnotation resultAssertion = new AssertionAnnotation();

      AssertionValue actualAssertionValue = null;
      if (actualAssertionValueString != null)
      {
        actualAssertionValue = AssertionValue.valueOf(actualAssertionValueString.toUpperCase());
      }
      resultAssertion.setAssertionValue(actualAssertionValue);
      resultAssertion.setBegin(originalAssertion.getBegin());
      resultAssertion.setEnd(originalAssertion.getEnd());
      resultAssertion.setConceptText(originalAssertion.getConceptText());
      resultAssertion.setConceptType(originalAssertion.getConceptType());

      List<AssertionAnnotation> listOfResultAssertions = mapOfResultBySourceFile.get(currentEvalInstance.getFilename());

      if (listOfResultAssertions == null)
      {
        listOfResultAssertions = new ArrayList<AssertionAnnotation>();
        mapOfResultBySourceFile.put(currentEvalInstance.getFilename(), listOfResultAssertions);
      }

      listOfResultAssertions.add(resultAssertion);

      if (mode == Mode.EVAL)
      {
        String expectedValue = currentEvalInstance.getExpectedValue();
        boolean actualMatchesExpected = actualAssertionValueString.equalsIgnoreCase(expectedValue);
        if (actualMatchesExpected)
        {
          logger.info(String.format("MATCHES (actual/expected) %s/%s [%s:%d] [assertion line: %d] %s ###BEGIN FEATURES###%s###END FEATURES###%n", actualAssertionValueString, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber(), currentEvalInstance.getAssertAnnotateForTI().getAnnotationFileLineNumber(), currentEvalInstance.getAssertAnnotateForTI(), currentEvalInstance.getFeatureSet().toString()));
          matchCount++;
        } else
        {
          //Added more information on 'DOES NOT MATCH' printouts (what the assertion looks like, list of features for this instance).
          //Keep everyhing on one line so it will be easier to pull out using 'grep', etc.
          //Features are separated by ', ', but only the space is reliable as many features have a comma as part of their name.
          //-Alex Yeh
          //System.err.format("DOES NOT MATCH (actual/expected) %s/%s [%s:%d] %s Features: %s%n", actualAssertionValueString, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber(), currentEvalInstance.getAssertAnnotateForTI(), currentEvalInstance.getFeatureSet().toString());
          System.err.format("DOES NOT MATCH (actual/expected) %s/%s [%s:%d] [assertion line: %d] %s ###BEGIN FEATURES###%s###END FEATURES###%n", actualAssertionValueString, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber(), currentEvalInstance.getAssertAnnotateForTI().getAnnotationFileLineNumber(), currentEvalInstance.getAssertAnnotateForTI(), currentEvalInstance.getFeatureSet().toString());
          notMatchCount++;
          //Print out string tokens and annotations for the line of this instance with a mismatch in value -Alex Yeh
          //  Side note: because this prints out in the "err stream" and "matches" prints out in the "out stream", this line may end-up appearing inbetween some of the "matches" line print outs
          String stringTokens = "=> ";
          for (String nxtToken : currentEvalInstance.getTokensForLine())
          {
            stringTokens = stringTokens + " " + nxtToken;
          }
          System.err.format("  LnTk[%d]%s%n", currentEvalInstance.getLineNumber(), stringTokens);
          for (Annotation annotationInLine : currentEvalInstance.getAnnotationsForLine())
          {
            //Side note: because this prints out in the "err stream" and "matches" prints out in the "out stream",
            // some of these lines may get interleaved with some of the "matches" line print outs
            System.err.format("  LnAn=> %s%n", annotationInLine.toString());
          }
        }
        xmlOutputLogger.addAssertion(actualMatchesExpected, actualAssertionValueString, expectedValue, currentEvalInstance.getFilename(), currentEvalInstance.getLineNumber(), currentEvalInstance.getAssertAnnotateForTI().getAnnotationFileLineNumber(), currentEvalInstance.getFeatureSet());
      } else if (mode == Mode.DECODE)
      {

      }
    }

    xmlOutputLogger.finishDocument();

    printOutResultFiles();

    logger.info(String.format("matches: %d%n", matchCount));
    logger.info(String.format("not matches: %d%n", notMatchCount));
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

  public static Set<String> loadEnabledFeaturesFromFile(File enabledFeaturesFile)
  {
    FileReader fileReader = null;
    Set<String> featureIdSet = new HashSet<String>();
    try
    {
      logger.info(String.format("opening enabled features file: %s%n", enabledFeaturesFile));
      fileReader = new FileReader(enabledFeaturesFile);
      BufferedReader br = new BufferedReader(fileReader);

      logger.fine("=== FEATURE IDS BEGIN ===");
      for (String currentLine = null; (currentLine = br.readLine()) != null; )
      {
        currentLine = currentLine.trim();
        // skip the current line if it's empty or if it's commented out
        if (currentLine.isEmpty() || currentLine.startsWith("#"))
        {
          continue;
        }
        logger.fine(String.format(" - FEATURE ID: %s%n", currentLine));
        featureIdSet.add(currentLine);
      }
      logger.fine("=== FEATURE IDS END ===");

      logger.fine("+++ FEATURE ID SET BEGIN +++");
      logger.fine(String.format("feature id set size: %d%n", featureIdSet.size()));
      for (String currentFeatureId : featureIdSet)
      {
        logger.fine(String.format(" feature id: \"%s\"%n", currentFeatureId));
      }
      logger.fine("+++ FEATURE ID SET END +++");

      return featureIdSet;
    } catch (IOException ex)
    {
      String message = "problem loading features file (IOException)";
      Logger.getLogger(BatchRunner.class.getName()).log(Level.SEVERE, message, ex);
      throw new RuntimeException(message, ex);
    } finally
    {
      try
      {
        if (fileReader != null)
        {
          fileReader.close();
        }
      } catch (IOException ex)
      {
        String message = "problem closing features file";
        Logger.getLogger(BatchRunner.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      }
    }

  }

  public void processEnabledFeaturesFile(File enabledFeaturesFile)
  {
    Set<String> featureIdSet = null;
    featureIdSet = BatchRunner.loadEnabledFeaturesFromFile(enabledFeaturesFile);
    setEnabledFeatureIdSet(featureIdSet);
  }

  public Set<String> getEnabledFeatureIdSet()
  {
    return enabledFeatureIdSet;
  }

  public void setEnabledFeatureIdSet(Set<String> enabledFeatureIdSet)
  {
    this.enabledFeatureIdSet = enabledFeatureIdSet;
  }

  public Mode getMode()
  {
    return mode;
  }

  public void setMode(Mode mode)
  {
    this.mode = mode;
  }

  public Map<String, List<AssertionAnnotation>> getMapOfResultBySourceFile()
  {
    return mapOfResultBySourceFile;
  }

  public void setMapOfResultBySourceFile(Map<String, List<AssertionAnnotation>> mapOfResultBySourceFile)
  {
    this.mapOfResultBySourceFile = mapOfResultBySourceFile;
  }

  private void printOutResultFiles()
  {
    for (Entry<String, List<AssertionAnnotation>> currentResultEntry : mapOfResultBySourceFile.entrySet())
    {
      String oldFileName = currentResultEntry.getKey();
      List<AssertionAnnotation> assertionList = currentResultEntry.getValue();

      Matcher oldFileNameMatcher = FILE_EXTENSION_PATTERN.matcher(oldFileName);

      String newFileName = oldFileNameMatcher.replaceFirst(".ast.output");

      logger.info(String.format("assertion output filename: %s%n", newFileName));

      FileWriter fileWriter = null;
      BufferedWriter bw = null;
      PrintWriter printer;
      try
      {
        fileWriter = new FileWriter(newFileName);
        bw = new BufferedWriter(fileWriter);
        printer = new PrintWriter(bw);

        for (AssertionAnnotation a : assertionList)
        {
          printer.println(a.toI2B2String());
        }

        printer.close();
        bw.close();
        fileWriter.close();

      } catch (IOException ex)
      {
        String message = "problem writing to output assertion file (IOException)";
        Logger.getLogger(BatchRunner.class.getName()).log(Level.SEVERE, message, ex);
        throw new RuntimeException(message, ex);
      }

    }
  }

  private void writeModelToFile(String model, String trainingDirectory, String filename)
  {
    try
    {
      File outputFile = new File(trainingDirectory, filename);
      FileWriter fileWriter = new FileWriter(outputFile);
      BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

      bufferedWriter.append(model);

      bufferedWriter.close();
      fileWriter.close();
    } catch (IOException e)
    {
      String message = "IOException while trying to write model to file";
      logger.log(Level.SEVERE, message, e);
      throw new RuntimeException(message, e);
    }
  }

  public ScopeParser getScopeParser()
  {
    return scopeParser;
  }

  public PartOfSpeechTagger getPartOfSpeechTagger() {
    return posTagger;
  }

  public void setScopeParser(ScopeParser scopeParser)
  {
    this.scopeParser = scopeParser;
  }

  public void setPosTagger(PartOfSpeechTagger pt) {
    this.posTagger = pt;
  }

  public RunConfiguration getRunConfiguration()
  {
    return runConfiguration;
  }

  public void setRunConfiguration(RunConfiguration runConfiguration)
  {
    this.runConfiguration = runConfiguration;
  }

  public String getFileNameSuffix()
  {
    return fileNameSuffix;
  }

  public void setFileNameSuffix(String fileNameSuffix)
  {
    this.fileNameSuffix = fileNameSuffix;
  }


}

