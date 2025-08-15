package org.therismos.job;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * Annual payroll report by month and employee.  Expected POST body:
 * {
    "type":"PayrollReport",
    "key_personnel": ["5112"],
    "startDate": "2022-01-01"
    }
 * @Since Ver 7.0
 * @author cp_liu
 */
public class PayrollReport extends AbstractXlsxJob {
    static final Logger LOG = Logger.getLogger(PayrollReport.class.getName());
    LocalDate startDate;
    List<YearMonth> yearMonth;
    final List<String> remarks;
    DateTimeFormatter yyyyMM;
    ResourceBundle bundle;
    FormulaEvaluator evaluator;
    CellStyle nameStyle, accountStyle, amountStyle, diffAmountStyle; // from template to get style from
        
    final String sqlSalary = "SELECT a.code,a.name_chi AS name,e.date1,e.detail,0-e.amount AS amt "
            + "FROM entries e INNER JOIN accounts a ON e.account_id=a.id "
            + "WHERE a.code LIKE '511%' AND a.code not in ('5110','511X') AND e.date1 "
            + "BETWEEN ? AND ? ORDER BY a.code, e.date1";
    final String sqlByCode = "SELECT e.date1,e.detail,0-e.amount AS amt "
            + "FROM entries e INNER JOIN accounts a ON e.account_id=a.id "
            + "WHERE a.code=? and e.date1 BETWEEN ? AND ? ORDER BY e.date1";

    public PayrollReport(ApplicationBean srv, Document config) {
        super(srv, config);
        startDate = LocalDate.parse(config.getString("startDate"), DateTimeFormatter.ISO_DATE);
        yearMonth = new ArrayList<>();
        remarks = new ArrayList<>();
        yyyyMM = DateTimeFormatter.ofPattern("yyyy-MM");
    }

    /**
     * File pattern to identify this type of downloadable file, must be present
     * for jobs producing downloadables
     * @return regex string, one single matcher group representing the timestamp in ms
     */
    public static String getFilePattern() {return "PayrollReport_[^_]+_([0-9]+)\\.xlsx";}

    @Override
    protected String getFilePrefix() {
        return String.format("PayrollReport_%s", DateTimeFormatter.ofPattern("yyyyMM").format(startDate));
    }

    @Override
    protected XSSFWorkbook buildExcel() throws Exception {
        super.buildExcel("Payroll Report");
        evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        LocalDate ld = startDate;
        LocalDate endDate = startDate.plusYears(1).minusDays(1);
        for (int i = 0; i < 12; i++) {
            yearMonth.add(YearMonth.from(ld));
            ld = ld.plusMonths(1);
        }
        bundle = ResourceBundle.getBundle("payrollReport", Locale.CHINESE);
        if (startDate.getDayOfMonth() != 1) {
            remarks.add(MessageFormat.format(bundle.getString("payroll.odd_startDate"), 
                    startDate.format(DateTimeFormatter.ISO_DATE))
            );
        }
        config.append("year_months", this.yearMonth);
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        Document salaries = this.processResults(run.query(sqlSalary, new ArrayListHandler(),
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)),
                bundle); 
        Document mpf = new Document();/*
        Map<String, Double> mpf_total = new HashMap<>();
        for (YearMonth ym: this.yearMonth) {
            mpf_total.put(ym.toString(), 0.0);
        }*/
        for (Map.Entry<String, Object> e:salaries.entrySet()) {
            Map<String,Double> map = (Map)e.getValue();
            Map<String, Double> calc = new HashMap<>();
            for (Map.Entry<String, Double> e2: map.entrySet()) {
                Double calced = calcEmployerContrib(e2.getValue());
                calc.put(e2.getKey(), calced);
            //    mpf_total.put(e2.getKey(), calced + mpf_total.get(e2.getKey()));
            }
            mpf.put(e.getKey(), calc);
        }
        Map<String, Double> mpf_queried = new HashMap<>();
        for (Object[] row :  run.query(sqlByCode, new ArrayListHandler(),
                "5121",
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate))) {
            java.sql.Date date1 = (java.sql.Date)row[0];
            String key = YearMonth.from(date1.toLocalDate()).toString();
            Number amt = (Number)row[2];
            mpf_queried.put(key, mpf_queried.getOrDefault(key, 0.0) + amt.doubleValue());
        }
        config.append("mpf_queried", mpf_queried);
        config.append("mpf", mpf);
        //config.append("mpf_total", mpf_total);
        config.append("remarks", remarks);
        nameStyle = workbook.createCellStyle();
        nameStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(2).getCellStyle()); // from cell C5
        accountStyle = workbook.createCellStyle();
        accountStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(3).getCellStyle()); // from cell D5
        amountStyle = workbook.createCellStyle();
        amountStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(5).getCellStyle()); // from cell F5
        diffAmountStyle = workbook.createCellStyle();
        diffAmountStyle.cloneStyleFrom(srcSheet.getRow(31).getCell(5).getCellStyle());
        int row_no = 1;
        Row row = cloneRow(1, row_no++);  // Row for months
        Cell cellYM = srcSheet.getRow(1).getCell(5); // Jan-24
        for (int i=0; i<12; i++) {
            Cell cell = cloneCell(cellYM, row, 5+i, false);
            cell.setCellValue(LocalDate.of (yearMonth.get(i).getYear(), yearMonth.get(i).getMonth(), 1));
        } 
        cloneCell(srcSheet.getRow(1).getCell(18), row, 18, true); // for label Total for employee
        //row = cloneRow(2, row_no++);  // Row for gross salaries row
        //cloneCell(srcSheet.getRow(2).getCell(0), row, 0, true);// Cell A3 is section header
        this.cloneColumn1(2, row_no, 0, false);
        List<String> key_personnel = config.getList("key_personnel", String.class);
        List<String> non_key_personnel = config.getList("non_key_personnel", String.class);
        Map<String, String> staff_names = config.get("staff_names", Map.class);
        int row_no1 = row_no; // the first row of this section
        int row_no_total = 15;
        //String total_in_chinese = srcSheet.getRow(row_no_total).getCell(0).getStringCellValue(); // the row with "Total" in the first column
        Row totalRow = srcSheet.getRow(row_no_total);
        if (!key_personnel.isEmpty()){
            row_no = printSection(row_no, 3, key_personnel, staff_names, salaries) + 1; // +1 to skip one line
        }
        if (!non_key_personnel.isEmpty()){
            row_no = printSection(row_no, 7, non_key_personnel, staff_names, salaries) + 1;
        }
        if (row_no > row_no1+2) {
            row_no = printTotalRow(row_no_total, row_no, totalRow, row_no1);
        }
        //LOG.log(Level.INFO, config.toJson(applicationBean.getPojoCodecRegistry().get(Document.class)));
        row_no++;
        row_no1 = row_no;
        // MPF now
        //row = cloneRow(17, row_no++);  
        row = this.cloneColumn1(17, row_no++, 0, false);// Row for mpf row
        cloneCell(srcSheet.getRow(17).getCell(0), row, 0, true);
        if (!key_personnel.isEmpty()){
            row_no = printSection(row_no, 3, key_personnel, staff_names, mpf) + 1; // +1 to skip one line
        }
        if (!non_key_personnel.isEmpty()){
            row_no = printSection(row_no, 7, non_key_personnel, staff_names, mpf) + 1;
        }
        if (row_no > row_no1+2) {
            row_no = printTotalRow(row_no_total, row_no, totalRow, row_no1);
        }
        Row srcRow = this.cloneColumn1(31, row_no, 0, true);
        Row prevRow = sheet.getRow(row_no - 1);
        row = sheet.getRow(row_no++);
        //row_no++;
        for (int i = 0; i<12; i++) {
            Cell ce = row.createCell(5+i);//cloneCell(srcRow.getCell(5), row, 5+i, false);
            String key = yearMonth.get(i).format(yyyyMM);
            Double s = mpf_queried.getOrDefault(key, 0.0);
            double to_check = prevRow.getCell(5+i).getNumericCellValue();
            LOG.log(Level.INFO, "column {0}, {1} vs {2}", new Object[] {5+i, s, to_check});
            ce.setCellValue(s);
            ce.setCellStyle(Math.abs(s-to_check) < 0.01 ? amountStyle : diffAmountStyle) ;
        }
        Cell c = cloneCell(srcRow.getCell(18), row, 18, false); // row total
        c.setCellFormula(String.format(
        "IF(S%d=SUM(F$row:Q$row),\"Per ledger\",SUM(F$row:Q$row))".replaceAll("\\$row", String.valueOf(row_no)),
                row_no-1));
        evaluator.evaluateFormulaCell(c);
        row_no++;
        List<Object[]> trainings = run.query(sqlByCode, new ArrayListHandler(),
                    "5122",
                    java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate));
        if (!trainings.isEmpty()) {
            this.cloneColumn1(33, row_no++, 0, false);
            for (Object[] training : trainings) {
                Row r = sheet.createRow(row_no++);
                Cell ce = r.createCell(2);
                ce.setCellValue(training[1].toString());
                ce.setCellStyle(nameStyle);
                for (int i=0; i<12; i++) {
                    ce = r.createCell(5+i);
                    ce.setCellValue(0.0);
                    ce.setCellStyle(amountStyle);
                }
                java.sql.Date date1 = (java.sql.Date)training[0];
                int delta_month = Period.between(startDate,date1.toLocalDate()).getMonths();
                Number amount = (Number)training[2];
                r.getCell(5 + delta_month).setCellValue(amount.doubleValue());
            }
        }
        row_no++;
        if (!remarks.isEmpty()) {
            this.cloneColumn1(38, row_no++, 0, true);
            for (String rem : remarks) {
                Row r = sheet.createRow(row_no++);
                Cell ce = r.createCell(1);
                ce.setCellStyle(nameStyle);
                ce.setCellValue(rem);
            }
        }
        return workbook;
    }

    private int printTotalRow(int row_no_total, int row_no, Row totalRow, int row_no1) throws FormulaParseException, IllegalStateException {
        //Row row;
        /* some rows written
        row = cloneRow(row_no_total, row_no);
        Cell cell = row.createCell(0);
        cell.setCellValue(total_in_chinese);
        cell.setCellStyle(nameStyle);*/
        Row row = cloneColumn1(row_no_total, row_no, 0, false);
        for (int i = 0; i<12; i++) {
            Cell ce = this.cloneCell(totalRow.getCell(5), row, 5+i, false);
            char[] ca = new char[1]; ca[0]=(char)('F' + i); String column = new String(ca);
            ce.setCellFormula(String.format("SUM(%s%d:%s%d)", column, row_no1+2, column, row_no-1));
            evaluator.evaluateFormulaCell(ce);
        }
        Cell totalCell = this.cloneCell(totalRow.getCell(18), row, 18, false);
        totalCell.setCellFormula(String.format("SUM(F%d:Q%d)", row_no+1, row_no+1));
        row_no++;
        return row_no;
    }
    
    /**
     * Clone srcRowNo from srcSheet and the row header
     * @param srcRowNo the target row offset in srcSheet
     * @param row_no the target row offset in sheet
     * @param col_header_offset the column offset of the row header
     * @param want_src_row what row in srcSheet or sheet?
     * @return the row depending on want_src_row
     */
    private Row cloneColumn1(int srcRowNo, int row_no, int col_header_offset, boolean want_src_row) {
        Row row = cloneRow(srcRowNo, row_no);  // Row for per_ledger row
        Row srcRow = srcSheet.getRow(srcRowNo);
        cloneCell(srcRow.getCell(col_header_offset), row, col_header_offset, true);
        return want_src_row ? srcRow : row;
    }

    private int printSection(int row_no, int row_no_section_head, List<String> personnel, Map<String, String> staff_names, Document salaries) {
        cloneColumn1(row_no_section_head, row_no++, 0, false);
/*        row = cloneRow(row_no_section_head, row_no++);  // Row for section head
        cloneCell(srcSheet.getRow(row_no_section_head).getCell(0), row, 0, true);// clone cell for personnel header
*/
        Row row;
        for (String code : personnel) {
            row = cloneRow(row_no_section_head+1, row_no);  // template detail row for personnel salary
            Cell c = row.createCell(2);
            c.setCellValue(staff_names.getOrDefault(code, "???"));
            c.setCellStyle(nameStyle);
            c = row.createCell(3);
            c.setCellValue(MessageFormat.format(bundle.getString("payroll.account_code"), code));
            c.setCellStyle(accountStyle);
            Map<String, Double> sal = salaries.get(code, Map.class);
            for (int i = 0; i<12; i++) {
                Cell ce = row.createCell(5+i);
                String key = yearMonth.get(i).format(yyyyMM);
                Double s = sal.getOrDefault(key, 0.0);
                ce.setCellValue(s);
                ce.setCellStyle(amountStyle);
            }
            c = row.createCell(18); // row total
            c.setCellFormula(String.format("SUM(F%d:Q%d)", row_no+1, row_no+1));
            c.setCellStyle(amountStyle);
            evaluator.evaluateFormulaCell(c);
            row_no++;
        }
        return row_no;
    }

    /**
     * Want Result { 5111:
     * {'2022-01':12345.00, '2022-02':12345.00, ..., '2022-12':12567},
     * 5112: {'2022-01':12345.00, '2022-02':12345.00, ...,
     * '2022-12':12567}, } 
     * Side effect: append to remarks for non unique payments
     * Side effect: set config for keys staff_names, non_key_personnel
     * @param results columns: code, name, date1, detail, amt and ordered by code, date1
     * @param rb Resource bundle
     * @return above result
     */
    private Document processResults(List<Object[]> results, ResourceBundle rb) {
        Document doc = new Document();
        final List<String> codes = new ArrayList<>();
        codes.add("0");
        final List<String> key_personnel = config.getList("key_personnel", String.class);
        final List<String> non_key_personnel = new ArrayList<>();
        final List<Double> payments = new ArrayList<>();
        final List<YearMonth> yms = new ArrayList<>();
        final Map<String, String> staffNames =  new HashMap<>();
        final Map<String, Double> monthTotals = new HashMap<>();
        yms.add(YearMonth.from(LocalDate.now().plusYears(10)));
        results.forEach((Object[] t) -> {
            YearMonth ym = YearMonth.from(((java.sql.Date)t[2]).toLocalDate());
            Number amt = (Number) t[4];
            String code = (String) t[0];
            if (codes.get(0).equals(code) && yms.get(0).equals(ym)) {
                payments.add(amt.doubleValue());
            } 
            else {
                if (!key_personnel.contains(code) && !non_key_personnel.contains(code)) {
                    non_key_personnel.add(code);
                }
                if (!yms.get(0).equals(ym)) {
                    double monthTotal = lumped_payments(payments, rb, code, yms.get(0));
                    monthTotals.put(yms.get(0).toString(), monthTotal);
                    yms.clear(); yms.add(ym);
                    payments.clear();
                }
                if (!codes.get(0).equals(code)) { // new code found
                    // different code, inherent different month too
                    if (!"0".equals(codes.get(0))) {
                        Map<String, Double> l = new HashMap(monthTotals);
                        doc.append(codes.get(0), l);
                    }
                    staffNames.put(code, deriveStaffName(t[1]));
                    monthTotals.clear();
                    yms.clear(); yms.add(ym);
                    codes.clear(); codes.add(code);
                }
                payments.add(amt.doubleValue());
            }
        });
        // clear out what is left
        if (!"0".equals(codes.get(0))) {
            monthTotals.put(yms.get(0).toString(),lumped_payments(payments, rb, codes.get(0), yms.get(0)));
            Map<String, Double> l = new HashMap(monthTotals);
            doc.append(codes.get(0), l);
        }
        config.append("staff_names", staffNames);
        config.put("non_key_personnel", non_key_personnel);
        return doc;
    }

    private double lumped_payments(final List<Double> payments, ResourceBundle rb, String code, YearMonth ym) {
        double monthTotal;
        if (payments.isEmpty()) {
            monthTotal = 0.0;
        }
        else if (payments.size() > 1) {
            monthTotal = payments.stream().reduce(0.0d, (a, b) -> a + b);
            String pattern = rb.getString("payroll.non_unique");
            remarks.add(MessageFormat.format(pattern, ym.toString(), code, payments.size()));
        }
        else {
            monthTotal = payments.get(0);
        }
        return monthTotal;
    }
    
    private String deriveStaffName(Object t) {
        String an = (String)t;
        return (an.lastIndexOf('-')>1) ? an.substring(an.lastIndexOf('-')+1) : an;
    }

    private double calcEmployerContrib(double salary) {
        if (salary <= 0.1) return 0.0;
        double x = salary * 0.05;
        return x >= 1500.00 ? 1500.00 : x;
    }
    
}