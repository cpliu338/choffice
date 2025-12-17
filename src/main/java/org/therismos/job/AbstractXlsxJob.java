package org.therismos.job;


import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.logging.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * base class to create an xlsx file for download based on a template xlsx file
 * @author cp_liu
 */
public abstract class AbstractXlsxJob extends AbstractJob {
    
    protected XSSFWorkbook templateXlsx;
    protected XSSFWorkbook workbook;
    protected Sheet srcSheet /* template sheet */, sheet /* target sheet */;
    
    @Override
    protected void init() {
        try {
            templateXlsx = new XSSFWorkbook(new FileInputStream(getTemplateFilePath()));
        } catch (IOException ex) {
            templateXlsx = null;
        }
    }
    
    protected abstract XSSFWorkbook buildExcel() throws Exception;
    
    protected XSSFWorkbook buildExcel(String sheetName) throws Exception {
        if (workbook == null) // no need to create again for the second sheet
            workbook = new XSSFWorkbook();
        if (templateXlsx == null)
            throw new IOException("template file not found: " + getTemplateFilePath().getAbsolutePath());
        srcSheet = templateXlsx.getSheet(sheetName);
        if (srcSheet == null)
            throw new IOException(String.format("Sheet %s not found in %s ", sheetName, getTemplateFilePath().getAbsolutePath()));
        sheet = workbook.createSheet(sheetName);
        return workbook;
    }
    
    protected final File getTemplateFilePath() {
        return new File( Paths.get(applicationBean.getDatapath(), "templates", String.format("%s%s.xlsx",
                this.getClass().getSimpleName(), "Template")).toUri()
        );        
    }
    
    @Override
    public String getFileExtension() {
        return "xlsx";
    }
    
    /**
     * Clone a style from srcSheet[row, column] in the template to a style in workbook
     * @param row
     * @param column
     * @return 
     */
    protected CellStyle cloneStyle(int row, int column) {
        CellStyle s1 = srcSheet.getRow(row).getCell(column).getCellStyle();
        CellStyle s = workbook.createCellStyle();
        s.cloneStyleFrom(s1);
        return s;
    }
    
    protected Cell cloneCell(Cell toCopy, Row row, int columnOffset, boolean cloneValue) {
        Cell cell = row.createCell(columnOffset);
        CellStyle srcStyle = toCopy.getCellStyle();
        CellStyle targetStyle = workbook.createCellStyle();
        targetStyle.cloneStyleFrom(srcStyle);
        cell.setCellStyle(targetStyle);
        if (cloneValue)
            cell.setCellValue(toCopy.getStringCellValue());
        return cell;
    }
    
    /**
     * Clone the row (remove original row if exists). This method only clones the row height, column widths
     * @param srcRowNo
     * @param rowno
     * @return 
     */
    protected Row cloneRow(int srcRowNo, int rowno) {
        if (sheet.getRow(rowno) != null) {
            sheet.removeRow(sheet.getRow(rowno));
        }
        Row srcRow1 = srcSheet.getRow(srcRowNo);
        Row row = sheet.createRow(rowno);
        row.setHeight(srcRow1.getHeight());
        for (Cell cell : srcRow1) {
            int index = cell.getColumnIndex();
            sheet.setColumnWidth(index, srcSheet.getColumnWidth(index));
            Logger.getLogger("AbstractXlsxJob").log(Level.FINE, "Set column {0} to {1}", 
                    new Object[]{index, srcSheet.getColumnWidth(index)});
        }
        return row;
    }
    
    @Override
    public Document call() throws Exception {
        File f = super.getDownloadPath();
        getConfig().append("download-path", f.getAbsolutePath());
        try (FileOutputStream fileOut = new FileOutputStream(f)) {
            buildExcel().write(fileOut);
        }
        catch (Exception ex) {
            getConfig().append("exception-class", ex.getClass().getName());
            getConfig().append("exception-message", ex.getMessage());
            if (ex instanceof RuntimeException || ex instanceof IOException || ex instanceof SQLException) {} else
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return getConfig();
    }
    
}
