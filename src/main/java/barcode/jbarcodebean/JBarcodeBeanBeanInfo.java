/**
 *  $Id: JBarcodeBeanBeanInfo.java,v 1.1.1.1 2004/01/15 15:56:02 dwalters Exp $ 
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

import java.beans.*;

/**
 * This class is an implementation of the <tt><b>java.beans.BeanInfo</b></tt>
 * interface, that provides GUI Builders with information about
 * {@link JBarcodeBean}.
 *
 * @version 1.0.2
 */
public class JBarcodeBeanBeanInfo extends SimpleBeanInfo {

  public java.awt.Image getIcon(int iconKind) {
    java.awt.Image img = null;
    switch (iconKind) {
      case BeanInfo.ICON_MONO_16x16:
        img = loadImage("barcode_bw16.gif");
        break;
      case BeanInfo.ICON_COLOR_16x16:
        img = loadImage("barcode_c16.gif");
        break;
      case BeanInfo.ICON_MONO_32x32:
        img = loadImage("barcode_bw32.gif");
        break;
      case BeanInfo.ICON_COLOR_32x32:
        img = loadImage("barcode_c32.gif");
        break;
    }
    return img;
  }

  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      PropertyDescriptor code = new PropertyDescriptor("code", beanClass);
      code.setBound(true);
      PropertyDescriptor narrowestBarWidth = new PropertyDescriptor("narrowestBarWidth", beanClass);
      narrowestBarWidth.setBound(true);
      PropertyDescriptor checkDigit = new PropertyDescriptor("checkDigit", beanClass);
      checkDigit.setBound(true);
      PropertyDescriptor codeType = new PropertyDescriptor("codeType", beanClass);
      codeType.setBound(true);
      codeType.setPropertyEditorClass(BarcodeStrategyEditor.class);
      PropertyDescriptor font = new PropertyDescriptor("font", beanClass);
      font.setBound(true);
      PropertyDescriptor border = new PropertyDescriptor("border", beanClass);
      border.setBound(true);
      PropertyDescriptor background = new PropertyDescriptor("background", beanClass);
      background.setBound(true);
      PropertyDescriptor foreground = new PropertyDescriptor("foreground", beanClass);
      foreground.setBound(true);
      PropertyDescriptor barcodeBackground = new PropertyDescriptor("barcodeBackground", beanClass);
      barcodeBackground.setBound(true);
      PropertyDescriptor barcodeHeight = new PropertyDescriptor("barcodeHeight", beanClass);
      barcodeHeight.setBound(true);
      PropertyDescriptor showText = new PropertyDescriptor("showText", beanClass);
      showText.setBound(true);
      PropertyDescriptor angleDegrees = new PropertyDescriptor("angleDegrees", beanClass);
      angleDegrees.setBound(true);

      PropertyDescriptor rv[] = {code, narrowestBarWidth, checkDigit, codeType,
        font, border, background, foreground, barcodeBackground,
        barcodeHeight, showText, angleDegrees};
      return rv;
    } catch( IntrospectionException e) {
      throw new Error(e.toString());
    }
  }

  public int getDefaultPropertyIndex() {
    // the index for the code property.
    return 0;
  }

  private final static Class beanClass = JBarcodeBean.class;
}
