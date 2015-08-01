///////////////////////////////////////////////////////////////////////////////
//FILE:          TestSaimErrorFunction.java
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
public class TestSaimErrorFunction extends TestCase {
   
   public TestSaimErrorFunction(String testName) {
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
       // calculate the error with these ideal points (shoudle be 0)
      SaimErrorFunction sef = new SaimErrorFunction(data, points);
      double[] parameters = {data.A_, data.B_, data.h_};
      double error = sef.value(parameters);
      
      System.out.println("SaimError error: " + error);
      assertEquals(0.0, error, maxError); 
      
      // now add 1 to all the data points.  Error should be number of data points
      ArrayList<WeightedObservedPoint> newPoints = 
              new ArrayList<WeightedObservedPoint>();
      for (WeightedObservedPoint point : points) {
         newPoints.add(new WeightedObservedPoint(1.0, point.getX(), point.getY() + 1.0));
      }
      SaimErrorFunction sef2 = new SaimErrorFunction(data, newPoints);
      error = sef2.value(parameters);
      
      System.out.println("SaimError error: " + error);
      assertEquals(points.size(), error, maxError); 
      
      
    }
}
