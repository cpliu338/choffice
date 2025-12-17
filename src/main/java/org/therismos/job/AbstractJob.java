package org.therismos.job;

import java.io.File;
import java.nio.file.Paths;
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
    protected Document config;
    protected ApplicationBean applicationBean;

    @Override
    public String getFileDesc() {
            return java.time.Instant.ofEpochMilli(create_ts).toString();
    }
    
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
    
    // We use a setter or protected constructor so the factory can inject the description
    protected void init() {        
    }

    /**
     * Factory method to instantiate a Job by class name.
     * @param <T> The target class to create
     * @param className The simple name of the class (e.g., "AJob")
     * @param srv
     * @param config
     * @param type The Class type to avoid manual casting
     * @return the class correctly typed
     * @throws java.lang.Exception
     */
    public static <T extends AbstractJob> T createJob(String className, ApplicationBean srv, Document config, Class<T> type) 
            throws Exception {
        
        // Construct the full package path if necessary
        String packageName = AbstractJob.class.getPackageName();
        Class<?> clazz = Class.forName(packageName + "." + className);
        
        // Create instance and cast it to the generic type T
        T jobInstance = type.cast(clazz.getDeclaredConstructor().newInstance());
        jobInstance.config = config;
        jobInstance.applicationBean = srv;        
        jobInstance.init();
        return jobInstance;
    }
}