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
    
    @GET
    @Produces("application/json")
    public Response getData() {
        Document result = new Document();
        Resolver resolver = new FilestoreResolver();
        try {
            int page = 1;
            try {
                page = Integer.parseInt(uriInfo.getQueryParameters().getFirst("page"));
            } catch (RuntimeException ignored){}
            int pageSize = 20;
            try {
                pageSize = Integer.parseInt(uriInfo.getQueryParameters().getFirst("pageSize"));
            } catch (RuntimeException ignored){}
            result.append("data", resolver.getData(new Document(
                    "baseFolder", "/home/cp_liu/Documents")
                .append("path", uriInfo.getQueryParameters().getFirst("path")==null ? "" : uriInfo.getQueryParameters().getFirst("path"))
                .append("page", page)
                .append("pageSize", pageSize)
                .append("sortKey", uriInfo.getQueryParameters().getFirst("sort")==null ? NAME : uriInfo.getQueryParameters().getFirst("sort"))
            ));
        } catch (RuntimeException ex) {
            result.append("exception_class", ex.getClass().getName());
            result.append("exception_message", ex.getMessage());
            System.getLogger(NonSqlDatasource.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }
    
}
