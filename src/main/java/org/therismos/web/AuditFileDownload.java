package org.therismos.web;

import java.io.File;
//import java.text.ParseException;
import java.util.*;
//import java.util.logging.*;
import javax.enterprise.inject.Model;
//import org.therismos.ejb.MonthlyReportTask;

/**
 *
 * @author cpliu
 */
@Model
public class AuditFileDownload extends AbstractFileDownload {
    
    
    
    @Override
    public List<String> getReports() {
        this.setFilenameFilter(
            new java.io.FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("Aud") && name.endsWith(".xlsx");
                }
            });
        return super.getReports();
    }
    
}