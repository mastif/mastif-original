/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mitre.medfacts.i2b2.util.test;

import org.mitre.medfacts.i2b2.util.StringHandling;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author MCOARR
 */
public class AbsoluteFileNameTest
{

  @Test
  public void testAbsoluteFileNameTest()
  {
    String windowsAbsoluteFileName = "c:\\home\\mcoarr\\myfile.txt";
    String windowsRelativeFileName = "myfile.txt";
    String unixAbsoluteFileName = "/my/dir/myfile.txt";
    String unixRelativeFileName = "myfile.txt";

//    String regexString = "^((/)|([A-Za-z]:[/\\\\])).*";
//    Pattern pattern = Pattern.compile(regexString);
//    Matcher matcher = pattern.matcher(windowsAbsoluteFileName);
//
//    boolean isMatch = matcher.matches();
//    assertTrue("should be a windows absolute filename", isMatch);
    
    boolean expected = true;
    boolean actual = StringHandling.isAbsoluteFileName(windowsAbsoluteFileName);
    assertTrue("oops, did not match correctly for windows absolute file name", expected == actual);

    expected = false;
    actual = StringHandling.isAbsoluteFileName(windowsRelativeFileName);
    assertTrue("oops, did not match correctly for windows relative file name", expected == actual);

    expected = true;
    actual = StringHandling.isAbsoluteFileName(unixAbsoluteFileName);
    assertTrue("oops, did not match correctly for unix absolute file name", expected == actual);

    expected = false;
    actual = StringHandling.isAbsoluteFileName(unixRelativeFileName);
    assertTrue("oops, did not match correctly for unix relative file name", expected == actual);
}
}
