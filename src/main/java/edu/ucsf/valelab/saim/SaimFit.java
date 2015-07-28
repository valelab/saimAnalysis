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
import edu.ucsf.valelab.saim.data.SaimData;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.apache.commons.math3.fitting.WeightedObservedPoint;

/**
 * Plugin that fits all pixels of a stack using the Saim equation
 * Three images are produced, representing A, B, and h (height)
 * 
 * @author nico
 */
public class SaimFit implements PlugIn, DialogListener {
   private final SaimData sd_ = new SaimData();
   private int threshold_ = 5000;
   private final AtomicBoolean isRunning_ = new AtomicBoolean(false);
   private final AtomicInteger nrXProcessed_ = new AtomicInteger(0);
   
   Frame plotFrame_;
   
   @Override
   public void run(String arg) {
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Saim Fit" );
      
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
      gd.addMessage("Only fit pixels higher then");
      gd.addNumericField("Threshold", threshold_, 0);
      
      gd.addPreviewCheckbox(null, "Show");

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
         threshold_ = (int) gd.getNextNumber();
         final int nrThreads = ij.Prefs.getThreads();
         
         final ImagePlus ip = ij.IJ.getImage();
         if (! (ip.getProcessor() instanceof ShortProcessor)) {
            ij.IJ.showMessage("Can only do Saim Fit on 16 bit images");
            return true;
         }
         
         if (isRunning_.get()) {
            ij.IJ.showMessage("Saim Fit is already running");
            return true;
         }
         
         isRunning_.set(true);
         final long startTime = System.nanoTime();
         
         // this assumes a stack of shorts with NSlices > 1 and all other dimensions 1
         // TODO: check!
        
         final ImageStack is = ip.getImageStack();
         final int width = ip.getWidth();
         final int height = ip.getHeight();
         final int stackSize = is.getSize();
         final ImageStack newStack = new ImageStack(width, height, 3);
         final FloatProcessor ipA = new FloatProcessor(width, height);
         final FloatProcessor ipB = new FloatProcessor(width, height);
         final FloatProcessor iph = new FloatProcessor(width, height);
         
         // prepopulate an array with angles in radians
         final double[] angles = new double[stackSize];
         for (int i = 0; i < angles.length; i++) {
            double angle = sd_.firstAngle_ + i * sd_.angleStep_;
            angles[i] = Math.toRadians(angle);
         }

         
         class UserFeedback implements Runnable {
            final AtomicBoolean stop_ = new AtomicBoolean(false);
           
            @Override
            public void run() {
               while (!stop_.get()) {
                  ij.IJ.showProgress(nrXProcessed_.get(), width);
                  try {
                     synchronized(nrXProcessed_) {
                        nrXProcessed_.wait();
                     }
                  } catch (InterruptedException ex) {
                     ij.IJ.log("userFeedback thread was interupted");
                  }
               }
            }

            public void stop() {
               stop_.set(true);
               synchronized(nrXProcessed_) {
                  nrXProcessed_.notify();
               }
            }
         }
         
         class RunFit implements Runnable {
            private final int startX_;
            private final int numberX_;

            public RunFit(int startX, int numberX) {
               startX_ = startX;
               numberX_ = numberX;
            }

            @Override
            public void run() {

               // create the fitter
               final SaimFunctionFitter sff = new SaimFunctionFitter(
                       sd_.wavelength_, sd_.dOx_, sd_.nSample_);
               double[] guess = new double[]{sd_.A_, sd_.B_, sd_.h_};
               sff.setGuess(guess);

               
               // now cycle through the x/y pixels and fit each of them
               ArrayList<WeightedObservedPoint> points
                       = new ArrayList<WeightedObservedPoint>();
               int lastX = startX_ + numberX_;
               for (int x = startX_; x < lastX; x++) {
                  //ij.IJ.showProgress((double) x / (double) width);
                  for (int y = 0; y < height; y++) {
                     // only calculate if the pixels intensity is
                     // above the threshold
                     // TODO: calculate average of the stack and use threshold on that
                     if (ip.getProcessor().get(x, y) > threshold_) {
                        points.clear();
                        for (int i = 1; i < ip.getNSlices(); i++) {
                           WeightedObservedPoint point = new WeightedObservedPoint(
                                   1.0, angles[i], is.getProcessor(i).get(x, y));
                           points.add(point);
                        }
                        try {
                           double[] result = sff.fit(points);
                           ipA.setf(x, y, (float) result[0]);
                           ipB.setf(x, y, (float) result[1]);
                           iph.setf(x, y, (float) result[2]);
                        } catch (TooManyIterationsException tiex) {
                           ij.IJ.log("Failed to fit pixel " + x + ", " + y);
                        }
                     }
                  }
                  nrXProcessed_.getAndIncrement();
                  synchronized(nrXProcessed_) {
                     nrXProcessed_.notify();
                  }
               }
            }
         }


         
         class DoWork implements Runnable {

            @Override
            public void run() {
               nrXProcessed_.set(0);
               ij.IJ.showStatus("Saim Fit is running...");
               Thread[] fitThreads = new Thread[nrThreads];
               
               // start all threads
               int nrXPerThread = (width/nrThreads);
               for (int i = 0; i < nrThreads; i++) {
                  RunFit rf = new RunFit(0 + (i * nrXPerThread), nrXPerThread);
                  fitThreads[i] = new Thread(rf);
                  fitThreads[i].start();
               }
               UserFeedback uf = new UserFeedback();
               Thread ufThread = new Thread(uf);
               ufThread.start();
               
               
               // wait for the threads to end 
               // TODO: have a way to kill the threads
               // TODO: have a timeout
               for (int i = 0; i < nrThreads; i++) {
                  try {
                     fitThreads[i].join();
                  } catch (InterruptedException ex) {
                     ij.IJ.log("fitThread was interupted");
                  }
               }
               uf.stop();
               try {
                  ufThread.join();
               } catch (InterruptedException ex) {
                  ij.IJ.log("fitThread was interupted");
               }

               newStack.setProcessor(ipA, 1);
               newStack.setProcessor(ipB, 2);
               newStack.setProcessor(iph, 3);

               ImagePlus rIp = new ImagePlus("Fit result", newStack);
               rIp.show();
               ij.IJ.showProgress(1);
               ij.IJ.showStatus("");
               isRunning_.set(false);
               ij.IJ.log("Analysis took " + 
                       (System.nanoTime() - startTime) / 1000000 + "ms");
            }
         }
         // TODO: make this multi-threaded (just slice up x)
         (new Thread(new DoWork())).start();

      }

      return true;
   }
   
   public static String fmt(double d) {
      DecimalFormat df = new DecimalFormat("#.#");
      String s = df.format(d);
      return s;
   }
}
