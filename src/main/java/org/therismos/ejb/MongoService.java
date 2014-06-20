package org.therismos.ejb;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.*;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import org.therismos.model.BudgetModel;

/**
 *
 * @author USER
 */
@Singleton
@Startup
public class MongoService {
    
    MongoClient mongoClient;
    DBCollection coll;
    
    @PostConstruct
    public void init() {
        try {
            mongoClient = new MongoClient("ds043358.mongolab.com", 43358);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MongoService.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        DB db = mongoClient.getDB("therismos");
        if (!db.authenticate("therismos","26629066".toCharArray()))
            throw new RuntimeException("Cannot authenticate");
        coll = db.getCollection("budget");
        coll.setObjectClass(BudgetModel.class);
    }
    
    @Produces
    public DBCollection getBudgetCollection() {
        return (mongoClient==null) ? null : coll;
    }
            
    @PreDestroy
    public void cleanup() {
        if (mongoClient != null)
            mongoClient.close();
    }
    
}
