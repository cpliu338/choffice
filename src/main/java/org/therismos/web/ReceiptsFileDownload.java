package org.therismos.web;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import javax.faces.bean.ManagedBean;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author cpliu
 */
@ManagedBean
@javax.faces.bean.SessionScoped
public class ReceiptsFileDownload {
    
    @javax.inject.Inject
    UserBean userBean;
    
    public File[] getReports() {
        File receiptsDir = new java.io.File(userBean.getBasePath(), "receipts");
        File[] results=null;
        if (receiptsDir.isDirectory()) {
            results = receiptsDir.listFiles(new java.io.FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".pdf");
                }
            });
        }
        if (results == null) return new File[0];
        return results;
    }
    
    private String fname;
    
    public StreamedContent getFile() {
        DefaultStreamedContent c;
        File receiptsDir = new java.io.File(userBean.getBasePath(), "receipts");
        File f = new File(receiptsDir, fname);
        FileInputStream stream;
        try {
            stream = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReceiptsFileDownload.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new DefaultStreamedContent(stream, "application/pdf", fname);
    }
    
    /**
     * @return the fname
     */
    public String getFname() {
        return fname;
    }

    /**
     * @param fname the fname to set
     */
    public void setFname(String fname) {
        this.fname = fname;
    }
}
