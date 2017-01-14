package org.therismos.ejb;

import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.therismos.entity.*;

/**
 *
 * @author cpliu
 */
public class MonthlyReportTask extends AbstractXlsxTask implements Runnable {
    
    public static final long serialVersionUID = 8634594537L;
    private Map<String, Double> grandtotal;
    protected int level;

    /**
     * opening balance for mortgage debt
     */
    private java.math.BigDecimal opening231;

    public void setOpening231(java.math.BigDecimal opening231) {
        this.opening231 = opening231;
    }
/*
    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }*/
    private short rowno;
    private final Properties translate;
    public static final String prefix="legend.";
    protected AccountService accountService;    
    
    static final Logger logger = Logger.getLogger(MonthlyReportTask.class.getName());
    
    public static File timestampedWorkbook() {
        return new File(new File(tmpfolder), String.format("Rep%s.xlsx",fmt.format(new java.util.Date())));
    }
    
    /**
     * @param accountService the accountService to set
     */
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public MonthlyReportTask() {
        translate = new Properties();
        grandtotal= Collections.EMPTY_MAP;
    }
    
    private void BuildPageTop(String format) {
        sheet.setColumnWidth(0, 24*256);
        sheet.setColumnWidth(1, 18*256);
        sheet.setColumnWidth(2, 18*256);
        Row row=sheet.createRow(0);
        row.setHeight((short)(20*36)); // 36 pt height
        Cell cell = row.createCell(0);
        cell.setCellValue(translate.getProperty(prefix+"header", "Church Name"));
        cell.setCellStyle(this.styleHeader1);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,2));
        row = sheet.createRow(2);
        row.setHeight((short)(20*36)); // 36 pt height
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
        List<Account> accounts;
        rowno=6;
        Row row;// = sheet.createRow(rowno++);
        Cell cell;
        String[] types = {"1","2","3"};
        
        message = "Scanning subtypes";
        logger.info(message);
        for (String subtype : types) {
            try {
                accounts = accountService.getAccountsBelow(subtype);
            } catch (RuntimeException ex) {
                errLevel = 2;
                message = ex.getClass() + ":" + ex.getMessage();
                logger.log(Level.SEVERE, null, ex);
                return false;
            }
            for (Account account : accounts) {
                String code= account.getCode();
                String name= account.getNameChi();
                Double tot = totals.get(code);
                if (tot == null || (tot > -0.001 && tot < 0.001)) continue;
                row = sheet.createRow(rowno++);
                cell = row.createCell(0);
                cell.setCellValue(name);
                cell.setCellStyle(styleText);
                /*
                if (incByCR(subtype))
                    grandtotal[0] += tot;
                else
                    grandtotal[1] -= tot;
                */
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
            if (!incByCR(subtype)) {
                rowno=this.insertBlankLine(rowno); // insert blank line after each income/expense pair
            }
        }
        message = "Building Balance Sheet";
        logger.info(message);
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        if (grandtotal.get("5") > grandtotal.get("4")) {
            cell.setCellValue(translate.getProperty(prefix+"deficit", "deficit"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue(grandtotal.get("5")-grandtotal.get("4"));
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
            cell.setCellValue(grandtotal.get("4")-grandtotal.get("5"));
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
        if (grandtotal.get("4") > grandtotal.get("5")) { // surplus
            cell1.setCellValue(grandtotal.get("1"));
            cell2.setCellValue(grandtotal.get("23")+grandtotal.get("4")-grandtotal.get("5"));
        }        
        else {
            cell1.setCellValue(grandtotal.get("1")+grandtotal.get("5")-grandtotal.get("4"));
            cell2.setCellValue(grandtotal.get("23"));
        }
        return true;
    }
    
    private boolean BuildPandL() {
        BuildPageTop(translate.getProperty("format.cut-off-date"));
        List<Account> accounts;
        rowno = 6;
        Row row;// = sheet.createRow(rowno++);
        Cell cell;
        //String[] types = {"4","5"};
        message = "Scanning subtypes";
        logger.info(message);
        accounts = new ArrayList<>();
        accounts.addAll(accountService.getAccountsBelow("4"));
        accounts.addAll(accountService.getAccountsBelow("5"));
        for (Account account : accounts) {
            Double tot = totals.get(account.getCode());
            if (tot == null || (tot > -0.001 && tot < 0.001)) continue;
            row = sheet.createRow(rowno++);
            cell = row.createCell(0);
            cell.setCellValue(account.getNameChi());
            cell.setCellStyle(styleText);
//            accum += tot;
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
        message = "Building P and L";
        logger.info(message);
        row = sheet.createRow(rowno++);
        cell = row.createCell(0);
        double surplus = grandtotal.get("4") - grandtotal.get("5");
        if (surplus >= 0.0) {
            cell.setCellValue(translate.getProperty(prefix+"surplus", "surplus"));
            cell.setCellStyle(styleBoldText);
            cell = row.createCell(1);
            cell.setCellValue(surplus);
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
            cell.setCellValue(0.0-surplus);
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
        if (surplus>=0.0) {
            cell1.setCellValue(grandtotal.get("5")+surplus);
            cell2.setCellValue(grandtotal.get("4"));
        }        
        else {
            cell1.setCellValue(grandtotal.get("5"));
            cell2.setCellValue(grandtotal.get("4")-surplus);
        }
        // print how much mortgage principal was repaid
        //accounts=this.getAccountsLike("231"); // Bank loan for mortgage
        Account summary = accountService.getAccountByCode("231");
        if (summary != null) {
//            accounts = accountService.getAccountsBelow("23");
            rowno+=2;
            row = sheet.createRow(rowno++);
            cell = row.createCell(0);
            cell.setCellStyle(styleBoldText);
            cell1 = row.createCell(1);
            cell.setCellValue(translate.getProperty(prefix+"mortgage"));
            cell1.setCellStyle(styleCheckSum);
            cell1.setCellValue(opening231.doubleValue() - totals.get("231"));
        }
        return true;
    }
    
    private boolean incByCR(String subtype) {
        return subtype.startsWith("4") ||
                subtype.startsWith("2") || subtype.startsWith("3");
    }
    /*
    private void getAccountByCode() {
        accountService.getAccounts();
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
        logger.info(message);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        downloadEntries();// level);//, fmt.format(cutoffDate));
        //totals = accountService.getTotals();
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
            logger.log(Level.SEVERE, null, ex);
        }
        message = "Run finished";
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
    
    private void downloadEntries() {//, String endDate) {
        totals = new HashMap<>();
        grandtotal = new HashMap<>();
        List<Account> accounts;
        for (String subtype : new String[]{"1","2","3","4","5"}) {
            try {
                accounts = accountService.getAccountsBelow(subtype);
            } catch (RuntimeException ex) {
                errLevel = 2;
                message = ex.getClass() + ":" + ex.getMessage();
                logger.log(Level.SEVERE, null, ex);
                return;
            }
            grandtotal.put(subtype, 0.0);
            for (Account account : accounts) {
                totals.put(account.getCode(), accountService.reckon(account.getCode(), cutoffDate));
                if (incByCR(subtype)) {
                    grandtotal.put(subtype, grandtotal.get(subtype) + totals.get(account.getCode()));
                }
                else {
                    grandtotal.put(subtype, grandtotal.get(subtype) - totals.get(account.getCode()));
                }
            }
        }
        grandtotal.put("23", grandtotal.get("2")+grandtotal.get("3"));
        message = "downloaded entries";
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

}
