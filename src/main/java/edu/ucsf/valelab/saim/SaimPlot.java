///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimPlot.java
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
import edu.ucsf.valelab.saim.data.SaimData;
import edu.ucsf.valelab.saim.plot.PlotUtils;
import edu.ucsf.valelab.saim.preferences.SaimPrefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.NonBlockingGenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import java.awt.AWTEvent;
import java.awt.Frame;
import java.util.prefs.Preferences;
import org.jfree.data.xy.XYSeries;

/**
 * ImageJ plugin that plots theoretical SAIM curves
 *
 * @author nico
 */
public class SaimPlot implements PlugIn, DialogListener
{
   private Frame plotFrame_;   
   private SaimData sd_ = new SaimData();
   private int lastAngle_ = 42;
   private boolean listValues_ = false;
   private ResultsTable saimTable_;
   
   public static void main( String[] args )
   { 
      double[] heights = {16.0, 24.0};
      new SaimPlot().plotFig(false, 488.0, 1.36, 500.0, -50, 50, heights);
   }
 
    public SaimPlot() {
    }

    public void plotFig(boolean list, double wavelength, double nSample, double dOx,
            int firstAngle, int lastAngle, double[] heights) {

        int n = heights.length;
        boolean[] showShapes = new boolean[n];
        XYSeries[] plots = new XYSeries[n];
        for (int i = 0; i < n; i++) {
            plots[i] = new XYSeries("" + heights[i] + "nm", false, false);
            showShapes[i] = false;
        }

        for (int i = firstAngle; i <= lastAngle; i += 1) {
            double angle = Math.toRadians(i);
            for (int j = 0; j < n; j++) {
                double fieldStrength = SaimCalc.fieldStrength(
                        wavelength, angle, nSample, dOx, heights[j]);
                plots[j].add(i, fieldStrength);
            }
        }

        for (int i = 0; i < plots.length; i++) {
            plots[i] = PlotUtils.normalize(plots[i]);
        }

        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        PlotUtils pu = new PlotUtils(prefs);
        if (plotFrame_ != null) {
            plotFrame_.dispose();
        }
        plotFrame_ = pu.plotDataN("Saim at " + wavelength + " nm, n " + nSample
                + ", dOx: " + dOx + " nm", plots,
                "Angle of incidence (degrees)",
                "Normalized Intensity", showShapes, "");
        
        if (list) {
            if (saimTable_ == null) {
                saimTable_ = new ResultsTable();
            }
            saimTable_.reset();
            
            
            for (int i = firstAngle; i <= lastAngle; i += 1) {
                saimTable_.incrementCounter();
                saimTable_.addValue("Angle", i);
                for (int j = 0; j < n; j++) {
                    saimTable_.addValue("" + heights[j] + " nm", 
                            plots[j].getY(i - firstAngle).doubleValue());
                }
            }
            saimTable_.show("Saim Plot Output");

        }

    }

    @Override
    public void run(String arg) {

        final NonBlockingGenericDialog gd = new NonBlockingGenericDialog(
                "Saim Plot  " + Version.VERSION);

        SaimData sd = (SaimData) SaimPrefs.getObject(SaimPrefs.SAIMDATAKEY);
        if (sd != null) {
           sd_ = sd;
        }
        
        gd.addNumericField("Wavelenght (nm)", sd_.wavelength_, 1);
        gd.addNumericField("Sample Refractive Index", sd_.nSample_, 2);
        gd.addNumericField("Thickness of oxide layer (nm)", sd_.dOx_, 1);
        gd.setInsets(15, 0, 3);
        gd.addMessage("Angles to be plotted:");
        gd.addNumericField("First angle", sd_.firstAngle_, 0);
        gd.addNumericField("Last angle", lastAngle_, 0);
        gd.setInsets(15, 0, 3);
        gd.addMessage("Heights as comma separated values:");
        gd.addStringField("Heights in nm", SaimData.toString(sd_.heights_), 18 );
        gd.addCheckbox("Output values", listValues_);
        gd.addPreviewCheckbox(null, "Plot");

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
            sd_.firstAngle_ = (int) gd.getNextNumber();
            lastAngle_ = (int) gd.getNextNumber();
            sd_.heights_ = SaimData.fromString(gd.getNextString());
            listValues_ = gd.getNextBoolean();
            
            SaimPrefs.putObject(SaimPrefs.SAIMDATAKEY, sd_);

            plotFig(listValues_, sd_.wavelength_, sd_.nSample_, sd_.dOx_, 
                    (int) sd_.firstAngle_, lastAngle_, sd_.heights_);
            
            // switch checkbox off when we are done
            gd.getPreviewCheckbox().setState(false);
        }

        return true;
    }
}
