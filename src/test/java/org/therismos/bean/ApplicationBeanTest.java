package org.therismos.bean;

import java.util.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cp_liu
 */
public class ApplicationBeanTest {
    
    ApplicationBean instance;
    
    public ApplicationBeanTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        instance = new ApplicationBean();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of init method, of class ApplicationBean.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        instance.init();
        for (Map.Entry<Object, Object> entry : instance.getProperties().entrySet()) {
            System.out.println(entry.getKey().toString() + ":" + entry.getValue().toString());
        }
        // TODO review the generated test code and remove the default call to fail.
    }
    
}
