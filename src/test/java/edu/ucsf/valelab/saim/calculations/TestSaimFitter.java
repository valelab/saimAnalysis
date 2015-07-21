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
import org.apache.commons.math3.complex.Complex;
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
   
      double A = 100.0;
      double B = 100.0;
      double h = 16.0;
      
      
      double fractionMaxError = 0.0000001;
      final int nrTries = 10;
      
      // make a collection of "observed" points
      ArrayList<WeightedObservedPoint> points = 
              new ArrayList<WeightedObservedPoint>();
       for (int i = -50; i <= 50; i+=2) {
         double angle = Math.toRadians(i);
         double I = A * SaimCalc.fieldStrength(wavelength, angle, nSample, dOx, h) +
                 B;
         WeightedObservedPoint point = new WeightedObservedPoint(1.0, angle, I);
         points.add(point);
      }
       
       
      // create the fitter
      SaimFunctionFitter sff = new SaimFunctionFitter(wavelength, dOx, nSample);
      double[] values = new double[] {A, B, h};
      
      // test by varying the input for height
      for (int i = 0; i < nrTries; i++) {
         double[] guess = values.clone();
         guess[0] = 50.0;
         guess[1] = 150.0;
         guess[2] = guess[2] + (guess[2] * (Math.random() - 0.5) * 10);
         if (guess[2] < 1.0)
            guess[2] = 1.0;
         for (int j = 0; j < guess.length; j++) {
            System.out.println("Guess: " + j + " " + guess[j]);
         }
         
         sff.setGuess(guess);

         final double[] coefficients = sff.fit(points);
         for (int j = 0; j < coefficients.length; j++) {
            System.out.println("coefficient " + j + ", expected:  "
                    + values[j] + ", found: " + coefficients[j]);
            assertEquals(values[j], coefficients[j], values[j] * fractionMaxError);
         }

         System.out.println("Value was calculated: " + sff.getCalcCount() + " times");
      }
      
      // test by adding noise to the input
       // make a collection of "observed" points
      final double noiseFactor = 0.1; // 10% noise
      fractionMaxError = noiseFactor;

      double[] guess = values.clone();
      guess[0] = 50.0;
      guess[1] = 150.0;
      guess[2] = 50.0;
      for (int j = 0; j < guess.length; j++) {
         System.out.println("Guess: " + j + " " + guess[j]);
      }

      for (int i = 0; i < nrTries; i++) {
         ArrayList<WeightedObservedPoint> noisyPoints
                 = new ArrayList<WeightedObservedPoint>();
         for (int j = -50; j <= 50; j += 2) {
            double angle = Math.toRadians(j);
            double I = A * SaimCalc.fieldStrength(wavelength, angle, nSample, dOx, h)
                    + B;
            WeightedObservedPoint noisyPoint = new WeightedObservedPoint(1.0, angle,
                    I + I * (Math.random() - 0.5) * noiseFactor);
            noisyPoints.add(noisyPoint);
         }

         sff.setGuess(guess);

         final double[] coefficients = sff.fit(noisyPoints);
         for (int j = 0; j < coefficients.length; j++) {
            System.out.println("coefficient " + j + ", expected:  "
                    + values[j] + ", found: " + coefficients[j]);
            assertEquals(values[j], coefficients[j], values[j] * fractionMaxError);
         }

         System.out.println("Value was calculated: " + sff.getCalcCount() + " times");
      }
      
      
      // Temporary code for performance testing
      // implementation not using Complex numbers is at least 10 times faster
      double angle = Math.toRadians(0.0);
      SaimFunction sf = new SaimFunction(wavelength, dOx, nSample);
      Complex rTE = sf.getFresnelTE(0);
      double f = 4.0 * Math.PI * nSample * Math.cos(angle) / wavelength;
      double phaseDiff = f * h;
      
      long startTime = System.nanoTime();
      long nrRuns = 10000000;
      double c = rTE.getReal();
      double d = rTE.getImaginary();

      for (int i = 0; i < nrRuns; i++) {
         double val = 1 + 2 * c * Math.cos(phaseDiff) - 
                 2 * d * Math.sin(phaseDiff) + c * c + d * d;
      }
      long endTime = System.nanoTime();
      long took = endTime - startTime;
      System.out.println("First method: " + nrRuns + " took: " + took + " nanoseconds");

      startTime = System.nanoTime();
      for (int i = 0; i < nrRuns; i++) {
         Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
         Complex fieldStrength = rTE.multiply(tmp);
         fieldStrength = fieldStrength.add(1.0);
         // square of absolute 
         double val = fieldStrength.getReal() * fieldStrength.getReal()
                 + fieldStrength.getImaginary() * fieldStrength.getImaginary();
      }
      endTime = System.nanoTime();
      took = endTime - startTime;
      System.out.println("Second method: " + nrRuns + " took: " + took + " nanoseconds");


   }
}
