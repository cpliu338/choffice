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

    /**
     * @param appBean the appBean to set
     */
    @Override
    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    private ApplicationBean appBean;
    
    public MongoDbResolver() {
    }
    
    @Override
    public Document getDefaults() {
        return new Document("collection", "reconcile");
    }    

    /**
     * 
     * @param param Document with collection, filter
     * @return 
     */
    @Override
    public Map<String, Object> getData(Document param) {
        Bson filter = param.get("filter", Bson.class);
        if (filter == null) {
            filter = Filters.eq("accountId", "11201");
        }
        String collection = param.getString("collection");
        Spliterator<Document> it = appBean.getCollection(collection, Document.class).find(filter).spliterator();
        long total_count = it.estimateSize(); // getExactSizeIfKnown()
        List<Map<String,Object>> list = StreamSupport.stream(it, false)
                .map((doc)->{
                    Map<String,Object> m = new HashMap<>();
                    m.putAll(doc);
                    return m;
                })
                .collect(Collectors.toList());
        Map<String,Object> m = new HashMap<>();
        m.put("total_count", total_count);
        m.put("entries", list);
        return m;
    }
    
}
