package org.therismos.job;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.therismos.bean.ApplicationBean;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.therismos.bean.FilesResource;
import org.therismos.web.JobDownloadServlet;

/**
 *
 * @author cp_liu
 */
public class JobTest {
    
    public JobTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        config = new Document();
    }
    
    @After
    public void tearDown() {
    }
    
    Document config;
    AbstractXlsxJob instance;

    /**
     * Test of getFilePattern method, of class WeeklyReport.
    @Test
     */
    public void testPrintReceipt() throws Exception {
        System.out.println("test PrintReceipt");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("end", "2021-03-31");
        config.append("batch", 1);
        GenerateReceipts gr = AbstractJob.createJob("GenerateReceipts", appBean, config, GenerateReceipts.class);
        System.out.println(gr.call().toJson());
    }
    
    public void testMonthlyReckon() throws Exception {
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("end", "2024-12-31");
        MonthlyReport monthlyReport = AbstractJob.createJob("MonthlyReport", appBean, config, MonthlyReport.class);
        BigDecimal sum = monthlyReport.reckon("510", LocalDate.parse("2024-07-31"));
        System.out.print(sum);
        assert(sum.add(new BigDecimal("754964.79")).compareTo(BigDecimal.ZERO) == 0);
        assert(sum.compareTo(new BigDecimal("-754964.79")) == 0);
    }
    
    public void testMonthlyReport() throws Exception {
        System.out.println("test Monthly Report");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("end", "2024-12-31");
        instance = AbstractJob.createJob("MonthlyReport", appBean, config, MonthlyReport.class);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
    public void testAuditedAccounts() throws Exception{
        System.out.println("test Audited Accounts");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("year", 2024);
        instance = AbstractJob.createJob("AuditedAccounts", appBean, config, AuditedAccounts.class);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
    public void testAuditExport() throws Exception {
        System.out.println("test Audit Export");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("start", "2024-01-01");
        config.append("end", "2024-12-31");
        instance = AbstractJob.createJob("AuditExport", appBean, config, AuditExport.class);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
    public void testPayrollReport() throws Exception {
        System.out.println("test Payroll Report");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("startDate", "2024-01-01");
        java.util.List<String> a = java.util.Arrays.asList( "5112");
        config.append("key_personnel", a);
        instance = AbstractJob.createJob("PayrollReport", appBean, config, PayrollReport.class);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
    /**
     * Test of getFileDesc method, of class WeeklyReport.
     */
    public void testWeeklyReport() throws Exception {
        System.out.println("test WeeklyReport");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("reportDate", "2024-12-15");
        instance = AbstractJob.createJob("WeeklyReport", appBean, config, WeeklyReport.class);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
    public void testJobInfo() throws Exception {
        System.out.println("test JobInfo");
        JobInfo info = new JobInfo(UUID.randomUUID(), "WeeklyReport", System.currentTimeMillis()+60_000L);
        System.out.print(info.toJson());
    }
    
    //@Test
    public void testExpiredTempFileFilter() throws Exception {
        System.out.println("ExpiredTempFileFilter");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        File tempDir = new File(appBean.getDatapath(), "downloads");
        // Only delete files that are older than 12 hours
        //File[] oldFiles = tempDir.listFiles(new FilenameFilter()));
        for (File file : tempDir.listFiles(new FileFilter(){
            public boolean accept(File file) {
                long maximumTimestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12);
                if (!file.isFile()) {
                    return false; // Only consider files
                }
                String fileName = file.getName();
                System.out.println(fileName);
                // Extract timestamp from filename (assuming timestamp is separated by hyphens)
                int timestampStartIndex = fileName.lastIndexOf('_');
                        //fileName.indexOf('-', 5); // Start after "emis-"
                int timestampEndIndex = fileName.lastIndexOf('.');
                if (timestampStartIndex < 0 || timestampEndIndex <= timestampStartIndex) {
                    return false; // Invalid filename format
                }

                String timestampString = fileName.substring(timestampStartIndex + 1, timestampEndIndex);
                System.out.println(timestampString);
                long fileTimestamp;
                try {
                    fileTimestamp = Long.parseLong(timestampString);
                } catch (NumberFormatException e) {
                    return false; // Invalid timestamp format
                }

                return fileTimestamp < maximumTimestamp; // Accept files older than the max timestamp
            }
        })) {
            System.out.println(file.getName());
        }
    }
    
    
}
