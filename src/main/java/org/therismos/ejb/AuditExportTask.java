package org.therismos.ejb;
import com.mongodb.DBObject;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.poi.ss.usermodel.*;
import com.mongodb.BasicDBList;
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
        styleText.setBorderLeft(CellStyle.BORDER_NONE);
        styleText.setBorderBottom(CellStyle.BORDER_NONE);
        styleText.setBorderTop(CellStyle.BORDER_NONE);
        styleText.setBorderRight(CellStyle.BORDER_NONE);
        styleEntry.setBorderLeft(CellStyle.BORDER_NONE);
        styleEntry.setBorderBottom(CellStyle.BORDER_NONE);
        styleEntry.setBorderTop(CellStyle.BORDER_NONE);
        styleEntry.setBorderRight(CellStyle.BORDER_NONE);
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
        DBObject cheques = mongoService.getCheques(endDate);
        if (cheques == null) return;
        BasicDBList pending = (BasicDBList)cheques.get("pending");
        logger.log(Level.INFO, "reconciliation id: {0}", cheques.get("_id"));
        sheet = workbook.createSheet("Bank Reconciliation");
        int rowno = 0;
        Iterator it = pending.iterator();
        while (it.hasNext()) {
            Row row = sheet.createRow(rowno++);
            DBObject o = (DBObject)it.next();
            logger.log(Level.INFO, "Cheque: {0}", o.toString());
            Cell cell = row.createCell(0);
            cell.setCellValue(o.get("extra1").toString());
            cell = row.createCell(1);
            cell.setCellValue(o.get("amount").toString());
        }
        
    }


    protected void exportSheet(Integer year, Date start, Date end) {
        int rowno = 0;
        message = "Exporting year "+year;
        logger.info(message);
        sheet = workbook.createSheet(year.toString());
        sheet.setColumnWidth(1, 200);
        sheet.setColumnWidth(2, 320);
        sheet.setColumnWidth(3, 320);
        sheet.setColumnWidth(4, 600);
        sheet.setColumnWidth(5, 800);
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
