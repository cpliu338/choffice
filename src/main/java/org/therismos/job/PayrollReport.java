package org.therismos.job;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
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
    ResourceBundle bundle;
    CellStyle nameStyle, accountStyle, amountStyle; // from template to get style from
        
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
        LocalDate ld = startDate;
        LocalDate endDate = startDate.plusYears(1).minusDays(1);
        for (int i = 0; i < 12; i++) {
            yearMonth.add(YearMonth.from(ld));
            ld = ld.plusMonths(1);
        }
        bundle = ResourceBundle.getBundle("payrollReport", Locale.CHINESE);
        config.append("year_months", this.yearMonth);
        QueryRunner run = new QueryRunner(applicationBean.getDataSource());
        Document salaries = this.processResults(run.query(sqlSalary, new ArrayListHandler(),
                java.sql.Date.valueOf(startDate), java.sql.Date.valueOf(endDate)),
                bundle); 
        Document mpf = new Document();
        Map<String, Double> mpf_total = new HashMap<>();
        for (YearMonth ym: this.yearMonth) {
            mpf_total.put(ym.toString(), 0.0);
        }
        for (Map.Entry<String, Object> e:salaries.entrySet()) {
            Map<String,Double> map = (Map)e.getValue();
            Map<String, Double> calc = new HashMap<>();
            for (Map.Entry<String, Double> e2: map.entrySet()) {
                Double calced = calcEmployerContrib(e2.getValue());
                calc.put(e2.getKey(), calced);
                mpf_total.put(e2.getKey(), calced + mpf_total.get(e2.getKey()));
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
        config.append("mpf_total", mpf_total);
        config.append("remarks", remarks);
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();  
        nameStyle = workbook.createCellStyle();
        nameStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(2).getCellStyle()); // from cell C5
        accountStyle = workbook.createCellStyle();
        accountStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(3).getCellStyle()); // from cell D5
        amountStyle = workbook.createCellStyle();
        amountStyle.cloneStyleFrom(srcSheet.getRow(4).getCell(5).getCellStyle()); // from cell F5
        int row_no = 1;
        Row row = cloneRow(1, row_no++);  // Row for months
        Cell cellYM = srcSheet.getRow(1).getCell(5); // Jan-24
        for (int i=0; i<12; i++) {
            Cell cell = cloneCell(cellYM, row, 5+i, false);
            cell.setCellValue(LocalDate.of (yearMonth.get(i).getYear(), yearMonth.get(i).getMonth(), 1));
        } 
        cloneCell(srcSheet.getRow(1).getCell(18), row, 18, true); // for label Total for employee
        row = cloneRow(2, row_no++);  // Row for gross salaries row
        cloneCell(srcSheet.getRow(2).getCell(0), row, 0, true);// Cell A3 is section header
        List<String> key_personnel = config.getList("key_personnel", String.class);
        Map<String, String> staff_names = config.get("staff_names", Map.class);
        DateTimeFormatter yyyyMM = DateTimeFormatter.ofPattern("yyyy-MM");
        if (!key_personnel.isEmpty()){
            row = cloneRow(3, row_no++);  // Row for key_personnel row
            cloneCell(srcSheet.getRow(3).getCell(0), row, 0, true);// Cell A4 is key_personnel header#
            for (String code : key_personnel) {
                row = cloneRow(4, row_no++);  // template detail row for key_personnel salary
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
            }
        }
        //LOG.log(Level.INFO, config.toJson(applicationBean.getPojoCodecRegistry().get(Document.class)));
        return workbook;
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