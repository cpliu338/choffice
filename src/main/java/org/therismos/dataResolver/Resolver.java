package org.therismos.dataResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.bson.Document;

/**
 * HTTP GET /get-data?type={mandatory}&sort={default name}&direction={default desc}&page={default 1}&page_size={default 20}&path={optional}
 * @author cp_liu
 */
public interface Resolver {
    Map<String, Object> getData(Document param);
}
