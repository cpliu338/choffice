package org.therismos.dataResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import org.therismos.bean.ApplicationBean;

/**
 * Manage a read-only FS share
 * @author cp_liu
 */
public class FSService {

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

    protected ApplicationBean appBean;

    /**
     * 
     * @param root naspath or naspath2
     * @param path
     * @return
     * @throws IOException 
     */
    public InputStream getFile(String root, String path) throws IOException {
        String rootFolder = appBean.getNasRoot(root);
        return java.nio.file.Files.newInputStream(java.nio.file.Paths.get(rootFolder, path));
    }
    
    public boolean storeFileFromUrl(String server, String shareName, String user, String pass, 
                                 String targetPath, String sourceUrl) 
            throws IOException {
        //throw new java.lang.UnsupportedOperationException();
        String rootFolder = appBean.getNasRoot(shareName);
        Path path = Paths.get(rootFolder, targetPath);
        new URL(sourceUrl).openStream().transferTo(Files.newOutputStream(path));
        return true;
    }

    public boolean renameFile(String server, String shareName, String user, String pass, 
                           String oldPath, String newPath) throws IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    public boolean deleteFile(String server, String shareName, String user, String pass, 
                           String path) throws IOException {
        throw new java.lang.UnsupportedOperationException();
    }
}
