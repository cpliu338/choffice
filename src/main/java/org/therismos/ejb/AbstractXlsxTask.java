package org.therismos.ejb;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author cpliu
 */
public class AbstractXlsxTask {
    
    protected java.util.Date cutoffDate;
    protected File targetFile;
    protected CellStyle styleHeader1;
    protected CellStyle styleHeader2;
    protected CellStyle styleEntry;
    protected CellStyle styleText;
    protected CellStyle styleBoldText;
    protected CellStyle styleBoldEntry;
    protected CellStyle styleCheckSum;
    protected Sheet sheet;
    protected Workbook workbook;
    protected DataFormat format;
    protected Font fontBold;
    protected Font fontNormal;
    protected Font fontHeader1;
    protected Font fontHeader2;
    protected String message;
    protected CellStyle styleDate;
    protected int errLevel;
    public static final String tmpfolder = "/tmp";
    public static final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
    public static final java.text.SimpleDateFormat fmtYmd = new java.text.SimpleDateFormat("yyyy-MM-dd");
    
    public AbstractXlsxTask() {
        errLevel = 0;
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
        //styleSummary = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)1);
        styleText = this.createStyle(CellStyle.ALIGN_CENTER, false, false, (short)0);
        styleEntry = this.createStyle(CellStyle.ALIGN_RIGHT, false, true, (short)0);
        styleBoldText = this.createStyle(CellStyle.ALIGN_CENTER, true, false, (short)0);
        styleBoldEntry = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)0);
        styleCheckSum = this.createStyle(CellStyle.ALIGN_RIGHT, true, true, (short)2);
        //DataFormat format = workbook.createDataFormat();
        styleDate = workbook.createCellStyle();
        styleDate.setDataFormat(format.getFormat("yyyy-mm-dd"));
        message = "Task prepared";
    }
    
    protected final CellStyle createHeaderStyle(Font f) {
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
    protected final CellStyle createStyle(short align, boolean isBold, boolean isNumber, short isSummary) {
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
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public int getErrLevel() {
        return errLevel;
    }
    
    /**
     * @param cutoffDate the cutoffDate to set
     */
    public void setCutoffDate(Date cutoffDate) {
        this.cutoffDate = cutoffDate;
    }

}
