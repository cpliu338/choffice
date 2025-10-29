package org.therismos.dataResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Map;
import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
    @Test 
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

    public void testX(String sortKey) throws Exception {
        System.out.println("test x");
            Comparator<Path> secondaryComparator;
            final LinkOption[] followLinks = new LinkOption[]{}; // Follow symbolic links
            secondaryComparator = switch (sortKey) {
                case NAME -> Comparator.comparing(Path::getFileName);
                case DATE -> (path1, path2) -> {
                    try {
                        // Read attributes, following the link
                        BasicFileAttributes attr1 = Files.readAttributes(path1, BasicFileAttributes.class, followLinks);
                        BasicFileAttributes attr2 = Files.readAttributes(path2, BasicFileAttributes.class, followLinks);
                        // Newest first: path2 (newest) compared to path1 (oldest)
                        return attr2.lastModifiedTime().compareTo(attr1.lastModifiedTime());
                    } catch (IOException e) {
                        return 0; // Treat as equal on error
                    }
                };
                case SIZE -> (path1, path2) -> {
                    try {
                        // Files.size() automatically follows links by default, but let's be explicit
                        long size1 = Files.size(path1);
                        long size2 = Files.size(path2);
                        return Long.compare(size1, size2);
                    } catch (IOException e) {
                        return 0; // Treat as equal on error
                    }
                };
                default -> Comparator.comparing(Path::getFileName);
            }; // Compares file names alphabetically
        
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
