package org.therismos.bean;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.configuration.CodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import jakarta.annotation.*;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.faces.context.FacesContext;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.bson.Document;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * Global config and functions
 * TODO: test it
 * @author cp_liu
 */
@jakarta.inject.Named
@jakarta.inject.Singleton
public class ApplicationBean implements java.io.Serializable {

    /**
     * @return the projectStage
     */
    public String getProjectStage() {
        return projectStage;
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param dataSource the dataSource to set
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private static final long serialVersionUID = 1L;
    
    private Properties properties;
    @jakarta.annotation.Resource
    private String datapath;
    @jakarta.annotation.Resource
    private DataSource dataSource;
    
    MongoClient mongoClient;
    CodecRegistry pojoCodecRegistry;
    private List<JobFuture> jobList;
    private String projectStage;
    
    @jakarta.annotation.Resource
    private ManagedExecutorService managedExecutorService;
    static final Logger LOG = Logger.getLogger(ApplicationBean.class.getName());

    public String getDebug() {
        return mongoClient == null ? "MongoClient not injected" : "MongoClient injected";
                //managedExecutorService == null ? "managedExecutorService not injected" : "managedExecutorService injected";
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            projectStage = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("jakarta.faces.PROJECT_STAGE");
        }
        catch (NoClassDefFoundError r) {
            projectStage = "Test";
        }
        properties = new Properties();
        // This should be overwritten
        try (InputStream defaultI = this.getClass().getResourceAsStream("/choffice.properties")) {
            properties.load(defaultI);
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (datapath == null) // if in a test
            datapath = System.getenv("choffice");        
        Path path = Paths.get(datapath, "config", "choffice.properties");
        // This is the real config
        try (InputStream input = Files.newInputStream(path)) {
            if (input == null) {
                throw new IOException("Sorry, unable to find " + datapath);
            }
            properties.load(input);
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        mongoClient = MongoClients.create(properties.getProperty("mongodb.connectString"));
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        if (dataSource == null) {
            try {
                // jdbc:mariadb://db-01:3306/emis?user=webapp&password=asd82KK
                dataSource = new SingleConnectionDataSource(
                        String.format("%s?user=%s&password=%s",
                                properties.getProperty("db.url"), properties.getProperty("db.user"),
                                properties.getProperty("db.password"))
                );
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, (String) null, ex);
            }
        }
    }
        
    public <T> MongoCollection<T> getCollection(String name, Class<T> clazz) {
        return mongoClient.getDatabase(properties.getProperty("mongodb.db")).getCollection(name, clazz).withCodecRegistry(pojoCodecRegistry);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
    public String getProperty(String key) {
        return properties.getOrDefault(key, "undefined").toString();
    }

    /**
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @return the datapath
     */
    public String getDatapath() {
        return datapath;
    }

    /**
     * @param datapath the datapath to set
     */
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }
    
    // Global convenience methods
    
    /**
     * 
     * @param <T> type inferred
     * @param sql the SQL template with IN (%s) to be extrapolated
     * @param inClauseParams the list of params in the IN clause
     * @param beanType type of the Bean
     * @param otherParams other parameters follow the IN clause
     * @return the List of Beans
     * @throws SQLException 
     */
    public <T> List<T> queryWithInClause(String sql, List<?> inClauseParams, Class<T> beanType, Object ... otherParams)
    throws SQLException {
        if (inClauseParams==null || inClauseParams.isEmpty()) 
            return Collections.EMPTY_LIST;
        String placeHolders = inClauseParams.stream().map(p -> "?").collect(Collectors.joining(", "));
        String finalSql = String.format(sql, placeHolders);
        // combine parameters, IN clause first
        Object[] allParams = new Object[inClauseParams.size() + otherParams.length];
        System.arraycopy(inClauseParams.toArray(), 0, allParams, 0, inClauseParams.size());
        System.arraycopy(otherParams, 0, allParams, inClauseParams.size(), otherParams.length);
        return new QueryRunner(getDataSource()).query(finalSql, new BeanListHandler<>(beanType), allParams);
    }
    
    /**
     * Get the job list, side effect: garbage collection
     * Delete jobs expired
     * @return the jobList
     */
    public List<JobFuture> getJobList() {
        List<JobFuture> list1 = new ArrayList<>();
        jobList.forEach(jobFuture -> {
            if (jobFuture.expiryMsTimestamp > System.currentTimeMillis()) {
                list1.add(jobFuture);
            }
            else {
                Future<Document> future = jobFuture.future;
                if (future.isDone()) {
                    try {
                        Document result = future.get();
                        LOG.log(Level.INFO, result.toString());
                    } catch (InterruptedException | ExecutionException ex) {
                        LOG.log(Level.INFO, ex.getClass().getName());
                    }
                }
                LOG.log(Level.INFO, "Expired {0,date,yyyy-MM-dd HH:mm}", new java.util.Date(jobFuture.expiryMsTimestamp));
            }
        });
        jobList = list1;
        return jobList;
    }
    
    public void addFuture(String type, Future<Document> future, long msToExpire) {
        JobFuture jobFuture = new JobFuture();
        jobFuture.type = type;
        jobFuture.future = future;
        jobFuture.expiryMsTimestamp = System.currentTimeMillis() + msToExpire;
        getJobList().add(jobFuture);
        LOG.log(Level.INFO, "job list size is now {0}", jobList.size());
    }
    
    public static class JobFuture {

        /**
         * @return the future
         */
        public Future<Document> getFuture() {
            return future;
        }

        /**
         * @return the expiryMsTimestamp
         */
        public long getExpiryMsTimestamp() {
            return expiryMsTimestamp;
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }
        private Future<Document> future;
        private long expiryMsTimestamp;
        private String type;
    }
    
}
