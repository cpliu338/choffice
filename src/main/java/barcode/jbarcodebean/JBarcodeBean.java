package barcode.jbarcodebean;

/**
 *  $Id: JBarcodeBean.java,v 1.1.1.1 2004/01/15 15:56:01 dwalters Exp $ 
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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import javax.accessibility.*;
import java.io.*;
import java.awt.image.*;
//import inventory.utils.*;
import barcode.Acme.jpm.Encoders.*;
/**
 * <p>
 * JFC Swing-compatible JavaBeans
 * <sup><small>TM</small></sup>
 * component that renders barcodes in a variety of different formats.
 * <p>
 * [<a href='#skip_copyright'>skip over license and copyright</a>]
 * <hr>
 * <p><b>LICENSE INFORMATION</b></p>
 * <p>
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License (LGPL) as
 *  published by the Free Software Foundation; either version 2.1 of the
 *  License, or (at your option) any later version.
 * </p><p>
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY of FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *  </p>
 *  <p>See <a href='http://www.gnu.org/licenses/lgpl.html'
 *  >http://www.gnu.org/licenses/lgpl.html</a>.
 *  </p> 
 * <hr>
 * <p>
 * <strong>THIRD PARTY COPYRIGHT</strong>
 * <p>
 * The {@link #gifEncode(OutputStream) gifEncode} method of <strong>JBarcodeBean</strong>,
 * which allows a barcode to be encoded into a GIF output stream, utilizes
 * the {@link Acme.jpm.Encoders.GifEncoder GifEncoder} class, which is
 * the copyright of Jef Poskanzer.
 * <p>
 * <blockquote><i>
 * GifEncoder is Copyright (C)1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
 * </i></blockquote>
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * </p>
 * <hr>
 * <A NAME="skip_copyright"><!-- --></A>
 * <p>
 * <strong>JBarcodeBean</strong> employs the <strong>Strategy</strong> design pattern<b>*</b>
 * to abstract the encoding algorithm.  The
 * {@link #setCodeType(BarcodeStrategy) codeType}
 * property can be set to any class that implements the {@link BarcodeStrategy}
 * interface to determine how the {@link #setCode(String) code} property is encoded.
 * This version of JBarcodeBean is bundled with the following concrete implementations
 * of {@link BarcodeStrategy}:
 *
 * <ul>
 * <li>{@link Code128}
 * <li>{@link Code39}
 * <li>{@link Code39_2to1}
 * <li>{@link ExtendedCode39}
 * <li>{@link ExtendedCode39_2to1}
 * <li>{@link Interleaved25}
 * <li>{@link Interleaved25_2to1}
 * <li>{@link Codabar}
 * <li>{@link Codabar_2to1}
 * <li>{@link MSI}
 * </ul>
 * <p>
 *
 * <blockquote><b>*</b>See <i>"Design Patterns Elements of Reusable Object-Oriented Software",
 * Erich Gamma et al.</i> for more information about design patterns.</blockquote>
 *
 * @version 1.0.2
 */
public class JBarcodeBean extends JComponent implements java.io.Serializable, Accessible {

  // Property change constants

  /** Identifies a change in the <b>showText</b> property. */
  private static final String SHOW_TEXT_CHANGED_PROPERTY = "showText";
  /** Identifies a change in the <b>code</b> property. */
  private static final String CODE_CHANGED_PROPERTY = "code";
  /** Identifies a change in the <b>codeType</b> property. */
  private static final String CODE_TYPE_CHANGED_PROPERTY = "codeType";
  /** Identifies a change in the <b>checkDigit</b> property. */
  private static final String CHECK_DIGIT_CHANGED_PROPERTY = "checkDigit";
  /** Identifies a change in the <b>narrowestBarWidth</b> property. */
  private static final String NARROWEST_BAR_WIDTH_CHANGED_PROPERTY = "narrowestBarWidth";
  /** Identifies a change in the <b>barcodeHeight</b> property. */
  private static final String BARCODE_HEIGHT_CHANGED_PROPERTY = "barcodeHeight";
  /** Identifies a change in the <b>barcodeBackground</b> property. */
  private static final String BARCODE_BACKGROUND_CHANGED_PROPERTY = "barcodeBackground";
  /** Identifies a change in the <b>angleDegrees</b> property. */
  private static final String ANGLE_DEGREES_CHANGED_PROPERTY = "angleDegrees";

  // Private fields.
  private String code;
  private BarcodeStrategy codeType;
  private boolean checkDigit = false;
  private EncodedBarcode encoded = null;
  private int narrowestBarWidth = 1;
  private int barcodeHeight = 40;
  private Color barcodeBackground = Color.white;
  private boolean showText;
  private Dimension minimumSize;
  private Dimension preferredSize;
  private double angleDegrees;
  private int labelHeight;          // calculated
  private int labelWidth;           // calculated
  private int barcodeWidth;         // calculated
  private String encodeError = "";

  /**
   * <p>Contructor that allows an initial barcode and code type to be specified.
   *
   * @param code The text to encode.
   * @param codeType The type of barcode.
   */
  public JBarcodeBean(String code, BarcodeStrategy codeType) {

    // Set some Component and JComponent properties.
    setForeground(Color.black);
    setFont(new Font("Monospaced", Font.PLAIN, 12));

    // Set bean properties - don't fire property changes here since this is c'tor
    this.code = code;
    this.codeType = codeType;
    if (codeType.requiresChecksum() == BarcodeStrategy.MANDATORY_CHECKSUM) {
      checkDigit = true;
    } else if (codeType.requiresChecksum() == BarcodeStrategy.NO_CHECKSUM) {
      checkDigit = false;
    }

    // Disable double buffering, so that Java-2 printing is hi-res
    setDoubleBuffered(false);

    // Render the bean.
    encode();
    recalculateSizes();
    repaint();
  }

  /**
   * <p>
   * Parameterless constructor. Creates a <tt>JBarcodeBean</tt> object with default
   * property values, so that the bean will render as a typical barcode
   * when initially added to a GUI with a GUI Builder.
   * <p>
   * Defaulat values are:<b>
   * <p><code>code = "1234"</code><br>
   * <code>codeType = new Code39()</code></b><br>
   * <p>
   * @see Code39
   *
   */
  public JBarcodeBean() {
    this("1234", new Code39());
  }

  /**
   * Accessor method for the <tt><b>angleDegrees</b></tt> property,
   * which determines the angle (from horizontal) at which the barcode
   * is rendered.
   */
  public double getAngleDegrees() {
    return angleDegrees;
  }

  /**
   * Mutator method for the <tt><b>angleDegrees</b></tt> property,
   * which determines the angle (from horizontal) at which the barcode
   * is rendered.
   */
  public void setAngleDegrees(double angleDegrees) {
    double oldValue = this.angleDegrees;
    this.angleDegrees = angleDegrees;
    firePropertyChange(ANGLE_DEGREES_CHANGED_PROPERTY, oldValue, angleDegrees);
    recalculateSizes();
    repaint();
  }

  /**
   * Accessor method for the <tt><b>showText</b></tt> property, which
   * determines whether or not the text caption is visible below the
   * barcode. <tt>true</tt> = visible, <tt>false</tt> = not visible.
   */
  public boolean isShowText() {
    return showText;
  }

  /**
   * Mutator method for the <tt><b>showText</b></tt> property, which
   * determines whether or not the text caption is visible below the
   * barcode. <tt>true</tt> = visible, <tt>false</tt> = not visible.
   */
  public void setShowText(boolean showText) {
    boolean oldValue = this.showText;
    this.showText = showText;
    firePropertyChange(SHOW_TEXT_CHANGED_PROPERTY, oldValue, showText);
    recalculateSizes();
    repaint();
  }

  /**
   * Always returns <tt>true</tt>, as this component renders its entire
   * drawing area.
   */
  public boolean isOpaque() {
    return true;
  }

  /**
   * Accessor method for the <tt><b>foreground</b></tt> property, which
   * determines the color of the bars and caption text (typically black).
   */
  public Color getForeground() {
    return super.getForeground();
  }

  /**
   * Mutator method for the <tt><b>foreground</b></tt> property, which
   * determines the color of the bars and caption text (typically black).
   */
  public void setForeground(Color c) {
    super.setForeground(c);
    repaint();
  }

  /**
   * Accessor method for the <tt><b>background</b></tt> property, which
   * determines the control background color (the space around the barcode,
   * and behind the text caption).  Note that this is not the
   * same as the {@link #getBarcodeBackground barcodeBackground} property
   * (which is the color of the spaces between the bars in the barcode).
   */
  public Color getBackground() {
    return super.getBackground();
  }

  /**
   * Mutator method for the <tt><b>background</b></tt> property, which
   * determines the control background color (the space around the barcode,
   * and behind the text caption).  Note that this is not the
   * same as the {@link #setBarcodeBackground barcodeBackground} property
   * (which is the color of the spaces between the bars in the barcode).
   */
  public void setBackground(Color c) {
    super.setBackground(c);
    repaint();
  }

  /**
   * Accessor method for the <tt><b>barcodeBackground</b></tt> property, which
   * determines the color of the spaces between the bars in the barcode
   * (typically white).
   *
   * @see #setBackground
   */
  public Color getBarcodeBackground() {
    return barcodeBackground;
  }

  /**
   * Mutator method for the <tt><b>barcodeBackground</b></tt> property, which
   * determines the color of the spaces between the bars in the barcode
   * (typically white).
   *
   * @see #setBackground
   */
  public void setBarcodeBackground(Color barcodeBackground) {
    Color oldValue = this.barcodeBackground;
    this.barcodeBackground = barcodeBackground;
    firePropertyChange(BARCODE_BACKGROUND_CHANGED_PROPERTY, oldValue, barcodeBackground);
    repaint();
  }

  /**
   * Accessor methor for the <tt><b>border</b></tt> property.
   */
  public javax.swing.border.Border getBorder() {
    return super.getBorder();
  }

  /**
   * Mutator methor for the <tt><b>border</b></tt> property.
   */
  public void setBorder(javax.swing.border.Border border) {
    super.setBorder(border);
    recalculateSizes();
    repaint();
  }

  /**
   * Accessor method for the <tt><b>preferredSize</b></tt> property.
   */
  public Dimension getPreferredSize() {
    return preferredSize();
  }

  /**
   * Mutator method for the <tt><b>preferredSize</b></tt> property.
   */
  public void setPreferredSize(Dimension preferredSize) {
    Dimension oldValue = this.preferredSize;
    this.preferredSize = preferredSize;
    firePropertyChange("preferredSize", oldValue, preferredSize);
  }

  /**
   * Returns the preferred size of the component.
   *
   * @deprecated Use {@link #getPreferredSize}.
   */
  public Dimension preferredSize() {
    if (preferredSize != null) {
      return preferredSize;
    } else {
      return new Dimension(getWidth(), getHeight());
    }
  }

  /**
   * Accessor method for <tt><b>minimumSize</b></tt> property.
   */
  public Dimension getMinimumSize() {
    return minimumSize();
  }

  /**
   * Mutator method for the <tt><b>minimumSize</b></tt> property.
   */
  public void setMinimumSize(Dimension minimumSize) {
    Dimension oldValue = this.minimumSize;
    this.minimumSize = minimumSize;
    firePropertyChange("minimumSize", oldValue, minimumSize);
  }

  /**
   * Returns the minimum size of the component.
   *
   * @deprecated Use {@link #getMinimumSize}.
   */
  public Dimension minimumSize() {
    if (minimumSize != null) {
      return minimumSize;
    } else {
      return new Dimension(getWidth(), getHeight());
    }
  }

  /**
   * Accessor method for the <tt><b>font</b></tt> property.
   */
  public Font getFont() {
    return super.getFont();
  }

  /**
   * Mutator method for the <tt><b>font</b></tt> property.
   */
  public void setFont(Font font) {
    super.setFont(font);
    recalculateSizes();
    repaint();
  }

  /**
   * Accessor method for the <tt><b>barcodeHeight</b></tt> property, which
   * determines the height of the barcode (excluding caption text) in pixels.
   */
  public int getBarcodeHeight() {
    return barcodeHeight;
  }

  /**
   * Mutator method for the <tt><b>barcodeHeight</b></tt> property, which
   * determines the height of the barcode (excluding caption text) in pixels.
   */
  public void setBarcodeHeight(int barcodeHeight) {
    int oldValue = this.barcodeHeight;
    this.barcodeHeight = barcodeHeight;
    firePropertyChange(BARCODE_HEIGHT_CHANGED_PROPERTY, oldValue, barcodeHeight);
    recalculateSizes();
    repaint();
  }

  /**
   * Accessor method for the <tt><b>narrowestBarWidth</b></tt> property,
   * which determines the width (in pixels) of the narrowest bar in the barcode.
   */
  public int getNarrowestBarWidth() {
    return narrowestBarWidth;
  }

  /**
   * Mutator method for the <tt><b>narrowestBarWidth</b></tt> property,
   * which determines the width (in pixels) of the narrowest bar in the barcode.
   */
  public void setNarrowestBarWidth(int narrowestBarWidth) {
    int oldValue = this.narrowestBarWidth;
    this.narrowestBarWidth = narrowestBarWidth;
    firePropertyChange(NARROWEST_BAR_WIDTH_CHANGED_PROPERTY, oldValue, narrowestBarWidth);
    recalculateSizes();
    repaint();
  }

  /**
   * Always returns <tt>false</tt>, as this control cannot receive focus.
   */
  public boolean isFocusTraversable() {
    return false;
  }

  /**
   * Always returns the value of the {@link #getCode code} property.
   */
  protected String paramString() {
    return code;
  }

  /**
   * Always returns the value of the {@link #getCode code} property.
   */
  public String toString() {
    return code;
  }

  /**
   * Accessor method for the <tt><b>code</b></tt> property,
   * which is the text encoded in the barcode.
   */
  public String getCode() {
    return code;
  }

  /**
   * Mutator method for the <tt><b>code</b></tt> property,
   * which is the text encoded in the barcode.
   */
  public void setCode(String code) {
    String oldValue = this.code;
    this.code = code;
    firePropertyChange(CODE_CHANGED_PROPERTY, oldValue, code);
    encode();
    recalculateSizes();
    repaint();
  }

  /**
   * Accessor method for the <tt><b>codeType</b></tt> property,
   * which is the barcode type.
   */
  public BarcodeStrategy getCodeType() {
    return codeType;
  }

  /**
   * Mutator method for the <tt><b>codeType</b></tt> property,
   * which is the barcode type.
   */
  public void setCodeType(BarcodeStrategy codeType) {
    BarcodeStrategy oldValue = this.codeType;
    this.codeType = codeType;
    firePropertyChange(CODE_TYPE_CHANGED_PROPERTY, oldValue, codeType);
    if (codeType != null && codeType.requiresChecksum() == BarcodeStrategy.MANDATORY_CHECKSUM && checkDigit == false) {
      try {
        setCheckDigit(true);
      } catch (BarcodeException ex) {
      }
      // encode(), recalculateSizes(), and repaint() will be done in setChecked().
    } else if (codeType != null && codeType.requiresChecksum() == BarcodeStrategy.NO_CHECKSUM && checkDigit == true) {
      try {
        setCheckDigit(false);
      } catch (BarcodeException ex) {
      }
      // encode(), recalculateSizes(), and repaint() will be done in setChecked().
    } else {
      encode();
      recalculateSizes();
      repaint();
    }
  }

  /**
   * Accessor method for the <tt><b>checkDigit</b></tt> property,
   * which determines whether a check digit is encoded in the
   * barcode.  <tt>true</tt> = check digit is encoded, <tt>false</tt>
   * = check digit is not encoded.
   */
  public boolean isCheckDigit() {
    return checkDigit;
  }

  /**
   * Mutator method for the <tt><b>checkDigit</b></tt> property,
   * which determines whether a check digit is encoded in the
   * barcode.  <tt>true</tt> = check digit is encoded, <tt>false</tt>
   * = check digit is not encoded.
   */
  public void setCheckDigit(boolean checkDigit) throws BarcodeException {
    if (codeType != null) {
      if (codeType.requiresChecksum() == BarcodeStrategy.MANDATORY_CHECKSUM  && checkDigit == false) {
        // Cannot disable checksum
        throw new BarcodeException("Checksum mandatory");
      } else if (codeType.requiresChecksum() == BarcodeStrategy.NO_CHECKSUM  && checkDigit == true) {
        // Cannot enable checksum
        throw new BarcodeException("No checksum");
      }
    }
    boolean oldValue = this.checkDigit;
    this.checkDigit = checkDigit;
    firePropertyChange(CHECK_DIGIT_CHANGED_PROPERTY, oldValue, checkDigit);
    encode();
    recalculateSizes();
    repaint();
  }

  /**
   * Component paint method.
   */
  protected void paintComponent(Graphics graphics) {
    doPaint(graphics, getSize(), getInsets());
  }

  private void doPaint(Graphics graphics, Dimension d, Insets insets) {

    Graphics2D g = (Graphics2D) graphics;

    // Save graphics properties
    AffineTransform tran = g.getTransform();
    Shape oldClip = g.getClip();
    Color oldColor = g.getColor();
    Font oldFont = g.getFont();

    // Fill control background
    g.setColor(getBackground());
    g.fillRect(0, 0, d.width, d.height);

    // Set clip rectangle to prevent barcode overwriting border
    g.setClip(insets.left, insets.top,
      d.width - insets.left - insets.right, d.height - insets.top - insets.bottom);

    // Apply rotate and transale transform
    g.rotate(angleDegrees / 180 * Math.PI, d.width / 2.0, d.height / 2.0);

    int barcodeTop = (d.height - (barcodeHeight + labelHeight)) / 2;

    // Draw barcode
    if (encoded != null) {
      for(int i = 0, x = (d.width - barcodeWidth) / 2; i < encoded.elements.length; i++) {
        if (encoded.elements[i].bar) {
          // bar
          g.setColor(getForeground());
        } else {
          // space
          g.setColor(barcodeBackground);
        }
        int barWidth = encoded.elements[i].width * narrowestBarWidth;
        g.fillRect(x, barcodeTop, barWidth, barcodeHeight);
        x += barWidth;
      }

      // Draw text
      if (showText) {
        g.setFont(this.getFont());
        g.setColor(getForeground());
        FontMetrics fm = getFontMetrics(g.getFont());
        g.drawString(encoded.barcodeLabelText,
          (d.width - labelWidth) / 2,
          barcodeTop + barcodeHeight + fm.getAscent()
        );
      }
    } else if (!encodeError.equals("")) {
      // Display error
      g.setFont(new Font("Monospaced", Font.PLAIN, 10));
      FontMetrics fm = getFontMetrics(g.getFont());
      int labelWidth = fm.stringWidth(encodeError);
      g.setColor(getBarcodeBackground());
      g.fillRect((d.width - labelWidth) / 2, barcodeTop,
        labelWidth, fm.getHeight());
      g.setColor(getForeground());
      g.drawString(encodeError,
        (d.width - labelWidth) / 2,
        barcodeTop + fm.getAscent()
      );
    }

    // Restore graphics properties
    g.setTransform(tran);
    g.setClip(oldClip);
    g.setColor(oldColor);
    g.setFont(oldFont);
  }

  private void recalculateSizes() {

    if (showText) {
      FontMetrics fm = getFontMetrics(getFont());
      labelHeight = fm.getAscent() + fm.getDescent();
    } else {
      labelHeight = 0;
    }

    Dimension d = calculateControlSize(getRequiredWidth(), barcodeHeight + labelHeight);
    setPreferredSize(d);
    setMinimumSize(d);
    revalidate();
  }

  private Dimension calculateControlSize(int rectWidth, int rectHeight) {

    Insets insets = getInsets();
    Dimension d = new Dimension();
    double angleRadians = angleDegrees / 180.0 * Math.PI;

    d.height = (int) (Math.abs(rectWidth * Math.sin(angleRadians)) + Math.abs(rectHeight * Math.cos(angleRadians)))
        + insets.top + insets.bottom;

    d.width = (int) (Math.abs(rectWidth * Math.cos(angleRadians)) + Math.abs(rectHeight * Math.sin(angleRadians)))
        + insets.left + insets.right;

    return d;
  }

  private int getRequiredWidth() {

    this.barcodeWidth = 0;
    this.labelWidth = 0;

    if (encoded != null) {

      FontMetrics fm = getFontMetrics(getFont());
      labelWidth = fm.stringWidth(encoded.barcodeLabelText);

      for(int i = 0; i < encoded.elements.length; i++) {
        barcodeWidth += encoded.elements[i].width * narrowestBarWidth;
      }
    } else if (!encodeError.equals("")) {
      // error message only
      FontMetrics fm = getFontMetrics(getFont());
      labelWidth = fm.stringWidth(encodeError);
    }

    int width = (barcodeWidth > labelWidth) ? barcodeWidth : labelWidth;
    return width;
  }

  private void encode() {
    encodeError = "";
    if ((codeType != null) && (!code.equals(""))) {
      try {
        encoded = codeType.encode(code, checkDigit);
      } catch (BarcodeException e) {
        encoded = null;
        encodeError = e.getMessage();
      }
    } else {
      encoded = null;
    }
  }

  /**
   * Returns a simple subclass of <tt>AccessibleContext</tt>.
   */
  public AccessibleContext getAccessibleContext() {
    return new JComponent.AccessibleJComponent() {
      public String getAccessibleName() {
        return "barcode " + code;
      }
      public AccessibleRole getAccessibleRole() {
        return AccessibleRole.LABEL;
      }
    };
  }

  /**
   * <p>
   * Encodes the barcode into an <tt>OutputStream</tt> as a GIF image.
   * <p>
   * <b>Note:</b> Borders and insets are not rendered, but if these properties are set
   * on the component, the size of the resulting GIF will be increased accordingly
   * with blank space around the barcode image the same color as the
   * {@link #getBarcodeBackground barcodeBackground} property.
   */
  public void gifEncode(OutputStream out) throws IOException {
    encode();
    recalculateSizes();
    Color oldBackground = getBackground();
    setBackground(barcodeBackground);
    BufferedImage bi = (BufferedImage)createImage(preferredSize.width, preferredSize.height);
    if (bi == null) {
      // We are running server-side or component has not yet been displayed,
      // so a different method is needed to create the buffered image.
      bi = new BufferedImage(preferredSize.width, preferredSize.height, BufferedImage.TYPE_INT_RGB);
    }
    Graphics2D g = bi.createGraphics();
    doPaint(g, new Dimension(bi.getWidth(), bi.getHeight()), new Insets(0,0,0,0));
    setBackground(oldBackground);
//    inventory.Acme.jpm.Encoders.GifEncoder encoder = new inventory.Acme.jpm.Encoders.GifEncoder(bi, out);
    GifEncoder encoder = new GifEncoder(bi, out);
    encoder.encode();
  }
}

