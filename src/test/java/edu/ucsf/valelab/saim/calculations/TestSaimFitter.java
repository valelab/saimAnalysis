///////////////////////////////////////////////////////////////////////////////
 //FILE:          TestSaimFitter.java
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

import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 *
 * @author nico
 */
public class TestSaimFitter extends TestCase {
   
   public TestSaimFitter (String testName ) 
   {
      super (testName);
   }
   
   public void test() throws Exception {
      double wavelength = 488.0;
      double nSample = 1.36;
      double dOx = 500.0;
   
      double A = 1.0;
      double B = 100.0;
      double h = 16.0;
      
      // make a collection of "observed" points
      ArrayList<WeightedObservedPoint> points = 
              new ArrayList<WeightedObservedPoint>();
       for (int i = -50; i <= 50; i+=1) {
         double angle = Math.toRadians(i);
         double I = A * SaimCalc.fieldStrength(wavelength, angle, nSample, dOx, h) +
                 B;
         WeightedObservedPoint point = new WeightedObservedPoint(1.0, angle, I);
         points.add(point);
      }
       
      double percMaxError = 0.1;
       
      // create the fitter
      SaimFunctionFitter sff = new SaimFunctionFitter(wavelength, dOx, nSample);
      double[] values = new double[] {A, B, h};
      double[] guess = values.clone();
      for (int i=0; i < guess.length; i++) {
         guess[i] = values[i] + values[i] * Math.random() * 0.01;
         System.out.println("Guess: " + i + " " + guess[i]);
      }
      sff.setGuess(guess);
      final double[] coefficients = sff.fit(points);
      for (int i = 0; i < coefficients.length; i++) {
         System.out.println("coefficient " + i + ", expected:  " + 
                 values[i] + ", found: " + coefficients[i]);
         //assertEquals(values[i], coefficients[i], values[i] * percMaxError * 0.01);
      }
      
   }
   
}
