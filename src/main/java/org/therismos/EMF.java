package org.therismos;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */

public class EMF implements ServletContextListener {

    private static EntityManagerFactory emf;
    private static String basePath;
    
    public static String getBasePath() { return basePath;}
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        emf = Persistence.createEntityManagerFactory("churchPU");
        basePath = "/var/barcode";
        javax.naming.Context initCtx, envCtx;
        try {
            initCtx = new javax.naming.InitialContext();
            envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            basePath = envCtx.lookup("datapath").toString();
        } catch (javax.naming.NamingException ex) {
            Logger.getLogger(EMF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        emf.close();
    }
    
    public static EntityManager createEntityManager() {
        if (emf==null)
            throw new IllegalStateException("Context is not initialized yet.");
        return emf.createEntityManager();
    }
}
