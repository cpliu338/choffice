package org.therismos.dataResolver;

import com.mongodb.client.model.Filters;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.therismos.bean.ApplicationBean;
import org.therismos.bean.NonSqlDatasource;
import static org.therismos.dataResolver.FilestoreResolver.DATE;
import static org.therismos.dataResolver.FilestoreResolver.NAME;
import static org.therismos.dataResolver.FilestoreResolver.SIZE;

/**
 *
 * @author cp_liu
 */
public class FilestoreResolverTest {
    
    public FilestoreResolverTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getData method, of class FilestoreResolver.
     */
    public void testGetData() throws Exception {
        System.out.println("getData");
        Document param = new Document("baseFolder", "/home/cp_liu/Documents")
                    .append("path", "")
                    .append("page", 1)
                    .append("pageSize", 3)
                    .append("sortKey", null)
            ;
        FilestoreResolver r = new FilestoreResolver();
        Document result = new Document();
        result.putAll(r.getData(param));
        System.out.println(result.toJson());
    }
    
    @Test 
    public void testQueryToDocument() throws Exception {
        System.out.println("queryToDocument");
        NonSqlDatasource ds = new NonSqlDatasource();
        MultivaluedMap<String,String> uri_info = new MultivaluedHashMap();
        uri_info.addAll("page", "3", "4");
        uri_info.add("pageSize", "10");
        uri_info.add("sortKey", "size");
        uri_info.add("baseFolder", "root");
        Document defaults = new Document("page", 1).append("sortKey", "date").append("pageSize", 20);
        defaults = ds.queryToDocument(uri_info, defaults);
        System.out.println(defaults.toJson());
    }

    @Test 
    public void testMongoDbResolver() throws Exception {
        System.out.println("test MongoDbResolver");
        ApplicationBean appBean = new ApplicationBean();
        appBean.setDatapath("/home/cp_liu/Documents/java_dir");
        appBean.init();
        assert(appBean.getMongoClient() != null);
        assert(appBean.getCollection("reconcile", Document.class) != null);
        MongoDbResolver resolver = new MongoDbResolver();
        resolver.setAppBean(appBean);
        Document param = new Document("filter", Filters.eq("accountId", "11201"))
                .append("collection", "reconcile");
        Document result = new Document();
        result.putAll(resolver.getData(param));
        System.out.println(result.toJson());
    }
    
    /**
     * Test of getPagedAndSortedDirectoryStream method, of class FilestoreResolver.
     */
    public void testGetPagedAndSortedDirectoryStream() throws Exception {
        System.out.println("getPagedAndSortedDirectoryStream");
        Path folderPath = null;
        int pageSize = 0;
        int pageOffset = 0;
        String sortKey = "";
        FilestoreResolver instance = new FilestoreResolver();
        instance.getPagedAndSortedDirectoryStream(folderPath, pageSize, pageOffset, sortKey);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
