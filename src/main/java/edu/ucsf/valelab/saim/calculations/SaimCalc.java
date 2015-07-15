///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimCalc.java
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
 * Various calculations needed in the calculation of the strength of the local 
 * electric field as described in:
 * Paszek, M.J., C.C. DuFort, M.G. Rubashkin, M.W. Davidson, K.S. Thorn, J.T. 
 * Liphardt, and V.M. Weaver. 2012. 
 * Scanning angle interference microscopy reveals cell dynamics at the nanoscale. 
 * Nat Meth. 9:825–827. doi:10.1038/nmeth.2077.
 * 
 * @author nico
 */
public class SaimCalc {
   
   /**
    * Calculates the phase difference as a function of height as described in:
    * Paszek, M.J., C.C. DuFort, M.G. Rubashkin, M.W. Davidson, K.S. Thorn, J.T. 
    * Liphardt, and V.M. Weaver. 2012. 
    * Scanning angle interference microscopy reveals cell dynamics at the nanoscale. 
    * Nat Meth. 9:825–827. doi:10.1038/nmeth.2077.
    *
    * @param wavelength of the excitation light source in nm
    * @param nSample refractive index of the sample medium
    * @param angle Angle with respect to the normal in the sample in radians
    * @param axialPos Position in the sample above the silicon oxide (in nm)
    * @return phase difference (dimensionless?)
    */
   public static double PhaseDiff (
           final double wavelength, 
           final double angle, 
           final double nSample, 
           final double axialPos)
   {
      return 4.0 * Math.PI * nSample * axialPos * Math.cos(angle) / wavelength;     
   }
   
   /**
    * Calculates the Fresnel coefficient as described in:
    * Paszek, M.J., C.C. DuFort, M.G. Rubashkin, M.W. Davidson, K.S. Thorn, J.T. 
    * Liphardt, and V.M. Weaver. 2012. 
    * Scanning angle interference microscopy reveals cell dynamics at the nanoscale. 
    * Nat Meth. 9:825–827. doi:10.1038/nmeth.2077.
    * 
    * @param wavelength of the excitation light source in nm
    * @param angle with respect to the normal in radiance
    * @param dOx Thickness of the Silicon Oxide layer in nm
    * @param nSample Refractive index of the sample's buffer
    * @return FresnelCoefficient for these conditions
    */
   public static Complex fresnel(
           final double wavelength, 
           final double angle, 
           final double dOx, 
           final double nSample) {
      
      double nOx = RI.getRI(RI.Compound.SILICONOXIDE, wavelength);
      double kOx = k(wavelength, nOx);
      double cosOx = Math.cos(snell2(angle, nSample, 
                      RI.getRI(RI.Compound.SILICONOXIDE, wavelength)));
      double p1 = nOx * cosOx;
      double p2 = nSample * Math.cos(angle);
      double kOxdOxCosOx =  kOx * dOx * cosOx; 
      
      double m11TE = Math.cos( kOxdOxCosOx );
      Complex m12TE = Complex.I.multiply(-1/p1 * Math.sin( kOxdOxCosOx ) );
      Complex m21TE = Complex.I.multiply(-p1 * Math.sin( kOxdOxCosOx ) );
      double m22TE = Math.cos( kOxdOxCosOx );
      
      Complex tmp = m12TE.add(m11TE).multiply(p2).add(m21TE.subtract(m22TE));
      Complex tmp2 = m12TE.add(m11TE).multiply(p2).add(m21TE.add(m22TE));
      Complex rTE = tmp.divide(tmp2);
      
      return rTE;
   }
   
   public static double fieldStrength(
         final double wavelength,
         final double angle,
         final double nSample,
         final double dOx,
         final double distance) 
   {
      Complex rTE = SaimCalc.fresnel(wavelength,angle, dOx, nSample);
      double phaseDiff = PhaseDiff(wavelength, angle, nSample, distance);
      Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
      Complex fieldStrength = rTE.multiply(tmp);
      fieldStrength = fieldStrength.add(1);

      return fieldStrength.abs() * fieldStrength.abs();
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
   
   /**
    * Calculates the angle of incidence for light traveling through a medium
    * with known refractive index and angle of incidence into a medium with 
    * another known refractive index using Snell's law: sin(1)/sin(2) = n2/n1
    * 
    * @param angle1 angle with respect to the normal in radiance in medium 1
    * @param ri1 Refractive index of medium 1
    * @param ri2 Refractive index of medium 2
    * @return Angle of incidence in medium 2
    */
   public static double snell2(double angle1, double ri1, double ri2) {
      double sinAngle2 = ri1 * Math.sin(angle1) / ri2;
      return Math.asin(sinAngle2);
   }
}
