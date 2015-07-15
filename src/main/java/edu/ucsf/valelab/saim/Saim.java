package edu.ucsf.valelab.saim;

import edu.ucsf.valelab.saim.calculations.SaimCalc;
import edu.ucsf.valelab.saim.plot.PlotUtils;
import java.util.prefs.Preferences;
import org.apache.commons.math3.complex.Complex;
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
      
      double waveLength = 525.0;
      double nSample = 1.33;
      double dOx = 500.0;
      
      XYSeries[] plots = new XYSeries[2];
      plots[0] = new XYSeries(1);
      plots[1] = new XYSeries(2);
      boolean[] showShapes = new boolean[2];
      showShapes[0] = true; showShapes[1] = true;
      
      for (int i = 0; i <= 50; i+=2) {
         double angle = Math.toRadians(i);
         Complex rTE = SaimCalc.fresnel(waveLength,angle, dOx, nSample);
         // calculate for 16 nm
         double phaseDiff = SaimCalc.PhaseDiff(waveLength, angle, nSample, 16.0);
         Complex tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
         Complex fieldStrength = rTE.multiply(tmp);
         fieldStrength = fieldStrength.add(1);
         System.out.println(" FieldStrength at angle: " + i + " is: " + 
                 fieldStrength.toString());
         plots[0].add(i, fieldStrength.abs() * fieldStrength.abs());
         
         // calculate for 28nm
         phaseDiff = SaimCalc.PhaseDiff(waveLength, angle, nSample, 28.0);
         tmp = new Complex(Math.cos(phaseDiff), Math.sin(phaseDiff));
         fieldStrength = rTE.multiply(tmp);
         fieldStrength = fieldStrength.add(1);
         plots[1].add(i,  fieldStrength.abs() * fieldStrength.abs());
         
         
      }
      Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         PlotUtils pu = new PlotUtils(prefs);
         pu.plotDataN("Fig 1b", plots, "Angle of incidence(degree)", "Intensity", showShapes, ""); 
       
    }
}
