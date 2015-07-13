
package edu.ucsf.valelab.saim.data;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Use this class to get th refractive index of compounds of interest.
 * Refractive indices (RIs) depend on wavelength.  
 * This code uses tables from https://www.filmetrics.com.  These
 * tables are included in the jar file, and will be read when needed.
 * The RI will be deduced by linear interpolation and will be cached
 * for future use.
 * 
 * Use the class as follows:
 * double ri = RI.getRI(Compound.SILICON, 525.0);
 * 
 * @author nico
 */
public class RI {
 
   private static final String PATHINJAR = "/edu/ucsf/valelab/data/";
   
   public static enum Compound  { 
      SILICON ("siliconRI.txt"), 
      SILICONOXIDE ("siliconOxideRI.txt");
      
      // Hashmaps with cached values
      public static HashMap<Double, Double> siliconMap_;
      public static HashMap<Double, Double> siliconOxideMap_;
      
      private final String fileName_;
      Compound(String fileName) {
         fileName_ = fileName;
      }
      public String getFile() {
         return fileName_;
      }
      public static HashMap<Double, Double> getMap(Compound compound) {
         if (compound.equals(Compound.SILICON)) {
            return siliconMap_;
         }
         if (compound.equals(Compound.SILICONOXIDE)) {
            return siliconOxideMap_;
         }
         return null;
      }
   }
   
   public static double getRI(Compound compound, double waveLength) {
      HashMap<Double, Double> compoundMap = Compound.getMap(compound);
      if (compoundMap.containsKey(waveLength))
         return compoundMap.get(waveLength);
      
      double ri = getRIFromFile(compound, waveLength);
      compoundMap.put(waveLength, ri);
      return ri;
   } 

   
   private static double getRIFromFile(Compound compound, double waveLength) {
      InputStream input = RI.class.getResourceAsStream(
              PATHINJAR + compound.getFile());
      // TODO: parse the file and do the linear interpolation
      
      return 0.0;
   }
           
   
   
}
