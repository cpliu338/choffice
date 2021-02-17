package org.therismos.ejb;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
//import org.joda.time.DateTime;
import org.therismos.model.AccountModel;

/**
 *
 * @author USER
 */
@javax.inject.Named
@javax.enterprise.context.ApplicationScoped
public class MongoService implements java.io.Serializable {
    
    @Resource(name="java:comp/env/mongodb/MyMongoClient")
    private MongoClient mongoClient;
    MongoDatabase db;
    CodecRegistry pojoCodecRegistry;
    /*  
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
*/
    MongoCollection collCheques;
    MongoCollection<AccountModel> collAccounts;
    List<AccountModel> results;
    private java.util.Map<String, Double> totals;
    static final Logger logger = Logger.getLogger(MongoService.class.getName());

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
        if (code2.length()>level && (code2.charAt(0)=='4' || code2.charAt(0)=='5'
            || (code2.charAt(0)=='1' && code2.charAt(1)!='1') // not to summarize code 11xxx for bank accounts
        ))
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
            db = mongoClient.getDatabase("therismos");
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        /*
        if (!db.authenticate("therismos","26629066".toCharArray()))
            throw new RuntimeException("Cannot authenticate");
        collBudgets = db.getCollection("budget", BudgetModel.class);
                */
        //collBudgets.setObjectClass(BudgetModel.class);
        collAccounts = db.getCollection("accounts", AccountModel.class).withCodecRegistry(this.pojoCodecRegistry);
        collCheques = db.getCollection("reconcile");
        logger.log(Level.INFO, "Number of documents in reconcile: {0}", collCheques.countDocuments());
        collAccounts.find(AccountModel.class).sort(Sorts.ascending("code")).forEach(new Block<AccountModel>() {
            @Override
            public void apply(AccountModel t) {
                results.add(t);
            }
        });
    }
    
    public void saveCheques(Document o) {
        String end = o.getString("end");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DeleteResult r = collCheques.deleteMany(Filters.eq("end", end));
        long nDeleted = r.getDeletedCount();
        logger.log(Level.INFO, "Deleted {0} records with the same end date", nDeleted);
        LocalDate yearAgo;
        try {
            yearAgo = java.time.LocalDate.parse(end, fmt).minusYears(2);
            r = collCheques.deleteMany(Filters.lt("end", yearAgo.format(fmt)));
            logger.log(Level.INFO, "Deleted {0} records before {1}", 
                    new Object[]{r.getDeletedCount(), yearAgo});
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        collCheques.insertOne(o);
    }
    
    public MongoCollection<AccountModel> getAccountCollection() {
        return (mongoClient==null) ? null : collAccounts;
    }
    
    public MongoCollection getChequeCollection() {
        return (mongoClient==null) ? null : collCheques;
    }

    //@PreDestroy
    public void cleanup() {
        if (mongoClient != null)
            mongoClient.close();
    }
    
    public Document getCheques(String endDate) {
        FindIterable<Document> it = collCheques.
                find(Filters.eq("end", endDate), Document.class);
        return it.first();
    }
    
    public Stack<String> getChequeDates() {
        Stack<String> stack = new Stack<>();
        collCheques.find(Document.class)
            .sort(Sorts.ascending("end")).forEach(new Block<Document>() {
            @Override
            public void apply(Document t) {
                stack.add(t.getString("end"));
            }
        });
        return stack;
    }

    public AccountModel getAccountByCode(String code) {
        for (AccountModel a : results) {
            if (a.getCode().equals(code)) return a;
        }
        return null;
    }
    
}
