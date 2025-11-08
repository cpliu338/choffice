package org.therismos.dataResolver;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.therismos.bean.ApplicationBean;

/**
 *
 * Retrieves documents from a MongoCollection
 * where each items as a org.bson.Document
 * 
 *
 * @author cp_liu
 */
public class MongoDbResolver implements Resolver {
    
    Document param;

    /**
     * @param appBean the appBean to set
     */
    @Override
    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    private ApplicationBean appBean;
    
    public MongoDbResolver() {
        param = new Document();
    }
    
    @Override
    public Document getDefaults() {
        return Resolver.super.getDefaults().append("collection", "reconcile");
    }    

    /**
     * 
     * @param param Document with collection, filter
     * @return 
     */
    @Override
    public Map<String, Object> getData(Document param) {
        this.param.putAll(param);
        Document filter;
        try {
            filter = Document.parse(param.getString("filter"));
        }
        catch (Exception ex) {
            filter = new Document();
        }
System.getLogger(MongoDbResolver.class.getName()).log(System.Logger.Level.INFO, "param: {0}", filter.toJson());
        Bson sort;
        try {
            sort = Document.parse(param.getString(SORTKEY));
        }
        catch (Exception ex) {
            sort = new Document("_id", 1);
        }
        String collection = param.getString("collection");
        long total_count = appBean.getCollection(collection, Document.class).countDocuments(filter);
        Spliterator<Document> it = appBean.getCollection(collection, Document.class).find(filter).sort(sort).spliterator();
        
        List<Map<String,Object>> list = StreamSupport.stream(it, false)
                .skip(param.getInteger(PAGE)-1)
                .limit(param.getInteger(PAGESIZE))
                .map((doc)->{
                    Map<String,Object> m = new HashMap<>();
                    m.putAll(doc);
                    return m;
                })
                .collect(Collectors.toList());
        Map<String,Object> m = new HashMap<>();
        m.put(TOTALCOUNT, total_count);
        m.put(ENTRIES, list);
        return m;
    }
    
}
