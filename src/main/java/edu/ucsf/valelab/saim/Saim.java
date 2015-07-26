package edu.ucsf.valelab.saim;

import edu.ucsf.valelab.saim.calculations.SaimCalc;
import edu.ucsf.valelab.saim.plot.PlotUtils;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.util.prefs.Preferences;
import org.jfree.data.xy.XYSeries;

/**
 * 
 *
 * @author nico
 */
public class Saim implements PlugIn, DialogListener
{
   Frame plotFrame_;      
      static double wavelength = 488.0;
      static double nSample = 1.36;
      static double dOx = 500.0;
      static int firstAngle = -50;
      static int lastAngle = 50;
      String heightString = "16, 32, 48";
   
   public static void main( String[] args )
   { 
      double[] heights = {16.0, 24.0};
      new Saim().plotFig(wavelength, nSample, dOx, firstAngle, lastAngle, 
              heights);
   }
   
   public Saim() {
   }
   
   public void plotFig(double wavelength, double nSample, double dOx,
           int firstAngle, int lastAngle, double[] heights) {
      
      int n = 2;
      double[] height =  { 16.0, 28.0, 40.0, 56.0, 72.0, 88.0 };
      boolean[] showShapes = new boolean[n];
      XYSeries[] plots = new XYSeries[n];
      for (int i = 0; i < n; i++) {
         plots[i] = new XYSeries("" + height[i] + "nm", false, false);
         showShapes[i] = false;
      }
      
      for (int i = firstAngle; i <= lastAngle; i+=1) {
         double angle = Math.toRadians(i);
         // calculate for 16 nm
         for (int j = 0; j < n; j++) {
            double fieldStrength = SaimCalc.fieldStrength(
                    wavelength, angle, nSample, dOx, height[j]);
            plots[j].add(i, fieldStrength);
         }
      }
      
      for (int i = 0; i < plots.length; i++) {
         plots[i] = PlotUtils.normalize(plots[i]);
      }
      
      Preferences prefs = Preferences.userNodeForPackage(this.getClass());
      PlotUtils pu = new PlotUtils(prefs);
      if (plotFrame_ != null)
         plotFrame_.dispose();
      plotFrame_ = pu.plotDataN("Saim at " + wavelength + " nm, n " + nSample + 
              ", dOx: " + dOx + " nm", plots, 
              "Angle of incidence (degrees)", 
              "Normalized Intensity", showShapes, ""); 
       
    }

   @Override
   public void run(String arg) {

      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Saim Plot" );
      
      
      gd.addNumericField("Wavelenght (nm)", wavelength, 1);
      gd.addNumericField("Sample Refractive Index", nSample, 2);
      gd.addNumericField("Thickness of oxide layer (nm)", dOx, 1);
      gd.setInsets(15,0,3);
      gd.addMessage("Angles to be plotted:");
      gd.addNumericField("First angle", firstAngle, 0);
      gd.addNumericField("Last angle", lastAngle, 0);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Heights as comma separated values:");
      gd.addStringField("Height in nm", heightString);
      gd.addPreviewCheckbox(null, "Plot");

      gd.hideCancelButton();
      gd.setOKLabel("Close");
      
      gd.addDialogListener(this);
      
          
      gd.showDialog();
      
   }

   @Override
   public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
      if (gd.isPreviewActive()) {
         wavelength = gd.getNextNumber();
         nSample = gd.getNextNumber();
         dOx = gd.getNextNumber();
         firstAngle = (int) gd.getNextNumber();
         lastAngle = (int) gd.getNextNumber();
         heightString = gd.getNextString();
         String[] tokens = heightString.split("[,]");
         double[] heights = new double[tokens.length];
         for (int i = 0; i < tokens.length; i++) {
            heights[i] = Double.parseDouble(tokens[i]);
         }
         
         plotFig(wavelength, nSample, dOx, firstAngle, lastAngle, heights);
      }
      
      return true;
   }
}
