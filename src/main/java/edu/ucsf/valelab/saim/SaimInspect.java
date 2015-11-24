///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimInspect.java
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

package edu.ucsf.valelab.saim;

import edu.ucsf.valelab.saim.calculations.SaimFunction;
import edu.ucsf.valelab.saim.calculations.SaimFunctionFitter;
import edu.ucsf.valelab.saim.calculations.SaimUtils;
import edu.ucsf.valelab.saim.data.IntensityData;
import edu.ucsf.valelab.saim.data.SaimData;
import edu.ucsf.valelab.saim.exceptions.InvalidInputException;
import edu.ucsf.valelab.saim.plot.PlotUtils;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;
import org.jfree.data.xy.XYSeries;

/**
 * Plugin that shows the average intensity profile of a selection on a stack, as
 * well as the result of the Saim fit on those data
 *
 * @author nico
 */
public class SaimInspect implements PlugIn, DialogListener {

   private final SaimData sd_ = new SaimData();

   private Frame plotFrame_;


   @Override
   public void run(String arg) {
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog("Saim Inspect");

      gd.addNumericField("Wavelenght (nm)", sd_.wavelength_, 1);
      gd.addNumericField("Sample Refr. Index", sd_.nSample_, 2);
      gd.addNumericField("Oxide layer (nm)", sd_.dOx_, 1);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Angles:");
      gd.addNumericField("First angle", sd_.firstAngle_, 0);
      gd.addNumericField("Step size", sd_.angleStep_, 0);
      gd.addCheckbox("Mirror around 0", sd_.mirrorAround0_);
      gd.addCheckbox("0 angle is doubled", sd_.zeroDoubled_);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Guess:");
      gd.addNumericField("A", sd_.A_, 0);
      gd.addNumericField("B", sd_.B_, 0);
      gd.addNumericField("Height (nm)", sd_.h_, 0);
      gd.setInsets(15, 0, 3);

      gd.addPreviewCheckbox(null, "Inspect");

      gd.hideCancelButton();
      gd.addHelp("http://fiji.sc/Saim");
      gd.setOKLabel("Close");

      gd.addDialogListener(this);

      gd.showDialog();
   }

   @Override
   public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
      if (gd.isPreviewActive()) {
         sd_.wavelength_ = gd.getNextNumber();
         sd_.nSample_ = gd.getNextNumber();
         sd_.dOx_ = gd.getNextNumber();
         sd_.firstAngle_ = gd.getNextNumber();
         sd_.angleStep_ = gd.getNextNumber();
         sd_.A_ = gd.getNextNumber();
         sd_.B_ = gd.getNextNumber();
         sd_.h_ = gd.getNextNumber();
         sd_.mirrorAround0_ = gd.getNextBoolean();
         sd_.zeroDoubled_ = gd.getNextBoolean();

         ImagePlus ip = ij.IJ.getImage();
         Roi roi = ip.getRoi();
         if (roi == null) {
            ij.IJ.showMessage("Please draw an ROI on the image");
         }
         ResultsTable rt = new ResultsTable();
         Analyzer az = new Analyzer(ip, Analyzer.MEAN, rt);
         for (int i = 1; i <= ip.getNSlices(); i++) {
            ip.setPosition(i);
            az.measure();
         }
         float[] values = rt.getColumn(rt.getColumnIndex("Mean"));         
         
         
         XYSeries[] plots = new XYSeries[2];
         boolean[] showShapes = new boolean[2]; // leave false

         // collect the observedData and store in our own data structure
         IntensityData observedData = new IntensityData();

         final double anglesDegrees[] = new double[values.length];
         final double anglesRadians[] = new double[values.length];
         for (int i = 0; i < anglesRadians.length; i++) {
            double angle = sd_.firstAngle_ + i * sd_.angleStep_;
            anglesDegrees[i] = angle;
            anglesRadians[i] = Math.toRadians(angle);
         }

         try {
            // stores the data in observedData, taking mirroring and zerodoubling into account
            SaimUtils.organize(observedData, sd_, values, anglesDegrees, anglesRadians);
            
            // create the fitter
            SaimFunctionFitter sff = new SaimFunctionFitter(
                    sd_.wavelength_, sd_.dOx_, sd_.nSample_);
            double[] guess = new double[]{sd_.A_, sd_.B_, sd_.h_};
            sff.setGuess(guess);
            final double[] result = sff.fit(observedData.getWeightedObservedPoints());
            ij.IJ.log("A: " + result[0] + ", B: " + result[1] + ", h: " + result[2]);

            // use the fitted data to calculate the predicted values
            IntensityData predictedData = new IntensityData();
            SaimFunction saimFunction = new SaimFunction(sd_.wavelength_, 
                    sd_.dOx_, sd_.nSample_);
            SaimUtils.predictValues(observedData, predictedData, result, saimFunction);
            
            // plot
            plots[0] = observedData.getXYSeries("Observations");
            plots[1] = predictedData.getXYSeries("Fit");

            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            PlotUtils pu = new PlotUtils(prefs);
            if (plotFrame_ != null) {
               plotFrame_.dispose();
            }
            double rsq = SaimUtils.getRSquared(observedData, predictedData);

            plotFrame_ = pu.plotDataN("Saim at " + sd_.wavelength_ + " nm, n " + sd_.nSample_
                    + ", dOx: " + sd_.dOx_ + " nm", plots,
                    "Angle of incidence (degrees)",
                    "Normalized Intensity", showShapes,
                    "A: " + fmt(result[0], 0) + ", B:" + fmt(result[1], 0)
                    + ", h: " + fmt(result[2], 1) + ", r2: " + fmt(rsq, 2));
            gd.getPreviewCheckbox().setState(false);

         } catch (InvalidInputException ex) {
            ij.IJ.error("Saim Inspect", ex.getMessage());
         }
      }
      return true;
   }
   
   public static String fmt(double d, int digs) {
      String format = "#";
      if (digs > 0) {
         format += ".";
      }
      for (int i = 0; i < digs; i++) {
         format += "#";
      }
      DecimalFormat df = new DecimalFormat(format);
      String s = df.format(d);
      return s;
   }
   
   public static boolean sameSize (final ImagePlus imgOne, final ImagePlus imgTwo) {
      return !(
              imgOne.getHeight() != imgTwo.getHeight() ||
              imgOne.getWidth() != imgTwo.getWidth() ||
              imgOne.getBytesPerPixel() != imgTwo.getBytesPerPixel() );
   }

   public static float[] measure(final ImagePlus ip, final Roi roi) {
      ip.setRoi(roi);
      ResultsTable rt = new ResultsTable();
      Analyzer az = new Analyzer(ip, Analyzer.MEAN, rt);
      for (int i = 1; i <= ip.getNSlices(); i++) {
         ip.setPosition(i);
         az.measure();
      }
      return rt.getColumn(rt.getColumnIndex("Mean"));
   }
}
