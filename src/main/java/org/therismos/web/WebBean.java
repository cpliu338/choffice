package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.logging.Logger;

/**
 * all beans used in EL as #{..Bean} in EL, some convenience methods here
 * @since Ver 7.0
 * @author cp_liu
 */
public interface WebBean {
    
    default Logger getLog() {
        return Logger.getLogger(getClass().getName());
    }
    
    default void addMessage(FacesMessage.Severity severity, String content) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, content, content));        
    }
        
    default void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));        
    }
}
