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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Plugin that fits all pixels of a stack using the Saim equation
 * Three images are produced, representing A, B, and h (height)
 * 
 * @author nico
 */
public class SaimFit implements PlugIn, DialogListener {
   private final SaimData sd_ = new SaimData();
   private final AtomicBoolean isRunning_ = new AtomicBoolean(false);
   
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
      gd.addCheckbox("Mirror around 0", sd_.mirrorAround0_);
      gd.addCheckbox("0 angle is doubled", sd_.zeroDoubled_);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Guess:");
      gd.addNumericField("A", sd_.A_, 0);
      gd.addNumericField("B", sd_.B_, 0);
      gd.addNumericField("Height (nm)", sd_.h_, 0);
      gd.setInsets(15, 0, 3);
      gd.addMessage("Only fit pixels higher then");
      gd.addNumericField("Threshold", sd_.threshold_, 0);
      gd.setInsets(15, 0, 3);
      
      gd.addPreviewCheckbox(null, "Fit");

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
         sd_.mirrorAround0_ = gd.getNextBoolean();
         sd_.zeroDoubled_ = gd.getNextBoolean();
         sd_.A_ = gd.getNextNumber();
         sd_.B_ = gd.getNextNumber();
         sd_.h_ = gd.getNextNumber();
         
         sd_.threshold_ = (int) gd.getNextNumber();
         
         
         
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
         
         final int width = ip.getWidth();
         final int height = ip.getHeight();
         final ImageStack newStack = new ImageStack(width, height, 4);
         final FloatProcessor[] outputFP = new FloatProcessor[4];
         for (int i = 0; i < 4; i++) {
            final FloatProcessor fp = new FloatProcessor(width, height);
            outputFP[i] = fp;
         }






         
         class DoWork implements Runnable {
            private final AtomicInteger nrXProcessed_ = new AtomicInteger(0);
            
            @Override
            public void run() {
               nrXProcessed_.set(0);
               ij.IJ.showStatus("Saim Fit is running...");
               final int nrThreads = ij.Prefs.getThreads();
               Thread[] fitThreads = new Thread[nrThreads];
               
               // start all threads
               int nrXPerThread = (width/nrThreads);
               for (int i = 0; i < nrThreads; i++) {
                  RunTheFit rf = new RunTheFit(0 + (i * nrXPerThread), nrXPerThread, 
                          sd_.copy(), ip, outputFP, nrXProcessed_);
                  fitThreads[i] = new Thread(rf);
                  fitThreads[i].start();
               } 
               
               // wait for the threads to end 
               // TODO: have a way to kill the threads
               // TODO: have a timeout
               try {
                  for (int i = 0; i < nrThreads; i++) {
                     fitThreads[i].join();
                  }
                  for (int i = 0; i < 4; i++) {
                     newStack.setProcessor(outputFP[i], i + 1);
                  }

                  ImagePlus rIp = new ImagePlus("Fit result", newStack);
                  rIp.show();
                  ij.IJ.showProgress(1);
                  ij.IJ.showStatus("");
                  isRunning_.set(false);
                  ij.IJ.log("Analysis took "
                          + (System.nanoTime() - startTime) / 1000000 + "ms");
               } catch (InterruptedException ex) {
                  ij.IJ.log("fitThread was interupted");
               }
            }
         }
         
         DoWork workThread = new DoWork();

         (new Thread(workThread)).start();

      }

      return true;
   }
   
   public static String fmt(double d) {
      DecimalFormat df = new DecimalFormat("#.#");
      String s = df.format(d);
      return s;
   }
   
}