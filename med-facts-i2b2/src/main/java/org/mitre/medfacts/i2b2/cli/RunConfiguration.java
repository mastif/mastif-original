package org.mitre.medfacts.i2b2.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RunConfiguration
{

  protected int runNumber;
  protected List<File> fileList = new ArrayList<File>();
  protected Set<File> fileSet = new HashSet<File>();
  protected Set<String> fileStringSet = new HashSet<String>();

  public int getRunNumber()
  {
    return runNumber;
  }

  public void setRunNumber(int runNumber)
  {
    this.runNumber = runNumber;
  }

  public List<File> getFileList()
  {
    return fileList;
  }

  public void setFileList(List<File> fileList)
  {
    this.fileList = fileList;
  }

  public void addFile(File f)
  {
    fileList.add(f);
    fileSet.add(f);
    fileStringSet.add(f.getName());
  }

  public boolean containsFile(File f)
  {
    return fileStringSet.contains(f.getName());
  }
}
