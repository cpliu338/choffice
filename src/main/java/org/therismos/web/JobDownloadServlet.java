package org.therismos.web;

import jakarta.inject.Inject;
import java.io.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;
import org.therismos.job.JobInfo;

/**
 *
 * @author cp_liu
 */
@WebServlet("/job")
public class JobDownloadServlet extends HttpServlet {

    @Inject
    protected ApplicationBean appBean; // Injected once here, available to children
    protected File tempDir;

    /**
     * TomEE will instantiate this class, inject appBean, call @PostConstruct no servletContext yet, call init() 
     * @throws ServletException 
     */
    @Override
    public void init() throws ServletException {
        // servletContext is only available at this stage
        tempDir = (File) getServletContext().getAttribute("javax.servlet.context.tempdir");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String downloadFile = request.getParameter("download");
        String pollUuid = request.getParameter("uuid");

        if (downloadFile != null) {
            handleDownload(downloadFile, request, response);
        } else if (pollUuid != null) {
            handlePolling(pollUuid, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing uuid or download parameter");
        }
    }


    private void handlePolling(String uuid, HttpServletResponse response) throws IOException {
        JobInfo jobInfo = appBean.getJob(uuid);
        if (jobInfo == null) {
            response.sendError(404, "Job not found");
            return;
        }
        if (jobInfo.getStatus() == JobInfo.Status.RUNNING) {
            response.setStatus(202);
            response.getWriter().print("PROCESSING");
        } else {
            try {
                Document result = jobInfo.getResult();
                /*File f = new File(result.getString("filename"));
                if (result != null) {
                    if (result.containsKey("filename")) { // for GenerateReceipts
                        f = new File(result.getString("filename"));
                    }
                    else if (result.containsKey("download-path")) { // for AbstractXlsxJob
                        f = new File(result.getString("download-path"));
                        result.put("filename", result.getString("download-path"));
                    }
                }*/
                if (!result.containsKey("filename")) {
                    response.setStatus(400);
                }
                response.setContentType("application/json");
                response.getWriter().print(jobInfo.toJson()); 
                appBean.removeJob(uuid); // Essential cleanup
            } catch (Exception e) {
                response.setStatus(500);
                response.setContentType("text/plain");
                if (e.getCause() == null)
                    response.getWriter().print("Error: " + e.getMessage());
                else
                    response.getWriter().print("Error: " + e.getCause().getMessage());
                appBean.removeJob(uuid);
            }
        }
    }

    private void handleDownload(String filename, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        File file = Paths.get(appBean.getDatapath(), "downloads", filename).toFile();

        if (!file.exists()) {
            response.sendError(404, "File expired or not found");
            return;
        }

        response.setContentType(getServletContext().getMimeType(filename));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            fis.transferTo(os);
        }
        purgeExpiredFiles();
        // Optional: Delete from disk immediately after download
        //file.delete();
    }
    protected void purgeExpiredFiles() {
        long expirationThreshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12);
        // Only delete files that are older than 12 hours
        File[] oldFiles = Paths.get(appBean.getDatapath(), "downloads").toFile().listFiles(new ExpiredTempFileFilter(expirationThreshold));
        if (oldFiles != null) {
            for (File f : oldFiles) {
                f.delete(); 
            }
        }
    }
        
    class ExpiredTempFileFilter implements FileFilter {

        private final long maximumTimestamp;

        public ExpiredTempFileFilter(long minimumTimestamp) {
            this.maximumTimestamp = minimumTimestamp;
        }

        @Override
        public boolean accept(File file) {
            if (!file.isFile()) {
                return false; // Only consider files
            }

            String fileName = file.getName();
            /*
            if (!fileName.startsWith("emis-")) {
                return false; // Only consider files with the emis- prefix
            }*/

            // Extract timestamp from filename (assuming timestamp is separated by hyphens)
            int timestampStartIndex = fileName.lastIndexOf('_');
                    //fileName.indexOf('-', 5); // Start after "emis-"
            int timestampEndIndex = fileName.lastIndexOf('.');
            if (timestampStartIndex < 0 || timestampEndIndex <= timestampStartIndex) {
                return false; // Invalid filename format
            }

            String timestampString = fileName.substring(timestampStartIndex + 1, timestampEndIndex);
            long fileTimestamp;
            try {
                fileTimestamp = Long.parseLong(timestampString);
            } catch (NumberFormatException e) {
                return false; // Invalid timestamp format
            }

            return fileTimestamp < maximumTimestamp; // Accept files older than the max timestamp
        }
    }
    
}