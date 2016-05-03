package org.therismos.web;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import org.therismos.ejb.MonthlyReportTask;

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