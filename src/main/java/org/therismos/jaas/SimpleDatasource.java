package org.therismos.jaas;

import java.sql.*;
import java.io.*;
import java.util.*;
import javax.sql.DataSource;
import java.nio.file.Paths;
import org.mariadb.jdbc.MariaDbDataSource;

/**
 *
 * @author cp_liu
 */
public class SimpleDatasource {
   /**
     * Creates and configures a MariaDB DataSource from the specified properties file.
     *
     * @param propertiesFileName The name of the properties file in the classpath.
     * @return A configured javax.sql.DataSource.
     * @throws SQLException If a database access error occurs.
     * @throws IOException  If the properties file cannot be read.
     */
    public static DataSource createDataSource(String propertiesFileName) throws SQLException, IOException {
        // 1. Load database properties from the file
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(Paths.get(propertiesFileName).toFile())) {
            if (input == null) {
                throw new IOException("Sorry, unable to find " + propertiesFileName);
            }
            props.load(input);
        }

        // 2. Create and configure the MariaDbDataSource object
        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(props.getProperty("db.url"));
        dataSource.setUser(props.getProperty("db.user"));
        dataSource.setPassword(props.getProperty("db.password"));

        return dataSource;
    }
    
}
