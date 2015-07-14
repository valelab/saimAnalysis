
package edu.ucsf.valelab.saim.calculations;

import edu.ucsf.valelab.saim.data.RI;

/**
 *
 * @author nico
 */
public class FresnelCoefficient {
   public static double calculate(double waveLength, double angle, double dOx, 
           double riSample) {
      double m11TE = Math.cos(
              k(waveLength, RI.getRI(RI.Compound.SILICON, waveLength)) *
              dOx *
              Math.cos( snell2(angle, riSample, 
                      RI.getRI(RI.Compound.SILICONOXIDE, waveLength)))
      );
      
      
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
