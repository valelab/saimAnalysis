 ///////////////////////////////////////////////////////////////////////////////
 //FILE:          SaimFunctionFitter.java
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

import java.util.Collection;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;

/**
 * Fits the Saim function using pre-defined wavelength, thickness of the oxide
 * layer, and refractive index of the sample
 *
 * @author nico
 */
public class SaimFunctionFitter extends AbstractCurveFitter {

   private final SaimFunction saimFunction_;
   private double[] guess_ = { 1.0, 1.0, 1.0 };
   private int maxIterations_ = 100;

   public SaimFunctionFitter(double wavelength, double dOx, double nSample) {
      saimFunction_ = new SaimFunction(wavelength, dOx, nSample);
   }

   public void setGuess(double[] guess) {
      if (guess.length != 3) {
         throw new DimensionMismatchException(guess.length, 3);
      }
      guess_ = guess;
   }
   
   public void setMaxIterations (int val) {
      maxIterations_ = val;
   }

   public int getCalcCount() {
      return saimFunction_.counter;
   }
   
   @Override
   protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
      final int len = points.size();
      final double[] target = new double[len];
      final double[] weights = new double[len];

      int i = 0;
      for (WeightedObservedPoint point : points) {
         target[i] = point.getY();
         weights[i] = point.getWeight();
         i += 1;
      }

      final AbstractCurveFitter.TheoreticalValuesFunction model = 
              new AbstractCurveFitter.TheoreticalValuesFunction(
                      saimFunction_, points);

      ConvergenceChecker<PointVectorValuePair> checker =
          new SimpleVectorValueChecker(1.0e-6, 1.0e-10);
      
      return new LeastSquaresBuilder().
              maxEvaluations(Integer.MAX_VALUE).
              maxIterations(maxIterations_).
              //checker(checker).
              start(guess_).
              target(target).
              weight(new DiagonalMatrix(weights)).
              model(model.getModelFunction(), model.getModelFunctionJacobian()).
              build();
   }

}
