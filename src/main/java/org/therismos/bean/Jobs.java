package org.therismos.bean;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.annotation.*;
import jakarta.enterprise.concurrent.*;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.*;
import org.bson.Document;
import org.therismos.job.Job;

/**
 * REST Web Service
 *
 * @author cp_liu
 */
@Path("/jobs")
@RequestScoped
public class Jobs {
    
    @Inject
    ApplicationBean applicationBean;
    @Resource
    ManagedExecutorService mes;

    static final Logger logger = Logger.getLogger(Jobs.class.getName());
    
    @Context 
    UriInfo uriInfo;
    @Context 
    HttpHeaders headers;
    @Context
    HttpServletRequest req;
    
    @GET
    @Produces("application/json")
    public Response getJobs(
        @DefaultValue("") @QueryParam("type") String type) {
        List<Document> array = new ArrayList<>();
        applicationBean.getJobList().forEach(f -> {
            Document entity = new Document();
            entity.append("type", f.getType());
            entity.append("expiry", new Date(f.getExpiryMsTimestamp()));
            entity.append("done", f.getFuture().isDone());
            if (f.getFuture().isDone())
                try {
                    entity.append("result", f.getFuture().get());
                }
                catch (InterruptedException | ExecutionException ex) {
                    entity.append("exception-class", ex.getClass().getName());
                    entity.append("exception-cause", ex.getCause().getClass().getName());
                    logger.log(Level.SEVERE, null, ex);
                }
            array.add(entity);
        });
        Document result = new Document();
        return Response.ok(result.append("jobs", array), MediaType.APPLICATION_JSON).build();
    }
    
    public String getHeaders() {
        Document d = new Document();
        d.append("info", uriInfo==null ? "not injected" : uriInfo.getPath());
        headers.getRequestHeaders().keySet().forEach(key -> {
            d.append(key, headers.getRequestHeader(key));
        });
        return d.toJson();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createJob(String body) {
        Document requestBody = Document.parse(body);
        Document doc = new Document();
        try {
            String type = requestBody.getString("type");
            Class clazz = Class.forName("org.therismos.job." + type);
            Job instance =(Job)(clazz.getDeclaredConstructor(ApplicationBean.class, Document.class).newInstance(applicationBean, requestBody));
            Future<Document> future = mes.submit(instance);
            applicationBean.addFuture(type, future, 60000L);
            doc.append("jobs-size", applicationBean.getJobList().size());
            return Response.accepted(doc).build();
        } catch (//InterruptedException | TimeoutException | ExecutionException |
                ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
            doc.append("exception-class", ex.getClass().getName());
            doc.append("exception-message", ex.getMessage());
        }
        return Response.ok(doc).build();
    }

}
