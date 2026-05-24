package org.therismos.bean;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.therismos.entity.Reconcile;

/**
 *
 * @author cp_liu
 */
@Path("/reconcile")
@RequestScoped
public class ReconcileResource {

    @Inject
    private ApplicationBean applicationBean;
    MongoCollection<Reconcile> coll;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        coll = applicationBean.getCollection("reconcile", Reconcile.class);
    }
    
    /**
     * Get the list of end dates available within the specified period
     * @param toStrDate if null, default to today
     * @param fromStrDate default to toStrDate minus 1 year, throws 400 if not before toStrDate
     * @return 
     */
    @GET
    @Path("/end-dates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndDates(@QueryParam("from") String fromStrDate, @QueryParam("to") String toStrDate) {
        Document result = new Document();
        LocalDate toDate, fromDate;
        try {
            toDate = (toStrDate == null) ? LocalDate.now() : LocalDate.parse(toStrDate);
            fromDate = (fromStrDate == null) ? toDate.minusYears(1) : LocalDate.parse(fromStrDate);
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("from date is after to date");
            }
            List list = new ArrayList<String>();
            result.append("from", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append("to", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
            try (MongoCursor<Reconcile> cursor = coll.find(Filters.and(
                    Filters.gte("end", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                    Filters.lte("end", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    )).sort(Sorts.descending("end")).cursor()) {
                while (cursor.hasNext()) {
                    Reconcile r = cursor.next();
                    list.add(r.getEnd());
                }
            }
            result.append("end_dates", list);
        }
        catch (RuntimeException ex) {
            return Response.status(400).entity(handleException(ex, true)).build();
        }
        return Response.ok().entity(result).build();
    }
    
    @GET
    @Path("/{as_on}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOne(@PathParam("as_on") String as_on) {
        try {
            Reconcile r = coll.find(Filters.eq("end", as_on)).first();
            if (r == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            return Response.ok(r).build();
        }
        catch (RuntimeException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(handleException(ex, true)).build();
        }
    }
    
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(Reconcile reconcile) {
        Bson filter = Filters.and(
            Filters.eq("accountId", reconcile.getAccountId()),
            Filters.eq("end", reconcile.getEnd())
        );
        Reconcile existing = coll.find(filter).first();
        boolean inserting = (existing == null);

        if (inserting) {
            reconcile.setId(new ObjectId());
        } else {
            reconcile.setId(existing.getId());
        }

        coll.findOneAndReplace(
                filter,
                reconcile,
                new FindOneAndReplaceOptions().upsert(true));

        return Response.status(inserting ? Response.Status.CREATED : Response.Status.OK)
                    .entity(new Document("_id", reconcile.getId().toString()))
                    .build();
    }

    private Document handleException(Exception ex, boolean log) {
        if (log)
            System.getLogger(ReconcileResource.class.getName()).log(System.Logger.Level.ERROR, (String)null, ex);
        return new Document("exception_class", ex.getClass().getName()).append("message", ex.getMessage());        
    }
}
