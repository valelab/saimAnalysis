///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimData.java
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

import java.util.Arrays;

/**
 * Very simple data structure to hold parameters that are used over and over
 * again in the Saim calculations
 * @author nico
 */
public class SaimData {
   public double wavelength_ = 488.0;
   public double nSample_ = 1.36;
   public double dOx_ = 1900.0;
   public double firstAngle_ = -42;
   public double angleStep_ = 1;
   public double A_ = 1000.0;
   public double B_ = 5000.0;
   public double[] heights_ = new double[] {100.0};
   public int threshold_ = 5000;
   public boolean mirrorAround0_ = false;
   public boolean zeroDoubled_ = false;
   public boolean useBAngle_ = false;
   public String flatFieldFile_ = "";
   public String backgroundFile_ = "";
   
   public SaimData copy() {
      SaimData cp = new SaimData();
      cp.wavelength_ = wavelength_;
      cp.nSample_ = nSample_;
      cp.dOx_ = dOx_;
      cp.firstAngle_ = firstAngle_;
      cp.angleStep_ = angleStep_;
      cp.A_ = A_;
      cp.B_ = B_;
      cp.heights_ = heights_;
      cp.threshold_ = threshold_;
      cp.mirrorAround0_ = mirrorAround0_;
      cp.zeroDoubled_ = zeroDoubled_;
      return cp;
   }
   
   /**
    * Utility function to represent heights array to the user 
    * Array will be represented as:
    * val1, val2, valn
    * @param myArray Array of doubles
    * @return String representation
    */
   public static String toString(double[] myArray) {
      String result = Arrays.toString(myArray);
      return result.substring(1, result.length() - 1);
   }
   
   /**
    * Utility function to convert a String in the form of
    * val1, val2, val3, valn
    * into an arry of doubles
    * 
    * If the input is not properly formatted, expect a NumberFormat Exception
    * 
    * @param input 
    * @return array of doubles with expected values
    */
   public static double[] fromString(String input) {
      String[] strings = input.replace("[","").replace("]","").split(",");
      double[] result = new double[strings.length];
      for (int i = 0; i < result.length; i++) {
         strings[i] = strings[i].trim();
         result[i] = Double.parseDouble(strings[i]);
      }
      return result;
   }
   
}
