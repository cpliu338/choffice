package org.therismos.job;
import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * Monthly P&L and Balance Sheet report. Based on a template file MonthlyReportTemplate.xlsx
 * @since Ver 7.0
 * @author cp_liu
 */
public class MonthlyReport extends AbstractXlsxJob {
    
    static final Logger LOG = Logger.getLogger(MonthlyReport.class.getName());
    ResourceBundle bundle_zh;
    LocalDate endDate;
    private Map<String, BigDecimal> grandtotal;
    private Map<String, BigDecimal> total;
    CellStyle accountNameStyle, totalStyle, amountStyle, // from template to get style from
            sur_def_Style; // surplus or deficit name 
    
    public MonthlyReport(ApplicationBean srv, Document config) {
        super(srv, config);
        endDate = LocalDate.parse(config.getString("end"), DateTimeFormatter.ISO_DATE);
        bundle_zh = ResourceBundle.getBundle("monthlyReportLegend", Locale.CHINESE);
    }

    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public static String getFilePattern() {return "PandL_[^_]+_([0-9]+)\\.xlsx";}

    @Override
    protected String getFilePrefix() {
        return String.format("PandL_%s", DateTimeFormatter.ofPattern("yyyyMM").format(endDate));
    }

    public BigDecimal reckon(String code, LocalDate cutoff) throws java.sql.SQLException {
        LocalDate yearstart = cutoff.withMonth(1).withDayOfMonth(1);
        config.append("start", yearstart);
        String code2 = code;
        if (code.endsWith("0")) {
            code2 = code.substring(0, code.length()-1).concat("%");
        }
        String qry = "SELECT SUM(e.amount) from entries e WHERE " +
                "e.date1 <= ? AND e.date1>=? AND " +
                "e.account_id IN (SELECT id FROM accounts WHERE code LIKE ?)";
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        BigDecimal sum = run.query(qry, new ScalarHandler<>(),
                java.sql.Date.valueOf(cutoff),
                java.sql.Date.valueOf(yearstart),
                code2);
        if (sum==null) sum = BigDecimal.ZERO;
        config.append("Code"+code2, sum);
        return sum;
    }
    
    /**
     * Get code/summary code two levels below, e.g. for a top level summary code
     * @param int must be 1/2/3/4/5, nothing else 
     * @return e.g. 5 will give 536, 537, 5410 ...
     * @throws java.sql.SQLException 
     */
    private List<String> getAccountsBelow(int digit) throws java.sql.SQLException {
        String summaryCode = Integer.toString(digit);
        String qry = "SELECT code from accounts a WHERE "
                + String.format("(a.code LIKE '%s__' OR a.code LIKE '%s__0') ", summaryCode, summaryCode)
                + "AND a.code NOT LIKE '__0' ORDER BY a.code";
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        return run.query(qry, new ColumnListHandler<>("code"));
    }

    private boolean incByCR(String subtype) {
        return subtype.startsWith("4") ||
                subtype.startsWith("2") || subtype.startsWith("3");
    }
    
    @Override
    protected XSSFWorkbook buildExcel() throws Exception {
        total = new HashMap<>();
        grandtotal = new HashMap<>();
        sumAccounts();
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        super.buildExcel("P and L");
        int totalRowNo = 40; // this value valid for Sheet P and L of the template
        // following styles good for both sheets
            accountNameStyle = workbook.createCellStyle();
            accountNameStyle.cloneStyleFrom(srcSheet.getRow(6).getCell(0).getCellStyle());
            amountStyle = workbook.createCellStyle();
            amountStyle.cloneStyleFrom(srcSheet.getRow(6).getCell(1).getCellStyle());
            totalStyle = workbook.createCellStyle();
            totalStyle.cloneStyleFrom(srcSheet.getRow(totalRowNo).getCell(1).getCellStyle());
            sur_def_Style = workbook.createCellStyle();
            sur_def_Style.cloneStyleFrom(srcSheet.getRow(totalRowNo-1).getCell(1).getCellStyle());                
        buildPandL(run);
        super.buildExcel("Balance Sheet");
        buildBalanceSheet(run);
        return workbook;
    }
    
    public void buildPandL(QueryRunner run) {
        try {
            List<String> account_codes;
            account_codes = new ArrayList<>();
            account_codes.addAll(getAccountsBelow(4));
            account_codes.addAll(getAccountsBelow(5));
            fillDetailRows(account_codes, run, 1);
        }
        catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param account_codes
     * @param run
     * @param page1 for P&L, 2 for Bal Sheet
     * @throws FormulaParseException
     * @throws IllegalStateException
     * @throws SQLException 
     */
    private void fillDetailRows(List<String> account_codes, QueryRunner run, int page) throws FormulaParseException, IllegalStateException, SQLException {
        cloneHeaderRows(bundle_zh.getString(page==1 ? "format.cut-off-date" : "format.cut-off-date2"));
        double delta = 0.00001; // cater for imprecision
        int totalRowNo = page==1 ? 40 : 21;
        int first_detail_row = 6;
        int row_no = first_detail_row;
        Row row; Cell cell;
        for (String account_code : account_codes) {
            BigDecimal t = total.get(account_code);
            double amount = t.doubleValue();
            if (Math.abs(amount) < delta)
                continue;
            row = detailRow(row_no++);
            Cell n = row.createCell(0);
            n.setCellStyle(accountNameStyle);
            List<String> list = findNameChi(run, account_code);
            n.setCellValue(// account_code +
                    (list.isEmpty() ? "???" : list.get(0)));
            if (amount >= delta) {
                Cell n1 = row.createCell(1);
                n1.setCellStyle(amountStyle);
                Cell n2 = row.createCell(2);
                n2.setCellValue(amount);
                n2.setCellStyle(amountStyle);
            }
            else {
                Cell n1 = row.createCell(1);
                n1.setCellValue(0-amount);
                n1.setCellStyle(amountStyle);
                Cell n2 = row.createCell(2);
                n2.setCellStyle(amountStyle);
            }
        }
        row = cloneRow(totalRowNo-1, row_no++);
        double surplus = (grandtotal.get("5").subtract(grandtotal.get("4"))).doubleValue();
        Cell cell0 = cloneCell(srcSheet.getRow(totalRowNo-1).getCell(0), row, 0, false);
        cell0.setCellValue(bundle_zh.getString(surplus<delta ? "legend.surplus" : "legend.deficit"));
        Cell cell1 = row.createCell(1);
        Cell cell2 = cloneCell(srcSheet.getRow(totalRowNo-1).getCell(2), row, 2, false);
        if (page == 1) {
            if (surplus < delta) {
                cell1.setCellValue(0 - surplus);
                cell1.setCellStyle(amountStyle);
            }
            else {
                cell2.setCellValue(surplus);
                cell2.setCellStyle(amountStyle);
            }
        }
        else {
            if (surplus < delta) {
                cell2.setCellValue(0 - surplus);
                cell2.setCellStyle(amountStyle);
            }
            else {
                cell1.setCellValue(surplus);
                cell1.setCellStyle(amountStyle);
            }
        }
        row = cloneRow(totalRowNo, row_no++);
        cell = cloneCell(srcSheet.getRow(totalRowNo).getCell(1), row, 1, false);
        cell.setCellStyle(totalStyle);
        cell.setCellFormula(String.format("SUM(B%d:B%d)", first_detail_row+1, row_no-1));
        cell = cloneCell(srcSheet.getRow(totalRowNo).getCell(2), row, 2, false);
        cell.setCellStyle(totalStyle);
        cell.setCellFormula(String.format("SUM(C%d:C%d)", first_detail_row+1, row_no-1));
    }

    private void cloneHeaderRows(String format) {
        Row row = cloneRow(0, 0);
        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 2) // row1, row2, col1, col2
        );
        cloneCell(srcSheet.getRow(0).getCell(0), row, 0, true);
        // row = cloneRow(1, 1);  row offset 1 is empty
        row = cloneRow(2, 2);
        Cell cell = cloneCell(srcSheet.getRow(2).getCell(0), row, 0, false);        
        cell.setCellValue(
        MessageFormat.format(format, java.sql.Date.valueOf(endDate)) 
        );
        sheet.addMergedRegion(
                new CellRangeAddress(2, 2, 0, 2) // row1, row2, col1, col2
        );
        
        row = cloneRow(4, 4);
        cloneCell(srcSheet.getRow(4).getCell(0), row, 0, true);
        cloneCell(srcSheet.getRow(4).getCell(1), row, 1, true);
        cloneCell(srcSheet.getRow(4).getCell(2), row, 2, true);
        row = cloneRow(5, 5);
        cloneCell(srcSheet.getRow(5).getCell(1), row, 1, true);
        cloneCell(srcSheet.getRow(5).getCell(2), row, 2, true);
    }

    private void sumAccounts() throws SQLException {
        for (int i=1; i<=5; i++) {
            String subtype = Integer.toString(i);
            grandtotal.put(subtype, BigDecimal.ZERO);
            for(String code: this.getAccountsBelow(i)) {
                total.put(code, reckon(code, endDate));
                if (incByCR(subtype))
                    grandtotal.put(subtype, grandtotal.getOrDefault(subtype, BigDecimal.ZERO)
                            .add(total.getOrDefault(code, BigDecimal.ZERO)));
                else
                    grandtotal.put(subtype, grandtotal.getOrDefault(subtype, BigDecimal.ZERO)
                            .subtract(total.getOrDefault(code, BigDecimal.ZERO)));
            }
        }
    }
    
    /**
     * Get / create a detail row at row_no, assumed srcSheet.getRow(6) gives correct height
     * @param row_no
     * @return 
     */
    private Row detailRow(int row_no) {
        Row row = sheet.getRow(row_no);
        if (row == null) { 
            row = sheet.createRow(row_no);
            row.setHeight(srcSheet.getRow(6).getHeight());
        }
        return row;
    }
    
    private List<String> findNameChi(QueryRunner run, String account_code) throws SQLException {
        return run.query("Select name_chi from accounts where code=?",
                new ColumnListHandler<>("name_chi"),
                account_code
        );
    }
    
    private void buildBalanceSheet(QueryRunner run) {
        try {
            List<String> account_codes;
            account_codes = new ArrayList<>();
            account_codes.addAll(getAccountsBelow(1));
            account_codes.addAll(getAccountsBelow(2));
            account_codes.addAll(getAccountsBelow(3));
            fillDetailRows(account_codes, run, 2);
        }
        catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
}
