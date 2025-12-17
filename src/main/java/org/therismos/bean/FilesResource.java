package org.therismos.bean;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;

/**
 * Files in {datapath}/downloads folder
 * @author cp_liu
 */
@jakarta.ws.rs.Path("/files")
@RequestScoped
public class FilesResource {

    /**
     * @param applicationBean the applicationBean to set
     */
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }
    @Inject
    private ApplicationBean applicationBean;
    
    public List<Document> deleteOldFiles(List<Document> fileDetailsList, int hoursAgo) {
        // 1. Calculate the cutoff timestamp (1 hour ago)
        long cutoffTimestampSeconds = Instant.now()
                                             .minusSeconds(hoursAgo * 3600L) // 3600 seconds in an hour
                                             .getEpochSecond(); 
        return fileDetailsList.stream()
        // Filter the stream to identify documents for files we want to KEEP
        .filter(doc -> {
            String filePath = doc.getString("name");
            Long createdTimestamp = doc.getLong("created");

            // Check if the file is OLD enough to be deleted
            if (createdTimestamp < cutoffTimestampSeconds * 1000) { // createdTimestamp is in ms
                
                // --- File is OLD (candidate for deletion) ---
                Path fileToDelete = Paths.get(filePath);
                
                try {
                    // Check if the file actually exists before attempting deletion
                    if (Files.exists(fileToDelete)) 
                        Files.delete(fileToDelete);                    
                    // Crucial: Return false to filter out this Document from the final list
                    return false; 
                } catch (IOException e) {
                    // Return true to keep this document in the resulting list, 
                    // as the file was NOT successfully deleted.
                    return true;
                }
            } else {
                // --- File is NEW (must be kept) ---
                return true; // Keep this Document in the final list
            }
        })
        // 3. Collect the remaining (kept) documents into a new list
        .collect(Collectors.toList());
    }    
    /**
     * Sample output from GET /webresources/files?type=GenerateReceipts:
     * [
  {
    "name": "/home/cp_liu/Documents/java_dir/downloads/Receipts_202601_1765801170880.pdf",
    "desc": "202601",
    "created": 1765801170880
  },
  {
    "name": "/home/cp_liu/Documents/java_dir/downloads/Receipts_202101_1765801126587.pdf",
    "desc": "202101",
    "created": 1765801126587
  },
  {
    "name": "/home/cp_liu/Documents/java_dir/downloads/Receipts_202601_1765816435889.pdf",
    "desc": "202601",
    "created": 1765816435889
  }
]
     * @param className
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)    
    public Response getFiles(//@DefaultValue("") 
        @QueryParam("type") String className) {
        List<Document> filteredFiles = new ArrayList<>();
        // 1. Fully Qualified Class Name (assuming com.example package)
        String fqcn = "org.therismos.job." + className;
        
        try (Stream<java.nio.file.Path> pathStream = Files.walk(Paths.get(applicationBean.getDatapath(), "downloads"))) {            
            // 2. Load the Class Object using Reflection
            Class<?> filterClass = Class.forName(fqcn);

            // 3. Find the static method getFilePattern(String)
            Method getPatternMethod = filterClass.getMethod("getFilePattern");
            Method getFileDescMethod = filterClass.getMethod("getFileDesc", String.class);

            // 4. Invoke the static method to get the dynamic regex pattern
            // The first argument for static method invocation is null.
            String regexPattern = (String) getPatternMethod.invoke(null);

            // 5. Compile the dynamic regex into a Pattern object for efficient matching
            final Pattern pattern = Pattern.compile(regexPattern);
            filteredFiles.addAll(pathStream
                // 1. Filter for only regular files (excludes directories, symbolic links, etc.)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    // Safety check for null file name
                    if (path.getFileName() == null) {
                        return false;
                    }
                    // Match the filename against the compiled regex pattern
                    return pattern.matcher(path.getFileName().toString()).matches();
                })
                .map((Path path) -> {
                    String desc;
                    try {
                        desc = (String)getFileDescMethod.invoke(null, path.toFile().getName());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        System.getLogger(FilesResource.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                        desc = ex.getClass().getName();
                    }
                    return new Document("name", path.toFile().getAbsolutePath())
                        .append("desc", desc)
                        .append("created", org.therismos.job.AbstractJob.getCreateTs(path.toFile().getName()));
                })
                // 4. Collect the results into a List<File>
                .collect(Collectors.toList())
            );
            return Response.ok(this.deleteOldFiles(filteredFiles, 8)
            ).build();
        }
        catch (java.io.IOException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            System.getLogger(FilesResource.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            return Response.serverError().build();
        }
    }    
}
