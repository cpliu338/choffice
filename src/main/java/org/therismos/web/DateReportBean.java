package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.io.*;
import org.bson.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.omnifaces.util.Faces;
import org.therismos.bean.ApplicationBean;
import org.therismos.job.MonthlyReport;
import org.therismos.job.WeeklyReport;

/**
 * Call a report of a subtype of AbstractJob, often require a LocalDate type argument
 * @author cp_liu
 */
@Named
@ViewScoped
public class DateReportBean implements WebBean, java.io.Serializable {

    /**
     * @return the date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    @Inject
    ApplicationBean appBean;
    
    private LocalDate date;

    public void weeklyReport() {
        final String className = "WeeklyReport";
        Document config = new Document("type", className);
        config.append("reportDate", date.format(DateTimeFormatter.ISO_DATE));
        WeeklyReport instance = new WeeklyReport(appBean, config);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
            //addMessage(FacesMessage.SEVERITY_INFO, f.getAbsolutePath(), config.toJson());
            Faces.redirect("file-system/download.jsf?type=%s", className);
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, (String) null, ex);
            addMessage(FacesMessage.SEVERITY_ERROR, "IO Exception", ex.getMessage());
        }        
    }
    
    public void monthlyReport() {
        final String className = "MonthlyReport";
        Document config = new Document("type", className);
        config.append("end", date.format(DateTimeFormatter.ISO_DATE));
        MonthlyReport instance = new MonthlyReport(appBean, config);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
            Faces.redirect("file-system/download.jsf?type=%s", className);
        } catch (Exception ex) {
            getLog().log(Level.SEVERE, (String) null, ex);
            addMessage(FacesMessage.SEVERITY_ERROR, "IO Exception", ex.getMessage());
        }        
    }
    
}
