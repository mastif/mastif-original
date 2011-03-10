package org.mitre.medfact.learningcurve;

import java.io.File;
import org.mitre.medfacts.i2b2.annotation.ScopeParser;
import org.mitre.medfacts.i2b2.cli.BatchRunner;
import org.mitre.medfacts.i2b2.cli.Mode;



public class RunExecutor
{
  // --base-dir="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22" --cue-model="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/cue.model" --scope-model="C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/scope.model" --train=train --decode=eval --features-file="featureFile11d_no_class" --gaussian-prior=1.0 --mode=eval

  public static void main(String args[])
  {
    RunExecutor runExecutor = new RunExecutor();

    runExecutor.execute();
  }

  public void execute()
  {
    /*
    String arguments[] = 
      {
        "--base-dir=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22\"",
        "--cue-model=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/cue.model\"",
        "--scope-model=\"C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22/scope.model\"",
        "--train=train",
        "--decode=eval",
        "--features-file=\"featureFile11d_no_class\"",
        "--gaussian-prior=1.0",
        "--mode=eval"
      };
    BatchRunner.main(arguments);
    */

    BatchRunner b = new BatchRunner();
    b.setMode(Mode.EVAL);

    String baseDirectoryString = "C:/DO_NOT_BACKUP/_for_i2b2_paper/data.release.2011-02-22";
    File baseDirectory = new File(baseDirectoryString);

    File trainDirectory = new File(baseDirectory, "train");
    File evalDirectory = new File(baseDirectory, "eval");

    String scopeFileString = "scope.model";
    File scopeFile = new File(baseDirectory, scopeFileString);

    String cueFileString = "cue.model";
    File cueFile = new File(baseDirectory, cueFileString);

    ScopeParser scopeParser = new ScopeParser(scopeFile.getAbsolutePath(), cueFile.getAbsolutePath());
    b.setScopeParser(scopeParser);


    b.setTrainingDirectory(trainDirectory.getAbsolutePath());
    b.setDecodeDirectory(evalDirectory.getAbsolutePath());

    String featuresFileString = "featureFile11d_no_class";
    File featuresFile = new File(baseDirectory, featuresFileString);

    b.processEnabledFeaturesFile(featuresFile);

    b.execute();

  }
}
