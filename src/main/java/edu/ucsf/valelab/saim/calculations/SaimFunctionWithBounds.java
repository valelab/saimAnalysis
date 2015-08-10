///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimFunctionWithBounds.java
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
import org.apache.commons.math3.exception.DimensionMismatchException;

/**
 * Tries to impose bounds on the Saimfunction parameters by returning
 * Double.MaxValue whenever one of the partameters goes negative.
 * @author nico
 */
public class SaimFunctionWithBounds extends SaimFunction {
   
   public SaimFunctionWithBounds( double wavelength, double dOx, double nSample ) {
      super(wavelength, dOx, nSample);
   }
   
   public SaimFunctionWithBounds( SaimData sd ) {
      super(sd);
   }
   
  
   /**
    * Calculates the Saim function using established wavelength, angle, 
    * refractive index of the sample, and thickness of the oxide layer
    * @param h - height above the oxide layer in nm
    * @return - Field Strength (arbitrary units)
    */
   @Override
   public double value(double h) {
      if (h < 0) {
         return Double.MAX_VALUE;
      }
      return super.value(h);
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
      sd_.A_ = parameters[0];
      sd_.B_ = parameters[1];
      angle_ = x;
      if (parameters[0] < 0 || parameters[1] < 0 || parameters[2] < 0) {
         return Double.MAX_VALUE;
      }
      return value(parameters[2]);
   }
}