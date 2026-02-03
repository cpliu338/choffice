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
import jakarta.inject.*;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.ServletContext;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.bson.Document;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.therismos.codec.YearMonthCodec;
import org.therismos.job.JobInfo;

/**
 * Global config and functions
 * TODO: test it
 * @author cp_liu
 */
@Named
@Singleton
public class ApplicationBean implements java.io.Serializable {

    /**
     * @return the naspath2
     */
    public String getNaspath2() {
        return naspath2;
    }

    /**
     * @param naspath2 the naspath2 to set
     */
    public void setNaspath2(String naspath2) {
        this.naspath2 = naspath2;
    }

    private final Map<UUID, JobInfo> jobs = new ConcurrentHashMap<>();

    public UUID submit(
            String type,
            Callable<Document> task,
            long ttlMillis
    ) {
        UUID id = UUID.randomUUID();
        long expiresAt = System.currentTimeMillis() + ttlMillis;

        JobInfo job = new JobInfo(id, type, expiresAt);
        jobs.put(id, job);

        CompletableFuture
            .supplyAsync(() -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, managedExecutorService)
            .whenComplete((result, throwable) -> {
                if (throwable == null) {
                    job.markSuccess(result);
                } else {
                    job.markFailure(throwable.getCause());
                }
            });
        return id;
    }
    
    public void removeJob(String str_uuid) {
        jobs.remove(UUID.fromString(str_uuid));
    }
    /**
     * This method is lock-free, non-blocking, container-safe, extremely cheap
     * No background threads required.
     * @return 
     */
    public List<JobInfo> getJobs() {
        long now = System.currentTimeMillis();

        jobs.entrySet().removeIf(entry -> {
            JobInfo job = entry.getValue();
            if (job.getExpiresAt() < now) {
                job.markExpired();
                return true;
            }
            return false;
        });

        return List.copyOf(jobs.values());
    }
    
    public JobInfo getJob(String uuid) {
        return jobs.get(UUID.fromString(uuid));
    }
        
    /**
     * @return the naspath
     */
    public String getNaspath() {
        return naspath;
    }

    /**
     * @param naspath the naspath to set
     */
    public void setNaspath(String naspath) {
        this.naspath = naspath;
    }

    /**
     * @return the pojoCodecRegistry
     */
    public CodecRegistry getPojoCodecRegistry() {
        return pojoCodecRegistry;
    }

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
    private String naspath;
    @jakarta.annotation.Resource
    private String naspath2;
    @jakarta.annotation.Resource(name="churchDB")
    private DataSource dataSource;
    
    MongoClient mongoClient;
    private CodecRegistry pojoCodecRegistry;
    private String projectStage;
    @Inject
    private ServletContext servletContext;
    
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
            projectStage = servletContext.getInitParameter("jakarta.faces.PROJECT_STAGE");
        }
        catch (RuntimeException r) {
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
        pojoCodecRegistry = fromRegistries(
                fromCodecs(new YearMonthCodec()),
                MongoClientSettings.getDefaultCodecRegistry(), 
                fromProviders(pojoCodecProvider)
        );
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
        return mongoClient.getDatabase(properties.getProperty("mongodb.db")).getCollection(name, clazz).withCodecRegistry(getPojoCodecRegistry());
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
    
    
}
