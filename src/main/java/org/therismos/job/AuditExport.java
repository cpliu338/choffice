package org.therismos.job;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.poi.ss.usermodel.*;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * export entries to Auditor 
 * POST body: {"type":"AuditExport", "start":"yyyy-mm-dd", "end":"yyyy-mm-dd",}
 * @since Ver 7.0
 * @author cp_liu
 */
public class AuditExport extends AbstractXlsxJob {
    static final Logger LOG = Logger.getLogger(AuditExport.class.getName());
    final String START = "start";
    final String END = "end";
    final String RECONCILE = "reconcile";
    final String PENDING = "pending";
    LocalDate startDate, endDate;
    CellStyle centerTextStyle, textStyle, amountStyle, dateStyle; // from template to get style from

    public AuditExport(ApplicationBean srv, Document config) {
        super(srv, config);
        startDate = LocalDate.parse(config.getString(START), DateTimeFormatter.ISO_DATE);
        endDate = LocalDate.parse(config.getString(END), DateTimeFormatter.ISO_DATE);
    }
    
    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public static String getFilePattern() {return "Audit_[^_]+_([0-9]+)\\.xlsx";}
    
    @Override
    protected XSSFWorkbook buildExcel() throws Exception {
        if (startDate.isAfter(endDate))
            throw new IllegalArgumentException("start date must be before end date");
        super.buildExcel("Bank_Reconciliation");
        centerTextStyle = super.cloneStyle(2, 0);
        amountStyle = super.cloneStyle(2, 1);
        Row row = cloneRow(0, 0);
        Row srcRow = srcSheet.getRow(0);
        Cell cell;
        cell = this.cloneCell(srcRow.getCell(0), row, 0, false);
        cell.setCellValue(MessageFormat.format(srcRow.getCell(0).getStringCellValue(), endDate.format(DateTimeFormatter.ISO_DATE)));
        row = cloneRow(1, 1);
        srcRow = srcSheet.getRow(1);
        cloneCell(srcRow.getCell(0), row, 0, true);
        cloneCell(srcRow.getCell(1), row, 1, true);
        int rowno = 2;
        MongoCursor<Document> cursor = applicationBean.getCollection(RECONCILE, Document.class).find(
            Filters.lte(END, config.getString("end"))
        ).sort(Sorts.descending(END)).cursor();
        if (cursor.hasNext()) {
            Document record = cursor.next();
            List<Document> cheques = record.get(PENDING, List.class);
            if (cheques.isEmpty()) {
                row = cloneRow(2, rowno++);
                cloneCell(srcSheet.getRow(5).getCell(0), row, 0, true);                
            }
            else for (Document cheque: cheques) {
                row = cloneRow(2, rowno++);
                cell = row.createCell(0);
                cell.setCellValue(cheque.getString("extra1").replace("$", ""));
                cell.setCellStyle(centerTextStyle);
                cell = row.createCell(1);
                cell.setCellValue(cheque.get("amount", Number.class).doubleValue());
                cell.setCellStyle(amountStyle);
            }
        }
        else {
            row = cloneRow(2, rowno++);
            this.cloneCell(srcSheet.getRow(5).getCell(0), row, 0, true);
        }
        super.buildExcel("General_Ledger");
        textStyle = super.cloneStyle(1, 5);
        dateStyle = super.cloneStyle(1, 1);
        row = cloneRow(0, 0);
        srcRow = srcSheet.getRow(0);
        for (int i=0; i<6; i++) {
            cloneCell(srcRow.getCell(i), row, i, true);
        }
        rowno = 1;
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        List<Entry> a = run.query("select entries.transref, entries.date1, accounts.code, "
                    + "accounts.name_chi, entries.amount, entries.detail "
                    + "FROM entries INNER JOIN accounts ON entries.account_id = accounts.id "
                    + "WHERE entries.date1 BETWEEN ? AND ? ORDER BY code, date1, transref", 
                    new BeanListHandler<>(Entry.class), java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
        for (Entry entry : a) {
            row = cloneRow(1, rowno++);
            Cell ce = row.createCell(0);
            ce.setCellValue(entry.transref);
            ce.setCellStyle(centerTextStyle);
            ce = row.createCell(1);
            ce.setCellValue(entry.date1.toLocalDate());
            ce.setCellStyle(dateStyle);
            ce = row.createCell(2);
            ce.setCellValue(entry.code);
            ce.setCellStyle(centerTextStyle);
            ce = row.createCell(3);
            ce.setCellValue(entry.name_chi);
            ce.setCellStyle(centerTextStyle);
            ce = row.createCell(4);
            ce.setCellValue(entry.amount.doubleValue());
            ce.setCellStyle(amountStyle);
            ce = row.createCell(5);
            ce.setCellValue(entry.detail);
            ce.setCellStyle(textStyle);
        }
        return workbook;
    }

    @Override
    protected String getFilePrefix() {
        return String.format("Audit_%s", DateTimeFormatter.ISO_DATE.format(startDate));
    }
    
    public static class Entry {
        private String transref;
        private String code;
        private String name_chi;
        private String detail;
        private java.sql.Date date1;
        private Number amount;

        /**
         * @return the transref
         */
        public String getTransref() {
            return transref;
        }

        /**
         * @param transref the transref to set
         */
        public void setTransref(String transref) {
            this.transref = transref;
        }

        /**
         * @return the code
         */
        public String getCode() {
            return code;
        }

        /**
         * @param code the code to set
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * @return the name_chi
         */
        public String getName_chi() {
            return name_chi;
        }

        /**
         * @param name_chi the name_chi to set
         */
        public void setName_chi(String name_chi) {
            this.name_chi = name_chi;
        }

        /**
         * @return the detail
         */
        public String getDetail() {
            return detail;
        }

        /**
         * @param detail the detail to set
         */
        public void setDetail(String detail) {
            this.detail = detail;
        }

        /**
         * @return the date1
         */
        public java.sql.Date getDate1() {
            return date1;
        }

        /**
         * @param date1 the date1 to set
         */
        public void setDate1(java.sql.Date date1) {
            this.date1 = date1;
        }

        /**
         * @return the amount
         */
        public Number getAmount() {
            return amount;
        }

        /**
         * @param amount the amount to set
         */
        public void setAmount(Number amount) {
            this.amount = amount;
        }
    }
}
