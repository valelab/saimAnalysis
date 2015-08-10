///////////////////////////////////////////////////////////////////////////////
 //FILE:          TestSaimUtils.java
 //PROJECT:       SAIM
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Nico Stuurman
 //
 // COPYRIGHT:    University of California, San Francisco 2015
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package edu.ucsf.valelab.saim.calculations;

import edu.ucsf.valelab.saim.data.IntensityData;
import junit.framework.TestCase;

/**
 *
 * @author nico
 */
public class TestSaimUtils extends TestCase {
   
   public TestSaimUtils(String name) {
      super(name);
   }
   
   public void test() throws Exception {
      IntensityData observedPoints = new IntensityData();
      observedPoints.add(-2, -3, false);
      observedPoints.add(-1, -1, false);
      observedPoints.add(1, 2, false);
      observedPoints.add(4, 3, false);
      IntensityData calculatedPoints = new IntensityData();
      calculatedPoints.add(-2, myFun(-2), false);
      calculatedPoints.add(-1, myFun(-1), false);
      calculatedPoints.add(1, myFun(1), false);
      calculatedPoints.add(4, myFun(4), false);
      double r2 = SaimUtils.getRSquared(observedPoints, calculatedPoints);
      System.out.println("Measured R2 is: " + r2);
      assertEquals(r2, 0.87964, 0.0001);
      
   }
   
   /**
    * Kahn academy example function
    * @param x x value
    * @return results of this linear function
   */
   public double myFun(double x) {
      return 41.0/42.0 * x - 5.0/21.0; 
   }
   
}
