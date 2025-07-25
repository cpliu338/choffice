/**
 *  $Id: BarcodeStrategyEditor.java,v 1.1.1.1 2004/01/15 15:56:00 dwalters Exp $ 
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

import java.beans.PropertyEditorSupport;

/**
 * A property editor for the {@link BarcodeStrategy} type.
 * GUI Builders use this class for the
 * {@link JBarcodeBean#setCodeType(BarcodeStrategy) codeType} property of
 * the {@link JBarcodeBean} Javabean component.
 *
 * @version 1.0.2
 */
public class BarcodeStrategyEditor extends PropertyEditorSupport {

  public String[] getTags() {
    return new String[] {
      "Code 128",
      "Code 39 3:1",
      "Code 39 2:1",
      "Ext Code 39 3:1",
      "Ext Code 39 2:1",
      "Interleaved 25 3:1",
      "Interleaved 25 2:1",
      "MSI (mod 10 check)",
      "Codabar 3:1",
      "Codabar 2:1"
    };
  }

  public void setAsText(String s) {
    if (s.equals("Code 128")) {
      setValue(new Code128());
    } else if (s.equals("Code 39 3:1")) {
      setValue(new Code39());
    } else if (s.equals("Code 39 2:1")) {
      setValue(new Code39_2to1());
    } else if (s.equals("Ext Code 39 3:1")) {
      setValue(new ExtendedCode39());
    } else if (s.equals("Ext Code 39 2:1")) {
      setValue(new ExtendedCode39_2to1());
    } else if (s.equals("Interleaved 25 3:1")) {
      setValue(new Interleaved25());
    } else if (s.equals("Interleaved 25 2:1")) {
      setValue(new Interleaved25_2to1());
    } else if (s.equals("MSI (mod 10 check)")) {
      setValue(new MSI());
    } else if (s.equals("Codabar 3:1")) {
      setValue(new Codabar());
    } else if (s.equals("Codabar 2:1")) {
      setValue(new Codabar_2to1());
    } else {
      // Default to Code 39
      setValue(new Code39());
    }
  }

  public String getAsText() {
    BarcodeStrategy s = (BarcodeStrategy)getValue();
    if (s instanceof Code128) {
      // Code 128
      return "Code 128";
    } else if (s instanceof Code39_2to1) {
      // Code 3 of 9 2:1
      return "Code 39 2:1";
    } else if (s instanceof Code39) {
      // Code 3 of 9 3:1
      return "Code 39 3:1";
    } else if (s instanceof ExtendedCode39_2to1) {
      // Extended Code 3 of 9 2:1
      return "Ext Code 39 2:1";
    } else if (s instanceof ExtendedCode39) {
      // Extended Code 3 of 9 3:1
      return "Ext Code 39 3:1";
    } else if (s instanceof Interleaved25_2to1) {
      // Interleaved 25 2:1
      return "Interleaved 25 2:1";
    } else if (s instanceof Interleaved25) {
      // Interleaved 25 3:1
      return "Interleaved 25 3:1";
    } else if (s instanceof MSI) {
      // MSI
      return "MSI (mod 10 check)";
    } else if (s instanceof Codabar_2to1) {
      // Codabar 2:1
      return "Codabar 2:1";
    } else if (s instanceof Codabar) {
      // Codabar 3:1
      return "Codabar 3:1";
    } else {
      // Must set to something, so default to Code 39
      return "Code 39";
    }
  }

  public String getJavaInitializationString() {
    BarcodeStrategy s = (BarcodeStrategy)getValue();
    if (s instanceof Code128) {
      // Code 128
      return "new jbarcodebean.Code128()";
    } else if (s instanceof Code39_2to1) {
      // Code 3 of 9 2:1
      return "new jbarcodebean.Code39_2to1()";
    } else if (s instanceof Code39) {
      // Code 3 of 9 3:1
      return "new jbarcodebean.Code39()";
    } else if (s instanceof ExtendedCode39_2to1) {
      // Extended Code 3 of 9 2:1
      return "new jbarcodebean.ExtendedCode39_2to1()";
    } else if (s instanceof ExtendedCode39) {
      // Extended Code 3 of 9 3:1
      return "new jbarcodebean.ExtendedCode39()";
    } else if (s instanceof Interleaved25_2to1) {
      // Interleaved 25 2:1
      return "new jbarcodebean.Interleaved25_2to1()";
    } else if (s instanceof Interleaved25) {
      // Interleaved 25 3:1
      return "new jbarcodebean.Interleaved25()";
    } else if (s instanceof MSI) {
      // MSI
      return "new jbarcodebean.MSI()";
    } else if (s instanceof Codabar_2to1) {
      // Codabar 2:1
      return "new jbarcodebean.Codabar_2to1()";
    } else if (s instanceof Codabar) {
      // Codabar 3:1
      return "new jbarcodebean.Codabar()";
    } else {
      // Must set to something, so default to Code 39
      return "new jbarcodebean.Code39()";
    }
  }
}
