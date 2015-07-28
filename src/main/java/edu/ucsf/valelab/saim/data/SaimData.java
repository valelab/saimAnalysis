///////////////////////////////////////////////////////////////////////////////
//FILE:          SaimData.java
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

/**
 * Very simple data structure to hold parameters that are used over and over
 * again in the Saim calculations
 * @author nico
 */
public class SaimData {
   public double wavelength_ = 488.0;
   public double nSample_ = 1.36;
   public double dOx_ = 500.0;
   public double firstAngle_ = -50;
   public double angleStep_ = 1;
   public double A_ = 1000.0;
   public double B_ = 5000.0;
   public double h_ = 100.0;
}
