
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
      assertFalse( ri == 0.0 );
   }
   
}
