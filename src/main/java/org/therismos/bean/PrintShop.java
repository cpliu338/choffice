package org.therismos.bean;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public class PrintShop implements Runnable {

    private void addMetaData(Document document) {
            document.addTitle("Receipts");
            document.addSubject("Receipts 2011");
            document.addKeywords("Java, PDF, iText");
            document.addAuthor("Therismos");
            document.addCreator("Therismos");
    }
    
    private Document document;
    private PdfWriter writer;
    private PdfContentByte cb;
    private BaseFont bf1;
    private BaseFont bf2;
    private BaseFont bfen;
    private BaseFont bfenb;
    private MessageFormat fmt1;
    private MessageFormat fmt2;
    private String church;
    private ResourceBundle bundle_en;
    private ResourceBundle bundle_zh;
    private float right;
    private float top;
    private float left;
    private boolean isOdd;
    private float bottom;

    public void init(File file) throws java.io.IOException, DocumentException {
        isOdd = true;
        left = 36;
        bundle_en = ResourceBundle.getBundle("formatStrings", Locale.ENGLISH);
        bundle_zh = ResourceBundle.getBundle("formatStrings", Locale.CHINESE);
        fmt1 = new MessageFormat(bundle_zh.getObject("from_to").toString(), Locale.CHINESE);
        fmt2 = new MessageFormat(bundle_en.getObject("from_to").toString(), Locale.ENGLISH);
        document = new Document(com.itextpdf.text.PageSize.A4, 50, 50, 50, 50);
        right = document.getPageSize().getRight() - 36;
        top = (document.getPageSize().getTop()-document.getPageSize().getBottom())*0.46f;
//        Logger.getLogger(PrintShop.class.getName()).log(Level.INFO,
//            "{0,number},{1,number},{2,number},{3,number}", new Float[] {
//            document.getPageSize().getLeft(),
//            document.getPageSize().getTop(),
//            document.getPageSize().getRight(),
//            document.getPageSize().getBottom()}
//        );
        writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        addMetaData(document);
        document.open();
        bf1 = BaseFont.createFont("MSung-Light", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
        bf2 = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
        bfenb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "latin1", BaseFont.NOT_EMBEDDED);
        bfen = BaseFont.createFont(BaseFont.HELVETICA, "latin1", BaseFont.NOT_EMBEDDED);
        cb = writer.getDirectContent();
    }

    public void cleanup() {
        if (null != document)
        document.close();
    }

    public static void main(String[] args) {
        new Thread(new PrintShop()).start();
    }

    @Override
    public void run() {
        char separator = File.separatorChar;
        String text = JOptionPane.showInputDialog("Text="+separator);
        for (String fontname : this.getAllFonts()) {
            System.out.println(fontname+"\n");
        }
        File Dir = new File(System.getProperty("user.home"),
                ('/' == separator) ? "Documents" : "My Documents");
        System.out.println(Dir.getAbsolutePath());
    }

    private void drawLines(float offset) {
        cb.saveState();
        cb.setLineWidth(1.0f);
        cb.moveTo(right-216, 36+offset);
        cb.lineTo(right, 36+offset);
        cb.stroke();
        cb.setLineWidth(2.0f);
        cb.moveTo(left, top+offset);
        cb.lineTo(right, top+offset);
        cb.stroke();
        cb.moveTo(left, top+offset-48);
        cb.lineTo(right, top+offset-48);
        cb.stroke();
        cb.restoreState();
    }

    public void printRecord(Record rec) {
        float offset = document.getPageSize().getTop()/2;
        if (isOdd) {
            drawLines(0);
            drawLines(offset);
        }
        else {
            offset = 0;
        }
        String s1= fmt1.format(new java.util.Date[]{rec.getStartDate(), rec.getEndDate()});
        String s2 = fmt2.format(new java.util.Date[]{rec.getStartDate(), rec.getEndDate()});
        float middle=document.getPageSize().getRight()*0.5f;
        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("receipt").toString(),
            document.getPageSize().getRight()*0.5f, top+offset-90, 0);
        cb.setFontAndSize(bf1, 12);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("church").toString(),
                72, top+offset-20, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            rec.getName(),
            (middle-left)/2, top+offset-144, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("purpose").toString(),
            left, 132+offset, 0);
        cb.setFontAndSize(bf1, 10);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("compliments").toString(),
                right-108, 96+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            s1, document.getPageSize().getRight()*0.5f, top+offset-110, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("from").toString(),
            left, top+offset-144, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("currency").toString(),
            middle, top+offset-144, 0);
        cb.setFontAndSize(bf1, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("addr").toString(),
                middle+72, top+offset-20, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("handledby").toString(),
            right-108, 18+offset, 0);
        cb.setFontAndSize(bfenb, 14);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_en.getString("receipt").toString(),
            document.getPageSize().getRight()*0.5f, top+offset-72, 0);
        cb.setFontAndSize(bfen, 12);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_en.getString("purpose").toString(),
            left, 150+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_en.getString("church").toString(),
                72, top+offset-40, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            String.format("%.0f",rec.getAmount()),
            middle-left+(middle+left)/2, top+offset-144, 0);
        cb.setFontAndSize(bfen, 10);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_en.getString("compliments").toString(),
                right-108, 108+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_en.getString("from").toString(),
            left, top+offset-156, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_en.getString("currency").toString(),
            middle, top+offset-156, 0);
        cb.setFontAndSize(bfen, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            s2, document.getPageSize().getRight()*0.5f, top+offset-120, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_en.getString("addr").toString(),
                middle+72, top+offset-40, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            rec.getIdString(), left, 18+offset, 0);
        cb.endText();
        if (!isOdd)
            cb.getPdfDocument().newPage();
        isOdd = !isOdd;
    }

    private ArrayList<String> getAllFonts() {
	// Determine which fonts support Chinese here ...
        ArrayList<String> chinesefonts = new ArrayList();
	java.awt.Font[] allfonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	int fontcount = 0;
	String chinesesample = "\u4e00";
	for (int j = 0; j < allfonts.length; j++) {
	    if (allfonts[j].canDisplayUpTo(chinesesample) == -1) {
	        chinesefonts.add(allfonts[j].getFontName());
	    }
  	    fontcount++;
	}
        return chinesefonts;
    }
}
