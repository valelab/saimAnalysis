 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          RunTheFit.java
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
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.exception.TooManyIterationsException;

/**
 * This class does the actual work of fitting each pixel in the data set
 * to the SAIM equation.  Only pixels with intensity higher than a user-given
 * threshold will be fitted.  Pixels that were not fit will be set to 0, pixels
 * that failed to fit will be set to NaN (not a number)
 * 
 * The run method creates an image Stack with 4 images that will be populated 
 * as follows:
 * 1: Height in nm
 * 2: R-squared (estimate of the error between fit and experimental data)
 * 3: A
 * 4: B
 * For A and B, see the Paszek et al. 2012 paper or the equation elsewhere 
 * in this code
 * 
 * @author nico
 */
public class RunTheFit extends Thread {

   private final int startX_;   // image x coordinate at which we will start analysis
   private final int numberX_;  // Number of x coordinates that we will analyze
   private final SaimData sd_;  // Analysis settings data structure
   private final ImagePlus ip_; // ImageJ Image data
   private final FloatProcessor[] fpOut_; // Results as an ImageJ Stack of type Float
   private final AtomicInteger nrXProcessed_;
   private final AtomicBoolean stop_ = new AtomicBoolean(false);

   /**
    * 
    * @param startX image x coordinate at which analysis starts
    * @param numberX Number of x coordinates that  will be analyzes
    * @param sd Analysis settings data structure
    * @param ip ImageJ Image data
    * @param fpOut Results as an ImageJ Stack of type Float
    * @param nrXProcessed Number of X coordinates that were analyzed
    */
   public RunTheFit(int startX, int numberX, SaimData sd, ImagePlus ip,
           FloatProcessor[] fpOut, AtomicInteger nrXProcessed) {
      startX_ = startX;
      numberX_ = numberX;
      sd_ = sd;
      ip_ = ip;
      fpOut_ = fpOut;
      nrXProcessed_ = nrXProcessed;
   }

   @Override
   public void run() {
      // prepopulate arrays with angles in radians and in degrees
      final double[] anglesRadians = new double[ip_.getNSlices()];
      final double[] anglesDegrees = new double[ip_.getNSlices()];
      for (int i = 0; i < anglesRadians.length; i++) {
         double angle = sd_.firstAngle_ + i * sd_.angleStep_;
         anglesDegrees[i] = angle;
         anglesRadians[i] = Math.toRadians(angle);
      }
      final ImageStack is = ip_.getImageStack();
      final int width = ip_.getWidth();
      final int height = ip_.getHeight();

      // create the fitter
      final SaimFunctionFitter sff = new SaimFunctionFitter(
              sd_.wavelength_, sd_.dOx_, sd_.nSample_, sd_.useBAngle_);
      final SaimFunction sf = new SaimFunction(sd_);
      double[] guess = new double[]{sd_.A_, sd_.B_, sd_.h_};
      sff.setGuess(guess);

      // now cycle through the x/y pixels and fit each of them
      IntensityData observed = new IntensityData();
      IntensityData calculated = new IntensityData();
      int lastX = startX_ + numberX_;
      try {
         for (int x = startX_; x < lastX; x++) {
            for (int y = 0; y < height; y++) {
               if (stop_.get()) {
                  return;
               }
               // only calculate if the pixels intensity is
               // above the threshold
               // TODO: calculate average of the stack and use threshold on that
               if (ip_.getProcessor().get(x, y) > sd_.threshold_) {
                  observed.clear();
                  float[] values = new float[ip_.getNSlices()];
                  for (int i = 0; i < ip_.getNSlices(); i++) {
                     values[i] = is.getProcessor(i + 1).get(x, y);
                  }
                  SaimUtils.organize(observed, sd_, values, anglesDegrees,
                          anglesRadians);

                  try {
                     double[] result = sff.fit(
                             observed.getWeightedObservedPoints());
                     fpOut_[2].setf(x, y, (float) result[0]);
                     fpOut_[3].setf(x, y, (float) result[1]);
                     fpOut_[0].setf(x, y, (float) result[2]);
                     calculated.clear();
                     SaimUtils.predictValues(observed, calculated, result, sf);
                     try {
                        double r2 = SaimUtils.getRSquared(observed, calculated);
                        fpOut_[1].setf(x, y, (float) r2);
                     } catch (InvalidInputException ex) {
                        ij.IJ.log("Observed and Calculated datasets differe in size");
                     }
                  } catch (TooManyIterationsException tiex) {
                     for (int i = 0; i < 4; i++) 
                        fpOut_[i].setf(x, y, Float.NaN);
                     ij.IJ.log("Failed to fit pixel " + x + ", " + y);
                  }
               }
            }
            nrXProcessed_.getAndIncrement();
            synchronized (nrXProcessed_) {
               ij.IJ.showProgress(nrXProcessed_.get(), width);
            }
         }
      } catch (InvalidInputException ex) {
         ij.IJ.error("Saim Fit", ex.getMessage());
      }
   }
   
   /**
    * Method to set a stop flag
    */
   public void stopRun() {
      stop_.set(true);
   }
   
}