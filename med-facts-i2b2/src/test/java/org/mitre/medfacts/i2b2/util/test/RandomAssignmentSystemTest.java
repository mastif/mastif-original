/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mitre.medfacts.i2b2.util.test;

import java.util.Set;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mitre.medfacts.i2b2.util.RandomAssignmentItem;
import org.mitre.medfacts.i2b2.util.RandomAssignmentSystem;

/**
 *
 * @author MCOARR
 */
public class RandomAssignmentSystemTest {

    public RandomAssignmentSystemTest() {
    }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

     @Test
     public void testRandomAssignmentSystemSimple()
     {
       RandomAssignmentSystem system = new RandomAssignmentSystem();
       system.setTrainingRatio(0.8f);

       for (int i=0; i < 10; i++)
       {
         system.addItem(i);
       }

       system.createSets();

       Set<RandomAssignmentItem> trainingSet = system.getTrainingSetSorted();
       Set<RandomAssignmentItem> testSet = system.getTestSetSorted();

       System.out.println("=== TRAINING SET begin ===");
       printoutSet(trainingSet);
       System.out.println("=== TRAINING SET begin ===");
       System.out.println();

       System.out.println("=== TEST SET begin ===");
       printoutSet(testSet);
       System.out.println("=== TEST SET begin ===");
       Assert.assertEquals("training size on an incoming set of 10 with an 80/20 split should be 8 test items", 8, trainingSet.size());
       Assert.assertEquals("test size on an incoming set of 10 with an 80/20 split should be 2 test items", 2, testSet.size());
     }

  private void printoutSet(Set<RandomAssignmentItem> set)
  {
    for (RandomAssignmentItem current : set)
    {
      System.out.format(" - %s%n", current.toString());
    }
  }

}
