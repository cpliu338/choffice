package org.therismos.bean;

/**
 * A datasource for test only
 * @since version 7.0
 * @author cp_liu
 */
import java.io.PrintWriter;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleConnectionDataSource implements DataSource {

  private Connection connection;
  String jdbcUrl;
  //int connect_count = 0;
  
  private void reconnect() {
        // = "jdbc:mariadb://db-01:3306/emis?user=webapp&password=asd82KK";
        try {
            connection = DriverManager.getConnection(jdbcUrl);
        } catch (SQLException ex) {
            Logger.getLogger(SingleConnectionDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
      
  }
  
  public SingleConnectionDataSource(String jdbcUrl) throws SQLException {
      this.jdbcUrl = jdbcUrl;
  }

  @Override
  public Connection getConnection() throws SQLException {
      if (connection==null || connection.isClosed()) this.reconnect();
      return connection;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    // Not supported in this implementation as we manage a single connection
    throw new UnsupportedOperationException("getConnection(username, password) not supported");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isAssignableFrom(SingleConnectionDataSource.class)) {
      return (T) this;
    }
    throw new SQLException("Cannot unwrap to " + iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isAssignableFrom(SingleConnectionDataSource.class);
  }

  /* You can override finalize() for logging purposes but not rely on it for closing
  @Override
  protected void finalize() throws Throwable {
      super.finalize();
      this.close();
  }*/

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLogWriter(PrintWriter writer) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLoginTimeout(int i) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    public void close() throws SQLException {
        if (connection != null) {
            System.out.println("SingleConnectionDataSource connection closing"); // Log for debugging
          connection.close();
        }
        System.out.println("SingleConnectionDataSource connection closed"); // Log for debugging
        connection = null;
    }
}
