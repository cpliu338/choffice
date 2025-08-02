package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.io.*;
import org.bson.Document;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.therismos.bean.ApplicationBean;
import org.therismos.job.WeeklyReport;

/**
 *
 * @author cp_liu
 */
@Named
@ViewScoped
public class OfferBean extends AbstractBean implements java.io.Serializable {

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
    
    static final Logger LOG = Logger.getLogger(OfferBean.class.getName());
    @Inject
    ApplicationBean appBean;
    
    private LocalDate date;
    
    public void report() {
        Document config = new Document("type", "WeeklyReport");
        config.append("reportDate", date.format(DateTimeFormatter.ISO_DATE));
        WeeklyReport instance = new WeeklyReport(appBean, config);
        File f = instance.getDownloadPath();
        try (FileOutputStream out = new FileOutputStream(f)) {
            XSSFWorkbook wb = instance.buildExcel();
            wb.write(out);
            addMessage(FacesMessage.SEVERITY_INFO, f.getAbsolutePath(), config.toJson());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, (String) null, ex);
            addMessage(FacesMessage.SEVERITY_ERROR, "IO Exception", ex.getMessage());
        }
        
    }
    
}
