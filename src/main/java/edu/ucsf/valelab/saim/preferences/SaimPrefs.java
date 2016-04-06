/*
 * Copyright (c) 2016, ImageJ
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucsf.valelab.saim.preferences;


import edu.ucsf.valelab.saim.data.SaimData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 *  Uses code and examples from http://www.ibm.com/developerworks/library/j-prefapi/
 * 
 * @author nico
 */
public class SaimPrefs  {
   public static final String SAIMDATAKEY = "SaimDataKeyV" + SaimData.VERSION;  
   
   private final static int PIECELENGTH = (3*Preferences.MAX_VALUE_LENGTH)/4;
   private final static Preferences PREFS = Preferences.userNodeForPackage(SaimPrefs.class);
   
   public static Preferences getPrefs() {
      return PREFS;
   }
  
   static public void putObject(String key, Object o) {
      try {
         byte raw[] = object2Bytes(o);
         byte pieces[][] = breakIntoPieces(raw);
         writePieces(PREFS, key, pieces);
      } catch (IOException ie) {
      } catch (BackingStoreException be) {
      }
   }

   static public Object getObject(String key) {
      try {
         byte pieces[][] = readPieces(PREFS, key);
         byte raw[] = combinePieces(pieces);
         Object o = bytes2Object(raw);
         return o;
      } catch (IOException ie) {
         ij.IJ.log (ie.getMessage());
      } catch (BackingStoreException be) {
      } catch (ClassNotFoundException cne) {
      }
      return null;
   }

   static private byte[] object2Bytes(Object o) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(o);
      return baos.toByteArray();
   }

   static private Object bytes2Object(byte raw[])
           throws IOException, ClassNotFoundException {
      ByteArrayInputStream bais = new ByteArrayInputStream(raw);
      ObjectInputStream ois = new ObjectInputStream(bais);
      Object o = ois.readObject();
      return o;
   }

   static private void writePieces( Preferences prefs, String key,
      byte pieces[][] ) throws BackingStoreException {
    Preferences node = prefs.node( key );
    node.clear();
    for (int i=0; i<pieces.length; ++i) {
      node.putByteArray( ""+i, pieces[i] );
    }
  }

  static private byte[][] readPieces( Preferences prefs, String key )
      throws BackingStoreException {
    Preferences node = prefs.node( key );
    String keys[] = node.keys();
    int numPieces = keys.length;
    byte pieces[][] = new byte[numPieces][];
    for (int i=0; i<numPieces; ++i) {
      pieces[i] = node.getByteArray( ""+i, null );
    }
    return pieces;
  }

   
   static private byte[][] breakIntoPieces(byte raw[]) {
      int numPieces = (raw.length + PIECELENGTH - 1) / PIECELENGTH;
      byte pieces[][] = new byte[numPieces][];
      for (int i = 0; i < numPieces; ++i) {
         int startByte = i * PIECELENGTH;
         int endByte = startByte + PIECELENGTH;
         if (endByte > raw.length) {
            endByte = raw.length;
         }
         int length = endByte - startByte;
         pieces[i] = new byte[length];
         System.arraycopy(raw, startByte, pieces[i], 0, length);
      }
      return pieces;
   }

   static private byte[] combinePieces(byte pieces[][]) {
      int length = 0;
      for (int i = 0; i < pieces.length; ++i) {
         length += pieces[i].length;
      }
      byte raw[] = new byte[length];
      int cursor = 0;
      for (int i = 0; i < pieces.length; ++i) {
         System.arraycopy(pieces[i], 0, raw, cursor, pieces[i].length);
         cursor += pieces[i].length;
      }
      return raw;
   }

}
