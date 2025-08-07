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
        GenerateReceipts gr = new GenerateReceipts(appBean, config);
        System.out.println(gr.call().toJson());
    }
    
    public void testMonthlyReckon() throws Exception {
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("end", "2024-12-31");
        MonthlyReport m = new MonthlyReport(appBean, config);
        BigDecimal sum = m.reckon("510", LocalDate.parse("2024-07-31"));
        System.out.print(sum);
        assert(sum.add(new BigDecimal("754964.79")).compareTo(BigDecimal.ZERO) == 0);
        assert(sum.compareTo(new BigDecimal("-754964.79")) == 0);
    }
    
    @Test
    public void testMonthlyReport() throws Exception {
        System.out.println("test Monthly Report");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        config.append("end", "2024-12-31");
        instance = (MonthlyReport)new MonthlyReport(appBean, config);
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
        instance = new WeeklyReport(appBean, config);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
    }
    
}
