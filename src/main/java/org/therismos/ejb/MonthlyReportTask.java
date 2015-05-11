package org.therismos.ejb;

import com.mongodb.DBObject;
import java.io.*;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.therismos.entity.Entry;
import org.therismos.model.AccountModel;
import org.w3c.dom.NodeList;

/**
 *
 * @author cpliu
 */
public class MonthlyReportTask implements Runnable {
    
    //@Inject
    private MongoService mongoDao;
    
    public static final long serialVersionUID = 8634594537L;
    private int level;
    private java.util.Date cutoffDate;
    private File targetFile;
    private final double[] grandtotal;
    private final CellStyle styleHeader1;
    private final CellStyle styleHeader2, styleSummary, styleEntry, styleText;
    private final CellStyle styleBoldText, styleBoldEntry, styleCheckSum;
    private Sheet sheet;
    private final Workbook workbook;
    private double accum;
    private final DataFormat format;
    private final Font fontBold, fontNormal, fontHeader1, fontHeader2;
    private String message;
    private List<Entry> entries;

    /**
     * opening balance for mortgage debt
     */
    private java.math.BigDecimal opening231;

    public void setOpening231(java.math.BigDecimal opening231) {
        this.opening231 = opening231;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
    private short rowno;
    private final Properties translate;
    private int errLevel;
    
    public static final String prefix="legend.";
    public static final String tmpfolder = "/tmp";
    public static final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
    
    static final Logger logger = Logger.getLogger(MonthlyReportTask.class.getName());
    
    public static File timestampedWorkbook() {
        return new File(new File(tmpfolder), String.format("Rep%s.xlsx",fmt.format(new java.util.Date())));
    }
    
    public MonthlyReportTask() {
        errLevel = 0;
        translate = new Properties();
        grandtotal=new double[2];
        workbook = new XSSFWorkbook();
        format = workbook.createDataFormat();
        fontBold = workbook.createFont();
        fontBold.setFontHeight((short)240);
        fontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontNormal = workbook.createFont();
        fontNormal.setFontHeight((short)240);
        fontNormal.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        fontHeader1 =workbook.createFont();
        fontHeader1.setFontHeight((short)720);
        fontHeader1.setBoldweight(Font.BOLDWEIGHT_BOLD);
        fontHeader2 =workbook.createFont();
        fontHeader2.setFontHeight((short)400);
        fontHeader2.setUnderline(Font.U_SINGLE);
        styleHeader1 = this.createHeaderStyle(fontHeader1);
        styleHeader2 = this.createHeaderStyle(fontHeader2);
        styleSummary = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)1);
        styleText = this.createStyle(CellStyle.ALIGN_CENTER, false, false, (short)0);
        styleEntry = this.createStyle(CellStyle.ALIGN_RIGHT, false, true, (short)0);
        styleBoldText = this.createStyle(CellStyle.ALIGN_CENTER, true, false, (short)0);
        styleBoldEntry = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)0);
        styleCheckSum = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)2);
        message = "Task prepared";
//        error = "";
    }
    
    private void BuildPageTop(String format) {
        grandtotal[0]=0.0;
        grandtotal[1]=0.0;
        sheet.setColumnWidth(0, 24*256);
        sheet.setColumnWidth(1, 18*256);
        sheet.setColumnWidth(2, 18*256);
        Row row=sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(translate.getProperty(prefix+"header", "Church Name"));
        cell.setCellStyle(this.styleHeader1);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));
        row = sheet.createRow(2);
        cell = row.createCell(0);
        cell.setCellValue(MessageFormat.format(format, this.cutoffDate));
        cell.setCellStyle(this.styleHeader2);
        sheet.addMergedRegion(new CellRangeAddress(2,2,0,2));
        row = sheet.createRow(4);
        cell = row.createCell(0);
        cell.setCellValue(translate.getProperty(prefix+ "account", "account"));
        cell.setCellStyle(this.styleBoldText);
        cell = row.createCell(1);
        cell.setCellValue("DB");
        cell.setCellStyle(this.styleBoldText);
        cell = row.createCell(2);
        cell.setCellValue("CR");
        cell.setCellStyle(this.styleBoldText);
        row = sheet.createRow(5);
        cell = row.createCell(0);
        cell.setCellValue(" ");
        cell.setCellStyle(this.styleBoldText);
        cell = row.createCell(1);
        cell.setCellValue("$");
        cell.setCellStyle(this.styleBoldText);
        cell = row.createCell(2);
        cell.setCellValue("$");
        cell.setCellStyle(this.styleBoldText);
        
    }

    private boolean BuildBalanceSheet () {
        BuildPageTop(translate.getProperty("format.cut-off-date2"));
        List<DBObject> accounts;
        rowno=6;
        Row row;// = sheet.createRow(rowno++);
        Cell cell;
        String[] types = {"1","2","3"};
        //String [] codes = {"110","1110","1120","120","1310","1320","190","210","220","230","30"};
        message = "Scanning subtypes";
//        logger.info(message);
        for (String subtype : types) {
            try {
                accounts = mongoDao.getAccountsBelow(subtype);
            } catch (UnknownHostException ex) {
                errLevel = 2;
                message = "UnknownHostException";
                Logger.getLogger(MonthlyReportTask.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            for (DBObject obj : accounts) {
                // obj is in fact Account
                String code= (String)obj.get("code"); 
                String name= (String)obj.get("name_chi"); 
            // Balance sheet summary limited to 10, 20, 30
//            Account summary = mongoDao.getAccountByCode(subtype.substring(0,1) +"0");
                Double tot = totals.get(code);//(String)(accounts.get(0).get("code")));
                if (tot == null || (tot > -0.001 && tot < 0.001)) continue;
                row = sheet.createRow(rowno++);
                cell = row.createCell(0);
                cell.setCellValue(name);
                cell.setCellStyle(styleText);
                if (isIncByCR(subtype))
                    grandtotal[0] += tot;
                else
                    grandtotal[1] -= tot;
                if (tot < 0) {
                    cell = row.createCell(1);
                    cell.setCellValue(0.0-tot);
                    cell.setCellStyle(styleEntry);
                    cell = row.createCell(2);
                    cell.setCellValue("");
                    cell.setCellStyle(styleText);
                }
                else {
                    cell = row.createCell(1);
                    cell.setCellValue("");
                    cell.setCellStyle(styleText);
                    cell = row.createCell(2);
                    cell.setCellValue(tot);
                    cell.setCellStyle(styleEntry);
                }
            }
            if (!isIncByCR(subtype)) {
                rowno=this.insertBlankLine(rowno); // insert blank line after each income/expense pair
            }
        }
        message = "Building Balance Sheet";
//        logger.info(message);
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        if (grandtotal[0] > grandtotal[1]) {
            cell.setCellValue(translate.getProperty(prefix+"deficit", "deficit"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue(grandtotal[0]-grandtotal[1]);
            cell.setCellStyle(styleEntry);
            cell = row.createCell(2);
            cell.setCellValue("");
            cell.setCellStyle(styleText);
        }
        else {
            cell.setCellValue(translate.getProperty(prefix+"surplus", "surplus"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue("");
            cell.setCellStyle(styleText);
            cell = row.createCell(2);
            cell.setCellValue(grandtotal[1]-grandtotal[0]);
            cell.setCellStyle(styleEntry);
        }
        // Check sum row
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        cell.setCellStyle(styleBoldText);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        cell1.setCellStyle(styleCheckSum);
        cell2.setCellStyle(styleCheckSum);
        if (grandtotal[0] > grandtotal[1]) {
            cell1.setCellValue(grandtotal[0]);
            cell2.setCellValue(grandtotal[0]);
        }        
        else {
            cell1.setCellValue(grandtotal[1]);
            cell2.setCellValue(grandtotal[1]);
        }
        return true;
    }
    
    private boolean BuildPandL() {
        BuildPageTop(translate.getProperty("format.cut-off-date"));
        List<DBObject> accounts;
        rowno = 6;
        Row row;// = sheet.createRow(rowno++);
        Cell cell;
        String[] types = {"41","51","59","42","52","46","56"};
        message = "Scanning subtypes";
//        logger.info(message);
        for (String subtype : types) {
            try {
                accounts = mongoDao.getAccountsBelow(subtype);
            } catch (UnknownHostException ex) {
                errLevel = 2;
                message = "UnknownHostException";
                Logger.getLogger(MonthlyReportTask.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            int size = 0;
            for (DBObject obj : accounts) {
                // obj is in fact Account
                String code= (String)obj.get("code"); 
                size++;
            }
            AccountModel summary = mongoDao.getAccountByCode(subtype+"0");
            row = sheet.createRow(rowno++);
            cell = row.createCell(0);
            cell.setCellValue(summary.getDetail());
            cell.setCellStyle(styleBoldText);
            if (size==1) {
                double tot = totals.get((String)(accounts.get(0).get("code")));
                if (isIncByCR(subtype))
                    grandtotal[0] += tot;
                else
                    grandtotal[1] -= tot;
                if (tot < 0) {
                    cell = row.createCell(1);
                    cell.setCellValue(0.0-tot);
                    cell.setCellStyle(styleBoldEntry);
                    cell = row.createCell(2);
                    cell.setCellValue("");
                    cell.setCellStyle(styleText);
                }
                else {
                    cell = row.createCell(1);
                    cell.setCellValue("");
                    cell.setCellStyle(styleText);
                    cell = row.createCell(2);
                    cell.setCellValue(tot);
                    cell.setCellStyle(styleBoldEntry);
                }
            }
            if (size>1) {
                logger.log(Level.FINE, "more than 1");
                cell = row.createCell(1);
                cell.setCellValue("");
                cell.setCellStyle(styleText);
                cell = row.createCell(2);
                cell.setCellValue("");
                cell.setCellStyle(styleText);
                rowno = this.processEntry(accounts, rowno);
                if (isIncByCR(subtype))
                    grandtotal[0] += accum;
                else
                    grandtotal[1] -= accum;
                row = sheet.createRow(rowno++);
                cell = row.createCell(0);
                cell.setCellValue(" ");
                cell.setCellStyle(styleText);
                if (accum < 0) {
                    cell = row.createCell(1);
                    cell.setCellValue(0.0-accum);
                    cell.setCellStyle(styleSummary);
                    cell = row.createCell(2);
                    cell.setCellValue(" ");
                    cell.setCellStyle(styleText);
                }
                else {
                    cell = row.createCell(1);
                    cell.setCellValue(" ");
                    cell.setCellStyle(styleText);
                    cell = row.createCell(2);
                    cell.setCellValue(accum);
                    cell.setCellStyle(styleSummary);
                }
            }
            if (!isIncByCR(subtype)) {
                rowno=this.insertBlankLine(rowno); // insert blank line after each income/expense pair
            }
        }
        message = "Building P and L";
        logger.info(message);
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        if (grandtotal[0] > grandtotal[1]) {
            cell.setCellValue(translate.getProperty(prefix+"surplus", "surplus"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue(grandtotal[0]-grandtotal[1]);
            cell.setCellStyle(styleEntry);
            cell = row.createCell(2);
            cell.setCellValue("");
            cell.setCellStyle(styleText);
        }
        else {
            cell.setCellValue(translate.getProperty(prefix+"deficit", "deficit"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue("");
            cell.setCellStyle(styleText);
            cell = row.createCell(2);
            cell.setCellValue(grandtotal[1]-grandtotal[0]);
            cell.setCellStyle(styleEntry);
        }
        // Check sum row
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        cell.setCellStyle(styleBoldText);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        cell1.setCellStyle(styleCheckSum);
        cell2.setCellStyle(styleCheckSum);
        if (grandtotal[0] > grandtotal[1]) {
            cell1.setCellValue(grandtotal[0]);
            cell2.setCellValue(grandtotal[0]);
        }        
        else {
            cell1.setCellValue(grandtotal[1]);
            cell2.setCellValue(grandtotal[1]);
        }
        // print how much mortgage principal was repaid
        //accounts=this.getAccountsLike("231"); // Bank loan for mortgage
        AccountModel summary = mongoDao.getAccountByCode("231");
//        if (!accounts.isEmpty()) {
//            accounts = mongoDao.getAccountsBelow("23");
            rowno+=2;
            row = sheet.createRow(rowno++);
            cell = row.createCell(0);
            cell.setCellStyle(styleBoldText);
            cell1 = row.createCell(1);
            cell.setCellValue(translate.getProperty(prefix+"mortgage"));
            cell1.setCellStyle(styleCheckSum);
            /*
            try {
                Object o = mongoDao.getConfig("opening");
                DBObject config = (DBObject)o;
                opening231 = ((Number)config.get("231")).doubleValue();
            }
            catch (Exception ex) {
                return false;
            }
            */
            cell1.setCellValue(opening231.doubleValue() - totals.get("231"));
        return true;
    }
    
    private boolean isIncByCR(String subtype) {
        return subtype.startsWith("4") ||
                subtype.startsWith("2") || subtype.startsWith("3");
    }
    /*
    private void getAccountByCode() {
        mongoDao.getAccounts();
    }
    */
    private Map<String,Double> totals;

    @Override
    public void run() {
        try {
            translate.load(MonthlyReportTask.class.getResourceAsStream("/config.properties"));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            message = "Cannot load config";
            return;
        }
        message = "Downloading entries up to "+cutoffDate;
        scanEntries(level); 
        totals = mongoDao.getTotals();
//        logger.log(Level.INFO, "Depreciation {0},{1}", 
//                new Object[]{590, totals.get("590")});
//        logger.log(Level.INFO, "Depreciation {0},{1}", 
//                new Object[]{591, totals.get("591")});
//        if (level>0) return;
        sheet = workbook.createSheet("P and L");
        if (!BuildPandL()) return;
        sheet = workbook.createSheet("Balance Sheet");
        if (!BuildBalanceSheet()) return;
        try {
            FileOutputStream fileOut;
            fileOut = new FileOutputStream(MonthlyReportTask.timestampedWorkbook());
            workbook.write(fileOut);
            fileOut.close();
        } catch (Exception ex) {
            errLevel = 2;
            message = ex.getClass().getName() +" : "+ex.getMessage();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        message = "Run finished";
//        error = "";
    }
    
    private short processEntry(List<DBObject> accounts, short rowno) {
        accum = 0.0;
        for (DBObject ac : accounts) {
            String code = (String)ac.get("code");
            Double tot = totals.get(code);
            if (tot == null || (tot > -0.001 && tot < 0.001)) continue;
            Row row = sheet.createRow(rowno++);
            Cell cell = row.createCell(0);
            cell.setCellValue((String)ac.get("name_chi"));
            cell.setCellStyle(styleText);
            //if (tot==null) tot = 0.0;
            accum += tot;
            if (tot < 0) {
                cell = row.createCell(1);
                cell.setCellValue(0.0-tot);
                cell.setCellStyle(styleEntry);
                cell = row.createCell(2);
                cell.setCellValue("");
                cell.setCellStyle(styleText);
            }
            else {
                cell = row.createCell(1);
                cell.setCellValue("");
                cell.setCellStyle(styleText);
                cell = row.createCell(2);
                cell.setCellValue(tot);
                cell.setCellStyle(styleEntry);
            }
        }
        return rowno;
    }
    
    private short insertBlankLine(int n) {
        Row row = sheet.createRow(n);
        Cell cell = row.createCell(0);
        cell.setCellValue(" ");
        cell.setCellStyle(styleText);
        cell = row.createCell(1);
        cell.setCellValue(" ");
        cell.setCellStyle(styleText);
        cell = row.createCell(2);
        cell.setCellValue(" ");
        cell.setCellStyle(styleText);
        return (short) (n+1);
    }
    
    private void scanEntries(int level) {//, String endDate) {
        Map<Integer,String> codeLookup = new HashMap<>();
        for (AccountModel a : mongoDao.getAccounts()) {
            codeLookup.put(a.getId(), a.getCode());
        }
//        try {
        mongoDao.clearTotals();
        for (Entry entry : entries) {
            mongoDao.reckon(level, codeLookup.get(entry.getAccount().getId()), entry.getAmount().doubleValue());
        }
//        }
//        catch (Exception ex) {
//            logger.log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
//            error = ex.getClass().getName() +" : "+ex.getMessage();
//            return;
//        }
        message = "scanned entries";
    }
    
    private CellStyle createHeaderStyle(Font f) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(f);
        return style;
    }
    
    /**
     * Create a style based on parameters given
     * @param align
     * @param isBold
     * @param isNumber
     * @param isSummary 0 for normal, 1 for summary (underline), 2 for checksum (double underline)
     * @return 
     */
    private CellStyle createStyle(short align, boolean isBold, boolean isNumber, short isSummary) {
        CellStyle style = workbook.createCellStyle();
        style.setFont(isBold ? fontBold : fontNormal);
        switch (isSummary) {
            case 0: case 1: style.setBorderBottom(CellStyle.BORDER_THIN); break;
            case 2: style.setBorderBottom(CellStyle.BORDER_DOUBLE); break;
        }
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        switch (isSummary) {
            case 0: style.setBorderTop(CellStyle.BORDER_THIN); break;
            case 1: 
            case 2: style.setBorderTop(CellStyle.BORDER_MEDIUM); break;
        }
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setAlignment(align);
        if (isNumber) style.setDataFormat(format.getFormat("#,###.00"));
        return style;
    }

    /**
     * @param mongoDao the mongoDao to set
     */
    public void setMongoDao(MongoService mongoDao) {
        this.mongoDao = mongoDao;
    }

    /**
     * @param accountService the accountService to set
    public void setAccountService(MongoService accountService) {
        mongoDao = accountService;
    }
     */

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the cutoffDate
     */
    public Date getCutoffDate() {
        return cutoffDate;
    }

    /**
     * @param cutoffDate the cutoffDate to set
     */
    public void setCutoffDate(Date cutoffDate) {
        this.cutoffDate = cutoffDate;
    }

    /**
     * @return the targetFile
     */
    public File getTargetFile() {
        return targetFile;
    }

    /**
     * @param targetFile the targetFile to set
     */
    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    /**
     * @return the error
    private String getError() {
        return error;
    }
     */

    public int getErrLevel() {
        return errLevel;
    }
    
}
