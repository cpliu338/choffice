package org.therismos.ejb;

import com.mongodb.*;
import java.util.*;
import java.util.logging.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.therismos.entity.Account;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@javax.inject.Named
public class BudgetDao implements java.io.Serializable {

    @javax.ejb.EJB
    MongoService mongoService;
    DBCollection coll;
    @PersistenceContext
    private EntityManager em;
    static final Logger logger = Logger.getLogger(BudgetDao.class.getName());
    
    @javax.annotation.PostConstruct
    public void init() {
        coll = mongoService.getBudgetCollection();
    }
    
    public List findAllYears() {
        return coll.distinct(YEAR);
    }
    
    public List<String> findAllCodesInYear(int yr) {
        DBCursor cur = coll.find(new BasicDBObject(YEAR, yr));
        List<String> results = new ArrayList<>();
        while (cur.hasNext()) {
            results.add(cur.next().get(CODE).toString());
        }
        return results;
    }
    
    public List<Account> findAllAccountsUnder(String code) { // List<Account>
        if (code.length() < 2) return Collections.EMPTY_LIST;
        String pattern = code;
        if (code.endsWith("0"))
            pattern = code.substring(0, code.length()-1);
        return em.createNamedQuery("Account.findAccountsUnder", Account.class).setParameter("pattern", pattern+"%").getResultList();
    }

    public List<Map> aggregateEntries(Date start, Date end, List<Account> acclist) {
        logger.log(Level.FINE, "dates between {0} and {1} with {2} accounts", new Object[]{start,end,acclist.size()});
        HashMap<Integer,String> acc_ids = new HashMap<>();
        for (Account a : acclist) {
            try {
                acc_ids.put(a.getId(), a.getNameChi());
                logger.log(Level.FINE, "acc : {0}({1})", new Object[]{a.getId(),a.getCode()});
            }
            catch (RuntimeException ex) {
               logger.log(Level.WARNING, "Problematic acc: {0}", a);
            }
        }
        Query q = em.createNamedQuery("Entry.aggregate").setParameter("start", start)
        .setParameter("end", end).setParameter("acclist", acc_ids.keySet());
        List<Map> ret = new ArrayList<>();
        Integer i=0;
        for (Object o : q.getResultList()) {
            i++;
            Object[] ar = (Object[])o;
            logger.log(Level.FINE, "entry {0}:{1}", ar);
            HashMap<String,Object> entrySummary = new HashMap<>();
            entrySummary.put("account", ar[0]);
            entrySummary.put("name", acc_ids.get(ar[0]));
            entrySummary.put("total", ar[1]);
            ret.add(entrySummary);
        }
            logger.log(Level.FINE, "entries: {0}", i);
        return ret;
    }
    
    public WriteResult remove(int year, String code) {
        return coll.remove(buildCriterion(code, year));
    }
    
    public BudgetModel find(int year, String code) {
        return this.findOne(buildCriterion(code, year));
    }

    private static DBObject buildCriterion(String code, int year) {
        return new BasicDBObject(CODE,code).append(YEAR, year);
    }
    private static final String YEAR = "year";
    private static final String CODE = "code";
    
    public void save(BudgetModel m) {
        WriteResult writeResult;
        if (m.getYear()<2014 || m.getYear()>2047)
            throw new IllegalArgumentException("Invalid year, must be between 2014 and 2047");
        if (this.findAllAccountsUnder(m.getCode()).isEmpty()) {
            throw new IllegalArgumentException("Invalid account "+m.getCode());
        }
        DBObject crit = buildCriterion(m.getCode(),m.getYear());
        DBObject b = coll.findOne(crit);
            BasicDBList entries = new BasicDBList();
            Iterator<Map<String,Object>> it = m.getEntries().iterator();
            while (it.hasNext()) {
                Map map = it.next();
                entries.add(new BasicDBObject(DATE,map.get(DATE)).append(AMOUNT, map.get(AMOUNT)));
            }
        if (b==null) {
            // use crit to build a new DBObject
            Account a = em.createNamedQuery("Account.findByCode", Account.class).setParameter("code", m.getCode()).getSingleResult();
            crit.put(REMARKS, m.getRemarks());
            crit.put(ENTRIES, entries);
            crit.put(NAME_CHI, a.getNameChi());
            writeResult = coll.save(crit);
        }
        else {
            writeResult = coll.update(crit, new BasicDBObject("$set",
                    new BasicDBObject(REMARKS,m.getRemarks())
                    .append(ENTRIES, entries)
                ), false, false);
        }
        if (writeResult.getError()!=null) {
            throw new RuntimeException(String.format("Update error with n=%d, message: %s",writeResult.getN(),writeResult.getError()));
        }
    }
    private static final String NAME_CHI = "name_chi";
    private static final String ENTRIES = "entries";
    private static final String REMARKS = "remarks";
    private static final String AMOUNT = "amount";
    private static final String DATE = "date";
    
    private BudgetModel convertToModel(DBObject o) {
        if (o==null) return null;
        BudgetModel m = new BudgetModel();
        try {
        m.setYear((Integer)o.get(YEAR));
        m.setCode((String)o.get(CODE));
        m.setRemarks((String)o.get(REMARKS));
        m.setName_chi((String)o.get(NAME_CHI));
        BasicDBList l = (BasicDBList)o.get(ENTRIES);
        for (Object o1 : l) {
            DBObject dbo = (DBObject)o1;
            m.addToEntries((String)dbo.get(DATE), (Number)dbo.get(AMOUNT));
        }
                for (Account a : this.findAllAccountsUnder(m.getCode())) {
                    m.addToSubitems(a);
                }
//        l = (BasicDBList)o.get("subitems");
//        for (Object o1 : l) {
//            DBObject dbo = (DBObject)o1;
//            m.addToSubitems((String)dbo.get("code"), (String)dbo.get("name_chi"));
//        }
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return m;
    }
    
    public BudgetModel findOne(DBObject crit) {
        return  convertToModel(coll.findOne(crit));
    }
    
    public List<BudgetModel> find(DBObject crit) {
        List<BudgetModel> results = new ArrayList<>();
        DBCursor cursor;
        cursor = coll.find(crit);
        for (DBObject item : cursor) {
//            if (BudgetModel.class.isAssignableFrom(item.getClass())) {
                results.add(convertToModel(item));
//            }
        }
        return results;//.isEmpty() ? null : results;
    }
    
}
