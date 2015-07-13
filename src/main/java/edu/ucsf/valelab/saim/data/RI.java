
package edu.ucsf.valelab.saim.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
      public static Map<Double, Double> siliconMap_ = 
              new HashMap<Double, Double>();
      public static Map<Double, Double> siliconOxideMap_ = 
              new HashMap<Double, Double>();;
      
      private final String fileName_;
      Compound(String fileName) {
         fileName_ = fileName;
      }
      public String getFile() {
         return fileName_;
      }
      public static Map<Double, Double> getMap(Compound compound) {
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
      Map<Double, Double> compoundMap = Compound.getMap(compound);
      if (compoundMap.containsKey(waveLength))
         return compoundMap.get(waveLength);
      
      double ri = getRIFromFile(compound, waveLength);
      compoundMap.put(waveLength, ri);
      return ri;
   } 

   /**
    * Parse the file with refractive index information
    * file has the format:
    * 
    * Wavelength(nm)	n	k
    *210	1.5384	0
    *215	1.5332	0
    *208	1.046	2.944
    *208.7	1.066	2.937
    *209.4	1.07	2.963
    * 
    * @param compound
    * @param waveLength
    * @return 
    */
   private static double getRIFromFile(Compound compound, double waveLength) {
      InputStream input = RI.class.getResourceAsStream(
              PATHINJAR + compound.getFile());
      
      Scanner s = new Scanner(input);
      int counter = 0;
      ArrayList<Double> waveLengths = new ArrayList<Double>();
      ArrayList<Double> ris = new ArrayList<Double>();
      while (s.hasNext()) {
         if (s.hasNextDouble()) {
            waveLengths.add(s.nextDouble());
            if (s.hasNextDouble()) {
               ris.add(s.nextDouble());
            }
            // throw away the third column
            if (s.hasNextDouble()) {
               s.nextDouble();
            }
            if (waveLength <= waveLengths.get(counter) && counter > 0) {
               // TODO: linear interpolation with the previous number
               s.close();
               return ris.get(counter - 1);
            }
            counter++;
         } else {
            // read away the next token:
            s.next();
         }
      }
      
      // not found....
      s.close();
      return 0.0;
   }
           
   
   
}
