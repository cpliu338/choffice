package org.therismos.web;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author cp_liu
 */
public abstract class AbstractBean {
    
    protected void addMessage(FacesMessage.Severity severity, String content) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, content, content));        
    }
        
    protected void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));        
    }
}
