package org.therismos.job;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;


/**
 * Offer weekly report. Based on a template file WeeklyReportTemplate.xlsx
 * @since Ver 7.0
 * @author cp_liu
 */
public class WeeklyReport extends AbstractXlsxJob {
    
    static final Logger LOG = Logger.getLogger(WeeklyReport.class.getName());

    
    public WeeklyReport(ApplicationBean srv, Document config) {
        super(srv, config);
        reportDate = LocalDate.parse(config.getString("reportDate"), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public static String getFilePattern() {return "Offer_[^_]+_([0-9]+)\\.xlsx";}

    /**
     * This is not used?
     * File description to be used for display in Rest client
     * @param name appearing in the file system
     * @return File description to be used for display
     */
    @Deprecated
    public static String getFileDesc(String name) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    /*
    These variables are used in call()
    */
    //ResourceBundle bundle_zh;
    LocalDate reportDate;
    DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    protected String getFilePrefix() {
        return String.format("Offer_%s", reportDate.format(yyyyMMdd));
    }

    CellStyle accountNameStyle, subtotalStyle, nameStyle, amountStyle; // from template to get style from

    @Override
    public XSSFWorkbook buildExcel() throws Exception {
        /*workbook =*/ super.buildExcel();
        accountNameStyle = workbook.createCellStyle();
        accountNameStyle.cloneStyleFrom(srcSheet.getRow(2).getCell(0).getCellStyle());
        subtotalStyle = workbook.createCellStyle();
        subtotalStyle.cloneStyleFrom(srcSheet.getRow(2).getCell(1).getCellStyle());
        nameStyle = workbook.createCellStyle();
        nameStyle.cloneStyleFrom(srcSheet.getRow(3).getCell(0).getCellStyle());                
        amountStyle = workbook.createCellStyle();
        amountStyle.cloneStyleFrom(srcSheet.getRow(3).getCell(1).getCellStyle());
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
            List<Offer> offers = run.query("select m.name, o.account_id, a.name_chi, o.amount from "
                    + " offers o inner join members m on o.member_id=m.id inner join accounts a on o.account_id=a.id "
                    + "where date1=? order by o.account_id, m.name", 
                new BeanListHandler<>(Offer.class),
                java.sql.Date.valueOf(reportDate));
            config.append("offers-count", offers.size());
        // Cell[0,0] is title
        Row row = cloneRow(0, 0);
        sheet.addMergedRegion(
            new CellRangeAddress(0, 0, 0, 4) // row1, row2, col1, col2
        );
        cloneCell(srcSheet.getRow(0).getCell(0), row, 0, true);
        // Cell[1,0] is report date
        row = cloneRow(1, 1);
        Cell cell = cloneCell(srcSheet.getRow(1).getCell(0), row, 0, false);
        cell.setCellValue(reportDate);
        sheet.addMergedRegion(
            new CellRangeAddress(1, 1, 0, 4) // row1, row2, col1, col2
        );
        fillOffers(offers);
        return workbook;
    }
    
    private void fillOffers(List<Offer> offers) {
        int init_row_no = 1; // initial
        int accum_row = init_row_no; // row to write accumulated offers
        int last_account = 0; // impossible
        int total_size = offers.size();
        int row_no = init_row_no;
        double accum = 0.0;
        int columnOffset = 0; // 2-column layout, for the left, add 3 for the right column
        for (Offer offer : offers) {
            if (offer.getAccount_id() != last_account) {
                LOG.log(Level.FINEST, "row offset was {0}", row_no);
                // come to new account
                if (detailRow(accum_row).getCell(columnOffset) != null) {
                    // account name has been written, now write the accumulated subtotal
                    Cell total = detailRow(accum_row).createCell(columnOffset + 1);
                    total.setCellStyle(subtotalStyle);
                    total.setCellValue(accum);
                }
                if (columnOffset == 0 && (row_no-init_row_no) >= 0.4 * total_size) {
                    columnOffset = 3; // move to right column
                    row_no = init_row_no;
                }
                row_no+=2;
                last_account = offer.getAccount_id();
                accum = 0.0;
                accum_row = row_no;
                Cell accountName = detailRow(accum_row).createCell(columnOffset);
                accountName.setCellStyle(accountNameStyle);
                accountName.setCellValue(offer.getName_chi());
            }
            row_no++; // start from row after accum_row
            Row row = detailRow(row_no);
            Cell name = row.createCell(columnOffset);
            name.setCellStyle(nameStyle);
            name.setCellValue(offer.getName());
            Cell amt = row.createCell(columnOffset+1);
            amt.setCellStyle(amountStyle);
            amt.setCellValue(offer.getAmount());
            accum += offer.getAmount();
        }
        // finish up for the remaining subtotal
        if (detailRow(accum_row).getCell(columnOffset) != null) {
            // account name has been written, now write the accumulated subtotal
            Cell total = detailRow(accum_row).createCell(columnOffset + 1);
            total.setCellStyle(subtotalStyle);
            total.setCellValue(accum);
        }        
    }

    /**
     * Get / create a detail row at row_no
     * @param row_no
     * @return 
     */
    private Row detailRow(int row_no) {
        Row row = sheet.getRow(row_no);
        if (row == null) { 
            row = sheet.createRow(row_no);
            row.setHeight(srcSheet.getRow(3).getHeight());
        }
        return row;
    }
    
    public static class Offer {

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
         * @return the amount
         */
        public double getAmount() {
            return amount;
        }

        /**
         * @param amount the amount to set
         */
        public void setAmount(double amount) {
            this.amount = amount;
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
         * @return the account_id
         */
        public int getAccount_id() {
            return account_id;
        }

        /**
         * @param account_id the account_id to set
         */
        public void setAccount_id(int account_id) {
            this.account_id = account_id;
        }
        private String name;
        private double amount;
        private String name_chi;
        private int account_id;
    }
}
