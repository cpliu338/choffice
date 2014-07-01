package org.therismos.ejb;

import com.mongodb.*;
import java.util.*;
import java.util.logging.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@javax.inject.Named
public class BudgetDao {
    
    @Inject
    DBCollection coll;
    @PersistenceContext
    private EntityManager em;
    static final Logger logger = Logger.getLogger(BudgetDao.class.getName());

    public List<Map> aggregateEntries(Date start, Date end, BasicDBList acclist) {
        logger.log(Level.INFO, "dates between {0}:{1}", new Object[]{start,end});
        HashMap<Integer,String> acc_ids = new HashMap<>();
        for (Object o : acclist) {
            try {
                DBObject dbo = (DBObject)o;
                String key = dbo.keySet().iterator().next();
                Integer id1 = Integer.parseInt(key);
//                acc_ids.add(id1);
                acc_ids.put(id1, dbo.get(key).toString());
                logger.log(Level.INFO, "acc id: {0}", id1);
            }
            catch (NumberFormatException | NullPointerException ex) {
               logger.log(Level.INFO, "acc: {0}", o);
            }
        }
        Query q = em.createNamedQuery("Entry.aggregate").setParameter("start", start)
        .setParameter("end", end).setParameter("acclist", acc_ids.keySet());
        List<Map> ret = new ArrayList<>();
        Integer i=0;
        for (Object o : q.getResultList()) {
            i++;
            Object[] ar = (Object[])o;
            logger.log(Level.INFO, "entry {0}:{1}", ar);
            HashMap<String,Object> entrySummary = new HashMap<>();
            entrySummary.put("account", ar[0]);
            entrySummary.put("name", acc_ids.get((Integer)ar[0]));
            entrySummary.put("total", ar[1]);
            ret.add(entrySummary);
        }
            logger.log(Level.INFO, "entries: {0}", i);
        return ret;
    }
    
    // The following is copied from a desktop app
    public int remove(int year, String code) {
        return coll.remove(new BasicDBObject("code",code).append("year", year)).getN();
    }
    
    public BudgetModel find(int year, String code) {
//        List<BudgetModel> results = find(new BasicDBObject("code",code).append("year", year));
//        if (results==null || results.isEmpty()) return null;
//        return results.get(0);
        return this.findOne(new BasicDBObject("code",code).append("year", year));
    }
    
    public int save(BudgetModel m) {
        BudgetModel existing;
        if (m.getYear()<2013 || m.getYear()>2030)
            return -4; //invalid year
        //TODO check for invalid code
        if (m.getId()==null) {
            existing = find(m.getYear(), m.getCode());
            if (existing != null)
                return -3; // violate constraint: year+code must be unique
        }
        else {
            existing = (BudgetModel)coll.findOne(m.getId());
            if (existing==null)
                return -1; // invalid objectId
            else if (m.getYear()!=existing.getYear() || !m.getCode().equalsIgnoreCase(existing.getCode())) {
//        System.out.println(m.getYear());
//        System.out.println(existing.getYear());
//        System.out.println("'"+m.getCode()+"'");
//        System.out.println("'"+existing.getCode()+"'");
                return -2; // attempt to change year/code on an existing entry
            }
        }
        return coll.save(m).getN();
    }
    
    public BudgetModel findOne(DBObject crit) {
        return (BudgetModel)coll.findOne(crit);
    }
    
    public List<BudgetModel> find(DBObject crit) {
        List<BudgetModel> results = new ArrayList<>();
        DBCursor cursor;
        cursor = coll.find(crit);
        for (DBObject item : cursor) {
//            System.out.println("Found:"+item.get("_id").toString());
            if (BudgetModel.class.isAssignableFrom(item.getClass())) {
                results.add((BudgetModel)item);
            }
        }
        return results.isEmpty() ? null : results;
    }
    
}
