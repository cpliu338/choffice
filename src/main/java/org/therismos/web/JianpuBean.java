package org.therismos.web;

import javax.faces.bean.ManagedBean;

/**
 *
 * @author cp_liu
 */
@ManagedBean
@javax.faces.bean.ViewScoped
public class JianpuBean implements java.io.Serializable {
    private String measure;
    private String melody;
    private String phrase;

    public JianpuBean() {
        measure = "1 22";
        melody = "1 2 3";
        phrase = "Start";
    }
    
    public void convert() {
        String[] notes = melody.split("[ ,]+");
        StringBuilder buf = new StringBuilder();
        int j = 0;
        for (int i=0; i<measure.length(); i++) {
            char ch = measure.charAt(i);
            if (notes.length>j && Character.isDigit(ch)) {
                String note = notes[j++];
                for (int p=0; p<note.length(); p++) {
                    switch (note.charAt(p)) {
                        case '^': buf.append('\u0307'); break;
                        case 'v': buf.append('\u0323'); break;
                        case '#': buf.append('\u266f'); break;
                        case 'b': buf.append('\u266d'); break;
                        default: buf.append(note.charAt(p));
                    }
                }
                if (ch=='2')
                    buf.append('\u0332');
                else if (ch=='4')
                    buf.append('\u0333');
            }
            else
                buf.append(ch);
        }
        // append a bar line, no such thing as '\u1d100' for greater than ffff
        buf.appendCodePoint(0x1d100); 
        phrase = buf.toString();
    }
    /**
     * @return the melody
     */
    public String getMelody() {
        return melody;
    }

    /**
     * @param melody the melody to set
     */
    public void setMelody(String melody) {
        this.melody = melody;
    }

    /**
     * @return the phrase
     */
    public String getPhrase() {
        return phrase;
    }

    /**
     * @param phrase the phrase to set
     */
    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    /**
     * @return the measure
     */
    public String getMeasure() {
        return measure;
    }

    /**
     * @param measure the measure to set
     */
    public void setMeasure(String measure) {
        this.measure = measure;
    }
}
