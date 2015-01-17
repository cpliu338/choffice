package org.therismos.ejb;

import com.mongodb.*;
import java.util.*;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
public class ChequeDao {
    @javax.ejb.EJB
    MongoService mongoService;
    DBCollection coll;
    static final Logger logger = Logger.getLogger(ChequeDao.class.getName());
    
    @javax.annotation.PostConstruct
    public void init() {
        coll = mongoService.getChequeCollection();
    }
    
    public Stack<String> getChequeDates() {
        DBCursor results=coll.find(new BasicDBObject("accountId","11201"));
        java.util.Iterator<DBObject> it = results.iterator();
        Stack<String> ar = new Stack<>();
        while (it.hasNext()) {
            String s = it.next().get("end").toString();
            ar.push(s);
        }
        return ar;
    }
    
    public DBObject findCheques(String enddate) {
        DBCursor results=coll.find(new BasicDBObject("end",enddate));
        return results.hasNext() ? results.next() : null;
    }
    
}
