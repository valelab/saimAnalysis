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
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.plugin.PlugIn;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Plugin that fits all pixels of a stack using the Saim equation
 * Output is a stack with 4 images are produced, representing height, R-squared, 
 * A, B 
 * 
 * @author nico
 */
public class SaimFit implements PlugIn, DialogListener {
   private final SaimData sd_ = new SaimData();
   private final AtomicBoolean isRunning_ = new AtomicBoolean(false);
   private OverseeTheFit oft_;
   
   Frame plotFrame_;
   
   @Override
   public void run(String arg) {
      final NonBlockingGenericDialog gd = new NonBlockingGenericDialog( "Saim Fit" );
      
      gd.addNumericField("Wavelenght (nm)", sd_.wavelength_, 1);
      gd.addNumericField("Sample Refractive Index", sd_.nSample_, 2);
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
      gd.addMessage("Only fit pixels > ");
      gd.addNumericField("Threshold", sd_.threshold_, 0);
      gd.setInsets(15, 0, 3);
      
      gd.addPreviewCheckbox(null, "Fit");

      gd.hideCancelButton();
      gd.addHelp("http://fiji.sc/Saim");
      gd.setOKLabel("Close");
      
      gd.addDialogListener(this);
          
      gd.showDialog();
   }

   @Override
   public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
      
      if (!gd.isPreviewActive()) {
         if (isRunning_.get()) {  
            // try to abort the analysis
            if (oft_ != null && oft_.isAlive()) {
               // note, there could be a race condition here, probably not worth looking into
               oft_.stopRun();
            }
         }
      }
      
      if (gd.isPreviewActive()) {
         sd_.wavelength_ = gd.getNextNumber();
         sd_.nSample_ = gd.getNextNumber();
         sd_.dOx_ = gd.getNextNumber();
         sd_.firstAngle_ = gd.getNextNumber();
         sd_.angleStep_ = gd.getNextNumber();
         sd_.mirrorAround0_ = gd.getNextBoolean();
         sd_.zeroDoubled_ = gd.getNextBoolean();
         sd_.A_ = gd.getNextNumber();
         sd_.B_ = gd.getNextNumber();
         sd_.h_ = gd.getNextNumber();
         
         sd_.threshold_ = (int) gd.getNextNumber();
                 
         if (isRunning_.get()) {
            ij.IJ.showMessage("Saim Fit is already running");
            return true;
         }
         
         isRunning_.set(true);

         oft_ = new OverseeTheFit(gd, sd_, isRunning_);
         
         oft_.start();
      }

      return true;
   }
   
}