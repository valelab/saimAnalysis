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
    * @return phase difference (dimensionless)
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
    * Calculates the transverse electric (TE) component, perpendicular to the 
    * plane of incidence, of the Fresnel coefficient of reflection between the 
    * sample interface and the virtual silicon oxide–silicon layer, 
    * as described in:
    * Paszek, M.J., C.C. DuFort, M.G. Rubashkin, M.W. Davidson, K.S. Thorn, J.T. 
    * Liphardt, and V.M. Weaver. 2012. 
    * Scanning angle interference microscopy reveals cell dynamics at the nanoscale. 
    * Nat Meth. 9:825–827. doi:10.1038/nmeth.2077.
    * 
    * 
    * 1/11/2016: Note that the above manuscript contains a mistake that is corrected
    * in a later publication: http://dx.doi.org/10.1016/B978-0-12-420138-5.00013-6
    * That correction is now applied.
    * 
    * 
    * @param wavelength of the excitation light source in nm
    * @param angle with respect to the normal in radiance
    * @param dOx Thickness of the Silicon Oxide layer in nm
    * @param nSample Refractive index of the sample's buffer
    * @return FresnelCoefficient for these conditions
    */
   public static Complex fresnelTE(
           final double wavelength, 
           final double angle, 
           final double dOx, 
           final double nSample) {
       
      double nSi = RI.getRI(RI.Compound.SILICON, wavelength);
      double nOx = RI.getRI(RI.Compound.SILICONOXIDE, wavelength);
      double kOx = k(wavelength, nOx);
      double angleOx = snell2( angle, nSample, 
                      RI.getRI(RI.Compound.SILICONOXIDE, wavelength));
      double cosOx = Math.cos(angleOx);
      double cosSi = Math.cos( snell2( angleOx, nOx, 
              RI.getRI(RI.Compound.SILICON, wavelength)));
      double p0 = nSi * cosSi;
      double p1 = nOx * cosOx;
      double p2 = nSample * Math.cos(angle);
      double kOxdOxCosOx =  kOx * dOx * cosOx; 
      
      double cosOfkOxdOxCosOx = Math.cos(kOxdOxCosOx);
      double sinOfkOxdOxCosOx = Math.sin(kOxdOxCosOx);
      
      double m11TE = cosOfkOxdOxCosOx;
      Complex m12TE = Complex.I.multiply(-1/p1 * sinOfkOxdOxCosOx );
      Complex m21TE = Complex.I.multiply(-p1 * sinOfkOxdOxCosOx );
      double m22TE = cosOfkOxdOxCosOx;
      
      Complex tmp1 = ( (m12TE.multiply(p0)).add(m11TE) ).multiply(p2);
      // this is the only line changed due to the error in the NM paper
      Complex tmp2 = tmp1.subtract( m21TE.add(m22TE * p0) );
      //Complex tmp2 = tmp1.add( m21TE.subtract(m22TE * p0) );
      Complex tmp3 = tmp1.add( m21TE.add(m22TE * p0) );
      Complex rTE = tmp2.divide(tmp3);
      
      return rTE;
   }
   
   public static double fieldStrength(
         final double wavelength,
         final double angle,
         final double nSample,
         final double dOx,
         final double distance) 
   {
      Complex rTE = fresnelTE(wavelength, angle, dOx, nSample);
      double phaseDiff = PhaseDiff(wavelength, angle, nSample, distance);
      Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
      Complex fieldStrength = rTE.multiply(tmp);
      fieldStrength = fieldStrength.add(1.0);

      return  fieldStrength.getReal() * fieldStrength.getReal() + 
              fieldStrength.getImaginary() * fieldStrength.getImaginary() ;
   }
   
   /**
    * Returns the angular wavenumber given the wavelength and refractive index
    * See: https://en.wikipedia.org/wiki/Wavenumber#In_wave_equations
    * @param wavelength in nm in vacuum
    * @param ri refractive index of the compound of interest
    * @return wavenumber (in radians per nm)
    */
   public static double k(double wavelength, double ri) {
      return 2.0 * Math.PI * ri / wavelength;
   }
   
   /**
    * Calculates the angle of incidence for light traveling through a medium
    * with known refractive index and angle of incidence into a medium with 
    * another known refractive index using Snell's law: sin(1)/sin(2) = n2/n1
    * See: https://en.wikipedia.org/wiki/Snell%27s_law
    * 
    * @param angle1 angle with respect to the normal in radiance in medium 1
    * @param ri1 Refractive index of medium 1
    * @param ri2 Refractive index of medium 2
    * @return angle of incidence in medium 2 in radiance
    */
   public static double snell2(double angle1, double ri1, double ri2) {
      double sinAngle2 = ri1 * Math.sin(angle1) / ri2;
      return Math.asin(sinAngle2);
   }
}
