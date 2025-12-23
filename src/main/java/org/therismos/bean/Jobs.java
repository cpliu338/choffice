package org.therismos.bean;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.bson.Document;
import org.therismos.job.AbstractJob;
import org.therismos.job.Job;
import org.therismos.job.JobInfo;

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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JobInfo> getJobs() {
        return applicationBean.getJobs();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitJob(String body) {
        Document requestBody = Document.parse(body);
        String type = requestBody.getString("type");
        try {
            Job jobCallable = AbstractJob.createJob(type, applicationBean, requestBody, AbstractJob.class);
            UUID jobId = applicationBean.submit(
                type,
                jobCallable,
                60_000L
            );
            return Response.accepted()
                       .entity(Map.of("jobId", jobId))
                       .build();
        } catch (Exception ex) {
            System.getLogger(Jobs.class.getName()).log(System.Logger.Level.DEBUG, (String) null, ex);
            Document doc = new Document();
            doc.append("exception-class", ex.getClass().getName());
            doc.append("exception-message", ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(doc).build();
        }

    }
    
}
