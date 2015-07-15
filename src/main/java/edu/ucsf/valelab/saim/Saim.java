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
      new Saim().plotFig1b();
   }
   
   public Saim() {
   }
   
   public void plotFig1b() {
      
      double waveLength = 488.0;
      double nSample = 1.33;
      double dOx = 500.0;
      
      XYSeries[] plots = new XYSeries[4];
      plots[0] = new XYSeries(1);
      plots[1] = new XYSeries(2);
      plots[2] = new XYSeries(3);
      plots[3] = new XYSeries(4);
      boolean[] showShapes = new boolean[4];
      showShapes[0] = true; showShapes[1] = true; showShapes[2] = true; showShapes[3] = true;
      
      for (int i = 0; i <= 54; i+=2) {
         double angle = Math.toRadians(i);
         // calculate for 16 nm
         double fieldStrength = SaimCalc.fieldStrength(
                 waveLength, angle, nSample, dOx, 16.0);
         plots[0].add(i, fieldStrength);
         
         // calculate for 28nm
         fieldStrength = SaimCalc.fieldStrength(
                 waveLength, angle, nSample, dOx, 28.0);
         plots[1].add(i, fieldStrength);
                  
         // calculate for 44nm
         fieldStrength = SaimCalc.fieldStrength(
                 waveLength, angle, nSample, dOx, 44.0);
         plots[2].add(i, fieldStrength);
                  
         // calculate for 60nm
         fieldStrength = SaimCalc.fieldStrength(
                 waveLength, angle, nSample, dOx, 60.0);
         plots[3].add(i, fieldStrength);
      }
      
      Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         PlotUtils pu = new PlotUtils(prefs);
         pu.plotDataN("Fig 1b", plots, "Angle of incidence(degree)", "Intensity", showShapes, ""); 
       
    }
}
