///////////////////////////////////////////////////////////////////////////////
//FILE:          DataTest.java
//PROJECT:       SAIM
//SUBSYSTEM:     Tests  
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

import junit.framework.TestCase;

/**
 *
 * @author nico
 */
public class DataTest extends TestCase {
   
   public DataTest (String testName ) 
   {
      super (testName);
   }
   
   public void testRI() throws Exception {
      double ri = RI.getRI(RI.Compound.SILICON, 525.0);
      System.out.println("RI found was: " + ri);
      assertEquals(4.178688888888888, ri, 0.0000001 );
      ri = RI.getRI(RI.Compound.SILICONOXIDE, 525.0);
      System.out.println("RI found was: " + ri);
      assertEquals(1.461, ri, 0.0001);
   }
   
}
