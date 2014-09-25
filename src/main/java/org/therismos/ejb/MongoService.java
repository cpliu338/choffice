package org.therismos.ejb;

import com.mongodb.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.*;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.therismos.model.AccountModel;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@Singleton
@Startup
public class MongoService {
    
    MongoClient mongoClient;
    DB db;
    DBCollection collBudgets, collAccounts;
    List<AccountModel> results;
    private java.util.Map<String, Double> totals;

    public MongoService() {
        results = new ArrayList<>();
    }
    
    public void clearTotals() {
        if (totals == null)
            totals = new HashMap<>();
        else
            totals.clear();
    }
    
    //@Override
    public void reckon(int level, String code2, Double amount) {
        if (level < 1) throw new java.lang.IllegalArgumentException("Level must be at least 1");
        String code = code2;
        if (code2.length()>level && (code2.charAt(0)=='4' || code2.charAt(0)=='5'))
            code = code2.substring(0, level).concat("0");
        if (totals.containsKey(code)) 
            totals.put(code, totals.get(code)+amount);
        else
            totals.put(code, amount);
    }

    /**
     * @return the totals
     */
    //@Override
    public java.util.Map<String, Double> getTotals() {
        return totals;
    }
    
    @PostConstruct
    public void init() {
        results.clear();
        try {
            mongoClient = new MongoClient(); //"ds043358.mongolab.com", 43358);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MongoService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        db = mongoClient.getDB("therismos");
        /*
        if (!db.authenticate("therismos","26629066".toCharArray()))
            throw new RuntimeException("Cannot authenticate");
                */
        collBudgets = db.getCollection("budget");
        collBudgets.setObjectClass(BudgetModel.class);
        collAccounts = db.getCollection("accounts");
        collAccounts.setObjectClass(AccountModel.class);
        Iterator<DBObject> it = collAccounts.find().sort(new BasicDBObject("code",1)).iterator();
        while (it.hasNext()) {
            results.add((AccountModel)it.next());
        }
    }
    
    public DBCollection getBudgetCollection() {
        return (mongoClient==null) ? null : collBudgets;
    }
            
    public DBCollection getAccountCollection() {
        return (mongoClient==null) ? null : collAccounts;
    }
    
    @PreDestroy
    public void cleanup() {
        if (mongoClient != null)
            mongoClient.close();
    }
    
    public List<DBObject> getAccountsBelow(String summaryCode) throws UnknownHostException {
        DBObject match = new BasicDBObject("code", new BasicDBObject("$regex", "^"+summaryCode) );
        DBCursor cursor = collAccounts.find(match);
        return cursor.toArray();
    }

    public AccountModel getAccountByCode(String code) {
        for (AccountModel a : results) {
            if (a.getCode().equals(code)) return a;
        }
        return null;
    }
    
    public Object getConfig(String key) {
        try {
            return db.getCollection("config").findOne(new BasicDBObject("key",key)).get("value");
        }
        catch (Exception npe) {
            return npe;
        }
    }
    
    public java.util.List<AccountModel> getAccounts() {
        return results;
    }
    
}
