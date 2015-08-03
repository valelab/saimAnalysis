///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimErrorFunctionFitter.java
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

package edu.ucsf.valelab.saim.calculations;

import edu.ucsf.valelab.saim.data.SaimData;
import java.util.Collection;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;

/**
 *
 * @author nico
 */
public class SaimErrorFunctionFitter {
   SaimData data_;
   double[] guess_ = {5000.0, 5000.0, 100.0};
   
   public SaimErrorFunctionFitter(SaimData data) {
      data_ = data;
   }
   
   public void setGuess(double[] guess) {
      guess_ = guess;
   }
   
   public double[] fit(Collection<WeightedObservedPoint> observedPoints) {
      SaimErrorFunction ser = new SaimErrorFunction(data_, observedPoints);
      MultivariateOptimizer optimizer = new BOBYQAOptimizer(6, 10, 1.0E-8);
      double[] lb = {0.0, 0.0, 0.0};
      double[] ub = {64000, 64000, 1000};
      SimpleBounds sb = new SimpleBounds(lb, ub);
      PointValuePair results = optimizer.optimize(
              new MaxEval(20000),
              GoalType.MINIMIZE,
              new InitialGuess(guess_),
              new ObjectiveFunction(ser),
              sb);
      System.out.println("Value: " + results.getValue());
      return results.getPoint();
              
   }
}
