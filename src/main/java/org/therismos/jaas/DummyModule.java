package org.therismos.jaas;

import java.io.IOException;
import java.sql.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import java.util.*;
import java.util.logging.*;
import javax.naming.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.*;
import org.apache.commons.codec.digest.DigestUtils;
/**
 * Updated for Jakarta EE 10
 * TODO to be tested
 * JAAS module using MariaDB and md5 hashed password
 * @author cp_liu
 */
public class DummyModule implements LoginModule {

    /**
     * @return the ds
     */
    public DataSource getDs() {
        return ds;
    }

  private CallbackHandler handler;
  private Subject subject;
  private UserPrincipal userPrincipal;
  private RolePrincipal rolePrincipal;
  private String login;
  private List<String> userGroups;
  private HashMap<String,String> map;
  //private Properties props;
  private DataSource ds;
  private String ds_properties;

    /**
     * @param ds the ds to set
     */
    public void setDs_properties(String ds) {
        this.ds_properties = ds;
    }
    
  @Override
  public void initialize(Subject subject,
      CallbackHandler callbackHandler,
      Map<String, ?> sharedState,
      Map<String, ?> options) {
        userGroups = new ArrayList();
        map = new HashMap<>();
        handler = callbackHandler;
        this.subject = subject;
      Context initContext;
        try {
            if (ds_properties == null) {
                initContext = new InitialContext();
                ds = (DataSource)initContext.lookup("java:openejb/Resource/churchDB");
            }
            else {
                ds = SimpleDatasource.createDataSource(ds_properties);
            }
            if (getDs() == null) {
                throw new RuntimeException("Cannot make datasource");
            }
        } catch (NamingException | SQLException | IOException | RuntimeException ex) {
            Logger.getLogger(DummyModule.class.getName()).log(Level.SEVERE, null, ex);
        }
  }
  
@Override
  public boolean login() throws LoginException {

    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("login");
    callbacks[1] = new PasswordCallback("password", true);

    try (Connection conn = getDs().getConnection()) {
      handler.handle(callbacks);
      String name = ((NameCallback) callbacks[0]).getName();
      String password = String.valueOf(((PasswordCallback) callbacks[1])
          .getPassword());
      if (conn==null) 
          throw new RuntimeException("Connection is null");
      else {
          PreparedStatement stmt=conn.prepareStatement("SELECT name FROM members WHERE nickname=? AND pwd=?");
          stmt.setString(1, name);
          stmt.setString(2, DigestUtils.md5Hex(password));
          ResultSet rs = stmt.executeQuery();
          if (rs.next()) {
            map.put("givenName", rs.getString(1));
            map.put("nickname", name);
              rs.close();
              stmt.close();
              stmt=conn.prepareStatement("SELECT role FROM groups WHERE user=?");
              stmt.setString(1, name);
              rs = stmt.executeQuery();
              while (rs.next()) {
                userGroups.add(rs.getString(1));
              }
          }
          else {
              throw new RuntimeException("Wrong pwd");
          }
          rs.close();
          stmt.close();
          conn.close();
      }
        login = name;
        return true;
     } catch (RuntimeException | SQLException | IOException | UnsupportedCallbackException e) {
          throw new LoginException(e.getMessage());
     }

  }
  
  @Override
  public boolean commit() throws LoginException {

    userPrincipal = new UserPrincipal(login);
    userPrincipal.setMap(map);
    subject.getPrincipals().add(userPrincipal);

    if (userGroups != null && userGroups.size() > 0) {
      for (String groupName : userGroups) {
        rolePrincipal = new RolePrincipal(groupName);
        subject.getPrincipals().add(rolePrincipal);
      }
    }

    return true;
  }

  @Override
  public boolean abort() throws LoginException {
    return false;
  }

  @Override
  public boolean logout() throws LoginException {
    subject.getPrincipals().remove(userPrincipal);
    subject.getPrincipals().remove(rolePrincipal);
    return true;
  }
    
}
