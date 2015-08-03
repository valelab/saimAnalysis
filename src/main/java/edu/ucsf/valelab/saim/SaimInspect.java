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

import edu.ucsf.valelab.saim.calculations.SaimCalc;
import edu.ucsf.valelab.saim.calculations.SaimFunctionFitter;
import edu.ucsf.valelab.saim.data.SaimData;
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
import java.util.ArrayList;
import java.util.prefs.Preferences;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.jfree.data.xy.XYSeries;

/**
 * Plugin that shows the average intensity profile of a selection on a stack,
 * as well as the result of the Saim fit on those data
 * 
 * @author nico
 */
public class SaimInspect implements PlugIn, DialogListener {
   private final SaimData sd_ = new SaimData();
   private final String[] fitters_ = {"Curve Fitter", "Bounded Curve Fitter"};
   private final String fitter_ = "Curve Fitter";
   
   private Frame plotFrame_;
   
   @Override
   public void run(String arg) {
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Saim Inspect" );
      
     // gd.addMessage("Select an ROI in your SAIM stack, and click \"Show\".\n" + 
     //       "This plugin plots the intensity values and the best fit");  
     //gd.setInsets(15,0,3);
      
      gd.addNumericField("Wavelenght (nm)", sd_.wavelength_, 1);
      gd.addNumericField("Sample Refractive Index", sd_.nSample_, 2);
      gd.addNumericField("Thickness of oxide layer (nm)", sd_.dOx_, 1);
      gd.setInsets(15,0,3);
      gd.addMessage("Angles:");
      gd.addNumericField("First angle", sd_.firstAngle_, 0);
      gd.addNumericField("Step size", sd_.angleStep_, 0);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Guess:");
      gd.addNumericField("A", sd_.A_, 0);
      gd.addNumericField("B", sd_.B_, 0);
      gd.addNumericField("Height (nm)", sd_.h_, 0);
      gd.setInsets(15, 0, 3);
      gd.addChoice("Fitter", fitters_, fitter_);
      
      gd.addPreviewCheckbox(null, "Inspect");

      gd.hideCancelButton();
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
         sd_.firstAngle_ = (int) gd.getNextNumber();
         sd_.angleStep_ = (int) gd.getNextNumber();
         sd_.A_ = gd.getNextNumber();
         sd_.B_ = gd.getNextNumber();
         sd_.h_ = gd.getNextNumber();
         
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
         boolean[] showShapes = new boolean[2];
         plots[0] = new XYSeries("Observations", false, false);
         plots[1] = new XYSeries("Fit", false, false);
         showShapes[0] = false;

         ArrayList<WeightedObservedPoint> points = 
              new ArrayList<WeightedObservedPoint>();

         for (int i = 0; i < values.length; i++) {
            double angle = sd_.firstAngle_ + i * sd_.angleStep_;
            plots[0].add(sd_.firstAngle_ + i * sd_.angleStep_, values[i]);
            WeightedObservedPoint point = new WeightedObservedPoint(
                    1.0, Math.toRadians(angle), values[i]);
            points.add(point);
         }
         
         // create the fitter
         boolean bounded = fitter_.equals("Bounded Curve Fitter");
         SaimFunctionFitter sff = new SaimFunctionFitter(
                 sd_.wavelength_, sd_.dOx_, sd_.nSample_, bounded);
         double[] guess = new double[] {sd_.A_, sd_.B_, sd_.h_};
         sff.setGuess(guess);
         final double[] result = sff.fit(points);
         ij.IJ.log("A: " + result[0] + ", B: " + result[1] + ", h: " + result[2]);
         for (int i = 0; i < values.length; i++) {
            double angle = sd_.firstAngle_ + i * sd_.angleStep_;
            double I = result[0] * SaimCalc.fieldStrength(sd_.wavelength_, 
                    Math.toRadians(angle), sd_.nSample_, sd_.dOx_, result[2]) + result[1];
            plots[1].add(sd_.firstAngle_ + i * sd_.angleStep_, I);
         }
         
         Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         PlotUtils pu = new PlotUtils(prefs);
         if (plotFrame_ != null)
            plotFrame_.dispose();
         plotFrame_ = pu.plotDataN("Saim at " + sd_.wavelength_ + " nm, n " + sd_.nSample_ + 
              ", dOx: " + sd_.dOx_ + " nm", plots, 
              "Angle of incidence (degrees)", 
              "Normalized Intensity", showShapes, 
              "A: " + fmt(result[0]) + ", B:" + fmt(result[1]) + 
                      ", h: " + fmt(result[2])); 
      }
      
      return true;
   }
   
   public static String fmt(double d) {
      DecimalFormat df = new DecimalFormat("#.#");
      String s = df.format(d);
      return s;
   }
   
}
