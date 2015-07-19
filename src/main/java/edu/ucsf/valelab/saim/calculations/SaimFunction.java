///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimFunction.java
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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.exception.DimensionMismatchException;

/**
 *
 * @author nico
 */
public class SaimFunction implements UnivariateFunction, 
        ParametricUnivariateFunction {
   final double wavelength_; // excitation wavelength in nm
   final double dOx_;   // thickness of the oxide layer in nm
   final double nSample_;  // refractive index of the sample
   double angle_ = 0.0;   // input in degrees, internally used in radians
   double A_ = 1.0; // parameter in field strength calculation
   double B_ = 0.0; // parameter in field stenght calculation
   final Map<Double, Complex> fresnelTE_;
   
   /**
    * Constructor.  Stores several constants needed during calculations
    * @param wavelength - excitation wavelength in nm
    * @param dOx - thickness of the oxide layer in nm
    * @param nSample  - refractive index of the sample (likely 1.36 or so)
    */
   public SaimFunction( double wavelength, double dOx, double nSample ) {
      wavelength_ = wavelength;
      dOx_ = dOx;
      nSample_ = nSample;
      fresnelTE_ = new HashMap<Double, Complex>(100);
   }

   
   /**
    * Constructor that includes angle.  
    * Stores several constants needed during calculations.
    * @param wavelength - excitation wavelength in nm
    * @param dOx - thickness of the oxide layer in nm
    * @param nSample  - refractive index of the sample (likely 1.36 or so)
    * @param angle - angle with respect to the normal in the medium in degrees
    */
   public SaimFunction( double wavelength, double dOx, double nSample, 
           double angle ) {
      this(wavelength, dOx, nSample);
      angle_ = Math.toRadians(angle);
   }
   
   /**
    * Constructor that includes angle, and A and B parameters 
    * Stores several constants needed during calculations.
    * @param wavelength - excitation wavelegth in nm
    * @param dOx - thickness of the oxide layer in nm
    * @param nSample  - refractive index of the sample (likely 1.36 or so)
    * @param angle - angle with respect to the normal in the medium in degrees
    * @param A - parameter in field strength calculation
    * @param B - parameter in field strength calculation
    */
   public SaimFunction( double wavelength, double dOx, double nSample, 
           double angle, double A, double B ) {
      this(wavelength, dOx, nSample, angle);
      A_ = A;
      B_ = B;
   }
   
   /**
    * Cache the fresnelTEs since their calculation is expensive
    * @param angle angle 
    * @return 
    */
   private Complex getFresnelTE(double angle) {
      if (fresnelTE_.containsKey(angle)) {
         return fresnelTE_.get(angle);
      }
      Complex val = SaimCalc.fresnelTE(wavelength_, angle, dOx_, nSample_);
      fresnelTE_.put(angle, val);
      return val;
   }

   /**
    * Calculates the Saim function using established wavelength, angle, 
    * refractive index of the sample, and thickness of the oxide layer
    * @param h - height above the oxide layer in nm
    * @return - Field Strength (arbitrary units)
    */
   @Override
   public double value(double h) {
      Complex rTE = getFresnelTE(angle_);
      double phaseDiff = SaimCalc.PhaseDiff(wavelength_, angle_, nSample_, h);
      Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
      Complex fieldStrength = rTE.multiply(tmp);
      fieldStrength = fieldStrength.add(1.0);
      double val =  fieldStrength.getReal() * fieldStrength.getReal() + 
              fieldStrength.getImaginary() * fieldStrength.getImaginary() ;
      
      return A_ * val + B_;
   }


   /**
    * Calculates the field intensity at a given angle, given the parameters
    * A, B, and height.
    * @param x - angle in radians
    * @param parameters - array of 3 values:
    *    A - scaling parameter
    *    B - offset parameter, accounting for background
    *    h - height in nm
    * @return - Calculated field intensity
    */
   @Override
   public double value(double x, double... parameters) {
      if (parameters.length != 3)
         throw new DimensionMismatchException(parameters.length, 3);
      A_ = parameters[0];
      B_ = parameters[1];
      angle_ = x;
      return value(parameters[2]);
   }

   /**
    * Calculates the derivative of the SAIM function with respect to A, B, and h
    * at a given angle
    * 
    * @param x - angle in radians
    * @param parameters - array of 3 values:
    *    A - scaling parameter
    *    B - offset parameter, accounting for background
    *    h - height in nm
    * @return - array of 3 values with the partial derivatives for A, B, and h
    */
   @Override
   public double[] gradient(double x, double... parameters) {
      if (parameters.length != 3)
         throw new DimensionMismatchException(parameters.length, 3);
      
      double A = parameters[0];
      double h = parameters[2];
      
      // partial derivative for A is the square of |1+rTE*eiphi(h)|
      Complex rTE = getFresnelTE(angle_);
      double f = 4.0 * Math.PI * nSample_ * Math.cos(angle_) / wavelength_;
      double phaseDiff = f * h;
      Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
      Complex fieldStrength = rTE.multiply(tmp);
      fieldStrength = fieldStrength.add(1.0);
      // square of absolute 
      double val =  fieldStrength.getReal() * fieldStrength.getReal() + 
              fieldStrength.getImaginary() * fieldStrength.getImaginary();
      
      // partial derivative for B is 0
      
      // partial derivate for h is 
      // -2*A*f*sin(h) * (d + d2 + e2) + 2*A*f*cos(h)*(-e+d2+e2)
      // where f = phaseDiffFactor
      // d = rTE.Real(), and e = rTE.Imaginary()
      double d = rTE.getReal();
      double e = rTE.getImaginary();
      double pdh = -2 * A * f * Math.sin(h) * (d + d*d + e*e) +
                    2 * A * f * Math.cos(h) * (-e + d*d + e*e);
      
      double result[] = {val, 0.0, pdh};
      return result;
   }
   
   
   
}
