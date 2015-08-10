///////////////////////////////////////////////////////////////////////////////
//FILE:          IntensityDataItem.java
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

package edu.ucsf.valelab.saim.data;

/**
 * Simple data structure to hold measured and fitted data 
 * @author nico
 */

public class IntensityDataItem {
   private final double angleDegrees_;
   private final double angleRadians_;
   private final double intensity_;
   
   public IntensityDataItem(double angle, double intensity, boolean isRadians) {
      if (isRadians) {
         angleRadians_ = angle;
         angleDegrees_ = Math.toDegrees(angle);
      } else { // angle was given in degrees
         angleDegrees_ = angle;
         angleRadians_ = Math.toRadians(angle);
      }
      intensity_ = intensity;
   }
   
   public IntensityDataItem(double angleDegrees, double angleRadians, double intensity) {
      angleDegrees_ = angleDegrees;
      angleRadians_ = angleRadians;
      intensity_ = intensity;
   }
   
   public double getAngleDegree() { return angleDegrees_; }
   
   public double getAngleRadians() { return angleRadians_; }
   
   public double getIntensity() { return intensity_; }
   
}
