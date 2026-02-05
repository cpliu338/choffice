package org.therismos.bean;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.io.*;
import org.bson.Document;
import org.therismos.dataResolver.SMBService;
/**
 * SMB Service
 *
 * @author cp_liu
 */
@Path("smb")
@RequestScoped
public class SmbResource {

    @jakarta.inject.Inject
    ApplicationBean appBean;

    /**
     * Creates a new instance of SmbResource
     */
    public SmbResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/download")
    public Response downloadFile(@QueryParam("path") String path, @QueryParam("root") String root) {
        // Extract filename for Content-Disposition
        String fileName = "download";
        if (path != null && path.contains("/")) {
            fileName = path.substring(path.lastIndexOf("/") + 1);
        } else if (path != null) {
            fileName = path;
        }
        StreamingOutput streamingOutput = (OutputStream entityStream) -> {
            // Call your service to get the source stream
            SMBService smbService = new SMBService();
            smbService.setAppBean(appBean);
            try (InputStream smbInputStream = smbService.getFile(root, path)) {
                // Pipe the data directly to the HTTP response
                smbInputStream.transferTo(entityStream);
                entityStream.flush();
            } catch (Exception e) {
                throw new WebApplicationException("File Transfer failed", e);
            }
        };
        return Response.ok(streamingOutput).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
    }
    /**
     * PUT method for updating or creating an instance of SmbResource
     * @param args should contain server, shareName, user, pass, targetPath and newPath
     * @param content representation for the resource
     * @return 
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/rename")
    public Response rename(java.util.Map<String,Object> map) {
        SMBService smbService = new SMBService();
        smbService.setAppBean(appBean);
        boolean result = false;
        Document args = new Document(); args.putAll(map);
        try {
            if (!args.containsKey("dryRun"))
                result = smbService.renameFile(args.getString("server"), args.getString("shareName"), 
                    args.getString("user"), args.getString("pass"), args.getString("targetPath"), args.getString("newPath"));
        } catch (IOException ex) {
            System.getLogger(SmbResource.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            args.put("exception_class", ex.getClass().getName());
            args.put("exception_message", ex.getMessage());
        }
        ResponseBuilder builder = (result) ? Response.accepted() : Response.status(400);
        return builder.entity(args).build();
    }
}
