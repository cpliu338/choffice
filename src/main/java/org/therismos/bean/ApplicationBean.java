package org.therismos.bean;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

/**
 * Global config and functions
 * TODO: test it
 * @author cp_liu
 */
@jakarta.inject.Named
@jakarta.inject.Singleton
public class ApplicationBean {
    private Properties properties;
    @jakarta.annotation.Resource
    private String datapath;
    static final Logger LOG = Logger.getLogger(ApplicationBean.class.getName());
    
    @jakarta.annotation.PostConstruct
    public void init() {
        properties = new Properties();
        if (datapath == null)
            datapath = "/home/cp_liu/Documents/java_dir";
        Path path = Paths.get(datapath, "config", "choffice_test.properties");
        try (FileInputStream input = new FileInputStream(Paths.get(datapath, "config", "choffice_test.properties").toFile())) {
            if (input == null) {
                throw new IOException("Sorry, unable to find " + datapath);
            }
            properties.load(input);
        }
        catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
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
}
