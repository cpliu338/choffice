package org.therismos.dataResolver;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
//import com.hierynomus.mssmb2.SMB2AccessMask;

import java.io.*;
import java.net.URL;
import java.util.EnumSet;
import org.therismos.bean.ApplicationBean;

/**
 * Manage an SMB share, but with a dry-run option when smbd is not reachable
 * @author cp_liu
 */
public class SMBService {

    /**
     * @return the appBean
     */
    public ApplicationBean getAppBean() {
        return appBean;
    }

    /**
     * @param appBean the appBean to set
     */
    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    private ApplicationBean appBean;
    
    public static void main(String[] args) {
        java.util.Properties props = new java.util.Properties();
        SMBService service = new SMBService();
        boolean ret_value = false;
        try {
            props.load(new FileInputStream("/home/cp_liu/Documents/smb.properties"));
            if ("store".equalsIgnoreCase(args[0])) {
                ret_value = service.storeFileFromUrl(props.getProperty("server"), 
                        props.getProperty("shareName"), 
                        props.getProperty("user"), 
                        props.getProperty("password"), 
                        props.getProperty("targetPath"), 
                        props.getProperty("sourceUrl")
                );
            }
            else if ("delete".equalsIgnoreCase(args[0])) {
                ret_value = service.deleteFile(
                        props.getProperty("server"), 
                        props.getProperty("shareName"), 
                        props.getProperty("user"), 
                        props.getProperty("password"), 
                        props.getProperty("targetPath")
                );
            }
            else if ("rename".equalsIgnoreCase(args[0])) {
                ret_value = service.renameFile(props.getProperty("server"), 
                        props.getProperty("shareName"), 
                        props.getProperty("user"), 
                        props.getProperty("password"), 
                        props.getProperty("targetPath"), 
                        props.getProperty("newPath")
                );                           
            }
            System.out.println("Returned value was " + Boolean.toString(ret_value));
        }
        catch (Exception ex) {
            System.err.println(ex.getClass().getName());
            System.err.println(ex.getMessage());
        }
    }
    
    public InputStream getFile(String root, String path) throws IOException {
        String rootFolder = "pastors".equals(root) ? appBean.getNaspath() : appBean.getNaspath2();
        return java.nio.file.Files.newInputStream(java.nio.file.Paths.get(rootFolder, path));
    }

    // Method for @POST: Downloads from URL and uploads to SMB
    public boolean storeFileFromUrl(String server, String shareName, String user, String pass, 
                                 String targetPath, String sourceUrl) throws IOException {
        SMBClient client = new SMBClient();
        boolean ret_value = false;
        try (Connection conn = client.connect(server);
             Session session = conn.authenticate(new AuthenticationContext(user, pass.toCharArray(), ""))) {
            
            try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
                // Open remote remoteFile for writing
                com.hierynomus.smbj.share.File remoteFile = share.openFile(
                    targetPath,
                    EnumSet.of(AccessMask.FILE_WRITE_DATA),
                    null, SMB2ShareAccess.ALL,
                    SMB2CreateDisposition.FILE_OVERWRITE_IF,
                    null
                );

                try (InputStream in = new URL(sourceUrl).openStream();
                     OutputStream out = remoteFile.getOutputStream()) {
                    in.transferTo(out); // Efficiently pipes data
                }
                ret_value = true;
            }
        }
        return ret_value;
    }

    // Method for @PUT: Renames a remoteFile
    public boolean renameFile(String server, String shareName, String user, String pass, 
                           String oldPath, String newPath) throws IOException {
        SMBClient client = new SMBClient();
        boolean ret_value = false;
        try (Connection conn = client.connect(server);
             Session session = conn.authenticate(new AuthenticationContext(user, pass.toCharArray(), ""))) {
            try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
            // You must open the remoteFile with the DELETE access mask to rename it
                com.hierynomus.smbj.share.File remoteFile = share.openFile(
                    oldPath,
                EnumSet.of(AccessMask.DELETE, AccessMask.GENERIC_WRITE),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null);
                try {
                    // This is the actual rename call
                    remoteFile.rename(newPath);
                    ret_value = true;
                }
                finally {
                    if (remoteFile != null) {
                        remoteFile.close();
                    }
                }
            }
        }
        return ret_value;
    }

    // Method for @DELETE: Deletes a remoteFile
    public boolean deleteFile(String server, String shareName, String user, String pass, 
                           String path) throws IOException {
        SMBClient client = new SMBClient();
        try (Connection conn = client.connect(server);
             Session session = conn.authenticate(new AuthenticationContext(user, pass.toCharArray(), ""))) {
            try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
                if (share.fileExists(path)) {
                    share.rm(path);
                    return true;
                }
                else 
                    return false;
            }
        }
    }
}