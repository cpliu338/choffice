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
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
    
    @jakarta.annotation.Resource
    private ManagedExecutorService managedExecutorService;
    static final Logger LOG = Logger.getLogger(ApplicationBean.class.getName());

    public String getDebug() {
        return mongoClient == null ? "MongoClient not injected" : "MongoClient injected";
                //managedExecutorService == null ? "managedExecutorService not injected" : "managedExecutorService injected";
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
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
    
}
