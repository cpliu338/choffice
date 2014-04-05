package org.therismos.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import org.therismos.bean.Record;
import org.therismos.bean.Worker2;
import org.therismos.entity.Offer;

/**
 *
 * @author cpliu
 */
@ManagedBean
@javax.faces.bean.ViewScoped
public class OfferBean implements java.io.Serializable {
    
    private int batch;
    private List offers;
    private Date begin;
    private Date end;
    private boolean done;

    @javax.inject.Inject
    UserBean userBean;
    
    @javax.annotation.PostConstruct
    public void init() {
        offers = java.util.Collections.EMPTY_LIST;
        batch = 1;
        end = new Date();
        begin = new Date(end.getTime()-365*86400000L);
        done = false;
    }
    
    @javax.persistence.PersistenceContext
    EntityManager em;
    //@PostConstruct 
    public void print() {
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "print");
        FacesMessage msg = new FacesMessage();
        if (em != null) {
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "Selecting");
            offers = em.createQuery("SELECT o.member1.id, o.member1.name, SUM(o.amount) FROM Offer o WHERE o.receipt=:batch"
                + " AND o.date1 BETWEEN :d1 AND :d2 AND o.member1.groupname<>'special'" +
                " GROUP BY o.member1.id, o.member1.name").
                setParameter("batch", batch).
                setParameter("d1", begin, TemporalType.DATE).
                setParameter("d2", end, TemporalType.DATE)
                    .getResultList();
            ArrayList<Record> items = new ArrayList<>();
            for (int i=0; i<offers.size(); i++) {
                Object [] objs = (Object [])offers.get(i);
                Record r = new Record(begin, end, (Integer)objs[0], (String)objs[1], (Double)objs[2], batch);
                items.add(r);
            }
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "items size: {0}", items.size());
            if (items.size() > 0) {
                Worker2 worker = new Worker2();
                worker.setFolder(new java.io.File(userBean.getBasePath(), "receipts"));
                worker.setYear(end.getYear()+1900);
                worker.setBatch(batch);
                worker.setItems(items);
                worker.run();
                done = true;
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "Done");
                msg.setDetail("Done");
                msg.setSeverity(FacesMessage.SEVERITY_INFO);
            }
            else {
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "No offers found");
                msg.setDetail("No offers found");
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
            }
        }
        else {
        Logger.getLogger(OfferBean.class.getName()).log(Level.INFO, "No EM");
            msg.setDetail("cannot create PU");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);            
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * @return the batch
     */
    public int getBatch() {
        return batch;
    }

    /**
     * @param batch the batch to set
     */
    public void setBatch(int batch) {
        this.batch = batch;
    }

    /**
     * @return the offers
     */
    public List<Offer> getOffers() {
        return offers;
    }

    /**
     * @param offers the offers to set
     */
    public void setOffers(List<Offer> offers) {
        this.offers = offers;
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

    /**
     * @return the done
     */
    public boolean isDone() {
        return done;
    }
    
}
