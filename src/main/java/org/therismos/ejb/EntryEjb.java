package org.therismos.ejb;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
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
    
    public List<Entry> closeYear(Date end1) {
        List<Integer> accept_ids = new ArrayList<>();
        for (Account a : em.createNamedQuery("Account.findAll", Account.class).getResultList()) {
            if (a.getCode().endsWith("0")) continue;
            accept_ids.add(a.getId());
        }
        /*
        DateTime end = new DateTime(end1.getTime());
        DateTime begin = end.withMonthOfYear(1).withDayOfMonth(1);
        DateTime nextYr = begin.withYear(begin.getYear()+1);
        */
        LocalDate end = end1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate begin = end.withMonth(1).withDayOfMonth(1);
        LocalDate nextYr = begin.plusYears(1);
        javax.persistence.Query q = em.createQuery("SELECT e.account.id, SUM(e.amount) FROM Entry e WHERE e.date1>=:begin "
                + "AND e.date1<=:end AND e.account.id IN :ids GROUP BY e.account.id ORDER BY e.account.code");
        List<Object[]> results = q.setParameter("begin", java.sql.Date.valueOf(begin)).setParameter("end", java.sql.Date.valueOf(end))
                .setParameter("ids", accept_ids).getResultList();
        List<Entry> entries = new ArrayList<>();
        // Account 3xx comes before 4xx and 5xx
        Entry equity = null; // Account code 31 General funds
        BigDecimal earnings=BigDecimal.ZERO;
        for (Object[] result : results) {
            Account a = em.find(Account.class, ((Number)result[0]).intValue());
            Entry e = new Entry();
            logger.log(Level.FINE, "Account id:code {0}:{1}", new Object[]{a.getId(), a.getCode()});
            if (a.getCode().startsWith("4") || a.getCode().startsWith("5")) {
                earnings = earnings.add((BigDecimal)result[1]);
                logger.log(Level.FINE, "Add {0} and earning became {1}", new Object[]{(BigDecimal)result[1], earnings});
            }
            else {
                e.setDate1(java.sql.Date.valueOf(nextYr));
                e.setAccount(a);
                e.setAmount((BigDecimal)result[1]);
                e.setDetail("Closing Balance");
                e.setExtra1("");
                e.setTransref(0);
                if (a.getCode().equals("31")) {
                    equity = e;
                }
                else
                    entries.add(e);
            }
        }
        if (equity != null) {
            equity.setAmount(equity.getAmount().add(earnings));
            entries.add(equity);
        }
        return entries;
    }
    
    public List<Entry> getUncheques(Date end, int accountid) {
        List<Entry> r = em.createNamedQuery("Entry.findUnchequesBeforeDate", Entry.class).
            setParameter("enddate", end).setParameter("accountid", accountid).getResultList();
        logger.log(Level.FINE, "Result size{0}", r.size());
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
        logger.log(Level.FINE, "e1: {0,number} and {1,number}", new Object[]{e1.getAccount().getId(), e1.getAmount()});
        logger.log(Level.FINE, "e2: {0,number} and {1,number}", new Object[]{e2.getAccount().getId(), e2.getAmount()});
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
    // This may have been replaced by Accountservice.reckon()
    public BigDecimal aggregate(List<Integer> accs, Date start, Date end) {
        try {
            Object o = em.createQuery("SELECT SUM(e.amount) FROM Entry e WHERE e.account.id IN :acclist AND (e.date1 BETWEEN :start AND :end)")
                    .setParameter("start", start).setParameter("end", end).setParameter("acclist", accs).getSingleResult();
            if (o == null) {
                return BigDecimal.ZERO;
            }
            else {
                logger.log(Level.INFO, "result: {0}", o);
            return (BigDecimal)o;
            }
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return BigDecimal.ZERO; // should never happen
        
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
