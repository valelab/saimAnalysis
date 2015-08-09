 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          IntensityData.java
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

package edu.ucsf.valelab.saim.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.jfree.data.xy.XYSeries;

/**
 * Utility to isolate our own data storage mechanism from that of outside 
 * libraries
 * Use this to hold data, and expand output functions as needed
 * @author nico
 */
public class IntensityData {
   private final List<IntensityDataItem> data_;
   
   public IntensityData() {
      data_ = new ArrayList<IntensityDataItem>();
   }
   
   public void add(double angleDegree, double intensity) {
      IntensityDataItem item = new IntensityDataItem(angleDegree, intensity);
      data_.add(item);
   }
   
   public int size() {
      return data_.size();
   }
   
   public double avg() {
      double sum = 0.0;
      for (IntensityDataItem item : data_) {
         sum += item.getIntensity();
      }
      return (sum / (double) data_.size());
   }
   
   /**
    * Output as needed by apache commons Math library
    * Currently assigns the weight 1.0 to all points
    * @return list of weightedObservedPoints for fitting
    */
   public ArrayList<WeightedObservedPoint> getWeightedObservedPoints() {
      ArrayList<WeightedObservedPoint> points = 
              new ArrayList<WeightedObservedPoint>();
      for (IntensityDataItem item : data_) {
         points.add(new WeightedObservedPoint(1.0, item.getAngleRadians(), 
                 item.getIntensity()));
      }
      return points;
   }
      
   /**
    * Returns data in a form usable by the JFreeChart library
    * @param name of the data series in JFreeChart
    * @return JFreeChart XYSeries
    */
   public XYSeries getXYSeries(String name) {
      XYSeries series = new XYSeries(name, false, false);
      for (IntensityDataItem item : data_) {
         series.add(item.getAngleDegree(), item.getIntensity());
      }
      return series;
   }        
   
   /**
    * Provide access to a read-only version so that the user can iterate 
    * easily over the data without risking modifications
    * @return read-only version of the data
    */
   public List<IntensityDataItem> getDataList() {
      return Collections.unmodifiableList(data_);
   }
}
