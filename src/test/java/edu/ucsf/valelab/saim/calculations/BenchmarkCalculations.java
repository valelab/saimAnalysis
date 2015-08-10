///////////////////////////////////////////////////////////////////////////////
//FILE:          BenchmarkCalculations.java
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
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.ge edu.ucsf.valelab.saim.calculations;

package edu.ucsf.valelab.saim.calculations;

import junit.framework.TestCase;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author nico
 */
public class BenchmarkCalculations extends TestCase{
   
   public BenchmarkCalculations (String testName ) 
   {
      super (testName);
   }
   

   /**
    * Compares two methods to calculate the Saim function
    * The implementation not using Complex numbers appears to be at least
    * 10 times faster
    * @throws Exception 
    */
   public void test() throws Exception {
      
      long nrRuns = 100000000;
      
      double wavelength = 488.0;
      double nSample = 1.36;
      double dOx = 500.0;
      double h = 16.0;
      
      double angle = Math.toRadians(0.0);
      SaimFunction sf = new SaimFunction(wavelength, dOx, nSample);
      Complex rTE = sf.getFresnelTE(0);
      double f = 4.0 * Math.PI * nSample * Math.cos(angle) / wavelength;
      double phaseDiff = f * h;
      
      // method 1
      long startTime = System.nanoTime();
      double c = rTE.getReal();
      double d = rTE.getImaginary();
      for (int i = 0; i < nrRuns; i++) {
         double val = 1 + 2 * c * Math.cos(phaseDiff) - 
                 2 * d * Math.sin(phaseDiff) + c * c + d * d;
      }
      long endTime = System.nanoTime();
      long took = endTime - startTime;
      System.out.println("First method: " + nrRuns + " runs took: " + 
              took / 1000000 + " milliseconds");

      // method 2
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
      System.out.println("Second method: " + nrRuns + " runs took: " + 
              took / 1000000 + " milliseconds");

   }
   
}
