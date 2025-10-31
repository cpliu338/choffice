package org.therismos.dataResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * HTTP GET /get-data?type={mandatory}&sort={default name}&direction={default desc}&page={default 1}&page_size={default 20}&path={optional}
 * @author cp_liu
 */
public interface Resolver {
    /**
     * Defines the available keys for sorting directory contents.
     */
    public static final String NAME = "name";
    public static final String DATE = "date";
    public static final String SIZE = "size";
    public static final String SORTKEY = "sort";
    public static final String FILTER = "filter";
    public static final String PAGE = "page";
    public static final String PAGESIZE = "pageSize";
    public static final String TOTALCOUNT = "total_count";
    public static final String ENTRIES = "entries";
    public static final String DIRECTION = "direction";
    
    public Map<String, Object> getData(Document param);
    
    default public Document getDefaults() {
        return new Document(PAGE, 1).append(PAGESIZE, 20).append(DIRECTION, 1);
    }
    
    public void setAppBean(ApplicationBean appBean);
}
