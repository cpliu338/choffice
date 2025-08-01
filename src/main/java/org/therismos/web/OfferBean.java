package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;
import org.therismos.bean.ApplicationBean;

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
        super.addMessage(FacesMessage.SEVERITY_INFO, getDate().format(DateTimeFormatter.ISO_DATE));
    }
    
}
