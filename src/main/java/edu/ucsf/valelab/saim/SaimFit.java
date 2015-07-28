///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimFit.java
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

import edu.ucsf.valelab.saim.calculations.SaimFunctionFitter;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 * Plugin that fits all pixels of a stack using the Saim equation
 * Three images are produced, representing A, B, and h (height)
 * 
 * @author nico
 */
public class SaimFit implements PlugIn, DialogListener {
   double wavelength_ = 488.0;
   double nSample_ = 1.36;
   double dOx_ = 500.0;
   double firstAngle_ = -50;
   double angleStep_ = 1;
   double A_ = 1000.0;
   double B_ = 5000.0;
   double h_ = 100.0;
   
   Frame plotFrame_;
   
   @Override
   public void run(String arg) {
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Saim Fit" );
      
      gd.addNumericField("Wavelenght (nm)", wavelength_, 1);
      gd.addNumericField("Sample Refractive Index", nSample_, 2);
      gd.addNumericField("Thickness of oxide layer (nm)", dOx_, 1);
      gd.setInsets(15,0,3);
      gd.addMessage("Angles:");
      gd.addNumericField("First angle", firstAngle_, 0);
      gd.addNumericField("Step size", angleStep_, 0);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Guess:");
      gd.addNumericField("A", A_, 0);
      gd.addNumericField("B", B_, 0);
      gd.addNumericField("Height (nm)", h_, 0);
      
      gd.addPreviewCheckbox(null, "Show");

      gd.hideCancelButton();
      gd.setOKLabel("Close");
      
      gd.addDialogListener(this);
      
          
      gd.showDialog();
   }

   @Override
   public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
      if (gd.isPreviewActive()) {
         wavelength_ = gd.getNextNumber();
         nSample_ = gd.getNextNumber();
         dOx_ = gd.getNextNumber();
         firstAngle_ = (int) gd.getNextNumber();
         angleStep_ = (int) gd.getNextNumber();
         A_ = gd.getNextNumber();
         B_ = gd.getNextNumber();
         h_ = gd.getNextNumber();
         
         // this assumes a stack of shorts with NSlices > 1 and all other dimensions 1
         // TODO: check!
         final ImagePlus ip = ij.IJ.getImage();
         final int width = ip.getWidth();
         final int height = ip.getHeight();
         final int stackSize = ip.getNSlices();
         final ImageStack is = new ImageStack(width, height, 3);
         final FloatProcessor ipA = new FloatProcessor(width, height);
         final FloatProcessor ipB = new FloatProcessor(width, height);
         final FloatProcessor iph = new FloatProcessor(width, height);
         
         // prepopulate an array with angles in radians
         final double[] angles = new double[stackSize];
         for (int i = 0; i < angles.length; i++) {
            double angle = firstAngle_ + i * angleStep_;
            angles[i] = Math.toRadians(angle);
         }
         
         // create the fitter
         final SaimFunctionFitter sff = new SaimFunctionFitter(wavelength_, dOx_, nSample_);
         double[] guess = new double[] {A_, B_, h_};
         sff.setGuess(guess);

         class doWork implements Runnable {

            @Override
            public void run() {
               // now cycle through the x/y pixels and fit each of them
               ArrayList<WeightedObservedPoint> points
                       = new ArrayList<WeightedObservedPoint>();
               for (int x = 0; x < width; x++) {
                  ij.IJ.showProgress((double) x / (double) width);
                  for (int y = 0; y < height; y++) {
                     points.clear();
                     for (int i = 1; i < ip.getNSlices(); i++) {
                        ip.setPosition(i);
                        WeightedObservedPoint point = new WeightedObservedPoint(
                                1.0, angles[i], ip.getProcessor().get(x, y));
                        points.add(point);
                     }
                     double[] result = sff.fit(points);
                     ipA.setf(x, y, (float) result[0]);
                     ipB.setf(x, y, (float) result[1]);
                     iph.setf(x, y, (float) result[2]);
                  }
               }

               is.setProcessor(ipA, 1);
               is.setProcessor(ipB, 2);
               is.setProcessor(iph, 3);

               ImagePlus rIp = new ImagePlus("Fit result", is);
               rIp.show();
            }
         }
         (new Thread(new doWork())).start();

      }

      return true;
   }
   
   public static String fmt(double d) {
      DecimalFormat df = new DecimalFormat("#.#");
      String s = df.format(d);
      return s;
   }
}
