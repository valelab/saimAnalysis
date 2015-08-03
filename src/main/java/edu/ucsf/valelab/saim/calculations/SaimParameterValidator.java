 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          SaimParameterValidator.java
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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.RealVector;

/**
 * Very simple parameter validator that restrict parameters to 
 * never be smaller than lowerbound and never be higher than upperbound
 * @author nico
 */
public class SaimParameterValidator implements ParameterValidator {
   final double[] lowerBounds_;
   final double[] upperBounds_;
   
   public SaimParameterValidator(double[] lowerBounds, double[] upperBounds) {
      if (lowerBounds.length != 3)
         throw new DimensionMismatchException(lowerBounds.length, 3);
      if (upperBounds.length != 3)
         throw new DimensionMismatchException(upperBounds.length, 3);
      lowerBounds_ = lowerBounds;
      upperBounds_ = upperBounds;
   }
   
   /**
    * Very simple validator that uses lower and upper bounds to restrict
    * parameter values
    * @param params
    * @return 
    */
   @Override
   public RealVector validate(RealVector params) {
      if (params.getDimension() != 3)
         throw new DimensionMismatchException(params.getDimension(), 3);
      RealVector n = params.copy();
      for (int i = 0; i < 3; i++) {
         if (n.getEntry(i) < lowerBounds_[i])
            n.setEntry(i, lowerBounds_[i]);
         if (n.getEntry(i) > upperBounds_[i])
            n.setEntry(i, upperBounds_[i]);
      }
      return n;
   }
   
}
