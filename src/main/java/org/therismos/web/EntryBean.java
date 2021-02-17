package org.therismos.web;

//import com.mongodb.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.bson.Document;
import org.therismos.ejb.AccountService;
import org.therismos.ejb.EntryEjb;
import org.therismos.ejb.MongoService;
import org.therismos.ejb.AbstractXlsxTask;
import org.therismos.ejb.AuditExportTask;
import org.therismos.ejb.MonthlyReportTask;
import org.therismos.entity.Entry;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
@javax.faces.view.ViewScoped
public class EntryBean implements java.io.Serializable {
    private Date begin;
    private Date end;
    private String csv;
    private List<Entry> entries;
    private Document dbobject;

    public Document getDbobject() {
        return dbobject;
    }
    /*
    @javax.persistence.PersistenceContext
    private EntityManager em;
    */
    public List<Entry> getEntries() {
        return entries;
    }
    
    @javax.inject.Inject
    private EntryEjb entryEjb;
    
    @javax.inject.Inject
    private MongoService mongoService;

    @javax.inject.Inject
    private AccountService accountService;
    
    public String getCsv() {
        return csv;
    }
    static final Logger logger = Logger.getLogger(EntryBean.class.getName());

    public EntryBean() {
        csv = "";
        begin = new Date();
        end = new Date();
        entries = Collections.EMPTY_LIST;
        dbobject = new Document();
        dbobject.append("bookBalance", BigDecimal.ZERO);
        dbobject.append("uncheq", 0.0);
    }
    
    public void closeYear() {
        /*
        List<Integer> ids = new ArrayList<>();
        ids.add(11201);
        ids.add(11001);
        ids.add(11002);
        ids.add(11101);
        */
        entries = entryEjb.closeYear(end);
    }
    
    public void export() {
        /*
        Replaced since Version 5.1 
        csv = entryEjb.exportEntries(begin, end);
        Find unpresented cheques:
        db.reconcile.find({end:'2016-03-31'}).sort({_id:-1}).limit(1)
        */
        AuditExportTask task2 = new AuditExportTask();
        this.task = task2;
        task2.setCutoffDate(end);
        task2.setFinYear(begin.getYear()+1900);
        task2.setEntryEjb(entryEjb);
        task2.setMongoService(mongoService);
        runner = new Thread(task2);
        runner.start();
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
    
    public void endDateChanged() {
        begin.setYear(end.getYear());
        begin.setMonth(0);
        begin.setDate(1);
    }
    
    private Thread runner;
    private AbstractXlsxTask task;
    
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
        MonthlyReportTask task2 = new MonthlyReportTask();
        this.task = task2;
        task2.setAccountService(this.accountService);
        task2.setCutoffDate(end);
        begin.setYear(end.getYear());
        begin.setMonth(0);
        begin.setDate(1);
        task2.setLevel(3);
        //task.setEntries(entryEjb.getEntries(begin, end));
        task2.setOpening231(entryEjb.getOpening29001(begin, end));
        runner = new Thread(task2);
        runner.start();
    }
    
    public void saveCheques() {
        if (dbobject.containsKey("end")) {
            mongoService.saveCheques(dbobject);
        }
        // if dbobject does not contains key "end", will not save
        dbobject.remove("end");
    }
    
    public boolean isChequesSaveable() {
        return dbobject.containsKey((Object)("end"));
    }
    
    public void reconcile() {
        // if dbobject does not contains key "end", will not save
        dbobject.remove("end");
        entries = entryEjb.getUncheques(end, 11201);
        double sum=0.0;
        List<Document> list = new ArrayList<>();
        for (Entry e : entries) {
            Document o = new Document("id", e.getId());
            o.append("amount", e.getAmount().doubleValue());
            o.append("extra1", e.getExtra1());
            list.add(o);
            sum += e.getAmount().doubleValue();
        }
        dbobject = new Document("accountId", "11201");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        dbobject.append("start", fmt.format(begin));
        dbobject.append("end", fmt.format(end));
        List<Integer> accs = new ArrayList<>();
        accs.add(11201);
                logger.log(Level.INFO, "start: {0}, end: {1}, acc: {2}", new Object[]{
                    begin, end, accs.get(0)
                });
        java.math.BigDecimal bbal = entryEjb.aggregate(accs, begin, end);
        logger.log(Level.INFO, "Book Balance is {0}", bbal);
        dbobject.append("bookBalance", bbal.doubleValue());
        dbobject.append("uncheq", sum);
        dbobject.append("pending", list);
        //accountId, end, bookBalance, uncheq (sum), pending [list of entries],
        logger.log(Level.FINE, "Result: {0}", dbobject.toString());
    }
    
}
