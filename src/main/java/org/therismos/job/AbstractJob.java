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
    protected long create_ts;
    protected final Document config;
    protected final ApplicationBean applicationBean;

    protected AbstractJob(ApplicationBean srv, Document config) {
        this.config = config;
        this.applicationBean = srv;        
    }
    
    protected abstract String getFilePrefix();
    protected abstract String getFileExtension();
    
    public File getDownloadPath() {
        create_ts = System.currentTimeMillis();
        return new File( Paths.get(applicationBean.getDatapath(), "downloads", String.format("%s_%d.%s",
                getFilePrefix(), create_ts, getFileExtension())).toUri()
        );
    }
    
    public static long getCreateTs(String name) {
        int lastUnderscore = name.lastIndexOf("_");
        int lastDot =  name.lastIndexOf(".");
        if (lastDot > lastUnderscore + 1 && lastUnderscore > 1) {
            try {
                return Long.parseLong(name.substring(lastUnderscore + 1, lastDot));
            }
            catch (RuntimeException ex) {
                return 0L;
            }
        }
        return 0L;
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
    public long getCreate_ts() {
        return create_ts;
    }

    @Override
    public void setCreate_ts(long create_ts) {
        this.create_ts = create_ts;
    }
    
}
