/**
 *  $Id: BarcodeElement.java,v 1.1.1.1 2004/01/15 15:56:00 dwalters Exp $ 
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
 * Class representing a single barcode module (bar or space).
 *
 * @version 1.0.2
 */
public class BarcodeElement implements java.io.Serializable {
  /**
   * The width of the element expressed as a multiple of the narrowest module (bar/space) width.
   */
  public byte width;

  /**
   * <tt>true</tt> = bar (black), <tt>false</tt> = space (white).
   */
  public boolean bar;
}

