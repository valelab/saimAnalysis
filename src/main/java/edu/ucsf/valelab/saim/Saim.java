package edu.ucsf.valelab.saim;

import edu.ucsf.valelab.saim.calculations.SaimCalc;
import edu.ucsf.valelab.saim.plot.PlotUtils;
import java.util.prefs.Preferences;
import org.jfree.data.xy.XYSeries;

/**
 * 
 *
 */
public class Saim 
{
   public static void main( String[] args )
   {
      new Saim().plotFig();
   }
   
   public Saim() {
   }
   
   public void plotFig() {
      
      double waveLength = 488.0;
      double nSample = 1.36;
      double dOx = 500.0;
      
      int n = 2;
      double[] height =  { 16.0, 28.0, 40.0, 56.0, 72.0, 88.0 };
      boolean[] showShapes = new boolean[n];
      XYSeries[] plots = new XYSeries[n];
      for (int i = 0; i < n; i++) {
         plots[i] = new XYSeries("" + height[i] + "nm", false, false);
         showShapes[i] = false;
      }
      
      for (int i = 0; i <= 52; i+=1) {
         double angle = Math.toRadians(i);
         // calculate for 16 nm
         for (int j = 0; j < n; j++) {
            double fieldStrength = SaimCalc.fieldStrength(
                    waveLength, angle, nSample, dOx, height[j]);
            plots[j].add(i, fieldStrength);
         }
      }
      
      for (int i = 0; i < plots.length; i++) {
         plots[i] = PlotUtils.normalize(plots[i]);
      }
      
      Preferences prefs = Preferences.userNodeForPackage(this.getClass());
      PlotUtils pu = new PlotUtils(prefs);
      pu.plotDataN("Fig 1b, at " + waveLength + " nm, n " + nSample + ", dOx: " + dOx + " nm", plots, 
              "Angle of incidence (degrees)", 
              "Normalized Intensity", showShapes, ""); 
       
    }
}
