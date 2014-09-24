package org.therismos.ejb;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.therismos.entity.Entry;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
public class EntryEjb {
    @PersistenceContext
    private EntityManager em;
    static final Logger logger = Logger.getLogger(EntryEjb.class.getName());
    
    /**
     * Get all the entries between dates
     * @param begin start date
     * @param end end date
     * @return the entries found
     */
    public List<Entry> getEntries(Date begin, Date end) {
        return
        em.createNamedQuery("Entry.findBetweenDates", Entry.class).
        setParameter("date1", begin).setParameter("date2", end).getResultList();
    }
    
    public void report() {
        
    }
    
    /**
     * Export the entries for auditor, ultimately in xlsx format
     * @param begin start date
     * @param end end date
     * @return 
     */
    public String exportEntries(Date begin, Date end) {
        List<Entry> results = getEntries(begin, end);
        String csv;
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
        return csv;
    }

}
