package org.therismos.web;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import jakarta.annotation.*;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;
import org.therismos.entity.Entry;
import org.therismos.entity.Reconcile;
import org.therismos.entity.Uncheq;

/**
 *
 * @author cp_liu
 */
@Named
@ViewScoped
public class AccountBean implements WebBean, java.io.Serializable {

    /**
     * @return the chequeDateRange
     */
    public String getChequeDateRange() {
        return chequeDateRange;
    }

    /**
     * @return the entry
     */
    public Entry getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    /**
     * @return the selectedEndDate
     */
    public String getSelectedEndDate() {
        return selectedEndDate;
    }

    /**
     * @param selectedEndDate the selectedEndDate to set
     */
    public void setSelectedEndDate(String selectedEndDate) {
        this.selectedEndDate = selectedEndDate;
    }
    
    @Inject
    ApplicationBean appBean;
    MongoCollection<Reconcile> collReconcile;
    final List<SelectItem> endDates;
    private Reconcile reconcile;
    private Uncheq selectedCheque;
    private String selectedEndDate;
    private Entry entry;    
    private String chequeDateRange;
    
    public AccountBean() {
        endDates = new java.util.ArrayList<>();
        reconcile = new Reconcile();
    }
    
    public String getDebug() {
        // db.reconcile.find({"pending.extra1": '$7474'}, {"pending":0}).sort({end: 1})
        MongoCollection<Document> coll = appBean.getMongoClient().getDatabase("therismos").getCollection("reconcile");
        int i=0;
        try (MongoCursor<Document> cursor = 
                coll.find(Filters.eq("pending.extra1", "$7474"))
                    .projection(Projections.exclude("pending"))
                    .sort(Sorts.ascending("end")).cursor()) {
            while (cursor.hasNext()) {
                getLog().log(Level.INFO, cursor.next().toJson());
                i++;                
            }
        }
        catch (RuntimeException ex) {
            i = -1;
        }
        return Integer.toString(i);
    }
    
    @PostConstruct
    public void init() {
        collReconcile = appBean.getCollection("reconcile", Reconcile.class);
        selectedEndDate = null;
        try (MongoCursor<Reconcile> cursor = collReconcile.find().sort(Sorts.descending("end")).limit(10).cursor()) {
            cursor.forEachRemaining((reconcile) -> {
                if (selectedEndDate == null)
                    selectedEndDate = reconcile.getEnd();
                endDates.add(new SelectItem(reconcile.getEnd()));
            });
        }
        endDateChange();
    }
    
    public void endDateChange() {
        try (MongoCursor<Reconcile> cursor = collReconcile.find(Filters.eq("end", selectedEndDate)).sort(Sorts.descending("end")).limit(1).cursor()) {
            if (cursor.hasNext()) {
                reconcile = cursor.next();
                if (cursor.hasNext()) 
                    addMessage(FacesMessage.SEVERITY_WARN, "More than 1 reconcile on that date");
            }
            else {
                reconcile = new Reconcile();
                addMessage(FacesMessage.SEVERITY_WARN, "No reconcile on that date");
            }
        }        
    }
    
    public List<SelectItem> getEndDates() {
        return endDates;
    }

    /**
     * @return the reconcile
     */
    public Reconcile getReconcile() {
        return reconcile;
    }

    /**
     * @return the selectedCheque
     */
    public Uncheq getSelectedCheque() {
        return selectedCheque;
    }

    /**
     * @param selectedCheque the selectedCheque to set
     */
    public void setSelectedCheque(Uncheq selectedCheque) {
        this.selectedCheque = selectedCheque;
        try {
            setEntry(Entry.findById(appBean.getDataSource(), selectedCheque.getEntryId()));
        } catch (SQLException ex) {
            setEntry(null);
            this.addMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage());
        }
        if (entry == null) {
            FacesContext.getCurrentInstance().addMessage("entry",
            new FacesMessage(FacesMessage.SEVERITY_WARN, "not found", "not found"));
        }
        // find cheque appearing in date range
        String firstEnd = null; final List<String> lastEnds = new ArrayList<>();
        try (MongoCursor<Document> cursor = appBean.getCollection("reconcile", Document.class)
                .find(Filters.eq("pending.extra1", selectedCheque.getExtra1()))
                .projection(Projections.include("end"))
                .sort(Sorts.ascending("end")).cursor()) {
            if (cursor.hasNext()) {
                firstEnd = cursor.next().getString("end");
                cursor.forEachRemaining((d) -> lastEnds.add(d.getString("end")));
            }
            else {
                chequeDateRange = "Not possible";
            }
            if (lastEnds.isEmpty()) {
                chequeDateRange = "Found in " + firstEnd;
            }
            else {
                chequeDateRange = "Found from " + firstEnd + " to " + lastEnds.get(lastEnds.size()-1);
            }
        }
        
    }
}
