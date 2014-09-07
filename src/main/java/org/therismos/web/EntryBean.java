package org.therismos.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
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

    public String getCsv() {
        return csv;
    }
    static final Logger logger = Logger.getLogger(EntryBean.class.getName());

    @javax.persistence.PersistenceContext
    EntityManager em;
    
    public EntryBean() {
        csv = "";
    }

//    @javax.annotation.PostConstruct
    public void export() {
        java.util.List<Entry> results = em.createNamedQuery("Entry.findBetweenDates", Entry.class).setParameter("date1", begin).setParameter("date2", end).getResultList();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if (results.size() > 1) {
            StringBuilder buf = new StringBuilder();
            for (Entry e : results) {
                buf.append(String.format("%d,\"%s\", %d, \"%s\", %.2f, \"%s\"\n",
                e.getTransref(), fmt.format(e.getDate1()),e.getAccount().getId(),e.getAccount().getNameChi(),
                e.getAmount(),e.getDetail()));
            }
            csv = buf.toString();
        }
        else {
            csv = "Empty result";
        }
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
    
}
