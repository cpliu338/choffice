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
    
    protected AbstractXlsxJob(ApplicationBean srv, Document config) {
        super(srv, config);
        try {
            templateXlsx = new XSSFWorkbook(new FileInputStream(getTemplateFilePath()));
        } catch (IOException ex) {
            templateXlsx = null;
        }
    }
    
    protected XSSFWorkbook buildExcel() throws Exception {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Offers");
        srcSheet = templateXlsx.getSheetAt(0);
        if (templateXlsx == null)
            throw new IOException("template file not found: " + getTemplateFilePath().getAbsolutePath());
        return workbook;
    }
    
    protected final File getTemplateFilePath() {
        return new File( Paths.get(applicationBean.getDatapath(), "templates", String.format("%s%s.xlsx",
                this.getClass().getSimpleName(), "Template")).toUri()
        );        
    }
    
    @Override
    protected String getFileExtension() {
        return "xlsx";
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
        }
        return row;
    }
    
    @Override
    public Document call() throws Exception {
        File f = super.getDownloadPath();
        config.append("download-path", f.getAbsolutePath());
        try (FileOutputStream fileOut = new FileOutputStream(f)) {
            buildExcel().write(fileOut);
        }
        catch (Exception ex) {
            config.append("exception-class", ex.getClass().getName());
            config.append("exception-message", ex.getMessage());
            if (ex instanceof RuntimeException || ex instanceof IOException || ex instanceof SQLException) {} else
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }
    
}
