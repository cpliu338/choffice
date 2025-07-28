package org.therismos.web;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.*;
import java.util.logging.*;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 *
 * @author cp_liu
 */
@Named
@ViewScoped
public class AccountBean implements java.io.Serializable {
    static final Logger LOG = Logger.getLogger(AccountBean.class.getName());
    
    @Inject
    ApplicationBean appBean;
    
    public String getDebug() {
        // db.reconcile.find({"pending.extra1": '$7474'}, {"pending":0}).sort({end: 1})
        MongoCollection<Document> coll = appBean.getMongoClient().getDatabase("therismos").getCollection("reconcile");
        int i=0;
        try (MongoCursor<Document> cursor = 
                coll.find(Filters.eq("pending.extra1", "$7474"))
                    .projection(Projections.exclude("pending"))
                    .sort(Sorts.ascending("end")).cursor()) {
            while (cursor.hasNext()) {
                LOG.log(Level.INFO, cursor.next().toJson());
                i++;                
            }
        }
        catch (RuntimeException ex) {
            i = -1;
        }
        return Integer.toString(i);
    }
}
