package org.therismos.dataResolver;

import java.util.stream.*;
import java.util.*;
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
        return new Document(PAGE, 1).append(PAGESIZE, 20).append(DIRECTION, "1");
    }

    /**
     * Get the sort fields, delimited by : or , or | followed optionally by white space
     * @param doc interested in key: sort
     * @return a list of the fields
     */
    default public List<String> getSortFields(Document doc) {
        if (doc.containsKey(SORTKEY)) {
            return Arrays.asList(doc.getString(SORTKEY).split("[:,|]\\s*"));
        }
        else
            return Collections.EMPTY_LIST;
    }

    /**
     * Get the sort directions, delimited by : or , or | followed optionally by white space
     * @param doc interested in key: direction, "1" or starting with "asc" considered ascending, "-1", "desc" considered descending
     * @return a list of the directions, elements longer than sort fields are neglected, if shorting than sort fields
     */
    default List<Integer> getSortDirections(Document doc) {
        if (doc.containsKey(DIRECTION)) {
            String direction = doc.get(DIRECTION).toString(); // bug in Document class, "1" will return as Integer even if I used doc.getString()
            String[] elements = direction.split("[:,|]\\s*");
            final long n_chunks = getSortFields(doc).size();
            return StreamSupport.stream(Arrays.spliterator(elements), false)
                    .map((element) -> {
                        String e = element.toLowerCase();
                        if (e.startsWith("asc"))
                            return 1;
                        else if (e.startsWith("desc"))
                            return -1;
                        else try {
                            return Integer.parseInt(e) >= 0 ? 1 : -1;
                        }
                        catch (RuntimeException ex) {
                            return 1;
                        }
                    }
                    
                    ).limit(n_chunks).collect(Collectors.toList());
        }
        else
            return Collections.EMPTY_LIST;
    }
    
    public void setAppBean(ApplicationBean appBean);
}
