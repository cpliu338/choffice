

/**
 *  $Id: AbstractBarcodeStrategy.java,v 1.1.1.1 2004/01/15 15:55:59 dwalters Exp $ 
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
 * This abstract class, which implements the {@link BarcodeStrategy} interface,
 * provides a basic implementation that is subclassed by all the concrete
 * classes that provide Code39, Interleaved Code25, Codabar, MSI and all 
 * derivative encoding strategies.
 *
 * @version 1.0.2
 */
public abstract class AbstractBarcodeStrategy implements BarcodeStrategy {

  /**
   * Subclasses implement this method to return an array of
   * {@link AbstractBarcodeStrategy.CharacterCode CharacterCode}
   * objects, representing all possible encodings of bars and spaces for
   * every encodable character.
   *
   * @return An array of {@link AbstractBarcodeStrategy.CharacterCode CharacterCode}
   * objects, one for each possible character
   * that can be encoded using this strategy.
   */
  protected abstract CharacterCode[] getCodes();

  /**
   * Subclasses implement this method to calculate the checksum from the text
   * to encode, and return a String containing the text with the checksum
   * included.
   *
   * @param text The text to encode (after preprocessing - see
   * {@link AbstractBarcodeStrategy#preprocess}).
   *
   * @return A String containing the text passed into the method, augmented with
   * the checksum.  For barcode types that don't support a checksum, this would
   * simply be the text passed to the method.
   */
  protected abstract String augmentWithChecksum(String text) throws BarcodeException;

  /**
   * Subclasses implement this method to perform any preprocessing necessary on the
   * original text to encode.  The result of this method is the string that gets
   * passed to the {@link AbstractBarcodeStrategy#augmentWithChecksum} method.
   *
   * @param text The raw text to encode.
   *
   * @return The string after preprocessing. If no preprocessing is required,
   * the String passed to the method is returned.
   *
   * @throws BarcodeException Typically caused by passing in
   * a String containing illegal characters (characters that cannot be encoded in
   * this type of barcode).
   */
  protected abstract String preprocess(String text) throws BarcodeException;

  /**
   * Subclasses must implement this method to return <tt>true</tt> or <tt>false</tt>
   * depending on whether the barcode type is <i>interleaved</i>.
   *
   * @return <tt>true</tt> if barcode type is interleaved, <tt>false</tt> if it is not.
   */
  protected abstract boolean isInterleaved();

  /**
   * Subclasses implement this method to return the start sentinel character.
   *
   * @return The character, which when encoded into bars and spaces, appears on
   * the left edge of every barcode (immediately after the left margin).
   */
  protected abstract char getStartSentinel();

  /**
   * Subclasses implement this method to return the stop sentinel character.
   *
   * @return The character, which when encoded into bars and spaces, appears on
   * the right edge of every barcode (just before the right margin).
   */
  protected abstract char getStopSentinel();

  /**
   * Sublclasses implement this method to return the width of the whitespace
   * that must appear on each side of the barcode.
   *
   * @return The space that must appear on the left and right sides of the barcode
   * expressed as a multiple of the narrowest bar width.
   */
  protected abstract byte getMarginWidth();

  /**
   * Subclasses implement this method to return the text which appears
   * below the barcode.
   *
   * @param text The raw text to encode.
   *
   * @return A String containing the text that will appear beneath the barcode.
   */
  protected abstract String getBarcodeLabelText(String text);

  /**
   * Subclasses implement this method to perform any postprocessing required
   * to the text after including the checksum.
   *
   * @param text String to process (returned by
   * {@link AbstractBarcodeStrategy#augmentWithChecksum}).
   *
   * @return String after postprocessing.  If no postprocessing is required,
   * the String passed to the method is returned.
   */
  protected abstract String postprocess(String text);

  /**
   * This implementation carries out the following steps:
   * <ul>
   * <li>Call {@link AbstractBarcodeStrategy#preprocess}</li>
   * <li>Call {@link AbstractBarcodeStrategy#augmentWithChecksum} to add in the checksum</li>
   * <li>Call {@link AbstractBarcodeStrategy#postprocess}</li>
   * <li>Adds in the start and end sentinels</li>
   * <li>Use the CharacterCode array returned by getCodes to encode the text into a barcode</li>
   * <li>Insert left and right margins</li>
   * <li>Return EncodedBarcode object</li>
   * </ul>
   *
   * @param textToEncode The raw text to encode.
   * @param checked True if a checksum is to be calculated, False if not.
   *
   * @return The fully encoded barcode, represented as bars and spaces, wrapped
   * in a {@link EncodedBarcode} object.
   *
   * @throws BarcodeException Typically caused by passing in
   * a String containing illegal characters (characters that cannot be encoded in
   * this type of barcode).
   */
  public EncodedBarcode encode(String textToEncode, boolean checked) throws BarcodeException {

    String text;

    text = preprocess(textToEncode);
    text = checked ? augmentWithChecksum(text) : text;
    text = postprocess(text);

    // Simple to check to ensure start and end characters are not present in the
    // text to be encoded.
    if (((getStartSentinel() != 0xffff && text.indexOf(getStartSentinel()) >= 0)) ||
        (text.indexOf(getStopSentinel())) >= 0) {
      throw new BarcodeException("Invalid character in barcode");
    }

    if (getStartSentinel() != 0xffff) {
      text = getStartSentinel() + text;
    }

    text += getStopSentinel();

    int size = computeSize(text);
    BarcodeElement[] elements = new BarcodeElement[size];

    // Margin
    elements[0] = new BarcodeElement();
    elements[0].bar = false;
    elements[0].width = getMarginWidth();

    int len = text.length();
    int j = 1;
    for(int i = 0; i < len; i++) {
      char ch = text.charAt(i);
      CharacterCode cc = getCharacterCode(ch);
      if (cc == null) {
        throw new BarcodeException("Invalid character in barcode");
      }
      for (int k = 0; k < cc.widths.length; k++) {
        elements[j] = new BarcodeElement();
        elements[j].width = cc.widths[k];
        elements[j].bar = ((j % 2) == 0) ? false : true;
        if (isInterleaved() && ch != getStartSentinel() && ch != getStopSentinel()) {
          j += 2;
        } else {
          j++;
        }
      }
      if (isInterleaved() && ch != getStartSentinel() && ch != getStopSentinel()) {
        if (i % 2 == 1) {
          j -= (cc.widths.length * 2 - 1);
        } else {
          j -= 1;
        }
      }
    }

    elements[j] = new BarcodeElement();
    elements[j].bar = false;
    elements[j].width = getMarginWidth();
    j++;

    if (j != size) {
      throw new BarcodeException("Unexpected barcode size");
    }

    return new EncodedBarcode(elements, getBarcodeLabelText(textToEncode));
  }

  /**
   * Computes the length of the barcode (in bar/space modules) based on the
   * text to encode.
   *
   * @param text The text to encode including any check digit,
   * start and end sentinels.
   *
   * @return The number of module segments in this barcode (including margins).
   *
   * @throws BarcodeException Typically
   * occurs if attempting to encode invalid characters.
   */
  protected int computeSize(String text) throws BarcodeException {
    int size = 0;
    int l = text.length();
    for(int i = 0; i < l; i++) {
      char ch = text.charAt(i);
      CharacterCode cc = getCharacterCode(ch);
      if (cc == null) {
        throw new BarcodeException("Invalid character in barcode");
      }
      size = size + cc.widths.length;
    }
    size += 2;  // Margins
    return size;
  }

  /**
   * Looks for the specified character to encode in the CharacterCode array
   * returned by the {@link AbstractBarcodeStrategy#getCodes} method.
   *
   * @param character The character to encode.
   *
   * @return CharacterCode The element in the CharacterCode array (returned by
   * getCodes) that corresponds to the character passed to the method.
   */
  protected CharacterCode getCharacterCode(char character) {
    CharacterCode[] codes = getCodes();
    for (int i = 0; i < codes.length; i ++) {
      if (codes[i].character == character) {
        return codes[i];
      }
    }
    return null;
  }

  /**
   * Looks for an entry in the CharacterCode array
   * returned by the {@link AbstractBarcodeStrategy#getCodes} method,
   * by its <tt>check</tt> attribute.
   *
   * @param check The check attribute of the character being sought.
   *
   * @return CharacterCode The element in the CharacterCode array (returned by
   * getCodes) that corresponds to the character whose check attribute was passed
   * to the method.
   */
  protected CharacterCode getCharacterCode(int check) {
    CharacterCode[] codes = getCodes();
    for (int i = 0; i < codes.length; i ++) {
      if (codes[i].check == check) {
        return codes[i];
      }
    }
    return null;
  }

  /**
   * Inner class representing a character and its barcode encoding.
   */
  public static class CharacterCode {

    /** The character that is encoded */
    public char character;
    /** The widths of the modules (bars and spaces) of this encoded character */
    public byte[] widths;
    /** The check digit corresponding to this character, used in checksum calculations */
    public int check;

    /** Constructor which fully initializes the properties of the object. */
    public CharacterCode(char character, byte[] widths, int check) {
      this.character = character;
      this.widths = widths;
      this.check = check;
    }
  }
}



