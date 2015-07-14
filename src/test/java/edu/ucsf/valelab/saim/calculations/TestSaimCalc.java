///////////////////////////////////////////////////////////////////////////////
//FILE:          TestSaimCalc.java
//PROJECT:       SAIM
//SUBSYSTEM:     Tests  
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


import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author nico
 */
public class TestSaimCalc extends TestCase {
   
   public TestSaimCalc (String testName ) 
   {
      super (testName);
   }
   
   public void test() throws Exception {
      final double wl = 525.0;
      final double nSample = 1.33;
      
      // Phase difference
      double phaseDiff = SaimCalc.PhaseDiff(
              wl, Math.toRadians(0.0), nSample, 0.0);
      System.out.println("Phase difference: " + phaseDiff);
      assertEquals(0.0, phaseDiff, 0.00000000001);
      phaseDiff = SaimCalc.PhaseDiff(
              wl, Math.toRadians(0.0), nSample, 100.0);
      assertEquals(3.1834805556376575, phaseDiff, 0.00000000001);
      System.out.println("Phase difference: " + phaseDiff);
            phaseDiff = SaimCalc.PhaseDiff(
              wl, Math.toRadians(0.0), nSample, 200.0);
      System.out.println("Phase difference: " + phaseDiff);
      assertEquals(6.366961111275315, phaseDiff, 0.00000000001);
      
      // TODO: check that these Fresnel Coefficients are actually correct!
      Complex fc1 = SaimCalc.fresnel(wl, Math.toRadians(0.0), wl, nSample);
      System.out.println("Fresnel Coeficient: " + fc1.toString());
      assertEquals(0.19384803805353476, fc1.getReal(), 0.00000000001);
      assertEquals(0.20517053212175, fc1.getImaginary(), 0.00000000001);
      fc1 = SaimCalc.fresnel(wl, Math.toRadians(10.0), wl, nSample);
      assertEquals(0.2451834713177872, fc1.getReal(), 0.00000000001);
      assertEquals(0.289535850077905, fc1.getImaginary(), 0.00000000001);
      System.out.println("Fresnel Coeficient: " + fc1.toString());
   }
   
}
