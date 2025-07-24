package org.therismos.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.therismos.bean.ApplicationBean;

/**
 * TODO: remove sensitive password
 * @author cp_liu
 */
public class DummyModuleTest {
    
    DummyModule instance;
    
    public DummyModuleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        Subject subject = new Subject();
        CallbackHandler callbackHandler = new Handler();
        Map sharedState = null;
        Map options = null;
        instance = new DummyModule();
        instance.setDs_properties("/home/cp_liu/Documents/java_dir/config/choffice_test.properties");
        instance.initialize(subject, callbackHandler, sharedState, options);
        System.out.println("set up " + instance.getDs().toString());
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setDs_properties method, of class DummyModule.
     */
    public void testSetDs_properties() {
        System.out.println("setDs_properties");
        String ds = "";
        instance = new DummyModule();
        instance.setDs_properties(ds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of initialize method, of class DummyModule.
     */
    public void testInitialize() throws Exception {
        System.out.println("initialize");
    }

    /**
     * Test of login method, of class DummyModule.
     */
    @Test
    public void testLogin() throws Exception {
        System.out.println("login");
        assert(instance.getSubject().getPrincipals().isEmpty());
        boolean result = instance.login();
        assert(result);
        assert(instance.commit());
        Subject subject = instance.getSubject();
        for (Principal p : subject.getPrincipals()) {
            if (p instanceof UserPrincipal) {
                UserPrincipal up = (UserPrincipal)p;
                System.out.println("Given Name:" + up.getMap().get("givenName").toString());
                System.out.println("Name:" + up.getName());
            }
            if (p instanceof RolePrincipal) {
                RolePrincipal rp = (RolePrincipal)p;
                System.out.println("Role:" + rp.getName());
            }
        }
        assert(!subject.getPrincipals().isEmpty());
    }

    /**
     * Test of commit method, of class DummyModule.
     */
    public void testCommit() throws Exception {
        System.out.println("commit");
        //DummyModule instance = new DummyModule();
        boolean expResult = false;
        boolean result = instance.commit();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of abort method, of class DummyModule.
     */
    public void testAbort() throws Exception {
        System.out.println("abort");
        // DummyModule instance = new DummyModule();
        boolean expResult = false;
        boolean result = instance.abort();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of logout method, of class DummyModule.
     */
    public void testLogout() throws Exception {
        System.out.println("logout");
        // DummyModule instance = new DummyModule();
        boolean expResult = false;
        boolean result = instance.logout();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    static class Handler implements javax.security.auth.callback.CallbackHandler {

        @Override
        public void handle(Callback[] clbcks) throws IOException, UnsupportedCallbackException {
            ApplicationBean ab = new ApplicationBean();
            ab.init();
            String pwd = ab.getProperties().get("jaas.password").toString();
            for (Callback cb : clbcks) {
                if (cb instanceof NameCallback) {
                    NameCallback ncb = (NameCallback) cb;
                    ncb.setName("cpliu");
                }
                else if (cb instanceof PasswordCallback) {
                    PasswordCallback pcb = (PasswordCallback) cb;
                    pcb.setPassword(pwd.toCharArray());
                }
            }
        }
        
    }
}
