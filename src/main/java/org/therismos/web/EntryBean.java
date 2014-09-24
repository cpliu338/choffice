package org.therismos.web;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import org.therismos.ejb.EntryEjb;
import org.therismos.ejb.MongoService;
import org.therismos.ejb.MonthlyReportTask;
import org.therismos.entity.Entry;

/**
 *
 * @author cpliu
 */
@ManagedBean
@javax.faces.bean.ViewScoped
public class EntryBean implements java.io.Serializable {
    private Date begin;
    private Date end;
    private String csv;
    
    @javax.inject.Inject
    private EntryEjb entryEjb;
    
    @javax.ejb.EJB
    private MongoService mongoService;

    public String getCsv() {
        return csv;
    }
    static final Logger logger = Logger.getLogger(EntryBean.class.getName());

    public EntryBean() {
        csv = "";
    }
    
    public void export() {
        csv = entryEjb.exportEntries(begin, end);
    }
    
    /**
     * @return the begin
     */
    public Date getBegin() {
        return begin;
    }

    /**
     * @param begin the begin to set
     */
    public void setBegin(Date begin) {
        this.begin = begin;
    }

    /**
     * @return the end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Date end) {
        this.end = end;
    }
    
    private Thread runner;
    private MonthlyReportTask task;
    
    public boolean isTaskRunning() {
        return runner!=null && runner.isAlive();
    }
    
    public String getProgress() {
        return task==null ? "Task not running" : task.getMessage();
    }
    
    public String refresh() {
        if (task==null) return null;
        FacesMessage msg = new FacesMessage(task.getMessage());
        msg.setSeverity(task.getErrLevel()>0 ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_INFO);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return null;
    }
    
    public void report() {
        task = new MonthlyReportTask();
        task.setAccountService(mongoService);
        task.setCutoffDate(end);
        begin.setYear(end.getYear());
        begin.setMonth(0);
        begin.setDate(1);
        task.setLevel(3);
        task.setEntries(entryEjb.getEntries(begin, end));
        runner = new Thread(task);
        runner.start();
    }
    
}
