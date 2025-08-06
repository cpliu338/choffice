package org.therismos.job;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import org.bson.Document;
import org.therismos.bean.ApplicationBean;

/**
 * Base class for any job, get context from Application Bean for:
 * datapath, datasource, mongoclient
 * Jobs result in file downloadable from server
 * @since Ver 7.0
 * @author cp_liu
 */
public abstract class AbstractJob implements Job {

    /**
     * @return the config
     */
    public Document getConfig() {
        return config;
    }
    protected String type;
    protected Date expiry;
    protected final Document config;
    protected final ApplicationBean applicationBean;

    protected AbstractJob(ApplicationBean srv, Document config) {
        this.config = config;
        this.applicationBean = srv;        
    }
    
    protected abstract String getFilePrefix();
    protected abstract String getFileExtension();
    
    public File getDownloadPath() {
        return new File( Paths.get(applicationBean.getDatapath(), "downloads", String.format("%s_%d.%s",
                getFilePrefix(), System.currentTimeMillis(), getFileExtension())).toUri()
        );
    }
    
    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Date getExpiry() {
        return expiry;
    }

    @Override
    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }
    
}
