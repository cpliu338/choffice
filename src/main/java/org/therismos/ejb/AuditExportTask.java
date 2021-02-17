package org.therismos.ejb;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.poi.ss.usermodel.*;
import org.bson.Document;
import org.therismos.entity.*;

/**
 *
 * @author cpliu
 */
public class AuditExportTask extends AbstractXlsxTask implements Runnable {

    public static final long serialVersionUID = 890562243537L;
    static final Logger logger = Logger.getLogger(AuditExportTask.class.getName());
    protected EntryEjb entryEjb;
    protected MongoService mongoService;

    public AuditExportTask() {
        super();
        styleText.setBorderLeft(BorderStyle.NONE);
        styleText.setBorderBottom(BorderStyle.NONE);
        styleText.setBorderTop(BorderStyle.NONE);
        styleText.setBorderRight(BorderStyle.NONE);
        styleEntry.setBorderLeft(BorderStyle.NONE);
        styleEntry.setBorderBottom(BorderStyle.NONE);
        styleEntry.setBorderTop(BorderStyle.NONE);
        styleEntry.setBorderRight(BorderStyle.NONE);
    }
    
    public void setMongoService(MongoService mongoService) {
        this.mongoService = mongoService;
    }
    
    public void setEntryEjb(EntryEjb entryEjb) {
        this.entryEjb = entryEjb;
    }

    public static File timestampedWorkbook() {
        return new File(new File(tmpfolder), String.format("Aud%s.xlsx",fmt.format(new java.util.Date())));
    }
    
    private Integer finYear;

    public void setFinYear(int finYear) {
        this.finYear = finYear;
    }
    
    /**
     * @return the cutoffDate
     */
    public Date getCutoffDate() {
        return cutoffDate;
    }
    
    protected void uncheques(Date end) {
        //db.reconcile.find({end:'2016-03-31'}).sort({_id:-1}).limit(1)
        String endDate = AbstractXlsxTask.fmtYmd.format(end);
        logger.log(Level.INFO, "Uncheque entries as at: {0}", endDate);
        Document cheques = mongoService.getCheques(endDate);
        if (cheques == null) return;
        List<Document> pending = cheques.getList("pending", Document.class);
        logger.log(Level.INFO, "reconciliation id: {0}", cheques.get("_id"));
        sheet = workbook.createSheet("Bank Reconciliation");
        int rowno = 0;
        Iterator<Document> it = pending.iterator();
        while (it.hasNext()) {
            Row row = sheet.createRow(rowno++);
            Document o = it.next();
            logger.log(Level.INFO, "Cheque: {0}", o.toString());
            Cell cell = row.createCell(0);
            cell.setCellValue(o.getString("extra1"));
            cell = row.createCell(1);
            cell.setCellValue(o.getString("amount"));
        }
        
    }


    protected void exportSheet(Integer year, Date start, Date end) {
        int rowno = 0;
        message = "Exporting year "+year;
        logger.info(message);
        sheet = workbook.createSheet(year.toString());
        sheet.setColumnWidth(0, 256*8); // trans
        sheet.setColumnWidth(1, 256*11); // date
        sheet.setColumnWidth(2, 256*6); //code
        sheet.setColumnWidth(3, 256*16); // account
        sheet.setColumnWidth(4, 256*12); // amount
        sheet.setColumnWidth(5, 256*16); // detail
        Row row = sheet.createRow(rowno++);
        Cell cell = row.createCell(0);
        cell.setCellValue("Ref");
        cell.setCellStyle(styleText);
        cell = row.createCell(1);
        cell.setCellValue("Date");
        cell.setCellStyle(styleText);
        cell = row.createCell(2);
        cell.setCellValue("Code");
        cell.setCellStyle(styleText);
        cell = row.createCell(3);
        cell.setCellValue("Account");
        cell.setCellStyle(styleText);
        cell = row.createCell(4);
        cell.setCellValue("Amount");
        cell.setCellStyle(styleText);
        cell = row.createCell(5);
        cell.setCellValue("Detail");
        cell.setCellStyle(styleText);
        for (Entry e:entryEjb.getEntries(start, end)) {
            row = sheet.createRow(rowno++);
            cell = row.createCell(0);
            cell.setCellValue(e.getTransref());
            cell.setCellStyle(styleText);
            cell = row.createCell(1);
            cell.setCellValue(e.getDate1());
            cell.setCellStyle(styleDate);
            cell = row.createCell(2);
            cell.setCellValue(e.getAccount().getCode());
            cell.setCellStyle(styleEntry);
            cell = row.createCell(3);
            cell.setCellValue(e.getAccount().getNameChi());
            cell.setCellStyle(styleText);
            cell = row.createCell(4);
            cell.setCellValue(e.getAmount().doubleValue());
            cell.setCellStyle(styleEntry);
            cell = row.createCell(5);
            cell.setCellValue(e.getDetail());
            cell.setCellStyle(styleText);
        }
    }
    
    @Override
    public void run() {
        try {
            /* cutoffDate is the end date, e.g. finYear is 2015, cutoffDate is 2016-04-30
            One sheet for 2015-01-01 to 2015-12-31 is generated,
            Another sheet for 2016-04-30 is also generated.
            finYear is always <= cutoffDate.getYear (with century)
            if finYear==cutoffDate.getYear, entries are exported till 12-31 irrespectively
            */
            // fmt = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
            Date startDate = fmt.parse(finYear.toString()+"0101_000000");
            Date endDate = fmt.parse(finYear.toString()+"1231_000000");
            this.exportSheet(
                finYear,
                startDate, endDate
            );
            this.uncheques(endDate);
            int endYear = cutoffDate.getYear()+1900;
            for (Integer y=finYear+1; y<=endYear; y++) {
                Date e = fmt.parse(y.toString()+"1231_000000");
                if (y==endYear) {
                    e = cutoffDate;
                }
                this.exportSheet(y, 
                    fmt.parse(y.toString()+"0101_000000"), e);
            }
            FileOutputStream fileOut;
            fileOut = new FileOutputStream(AuditExportTask.timestampedWorkbook());
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ex) {
            message = ex.getClass().getName() +" : "+ex.getMessage();
            logger.log(Level.SEVERE, null, ex);
        }
        message = "Run finished";
    }
}
