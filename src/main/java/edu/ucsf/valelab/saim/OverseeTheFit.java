 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          OverseeTheFit.java
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
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread to spin up the threads that carry out the SAIM fitting
 * Detects if an image is open, cuts the image in pieces that each will be
 * handled by an individual thread.
 * Waits for the fitting threads to finish and displays the result to the user.
 * @author nico
 */
public class OverseeTheFit extends Thread {

   private final AtomicInteger nrXProcessed_ = new AtomicInteger(0);
   private final SaimData sd_;
   private final AtomicBoolean isRunning_;
   private final RunTheFit[] fitThreads_;
   private final int nrThreads_;
   private final GenericDialog gd_;

   /**
    * 
    * @param gd ImageJ GenericDialog, used to switch off the "Fit" checkbox
    * at the end of a run
    * @param sd Data structure with user-provided settings
    * @param isRunning Flag to signal to other threads whether or not to fit is 
    * currently running
    */
   public OverseeTheFit(GenericDialog gd, SaimData sd, AtomicBoolean isRunning) {
      gd_ = gd;
      sd_ = sd;
      isRunning_ = isRunning;
      nrThreads_ = ij.Prefs.getThreads();
      fitThreads_ = new RunTheFit[nrThreads_];
   }

   @Override
   public void run() {

      final	ImagePlus ip = WindowManager.getCurrentImage();
		if (ip==null) {
			IJ.noImage();
         isRunning_.set(false);
         return;
      }
      
      if (!(ip.getProcessor() instanceof ShortProcessor)) {
         ij.IJ.showMessage("Can only do Saim Fit on 16 bit images");
         isRunning_.set(false);
         return;
      }      
      
      ij.IJ.showStatus("Saim Fit is running...");
      final long startTime = System.nanoTime();
      nrXProcessed_.set(0);

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

      // start all threads
      int nrXPerThread = (width / nrThreads_);
      for (int i = 0; i < nrThreads_; i++) {
         RunTheFit rf = new RunTheFit(0 + (i * nrXPerThread), nrXPerThread,
                 sd_.copy(), ip, outputFP, nrXProcessed_);
         fitThreads_[i] = rf;
         fitThreads_[i].start();
      }

      // wait for the threads to end 
      try {
         for (int i = 0; i < nrThreads_; i++) {
            fitThreads_[i].join();
         }
         for (int i = 0; i < 4; i++) {
            newStack.setProcessor(outputFP[i], i + 1);
         }

         ImagePlus rIp = new ImagePlus("Fit result", newStack);
         rIp.show();
         ij.IJ.showProgress(1);
         ij.IJ.showStatus("");
         isRunning_.set(false);
         gd_.getPreviewCheckbox().setState(false);
         ij.IJ.log("Analysis took "
                 + (System.nanoTime() - startTime) / 1000000 + "ms");
      } catch (InterruptedException ex) {
         ij.IJ.log("fitThread was interupted");
      }
   }
   
   /**
    * Method to interrupt a running analysis
    */
   public void stopRun() {
      
      // first set a stop flag in all running threads
      for (int i=0; i < nrThreads_; i++) {
         if (fitThreads_[i] != null && fitThreads_[i].isAlive()) {
            fitThreads_[i].stopRun();
         }
      }
      
      // now wait for them to exit
      for (int i=0; i < nrThreads_; i++) {
         if (fitThreads_[i] != null && fitThreads_[i].isAlive()) {
            try {
               fitThreads_[i].join(50l);
            } catch (InterruptedException iex) {
               ij.IJ.log("Thread " + i + " was interrupted");
            }
         }
      }
      
      // If the thread is still alive, we have to get mean
      for (int i=0; i < nrThreads_; i++) {
         if (fitThreads_[i] != null && fitThreads_[i].isAlive()) {
            fitThreads_[i].interrupt();
         }
      }   
      
      // signal that the coast is clear
      gd_.getPreviewCheckbox().setState(false);
      isRunning_.set(false);
   }
   
}