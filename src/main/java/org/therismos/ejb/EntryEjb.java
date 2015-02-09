package org.therismos.ejb;

import java.math.BigDecimal;
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
public class EntryEjb implements java.io.Serializable {
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

    public List<Entry> getUncheques(Date end, int accountid) {
        List<Entry> r = em.createNamedQuery("Entry.findUnchequesBeforeDate", Entry.class).
            setParameter("enddate", end).setParameter("accountid", accountid).getResultList();
        logger.log(Level.INFO, "Result size{0}", r.size());
        return r;
    }
    
    public BigDecimal calcBookBalance(Date start, Date end, int accountid) {
        List<Integer> accs = new ArrayList<>();
        accs.add(11201);
        javax.persistence.Query q = em.createNamedQuery("Entry.aggregate").setParameter("start", start)
        .setParameter("end", end).setParameter("acclist", accs);
        BigDecimal o1 = BigDecimal.ZERO;
        for (Object o : q.getResultList()) {
            Object[] ar = (Object[])o;
            logger.log(Level.INFO, "entry {0}:{1}", ar);
            logger.log(Level.INFO, "class name {0}", ar[1].getClass().getName());
            o1 = (BigDecimal)(ar[1]);
        }
        return o1;
    }

    public BigDecimal getOpening29001(Date begin, Date end) {
        List<Entry> loans = em.createQuery("SELECT e FROM Entry e WHERE e.account.id = 29001 and e.date1 BETWEEN :begin and :end ORDER BY e.date1", Entry.class)
                .setParameter("begin", begin).setParameter("end", end).setMaxResults(1).getResultList();
        if (loans.isEmpty())
            return BigDecimal.ZERO;
        return loans.get(0).getAmount();
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