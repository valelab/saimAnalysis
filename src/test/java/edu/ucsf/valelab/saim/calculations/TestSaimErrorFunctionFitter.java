///////////////////////////////////////////////////////////////////////////////
//FILE:          TestSaimErrorFunctionFitter.java
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

import edu.ucsf.valelab.saim.data.SaimData;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 *
 * @author nico
 */
public class TestSaimErrorFunctionFitter extends TestCase {
   public TestSaimErrorFunctionFitter (String testName) {
      super (testName);
   }
   
   public void test() throws Exception {
      SaimData data = new SaimData();
      
      data.wavelength_ = 488.0;
      data.nSample_ = 1.36;
      data.dOx_ = 500.0;
      data.A_ = 1000.0;
      data.B_ = 5000.0;
      data.h_ = 75.0;
      
      
      double maxError = 0.00000000001;
      int nrRepeats = 5;
      
      // make a collection of "observed" points
      ArrayList<WeightedObservedPoint> points = 
              new ArrayList<WeightedObservedPoint>();
      for (int i = -50; i <= 50; i+=1) {
         double angle = Math.toRadians(i);
         double I = data.A_ * SaimCalc.fieldStrength(data.wavelength_, angle, 
                 data.nSample_, data.dOx_, data.h_) +
                 data.B_;
         WeightedObservedPoint point = new WeightedObservedPoint(1.0, angle, I);
         points.add(point);
      }
      System.out.println("BOBYQA Test results (TestSaimErrorFunctionFitter:");
      System.out.println("Goal: " + data.A_ + ", " + data.B_ + ", " + data.h_);
      
      for (int i = 0; i < nrRepeats; i++) {
         SaimErrorFunctionFitter sef = new SaimErrorFunctionFitter(data);
         double[] guess = {Math.random() * 4000.0, Math.random() * 10000.0, 
            Math.random() * 300.0};
         sef.setGuess(guess);
         double[] result = sef.fit(points);
         System.out.println("Guess: " + guess[0] + ", " + guess[1] + ", " + guess[2]);
         System.out.println("Result: "+  result[0] + ", " + result[1] + ", " + result[2]);
         
      }
   }
      
}
