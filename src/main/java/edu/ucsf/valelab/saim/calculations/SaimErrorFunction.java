///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimErrorFunction.java
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

import edu.ucsf.valelab.saim.data.SaimData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 * Calculates the error (as sum of absolute errors) between given set of 
 * data points and theoretical prediction given A, B, and h).
 * 
 * @author nico
 */
public class SaimErrorFunction implements MultivariateFunction {

   private final Collection<WeightedObservedPoint> observedPoints_;
   // note that we only use wavelength, dOx and nSample from SaimData
   private final SaimData data_;
   private final Map<Double, Complex> fresnelTE_;
   
   public SaimErrorFunction(SaimData data, Collection<WeightedObservedPoint> observedPoints) {
      data_ = data;
      observedPoints_ = observedPoints;
      fresnelTE_ = new HashMap<Double, Complex>(observedPoints_.size());
      // pre-calculate all the fresnel coefficients
      for (WeightedObservedPoint observedPoint : observedPoints_) {
         double angle = observedPoint.getX();
         Complex val = SaimCalc.fresnelTE(data_.wavelength_, 
                 angle, data_.dOx_, data_.nSample_);
         fresnelTE_.put(angle, val);
      }
   }

   /**
    * For each observedPoint.getX calculates the predicted intensity
    * Returns the sum of absolute errors
    * @param point {A, B, h}
    * @return sum of absolute errors
    */
   @Override
   public double value(double[] point) {
      if (point.length != 3) {
         throw new DimensionMismatchException(point.length, 3);
      }
      
      double A = point[0];
      double B = point[1];
      double h = point[2];
      
      double error = 0.0;
      for (WeightedObservedPoint observedPoint : observedPoints_) {
         double angle = observedPoint.getX();
         Complex rTE = fresnelTE_.get(angle);
         double phaseDiff = SaimCalc.PhaseDiff(data_.wavelength_, angle, 
                  data_.nSample_, h);
         double c = rTE.getReal();
         double d = rTE.getImaginary();
         double val = 1 + 2 * c * Math.cos(phaseDiff) - 
             2 * d * Math.sin(phaseDiff) + c * c + d * d;
         error += Math.abs(A * val + B - observedPoint.getY());
      }
      return error;
   }
   
}
