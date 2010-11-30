package org.mitre.medfacts.i2b2.api.example;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mitre.medfacts.i2b2.api.DecoderDirectoryLoader;
import org.mitre.medfacts.i2b2.util.StringHandling;

/**
 *
 * @author MCOARR
 */
public class ApiDecoderExample
{
  public static final Logger logger = Logger.getLogger(ApiDecoderExample.class.getName());

  protected String model;
  protected File baseDirectory;

  public void execute()
  {
    logger.info("inside execute()");

    DecoderDirectoryLoader l = new DecoderDirectoryLoader();
    l.setDirectory(baseDirectory);
    l.setModel(model);

    l.processDirectory();
  }

  public static void main(String args[])
  {
    Options options = new Options();
    options.addOption("b", "base-dir", true, "base directory from which train and decode directories are located");
    options.addOption("m", "model", true, "mode should either be \"eval\" or \"decode\".  eval is used if you have assertion files with expected assertion values.  decode is used if you have no assertion files and thus no known assertion values.");

    CommandLineParser parser = new GnuParser();
    CommandLine cmd = null;
    try
    {
      cmd = parser.parse(options, args);
    } catch (ParseException ex)
    {
      logger.log(Level.SEVERE, "problem parsing command-line options", ex);
      throw new RuntimeException("problem parsing command-line options", ex);
    }

    String baseDir = null;
    if (cmd.hasOption("base-dir"))
    {
      baseDir = cmd.getOptionValue("base-dir");
      logger.info(String.format("using base directory: \"%s\"", baseDir));
    }

    String modelFilename = null;
    if (cmd.hasOption("model"))
    {
      modelFilename = cmd.getOptionValue("model");
      logger.info(String.format("using model: \"%s\"", modelFilename));
    }

    File modelFile = new File(baseDir, modelFilename);
    String modelValue = StringHandling.readEntireContents(modelFile);

    logger.info(String.format("model length: %d", modelValue.length()));

    File baseDirectory = new File(baseDir);

    ApiDecoderExample example = new ApiDecoderExample();
    example.setModel(modelValue);
    example.setBaseDirectory(baseDirectory);

    example.execute();
  }

  public String getModel()
  {
    return model;
  }

  public void setModel(String model)
  {
    this.model = model;
  }

  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  public void setBaseDirectory(File baseDirectory)
  {
    this.baseDirectory = baseDirectory;
  }
}
