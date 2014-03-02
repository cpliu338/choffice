/**
 *  $Id: Interleaved25_2to1.java,v 1.1.1.1 2004/01/15 15:56:02 dwalters Exp $ 
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License (LGPL) as
 *  published by the Free Software Foundation; either version 2.1 of the
 *  License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY of FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details. 
 */

/**
 * Title:        JBarcodeBean
 * Description:  Barcode JavaBeans Component
 * Copyright:    Copyright (C) 2004
 * Company:      Dafydd Walters
 * @Version      1.0.2
 */
package barcode.jbarcodebean;

/**
 * This class, which implements the {@link BarcodeStrategy} interface,
 * knows how to encode the 2:1 (narrow)
 * variant of the Interleaved Code 2 of 5 barcode type.
 *
 * @version 1.0.2
 */
public class Interleaved25_2to1 extends Interleaved25 implements java.io.Serializable {

  private static CharacterCode[] codes = {
    new CharacterCode('1', new byte[] {2,1,1,1,2}, 1),
    new CharacterCode('2', new byte[] {1,2,1,1,2}, 2),
    new CharacterCode('3', new byte[] {2,2,1,1,1}, 3),
    new CharacterCode('4', new byte[] {1,1,2,1,2}, 4),
    new CharacterCode('5', new byte[] {2,1,2,1,1}, 5),
    new CharacterCode('6', new byte[] {1,2,2,1,1}, 6),
    new CharacterCode('7', new byte[] {1,1,1,2,2}, 7),
    new CharacterCode('8', new byte[] {2,1,1,2,1}, 8),
    new CharacterCode('9', new byte[] {1,2,1,2,1}, 9),
    new CharacterCode('0', new byte[] {1,1,2,2,1}, 0),
    new CharacterCode('A', new byte[] {1,1,1,1}, -1),   // Start
    new CharacterCode('B', new byte[] {2,1,1}, -1)      // Stop
  };

  /**
   * This implementation of <tt>getCodes</tt> returns an array of
   * {@link AbstractBarcodeStrategy.CharacterCode CharacterCode} objects
   * for the narrow Interleaved Code25 format.
   */
  protected CharacterCode[] getCodes() {
    return Interleaved25_2to1.codes;
  }
}
