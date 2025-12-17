package org.therismos.job;
import java.util.Date;
import java.util.concurrent.Callable;
import org.bson.Document;

/**
 * A job that invoked from a Restful call, a managedExecutorService call this
 * @since Ver 7.0
 * @author cp_liu
 */
public interface Job extends Callable<Document> {
    public String getType();
    public void setType(String type);
    public long getCreate_ts();
    public void setCreate_ts(long ts);
    public String getFilePattern();
    public String getFilePrefix();
    public String getFileExtension();
    public String getFileDesc();
}
