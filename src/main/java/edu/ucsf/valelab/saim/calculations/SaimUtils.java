 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          SaimUtils.java
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
import edu.ucsf.valelab.saim.data.SaimData;
import edu.ucsf.valelab.saim.exceptions.InvalidInputException;

/**
 * Collection of static utility functions for the data structures used in 
 * the SAIM plugin
 * @author nico
 */
public class SaimUtils {
   
   /**
    * Given collections of observed and calculated points, return the RSquared
    * @param observedPoints - observedPoints in out own format
    * @param calculatedPoints - calculated points in our own format
    * @return estimate of the goodness of fit
    * @throws InvalidInputException 
    */
   public static double getRSquared (IntensityData observedPoints, 
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
   
   /**
    * Using the function parameters and observed data, fill a dataset
    * with predicted values.  The dataset should already be created and be empty
    * @param observed - observed data set
    * @param predicted - data predicted by the function parameters
    * @param params - parameter ()
    * @param sf - SaimFunction, generated with the appropriate parameters
    */
   public static void predictValues(IntensityData observed, 
           IntensityData predicted, double[] params, SaimFunction sf) {
      for (IntensityDataItem item : observed.getDataList()) {
         double intensity = sf.value(item.getAngleRadians(), params);
         predicted.add(item.getAngleRadians(), intensity, true);
      }
   }
   
   /**
    * Organizes input data based on requirement to average values for 
    * positive and negative angles and whether there are two observations
    * for the zero angle.
    * Expects arrays of angles organized as if only the starting angle 
    * and stepsize was known
    * Takes information about mirroring and zero skipping from  SaimData
    * @param id Instance of IntensityData.  Data will be aded to this
    * @param sd SaimData, we will use mirrorAround0_ and zeroDoubled_
    * @param values array with intensity data
    * @param anglesDegrees array of same size as values with angles in degrees
    * @param anglesRadians array of same size as values with angles in radians
    * @throws InvalidInputException 
    */
   public static void organize(IntensityData id, SaimData sd, float[] values, 
           double[] anglesDegrees, double[] anglesRadians) 
           throws InvalidInputException {
      
      if (values.length != anglesDegrees.length || 
              values.length != anglesRadians.length) {
         throw new InvalidInputException (
                 "SaimUtils.organize: input arrays are not equal in size");
      }
      
      if (!sd.mirrorAround0_) {
         int indexCorrect = 0;
         for (int i = 0; i < values.length; i++) {
            if (sd.zeroDoubled_ && anglesDegrees[i] == 0) {
               indexCorrect = 1;
               continue;
            }
            if (sd.zeroDoubled_ && anglesDegrees[i] > 0) {
               if (anglesDegrees[i-1] == 0) {
                  id.add(0, anglesRadians[i - 1], 
                          (values[i] + values[i - 1]) / 2);
                  continue;
               }
            }
            int index = i - indexCorrect;
            id.add(anglesDegrees[index], anglesRadians[index], values[index]);
         }
      } else { // mirrored around 0
         // sanity check
         int lastIndex = anglesDegrees.length;
         if (sd.zeroDoubled_) {
            lastIndex--;
         }
         if (anglesDegrees[0] != -anglesDegrees[lastIndex - 1]) {
            throw new InvalidInputException ("First and last angle are different! " +
                    "Can not mirror around 0");
         }

         for (int i = 0 ; i < (values.length / 2); i++) {
            id.add(-anglesDegrees[i], -anglesRadians[i],
                    (values[values.length - 1 - i]) / 2 );
         }
      }
      
   }
   
}