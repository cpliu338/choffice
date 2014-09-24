package org.therismos.web;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.Model;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.therismos.ejb.MonthlyReportTask;

/**
 *
 * @author cpliu
 */
@Model
public class ReportFileDownload {
    
    public List<String> getReports() {
        File tmpdir = new File(MonthlyReportTask.tmpfolder);
        ArrayList<String> files = new ArrayList<String>();
        if (tmpdir.isDirectory()) {
            File[] results = tmpdir.listFiles(new java.io.FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("Rep") && name.endsWith(".xlsx");
                }
            });
            for (File f : results) {
                String name = f.getName();
                int i = name.indexOf(".xlsx");
                if (i < 4) continue;
                try {
                    Date d = MonthlyReportTask.fmt.parse(name.substring(3, i));
                    // delete reports older than 10 days
                    if (d.before(new Date(System.currentTimeMillis()-10*86400000L))) {
                        f.delete();
                        continue;
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(ReportFileDownload.class.getName()).log(Level.SEVERE, null, ex);
                    continue;
                }
                files.add(name);
            }
        }
        return files;
    }
    
    private String fname;
    
    public StreamedContent getFile() {
        DefaultStreamedContent c;
        File f = new File(MonthlyReportTask.tmpfolder, fname);
        FileInputStream stream;
        try {
            stream = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReportFileDownload.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new DefaultStreamedContent(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fname);
    }
    
    public String translate(String name) {
        int i = name.indexOf(".xlsx");
        String suffix = "";
        try {
            if (i < 4) throw new ParseException(name, 1);
            Date d = MonthlyReportTask.fmt.parse(name.substring(3, i));
            Double diff = (System.currentTimeMillis()-d.getTime())/1000.0;
            if (diff > 2*86400.0) // two days ago
                suffix = String.format("%.1f days", diff/86400.0);
            else if (diff > 2*3600.0) // two hours ago
                suffix = String.format("%.1f hours", diff/3600.0);
            else if (diff > 2*60.0) // two minutes ago
                suffix = String.format("%.1f minutes", diff/60.0);
            else 
                suffix = String.format("%.1f seconds", diff);
        } catch (ParseException ex) {
            Logger.getLogger(ReportFileDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
        return String.format("%s (%s ago)", name, suffix);
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