///////////////////////////////////////////////////////////////////////////////
//FILE:          FresnelCoefficient.java
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

import edu.ucsf.valelab.saim.data.RI;
import org.apache.commons.math3.complex.Complex;

/**
 *
 * @author nico
 */
public class FresnelCoefficient {
   
   public static double calculate(
           double waveLength, 
           double angle, 
           double dOx, 
           double riSample) {
      
      double nOx = RI.getRI(RI.Compound.SILICONOXIDE, waveLength);
      double kOx = k(waveLength, nOx);
      double cosOx = Math.cos( snell2(angle, riSample, 
                      RI.getRI(RI.Compound.SILICONOXIDE, waveLength)));
      double p1 = nOx * cosOx;
      Complex i = new Complex (0,1);
      double kOxdOxCosOx =  kOx * dOx * cosOx; 
      
      double m11TE = Math.cos( kOxdOxCosOx );
      Complex m12TE = i.multiply(-1/p1 * Math.sin( kOxdOxCosOx ) );
      Complex m21TE = i.multiply(-p1 * Math.sin( kOxdOxCosOx ) );
      double m22TE = Math.cos( kOxdOxCosOx );
      
      return 0.0;
   }
   
   /**
    * Returns the wavenumber given the wavelength and refractive index
    * @param waveLength in nm
    * @param ri of the compound of interest
    * @return wavenumber (# of waves per nm)
    */
   public static double k(double waveLength, double ri) {
      return 2 * Math.PI * ri / waveLength;
   }
   
   public static double snell2(double angle1, double ri1, double ri2) {
      double sinAngle2 = ri1 * Math.sin(angle1) / ri2;
      return Math.asin(sinAngle2);
   }
}
