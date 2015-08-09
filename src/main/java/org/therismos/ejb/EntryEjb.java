package org.therismos.ejb;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.therismos.entity.Account;
import org.therismos.entity.Entry;
import org.therismos.web.PayableBean;

/**
 *
 * @author cpliu
 */
@Stateless
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
    
    public List<Entry> findByEachAccount(List<Integer> accs, Date start, Date end) {
        List<Entry> r = em.createQuery("SELECT e FROM Entry e WHERE e.account.id IN :acclist AND (e.date1 BETWEEN :start AND :end)", Entry.class)
            .setParameter("start", start)
            .setParameter("end", end).setParameter("acclist", accs).getResultList();
        return r;
    }
    
    public List<Entry> findByTransref(int transref) {
        return em.createNamedQuery("Entry.findByTransref", Entry.class)
            .setParameter("transref", transref).getResultList();
    }
    
    /**
     * Charge payable debt to a new/existing entry pair
     * @param sample A sample entry.  Its id is ignored, 
     * transref used to locate entry pair (0 if new pair is needed),
     * amount is the chargeable amount (usually the shortfall in debt)
     * detail is the detail to be used, applicable to both legs of the pair
     * account id is the account (usually debt)
     * date1 is the desired date, applicable to both legs of the pair
     * @param counterpart Account id of the other leg of the entry pair
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void chargePayableShortfall(Entry sample, int counterpart) {
        if (sample == null || sample.getAmount()==null)
            throw  new UnsupportedOperationException("Invalid sample");
        Entry e1, e2;
        if (sample.getTransref() == 0) {
            e1 = sample; e1.setId(null);
            Account a = em.find(Account.class, counterpart==PayableBean.getCMAEXP()?PayableBean.getCMADEBT():PayableBean.getCMAMDEBT());
            e1.setAccount(a);
            a = em.find(Account.class, counterpart);
            e2 = new Entry(); e2.setAccount(a); e2.setAmount(BigDecimal.ZERO.subtract(e1.getAmount()));
            e2.setDate1(e1.getDate1()); e2.setExtra1(""); e2.setDetail(e1.getDetail());
            Entry lastTrans = em.createQuery("SELECT e FROM Entry e ORDER BY e.transref DESC", Entry.class).setMaxResults(1).getResultList().get(0);
            e1.setTransref(lastTrans.getTransref()+1);
            e2.setTransref(e1.getTransref());
        }
        else {
            List<Entry> results = em.createNamedQuery("Entry.findByTransref", Entry.class).setParameter("transref", sample.getTransref()).getResultList();
            if (results.size() != 2) throw new UnsupportedOperationException("Provided transref does not return a pair of (2) entries");
            e1 = results.get(0);
            e1.setDate1(sample.getDate1());
            e1.setDetail(sample.getDetail());
            int targetAccId = e1.getAccount().getId();
            if (targetAccId==sample.getAccount().getId()) {
                e1.setAmount(e1.getAmount().add(sample.getAmount()));
                targetAccId=counterpart;
            }
            else if (targetAccId==counterpart) {
                e1.setAmount(BigDecimal.ZERO.subtract(sample.getAmount()).subtract(e1.getAmount()));
                targetAccId=sample.getAccount().getId();
            }
            else
                throw new UnsupportedOperationException("Provided transref have no entry with account "+targetAccId);
            e2 = results.get(1);
            e2.setDate1(sample.getDate1());
            e2.setDetail(sample.getDetail());
            if (targetAccId==e2.getAccount().getId()) {
                e2.setAmount(BigDecimal.ZERO.subtract(e1.getAmount()));
            }
            else
                throw new UnsupportedOperationException("Provided transref have no entry with account "+targetAccId);
        }
        logger.log(Level.INFO, "e1: {0,number} and {1,number}", new Object[]{e1.getAccount().getId(), e1.getAmount()});
        logger.log(Level.INFO, "e2: {0,number} and {1,number}", new Object[]{e2.getAccount().getId(), e2.getAmount()});
        em.merge(e1);
        em.merge(e2);
    }
    
    public Map<Integer, BigDecimal> aggregateEach(List<Integer> accs, Date start, Date end) {
        javax.persistence.Query q = em.createNamedQuery("Entry.aggregate").setParameter("start", start)
        .setParameter("end", end).setParameter("acclist", accs);
        BigDecimal o1 = BigDecimal.ZERO;
        Map<Integer, BigDecimal> aggregates= new HashMap<>();
        for (Object o : q.getResultList()) {
            Object[] ar = (Object[])o;
            aggregates.put((Integer)ar[0], (BigDecimal)(ar[1]));
        }
        return aggregates;
    }
    
    public BigDecimal aggregate(List<Integer> accs, Date start, Date end) {
        javax.persistence.Query q = em.createNamedQuery("Entry.aggregate").setParameter("start", start)
        .setParameter("end", end).setParameter("acclist", accs);
        BigDecimal o1 = BigDecimal.ZERO;
        for (Object o : q.getResultList()) {
            Object[] ar = (Object[])o;
            logger.log(Level.FINE, "entry {0}:{1}", ar);
            logger.log(Level.FINE, "class name {0}", ar[1].getClass().getName());
            o1 = o1.add((BigDecimal)(ar[1]));
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
/*
    @NamedQuery(name = "Entry.findByAccount", query = "SELECT e FROM Entry e WHERE e.account.id=:account_id AND (e.date1 BETWEEN :start AND :end) ORDER BY e.date1"),
    @NamedQuery(name = "Entry.aggregate", query = "SELECT e.account.id, SUM(e.amount) FROM Entry e WHERE e.account.id IN :acclist AND (e.date1 BETWEEN :start AND :end) GROUP BY e.account.id ORDER BY e.account.id"),
    */    
    public List<Entry> findByAccount(int id, Date d1, Date d2) {
        return em.createNamedQuery("Entry.findByAccount", Entry.class)
                .setParameter("account_id", id)
                .setParameter("start", d1)
                .setParameter("end", d2)
                .setMaxResults(100).getResultList();
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
            buf.append("\"Ref\",\"Date\",\"Account code\",\"Account name\",\"Amount\",\"Detail\"\n");
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
