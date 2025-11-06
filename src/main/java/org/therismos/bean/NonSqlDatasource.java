package org.therismos.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.IOException;
import java.util.*;
import org.bson.Document;
import org.therismos.dataResolver.*;
import static org.therismos.dataResolver.FilestoreResolver.NAME;

/**
 *
 * @author cp_liu
 */
/**
 * REST Web Service for non SQL data
 *
 * @author cp_liu
 */
@Path("/get-data")
@RequestScoped
public class NonSqlDatasource {
    @Context
    HttpServletRequest req;    
    @Context
    UriInfo uriInfo;
    @jakarta.inject.Inject
    ApplicationBean appBean;
    
    /**
     * GET document with attributes of parameter names and values the FIRST value for that name, i.e. ?ar=a&ar=b&ar2=c 
     * @param uri_info
     * @param defaults the default value for each parameter, with the specified type, if there is no default value, make it a String
     * @return the document 
     */
    public Document queryToDocument(MultivaluedMap<String,String> uri_info, Document defaults) {
        uri_info.forEach((String key, List<String> values)-> {
            String givenValue = values.get(0);
            // TODO type cast exception will default
            if (defaults.containsKey(key)) {
                Object def_value = defaults.get(key);
                if (def_value instanceof Integer) {
                    defaults.put(key, Integer.valueOf(givenValue));
                }
                else if (def_value instanceof Long) {
                    defaults.put(key, Long.valueOf(givenValue));
                }
                else {// if (def_value instanceof String) {
                    defaults.put(key, givenValue);
                }
            }
            else {
                defaults.put(key, givenValue);
            }
        });
System.getLogger(NonSqlDatasource.class.getName()).log(System.Logger.Level.INFO, "params from url with default: {0}", defaults.toJson());
        return defaults;
    }
    
    @GET
    @Produces("application/json")
    public Response getData() {
        Map<String, Object> result = new HashMap<>();
        
        Resolver resolver;
        try {
            String fqcn = "org.therismos.dataResolver." + uriInfo.getQueryParameters().getFirst("type") + "Resolver"; // fully qualified class name
            Class<?> clazz = Class.forName(fqcn);   // Load the class
            Object obj = clazz.getDeclaredConstructor().newInstance(); // Call default constructor
            // Safe cast (if you’re sure the class implements Resolver)
            resolver = (Resolver) obj;
            resolver.setAppBean(appBean);
            result = resolver.getData(this.queryToDocument(uriInfo.getQueryParameters(), resolver.getDefaults()));
/*            

            int page = 1;
            try {
                page = Integer.parseInt(uriInfo.getQueryParameters().getFirst("page"));
            } catch (RuntimeException ignored){}
            int pageSize = 20;
            try {
                pageSize = Integer.parseInt(uriInfo.getQueryParameters().getFirst("pageSize"));
            } catch (RuntimeException ignored){
            }
            Document params = new Document(
                    "baseFolder", datapath)
                .append("path", uriInfo.getQueryParameters().getFirst("path")==null ? "" : uriInfo.getQueryParameters().getFirst("path"))
                .append("page", page)
                .append("pageSize", pageSize)
                .append("sortKey", uriInfo.getQueryParameters().getFirst("sort")==null ? NAME : uriInfo.getQueryParameters().getFirst("sort"));
            result.putAll(resolver.getData(params));
            result.put("type", "Filestore");
*/
        } catch (RuntimeException | ReflectiveOperationException ex) {
            result.put("type", "Error");
            result.put("exception_class", ex.getClass().getName());
            result.put("exception_message", ex.getMessage());
            System.getLogger(NonSqlDatasource.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }
    
}
