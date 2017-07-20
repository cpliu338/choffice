/**
 *  $Id: Code39_2to1.java,v 1.1.1.1 2004/01/15 15:56:01 dwalters Exp $ 
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
 * knows how to encode the
 * 2:1 (narrow) variant of the Code 3 of 9 barcode type.
 *
 * @version 1.0.2
 */
public class Code39_2to1 extends Code39 implements java.io.Serializable {

  /**
   * This implementation of <tt>getCodes</tt> returns {@link BaseCode39#codes2to1}.
   */
  protected CharacterCode[] getCodes() {
    return BaseCode39.codes2to1;
  }
}
