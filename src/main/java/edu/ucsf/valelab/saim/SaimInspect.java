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
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog(
              "Saim Inspect " + Version.VERSION);

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
      gd.addCheckbox("Use B * angle", sd_.useBAngle_);
      gd.addMessage("Guess:");
      gd.addNumericField("A", sd_.A_, 0);
      gd.addNumericField("B", sd_.B_, 0);
      gd.addStringField("Height (nm)", SaimData.toString(sd_.heights_), 15);
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
         sd_.mirrorAround0_ = gd.getNextBoolean();
         sd_.zeroDoubled_ = gd.getNextBoolean();
         sd_.useBAngle_ = gd.getNextBoolean();
         sd_.A_ = gd.getNextNumber();
         sd_.B_ = gd.getNextNumber();
         try {
            sd_.heights_ = SaimData.fromString(gd.getNextString());
         } catch (NumberFormatException nfe) {
            ij.IJ.error("Heights should look like: \"10.0, 230.5\"");
            return false;
         }

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
                    sd_.wavelength_, sd_.dOx_, sd_.nSample_, sd_.useBAngle_);
            
            final int nrTries = sd_.heights_.length;
            Double[] rsquareds = new Double[nrTries];
            IntensityData[] predictedDatas = new IntensityData[nrTries];
            double[][] results = new double[nrTries][];        
            XYSeries[] plots = new XYSeries[1 + nrTries];
            boolean[] showShapes = new boolean[1 + nrTries]; // leave false
            ij.IJ.log("New SAIM Inspect run");
            DecimalFormat df = new DecimalFormat("#.##");
            
            for (int i = 0; i < nrTries; i++) {
               double[] guess = new double[]{sd_.A_, sd_.B_, sd_.heights_[i]};
               sff.setGuess(guess);
               results[i] = sff.fit(observedData.getWeightedObservedPoints());

               // use the fitted data to calculate the predicted values
               SaimFunction saimFunction = new SaimFunction(sd_.wavelength_,
                       sd_.dOx_, sd_.nSample_, sd_.useBAngle_);
               predictedDatas[i] = new IntensityData();
               SaimUtils.predictValues(observedData, predictedDatas[i], 
                       results[i], saimFunction);
               rsquareds[i] = SaimUtils.getRSquared(observedData, predictedDatas[i]);
               ij.IJ.log("Result for height " + sd_.heights_[i] + 
                       "nm, A: " + df.format(results[i][0]) + 
                       ", B: " + df.format(results[i][1]) + 
                       ", h: " + df.format(results[i][2]) + 
                       ", r2: " + df.format(rsquareds[i]) );
            }
            int bestIndex = SaimUtils.getIndexOfMaxValue(rsquareds);
            
            
            // plot fits for all heights attempted
            plots[0] = observedData.getXYSeries("Observations");
            for (int i = 0; i < nrTries; i++) {
                plots[i+1] = predictedDatas[i].getXYSeries("Fit - height " + sd_.heights_[i]);
            }
           

            Preferences prefs = Preferences.userNodeForPackage(this.getClass());
            PlotUtils pu = new PlotUtils(prefs);
            if (plotFrame_ != null) {
               plotFrame_.dispose();
            }

            plotFrame_ = pu.plotDataN("Saim at " + sd_.wavelength_ + " nm, n " + sd_.nSample_
                    + ", dOx: " + sd_.dOx_ + " nm", plots,
                    "Angle of incidence (degrees)",
                    "Normalized Intensity", showShapes,
                    "A: " + fmt(results[bestIndex][0], 0) + ", B:" + 
                            fmt(results[bestIndex][1], 0)
                    + ", h: " + fmt(results[bestIndex][2], 1) + ", r2: " + 
                            fmt(rsquareds[bestIndex], 2));
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
