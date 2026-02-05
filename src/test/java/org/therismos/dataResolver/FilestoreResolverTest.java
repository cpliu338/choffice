package org.therismos.dataResolver;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.*;
import com.hierynomus.protocol.commons.EnumWithValue.EnumUtils;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
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
    public void testGetSort() throws Exception {
        System.out.println("get sort");
        Document param = new Document();
        FilestoreResolver r = new FilestoreResolver();
        List<String> sorts = r.getSortFields(param);
        assert(sorts.isEmpty());
        param.put("sort", "size");
        sorts = r.getSortFields(param);
        assert(sorts.size()==1 && sorts.get(0).equals("size"));
        param.put("sort", "size, name");
        sorts = r.getSortFields(param);
        assert(sorts.size()==2 && sorts.get(1).equals("name"));
        param.put("direction", "ascending");
        List<Integer> dirs = r.getSortDirections(param);
        assert(sorts.size()==2 && dirs.size()==1 && dirs.get(0)==1);
        param.put("direction", "1,desc");
        dirs = r.getSortDirections(param);
        for (Integer s: dirs) {System.out.println(s);}
        assert(sorts.size()==2);
        assert(dirs.size()==2);
        assert(dirs.get(0)==1 && dirs.get(1)==-1);
    }
    
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

    public void testMongoDbResolver() throws Exception {
        System.out.println("test MongoDbResolver");
        ApplicationBean appBean = new ApplicationBean();
        appBean.setDatapath(System.getenv("choffice"));
        appBean.init();
        assert(appBean.getMongoClient() != null);
        assert(appBean.getCollection("reconcile", Document.class) != null);
        MongoDbResolver resolver = new MongoDbResolver();
        resolver.setAppBean(appBean);
        Document param = new Document("filter", "{\"accountId\": \"11201\", \"end\": {\"$gt\": \"2024-01-01\"}}");
        param.putAll(resolver.getDefaults());
        Document result = new Document();
        result.putAll(resolver.getData(param));
        System.out.println(result.toJson());
    }
    
    /**
     * Test of getPagedAndSortedDirectoryStream method, of class FilestoreResolver.
     */
    public void testGetPagedAndSortedDirectoryStream() throws Exception {
        System.out.println("getPagedAndSortedDirectoryStream");
        ApplicationBean appBean = new ApplicationBean();
        appBean.setDatapath("/home/cp_liu/Documents/java_dir");//System.getenv("choffice"));
        appBean.init();
        appBean.setNaspath("/home/cp_liu/Documents/Therismos");
        appBean.setNaspath2("/home/cp_liu/Documents/java_dir");
        FilestoreResolver resolver = new FilestoreResolver();
        resolver.setAppBean(appBean);
        Document param = resolver.getDefaults();
        param.put("path", "");
        Document result = new Document();
        result.putAll(resolver.getData(param));
        System.out.println(result.toJson());
    }

    //@Test 
    public void testSmb() {
        // --- EDIT THESE PLACEHOLDERS ---
        String server = "IP";
        String shareName = "share";
        String user = "user";
        String password = "pwd";
        String domain = ""; // Leave empty if not on a domain
        // -------------------------------

        SMBClient client = new SMBClient();

        try (Connection connection = client.connect(server)) {
            AuthenticationContext ac = new AuthenticationContext(user, password.toCharArray(), domain);
            Session session = connection.authenticate(ac);

            // Connect to the specific share
            try (DiskShare share = (DiskShare) session.connectShare(shareName)) {
                System.out.println("Connected to " + shareName + " successfully!");
                System.out.println("Listing root directory content:");
                System.out.println("---------------------------------");

                // List files in the root (empty string "" or "." represents root)
                for (FileIdBothDirectoryInformation item : share.list("")) {
                    String name = item.getFileName();
                    
                    // Filter out the virtual navigation directories "." and ".."
                    if (name.equals(".") || name.equals("..")) continue;

                    // Check if the set of attributes contains the DIRECTORY attribute
                    long attributes = item.getFileAttributes();
                    boolean isDir = EnumUtils.isSet(attributes, FileAttributes.FILE_ATTRIBUTE_DIRECTORY);
                    System.out.printf("[%s] %s%n", isDir ? "DIR " : "FILE", name);
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to SMB server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    
    public void testCIFS() throws Exception {
        System.out.println("test CIFS");
        SMBClient client = new SMBClient();
        try (Connection connection = client.connect("127.0.0.1")) {
            AuthenticationContext ac = new AuthenticationContext("user", "pwd".toCharArray(), "DOMAIN");
            Session session = connection.authenticate(ac);

            try (DiskShare share = (DiskShare) session.connectShare("shareName")) {
                // Read a file
                File remoteFile = share.openFile("path/to/file.txt", 
                    EnumSet.of(AccessMask.FILE_READ_DATA), null, SMB2ShareAccess.ALL, 
                    SMB2CreateDisposition.FILE_OPEN, null);

                // Write a file
                File newFile = share.openFile("upload.txt", 
                    EnumSet.of(AccessMask.FILE_WRITE_DATA), null, SMB2ShareAccess.ALL, 
                    SMB2CreateDisposition.FILE_OVERWRITE_IF, null);
            }            
        }
        catch (Exception ex) {
            System.err.println(ex.getClass().getName());
            System.err.println(ex.getMessage());
        }
    }
    
}
