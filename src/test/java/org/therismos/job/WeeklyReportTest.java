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

/**
 *
 * @author cp_liu
 */
public class WeeklyReportTest {
    
    public WeeklyReportTest() {
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
    WeeklyReport instance;

    /**
     * Test of getFilePattern method, of class WeeklyReport.
    @Test
     */
    public void testGetFilePattern() {
        System.out.println("getFilePattern");
        String expResult = "";
        String result = WeeklyReport.getFilePattern();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileDesc method, of class WeeklyReport.
     */
    @Test
    public void testBuildExcel() throws Exception {
        System.out.println("test buildExcel");
        ApplicationBean appBean = new ApplicationBean();
        appBean.init();
        // jdbc:mariadb://db-01:3306/emis?user=webapp&password=asd82KK
        config.append("reportDate", "2024-12-15");
        WeeklyReport instance = new WeeklyReport(appBean, config);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
        }
        System.out.println("written to " + f.getAbsolutePath());
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
