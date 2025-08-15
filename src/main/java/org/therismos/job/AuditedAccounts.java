package org.therismos.job;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * Expected POST body
 * {
    "type":"AuditedAccounts",
    "year": 2024 // end date will be 12-31
    }
 * @since Ver 7.0
 * @author cp_liu
 */
public class AuditedAccounts extends AbstractXlsxJob {
    static final Logger LOG = Logger.getLogger(AuditedAccounts.class.getName());
    MongoCollection<Document> coll;
    private static final String YEAR = "year";
    private static final String ID = "_id";
    private final String COLUMNINDEX = "ABCDEFG";
    private static final String MANAGEMENT_ACCOUNTS = "managementAccounts";
    private static final String NAME = "name";
    private static final double CHURCHBUILDINGBAL = 6547.0;
    private final int FIRSTDETAILROW = 8; 
    private final int TYPICALROW = 9; // Row 9 is used for cloneRow(9, ..) to set column width
    ResourceBundle bundle;
    LocalDate endDate;
    LocalDate startDate;
    int year;
    final List<Cell> cellsToEval = new ArrayList<>();
    final List<String> fundsCodes = new ArrayList<>();
    final List<String> fundsNames = new ArrayList<>();
    final List<Integer> fundsIds = new ArrayList<>();
    final List<String> churchBuilding = List.of("32");
    final List<String> cash = List.of("122");
    CellStyle styleBoldText, 
            styleText, 
            styleEntry,  // number style
            styleUnderline, // amount with single underline at bottom
            styleSubtotal, // amount with single underline at top and bottom 
            styleTotal; // amount with double underline at bottom

    public AuditedAccounts(ApplicationBean srv, Document config) {
        super(srv, config);
        bundle = ResourceBundle.getBundle("auditedAccounts", Locale.CHINESE);
        coll = applicationBean.getCollection("auditUseAccounts", Document.class);
        Object o = config.get(YEAR);
        year = o==null ? 2024 :
                (o instanceof Integer ? (Integer)o : Integer.parseInt(o.toString()));
        endDate = LocalDate.of(year, 12, 31);
    }

    @Override
    public XSSFWorkbook buildExcel() throws Exception {
        if (year < 2010 || year > 2050)
            throw new IllegalArgumentException("Invalid year:" + year);
        startDate = LocalDate.of(year, 1, 1);
        config.put("end", endDate.format(DateTimeFormatter.ISO_DATE));
        try (MongoCursor<Document> cursor = coll.find(
            Filters.in(ID, "31", "32")).sort(Sorts.ascending(ID)).cursor()) {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                fundsCodes.add(d.getString(ID)) ; fundsNames.add(d.getString(NAME));
                fundsIds.add(d.getList(MANAGEMENT_ACCOUNTS, Integer.class).get(0));
            }
        }
        buildPageTop("auditedAccounts.AandLPage");
        styleBoldText = super.cloneStyle(8, 0);
        styleText = super.cloneStyle(9, 1);
        styleUnderline = super.cloneStyle(9, 4);
        styleEntry = super.cloneStyle(12, 4);
        styleSubtotal = super.cloneStyle(14, 4);
        styleTotal = super.cloneStyle(24, 4);
        buildOverview();
        buildPageTop("auditedAccounts.PandLPage");
        buildPandL();
        buildPageTop("auditedAccounts.fundsPage");
        buildFundsPage();
        evalFormulae();
        return workbook;
    }
    
    private void buildFundsPage() throws Exception {
        int row_no = FIRSTDETAILROW;
        row_no = printSection(row_no, "60", startDate, endDate, styleSubtotal);
        printSection(++row_no, "70", startDate, endDate, styleSubtotal);
    }
    
    private void buildPandL() throws Exception {
        int row_no = FIRSTDETAILROW;
        row_no = this.printSectionHead(row_no, "40");
        int row_profit = printSection(row_no, "40", startDate, endDate, styleSubtotal);
        row_no = this.printSectionHead(row_profit+2, "50");
        int row_50 = row_no+1;
        //int row_loss = 0; // to be determined below
        for (String subhead : getSubheads("50")) {
            int firstRow = row_no;
            row_no++;
            row_no = printSection(row_no, subhead, startDate, endDate, null);
            if (row_no > firstRow+1) {// retrospectively print sub head row
                Row row = sheet.createRow(firstRow);
                Cell cell = row.createCell(0);
                cell.setCellValue(coll.find(Filters.eq(ID, subhead)).first().getString(NAME));
                cell.setCellStyle(styleText);
            }
        }
        Row r = sheet.createRow(row_no-1);
        String fmla = String.format("SUM(A%d:A%d)", row_50, row_no-1);
        for (int col=4; col<=6; col++) {
            Cell c = r.createCell(col);
            c.setCellFormula(fmla.replaceAll("A", COLUMNINDEX.substring(col, col+1)));
            c.setCellStyle(styleSubtotal);
            this.cellsToEval.add(c);
        }
        fmla = String.format("A%d-A%d", row_profit, row_no);
        r = sheet.createRow(row_no + 1);
        Cell cell = r.createCell(0);
        cell.setCellValue(bundle.getString("auditedAccounts.earnings"));
        cell.setCellStyle(styleText);
        for (int col=4; col<=6; col++) {
            Cell c = r.createCell(col);
            c.setCellFormula(fmla.replaceAll("A", COLUMNINDEX.substring(col, col+1)));
            c.setCellStyle(styleTotal);
            this.cellsToEval.add(c);
        }        
    }
    
    /**
     * Fill in the first few rows up to row 7, duplicate Cells A1, A2, A3(with messageFormat for end date)
     * Also cells E6 - E8, F6 - F8
     * All 3 pages have the same format
     * Init srcSheet and sheet updated by calling super.buildExcel(String pageName)
     */
    private void buildPageTop(String pageName) throws Exception {
        Row row;
        super.buildExcel(bundle.getString(pageName));
        for (int i=0; i<2; i++) {
            row = cloneRow(i, i); 
            cloneCell(srcSheet.getRow(i).getCell(0), row, 0, true);
        }
        row = cloneRow(2, 2);
        Cell srcCell = srcSheet.getRow(2).getCell(0);
        Cell cell = cloneCell(srcCell, row, 0, false);
        cell.setCellValue(MessageFormat.format(srcCell.getStringCellValue(), endDate));
        LOG.log(Level.FINE, "Set cell {0}:{1} value to {2}", new Object[] {
                cell.getRowIndex(), cell.getColumnIndex(),
                MessageFormat.format(srcCell.getStringCellValue(), endDate)});
        for (int i=5; i<7; i++) {
            row = cloneRow(i, i); 
            Row srcRow = srcSheet.getRow(i);
            for (int j=4; j<7; j++) {
                cloneCell(srcRow.getCell(j), row, j, true);
            }
        }
    }
    
    public void buildOverview() throws Exception {
        int row_no = FIRSTDETAILROW;
        row_no = this.printSectionHead(row_no, "110");
        int row_fixedAssets = printSection(row_no, "110", startDate, endDate, styleSubtotal);
        row_no = this.printSectionHead(row_fixedAssets+1, "120");
        int row_currentAssets = printSection(row_no, "120", startDate, endDate, styleSubtotal);
        row_no = this.printSectionHead(row_currentAssets+1, "210");
        int row_currentDebt = printSection(row_no, "210", startDate, endDate, styleSubtotal);

        row_no = calcNet(bundle.getString("auditedAccounts.netLiquid"), this.styleUnderline, row_currentAssets, 0-row_currentDebt);

        row_no = this.printSectionHead(row_no+1, "220");
        int row_longDebt = printSection(row_no, "220", startDate, endDate, styleSubtotal);
        row_no = calcNet(bundle.getString("auditedAccounts.netAssets"), styleTotal,
                row_fixedAssets, row_currentAssets, 
                0-row_currentDebt, 0-row_longDebt); 
        row_no = printSectionHead(row_no+1, "30");
        printSection(row_no, "30", startDate, endDate, styleSubtotal);
    }

    protected int printSectionHead(int rowno, String code) {
        if (code == null || !code.endsWith("0"))
            throw new RuntimeException("Invalide code " + code);
        Document acc = coll.find(Filters.eq(ID, code)).first();
        if (acc== null)
            throw new RuntimeException("Invalide code " + code);
        return printBoldColumn1(rowno, acc.getString(NAME));
    }
    
    protected int printBoldColumn1(int rowno, String s) {
        Row row = cloneRow(TYPICALROW, rowno++);
        Cell cell = row.createCell(0);
        cell.setCellValue(s);
        cell.setCellStyle(styleBoldText);
        return rowno;
    }
    
    /**
     * Write a section of A & L under audit code e.g. 120 from start to end, using the query
     * db.auditUseAccounts.find({_id:/^12/, managementAccounts: {'$not':{$size:0}}})
     * @param row_no row no to start
     * @param audit_code _id in collection auditUseAccounts
     * @param start
     * @param end 
     * @param totalStyle to be used for the total row, if null, no total row needed
     * @return next row no
     */
    private int printSection(int row_no, String audit_code, LocalDate start, LocalDate end, CellStyle totalStyle) throws SQLException {
        if (row_no < 1) throw new RuntimeException("Invalid row no");
        if (audit_code.length()<2 || !audit_code.endsWith("0")) throw new RuntimeException("audit code must end with 0");
        if (start.isAfter(end)) throw new RuntimeException("start date must precede end date");
        List<Object[]> rows = audit_code.startsWith("6") || audit_code.startsWith("7")
            ? mockSectionRows(audit_code, start, end)
            : getSectionRows(audit_code, start, end);

            
        int rowno = row_no;
        CellStyle style = rows.size()==1 ? styleUnderline : styleEntry;
        for (Object[] r : rows) {
            double amount = (double)r[3];
            double amount2 = (double)r[4];
            if (Math.abs(amount) < 0.001) continue;
            Row row = sheet.createRow(rowno);
            Cell cell = row.createCell(1);
            cell.setCellValue(r[0].toString());
            cell.setCellStyle(styleText);
            cell = row.createCell(2);
            cell.setCellValue(r[1].toString());
            cell.setCellStyle(styleText);
            if (churchBuilding.contains(r[2].toString())) {
                Cell c = row.createCell(4);
                c.setCellValue(0.0);
                c.setCellStyle(style);
                c = row.createCell(5);
                c.setCellValue(CHURCHBUILDINGBAL);
                c.setCellStyle(style);
            }
            else if (cash.contains(r[2].toString())) {
                Cell c = row.createCell(4);
                c.setCellValue(amount-CHURCHBUILDINGBAL);
                c.setCellStyle(style);
                c = row.createCell(5);
                c.setCellValue(CHURCHBUILDINGBAL);
                c.setCellStyle(style);
            }
            else {
                Cell c = row.createCell(4);
                c.setCellValue(amount);
                c.setCellStyle(style);
                c = row.createCell(5);
                c.setCellValue(amount2);
                c.setCellStyle(style);
            }
            //cell.setCellStyle(style);
            cell = row.createCell(6);
            cell.setCellFormula(String.format("SUM(E%d:F%d)", rowno+1, rowno+1));
            cell.setCellStyle(style);
            this.cellsToEval.add(cell);
            rowno++;
        }
        if (totalStyle == null) return rowno;
        if (rowno > row_no+1) {
            // print sum row
            Row row = sheet.createRow(rowno);
            for (int col=4; col<=6; col++) {
                Cell cell = row.createCell(col);
                cell.setCellFormula(String.format("SUM(%s%d:%s%d)", 
                        COLUMNINDEX.substring(col, col+1), row_no+1, COLUMNINDEX.substring(col, col+1), rowno));
                cell.setCellStyle(totalStyle);
                cellsToEval.add(cell);
            }            
            rowno++;
        }
        return rowno;
    }
    
    private void evalFormulae() {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();  
        for (Cell cell : this.cellsToEval) {
            evaluator.evaluateFormulaCell(cell);
        }
    }
    
    private List<Object[]> mockSectionRows(String audit_code, LocalDate start, LocalDate end) throws SQLException {
        List<Object[]> results = new ArrayList<>();
        LocalDate startOfLastYear = start.minusYears(1L);
        LocalDate endOfLastYear = end.minusYears(1L);
        switch (audit_code) {
            case "60": 
                LOG.log(Level.FINE, "funds id: {0}, {1}", new Object[]{fundsIds.get(0), fundsIds.get(1)});
                results.add(
                    new Object[]{
                        MessageFormat.format(bundle.getString("auditedAccounts.balanceAsOn"), DateTimeFormatter.ISO_DATE.format(start.minusYears(1L))),
                        "", "", // placeholder
                        sumFromManagementAccounts(startOfLastYear, endOfLastYear, List.of(fundsIds.get(0)), "31"),
                        sumFromManagementAccounts(startOfLastYear, endOfLastYear, List.of(fundsIds.get(1)), "32"),
                });
                results.add(
                    new Object[]{
                        MessageFormat.format(bundle.getString("auditedAccounts.earnings"), Integer.toString(start.minusYears(1L).getYear())),
                        "", "", // placeholder
                        findEarnings(startOfLastYear, endOfLastYear),
                        0.0
                });
                break;
            case "70": 
                results.add(
                    new Object[]{
                        MessageFormat.format(bundle.getString("auditedAccounts.balanceAsOn"), DateTimeFormatter.ISO_DATE.format(start.minusDays(1L))),
                        "", "", // placeholder
                        sumFromManagementAccounts(start, end, List.of(fundsIds.get(0)), audit_code),
                        sumFromManagementAccounts(start, end, List.of(fundsIds.get(1)), audit_code),
                });
                results.add(
                    new Object[]{
                        MessageFormat.format(bundle.getString("auditedAccounts.earnings"), Integer.toString(start.getYear())),
                        "", "", // placeholder
                        findEarnings(start, end),
                        0.0
                });
                break;
        }
        return results;
    }
    
    private double findEarnings(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT SUM(AMOUNT) FROM entries "
                + "WHERE (account_id LIKE ? OR account_id LIKE ?) AND date1 BETWEEN ? AND ?";
                
        return new QueryRunner(applicationBean.getDataSource()).query(sql, new ScalarHandler<Number>(),
                "4%", "5%", java.sql.Date.valueOf(start), java.sql.Date.valueOf(end)).doubleValue();
    }

    private List<Object[]> getSectionRows(String audit_code, LocalDate start, LocalDate end) {
        Spliterator<Document> spliterator = coll.find(Filters.and(Filters.regex(ID, "^"+audit_code.substring(0, audit_code.length()-1)),
                Filters.not(Filters.size(MANAGEMENT_ACCOUNTS, 0))
        )).sort(Sorts.ascending(ID)).spliterator();
        return StreamSupport.stream(spliterator, false)
                .map((Document ac) -> {
                    try {
                        List<Integer> list = ac.getList(MANAGEMENT_ACCOUNTS, Integer.class);
                        return new Object[]{
                            ac.getString(NAME), ac.getString("detail"),
                            /*StreamSupport.stream(list.spliterator(),false)
                            .map((Integer i)->{return Integer.toString(i);})
                            .collect(Collectors.joining(",")),*/
                            ac.getString(ID),
                            sumFromManagementAccounts(start, end, list, audit_code), 0.0
                        };
                    } catch (SQLException ex) {
                        Logger.getLogger(AuditedAccounts.class.getName()).log(Level.SEVERE, null, ex);
                        return new Object[]{ac.getString(NAME), ac.getString("detail"),
                            ac.getString(ID),-99.99};
                    }
                })
                .collect(Collectors.toList());
//        return rows;
    }
    
    /**
     * 
     * SELECT SUM(0-amount) FROM entries WHERE date1 BETWEEN '2023-01-01' AND '2023-12-31' AND account_id IN (11101,11201)
     * @param start
     * @param end
     * @param account_ids 
     * @param audit_code need to invert for 1XX, 3XX because increase by DEBIT
     */
    private double sumFromManagementAccounts(LocalDate start, LocalDate end, List<Integer> account_ids, String audit_code) throws SQLException {
        String template = "SELECT SUM(%s) FROM entries WHERE date1 BETWEEN ? AND ? AND account_id %s";
        String sql;
        switch (account_ids.size()) {
            case 0: throw new RuntimeException("No management Accounts for code " + audit_code);
            case 1:
                sql = String.format(template, 
                        audit_code.startsWith("1") || audit_code.startsWith("5") ? "0-amount" : "amount", "=?");
                break;
            default:
                sql = String.format(template, 
                        audit_code.startsWith("1") || audit_code.startsWith("5") ? "0-amount" : "amount", " IN (" +
                    String.join(",", "?".repeat(account_ids.size()).split("")) + ")"
                );
        }
        try (Connection conn = applicationBean.getDataSource().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            stmt.setDate(index++, java.sql.Date.valueOf(start));
            stmt.setDate(index++, java.sql.Date.valueOf(end));
            for (int id: account_ids) {
                stmt.setInt(index++, id);
            }
            ResultSet result = stmt.executeQuery();
            if (result.first()) {
                return result.getDouble(1);
            }
            else {
                return -88.88;
            }
        }
    }
    
    /**
     * Use a list of row_offsets to build formula
     * formula +row_currentAssets, -row_currentDebt, ...
     * @param col1
     * @param row_offset
     * @param style
     * @throws IllegalStateException
     * @throws FormulaParseException 
     * @return the next row no to use
     */
    private int calcNet(String col1, CellStyle style, int ... row_offset) throws IllegalStateException, FormulaParseException {
        StringBuilder formula = new StringBuilder();
        // row at last row + 1
        int rowno = Math.abs(row_offset[row_offset.length-1]) + 1;
        Row row = sheet.createRow(rowno);
        Cell cell = row.createCell(0);
        cell.setCellValue(col1);
        cell.setCellStyle(styleBoldText);
        for (int i=0; i<row_offset.length; i++) {
            formula.append(row_offset[i] > 0 ? "+A" : "-A").append(Math.abs(row_offset[i]));
        }
        String cellFormula = formula.toString();
        FormulaEvaluator eval1 = workbook.getCreationHelper().createFormulaEvaluator();
        for (int i=4; i<=6; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(style);
            String fmla = cellFormula.replaceAll("A", COLUMNINDEX.substring(i, i+1));
            cell.setCellFormula(fmla);
            cellsToEval.add(cell);
        }
        eval1.clearAllCachedResultValues();
        return rowno+1;
    }
    
    private List<String> getSubheads(String head) {
        if (!head.endsWith("0")) throw new RuntimeException("Invalid head code " + head);
        Spliterator<Document> spliterator = coll.find(
                Filters.regex(ID, "^" + head.substring(0, head.length()-1) + "[1-9A-Za-z]0$")
        ).spliterator();
        return StreamSupport.stream(spliterator, false).map((acc)-> { return acc.getString(ID);})
                .collect(Collectors.toList());
    }    
    
    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public static String getFilePattern() {return "AuditedAccounts_[^_]+_([0-9]+)\\.xlsx";}

    @Override
    public String getFilePrefix() {
        return String.format("AuditedAccounts_%s", DateTimeFormatter.ISO_DATE.format(endDate));
    }
    
}
