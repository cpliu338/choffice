package org.therismos.job;

import java.io.*;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.therismos.bean.ApplicationBean;
/**
 * Job submitted with a JSON POST to resource "jobs" with: 
 * {"type":"GenerateReceipts","end":"2021-03-31","batch":1}
 * @author cp_liu
 */
public class GenerateReceipts extends AbstractJob {

    final Logger LOG = Logger.getLogger(GenerateReceipts.class.getName());

    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public String getFilePattern() {return "Receipts_[^_]+_([0-9]+)\\.pdf";}

    /*
     * GenerateReceipts job: based on end date, get offers for offers for the past year
     * print receipts as a PDF file using iText and put in download path
     * Call result:
     * if success, the filename written to download path,
     * if exception, the document with exception class and message
    SQL used:
        select members.id,members.name,sum(offers.amount) as total from 
        offers inner join members on offers.member_id=members.id 
        where offers.receipt=1 and (offers.date1 between '2018-04-01' and '2019-03-31') 
        and members.groupname<>'special' 
        group by members.id order by members.id 
    */
    @Override
    public org.bson.Document call() throws Exception {
        try {
            endDate = LocalDate.parse(getConfig().getString("end"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            batch = getConfig().getInteger("batch");
            startDate = endDate.minusYears(1).plusDays(1);
            LOG.log(Level.INFO, "From{0} to {1} batch {2}", new Object[]{startDate, endDate, batch});
            File f = super.getDownloadPath();
            QueryRunner run = new QueryRunner(applicationBean.getDataSource());
            java.util.List<Record> results = run.query("select members.id,members.name,sum(offers.amount) as total from" +
                " offers inner join members on offers.member_id=members.id" +
                " where offers.receipt=? and (offers.date1 between ? and ?)" +
                " and members.groupname<>'special'" +
                " group by members.id order by members.id", new BeanListHandler<>(Record.class), 
                batch, java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)
            );
            init(f);
            for (Record record: results) {
                printRecord(record);
            }
            if (document != null) document.close();
            getConfig().append("download-path", f.getAbsolutePath());
        }
        catch (DocumentException | SQLException| RuntimeException | IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            getConfig().append("exception-class", ex.getClass().getName());
            getConfig().append("exception-message", ex.getMessage());
            return getConfig();
        }
        return getConfig();
    }
    
    private LocalDate startDate;
    private LocalDate endDate;
    private int batch;
    
    private Document document;
    private PdfWriter writer;
    private PdfContentByte cb;
    private BaseFont bf1;
    private BaseFont bf2;
    private BaseFont bfen;
    private BaseFont bfenb;
    private MessageFormat fmt1;
    private MessageFormat fmt2;
    private ResourceBundle bundle_en;
    private ResourceBundle bundle_zh;
    private float right;
    private float top;
    private float left;
    private boolean isOdd;
    
    private void init(File file) throws IOException, DocumentException  {
        isOdd = true;
        left = 36;
        bundle_en = ResourceBundle.getBundle("formatStrings", Locale.ENGLISH);
        bundle_zh = ResourceBundle.getBundle("formatStrings", Locale.CHINESE);
        fmt1 = new MessageFormat(bundle_zh.getObject("from_to").toString(), Locale.CHINESE);
        fmt2 = new MessageFormat(bundle_en.getObject("from_to").toString(), Locale.ENGLISH);
        document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 50, 50, 50, 50);
        right = document.getPageSize().getRight() - left;
        top = (document.getPageSize().getTop()-document.getPageSize().getBottom())*0.46f;
        writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        //addMetaData(document);
        document.addTitle("Receipts");
        document.addSubject("Receipts " + endDate.getYear());
        document.addKeywords("Java, PDF, iText");
        document.addAuthor("Therismos");
        document.addCreator("Therismos");
        
        document.open();
        bf1 = BaseFont.createFont("MSung-Light", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
        bf2 = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
        bfenb = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "latin1", BaseFont.NOT_EMBEDDED);
        bfen = BaseFont.createFont(BaseFont.HELVETICA, "latin1", BaseFont.NOT_EMBEDDED);
        cb = writer.getDirectContent();        
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
    
    private void printRecord(Record rec) {
        LOG.log(Level.INFO, rec.getName());
        float offset = document.getPageSize().getTop()/2;
        if (isOdd) {
            drawLines(0);
            drawLines(offset);
        }
        else {
            offset = 0;
        }
        String s1 = fmt1.format(new java.util.Date[]{java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)});
        String s2 = fmt2.format(new java.util.Date[]{java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)});
        float middle=document.getPageSize().getRight()*0.5f;
        cb.beginText();
        cb.setFontAndSize(bf2, 14);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("receipt"),
            document.getPageSize().getRight()*0.5f, top+offset-90, 0);
        cb.setFontAndSize(bf1, 12);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("church"),
                72, top+offset-20, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            rec.getName(),
            (middle-left)/2, top+offset-144, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("purpose"),
            left, 132+offset, 0);
        cb.setFontAndSize(bf1, 10);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("compliments"),
                right-108, 96+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            s1, document.getPageSize().getRight()*0.5f, top+offset-110, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("from"),
            left, top+offset-144, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("currency"),
            middle, top+offset-144, 0);
        cb.setFontAndSize(bf1, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            bundle_zh.getString("addr"),
                middle+72, top+offset-20, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_zh.getString("handledby"),
            right-108, 18+offset, 0);
        cb.setFontAndSize(bfenb, 14);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            bundle_en.getString("receipt"),
            document.getPageSize().getRight()*0.5f, top+offset-72, 0);
        cb.setFontAndSize(bfen, 12);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                bundle_en.getString("purpose"),
            left, 150+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                bundle_en.getString("church"),
                72, top+offset-40, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            String.format("%.0f",rec.getTotal()),
            middle-left+(middle+left)/2, top+offset-144, 0);
        cb.setFontAndSize(bfen, 10);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
                bundle_en.getString("compliments"),
                right-108, 108+offset, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                bundle_en.getString("from"),
            left, top+offset-156, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                bundle_en.getString("currency"),
            middle, top+offset-156, 0);
        cb.setFontAndSize(bfen, 8);
        cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
            s2, document.getPageSize().getRight()*0.5f, top+offset-120, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                bundle_en.getString("addr"),
                middle+72, top+offset-40, 0);
        cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
            String.format("Ref%d:%d:%d", startDate.getYear(), batch, rec.getId())
            , left, 18+offset, 0);
        cb.endText();
        if (!isOdd)
            cb.getPdfDocument().newPage();
        isOdd = !isOdd;
    }

    @Override
    public String getFilePrefix() {
        return String.format("Receipts_%d%02d", endDate.getYear(), batch);
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }
    
    public static class Record {

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the total
         */
        public double getTotal() {
            return total;
        }

        /**
         * @param total the total to set
         */
        public void setTotal(double total) {
            this.total = total;
        }
        private int id;
        private String name;
        private double total;
    }
    
}
