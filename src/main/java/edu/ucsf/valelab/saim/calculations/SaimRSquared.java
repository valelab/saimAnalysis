
 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          SaimRSquared.java
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

import edu.ucsf.valelab.saim.data.IntensityData;
import edu.ucsf.valelab.saim.data.IntensityDataItem;
import edu.ucsf.valelab.saim.exceptions.InvalidInputException;

/**
 *
 * @author nico
 */
public class SaimRSquared {
   
   public static double get (IntensityData observedPoints, 
           IntensityData calculatedPoints) throws InvalidInputException {
      if (observedPoints.size() != calculatedPoints.size())
         throw new InvalidInputException("Observed and Calculated Data sets differ in size");
      
      // calculate (y-yavg)sqr for the observed points
      double observedAvg = observedPoints.avg();
      double errSum = 0.0;
      for (IntensityDataItem item : observedPoints.getDataList()) {
         errSum += (item.getIntensity() - observedAvg) * 
                 (item.getIntensity() -observedAvg);
      }
      
      // calculate (yobs - ycalc)sqr
      double calcErrSum = 0.0;
      for (int i = 0; i < observedPoints.size(); i++) {
         double diff = observedPoints.getDataList().get(i).getIntensity() - 
                 calculatedPoints.getDataList().get(i).getIntensity();
         calcErrSum += diff * diff;      
      }
      
      return 1.0 - (calcErrSum / errSum);
      
   }
}
