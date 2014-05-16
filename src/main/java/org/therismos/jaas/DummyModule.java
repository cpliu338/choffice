package org.therismos.jaas;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import java.sql.*;
import javax.naming.*;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.sql.DataSource;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author cpliu
 */
public class DummyModule  implements LoginModule {

  private CallbackHandler handler;
  private Subject subject;
  private UserPrincipal userPrincipal;
  private RolePrincipal rolePrincipal;
  private String login;
  private List<String> userGroups;
  private java.util.HashMap<String,String> map;
  java.util.Properties props;

  @Override
  public void initialize(Subject subject,
      CallbackHandler callbackHandler,
      Map<String, ?> sharedState,
      Map<String, ?> options) {
        userGroups = new ArrayList();
        map = new java.util.HashMap<>();
        handler = callbackHandler;
        this.subject = subject;
  }
  
  public Map<String,String> test(String nickname, String hashed) {
      java.util.HashMap<String, String> map2 = new java.util.HashMap<>();
      try {
          Context initContext = new InitialContext();
          DataSource ds = (DataSource)initContext.lookup("java:openejb/Resource/churchDB");
          Connection conn = ds.getConnection();
          if (conn==null) 
              map2.put("Warning", "Connection is null");
          else {
              PreparedStatement stmt=conn.prepareStatement("SELECT id FROM members WHERE nickname=? AND pwd=?");
              stmt.setString(1, nickname);
              stmt.setString(2, hashed);
              ResultSet rs = stmt.executeQuery();
              if (rs.next()) {
                  map2.put("Id", rs.getString(1));
                  rs.close();
                  stmt.close();
                  stmt=conn.prepareStatement("SELECT role FROM groups WHERE user=?");
                  stmt.setString(1, nickname);
                  rs = stmt.executeQuery();
                  while (rs.next()) {
                      map2.put("group", rs.getString(1));
                  }
              }
              else {
                  map2.put("Info", "Wrong pwd");
              }
              rs.close();
              stmt.close();
              conn.close();
          }
      } catch (NamingException | SQLException ex) {
          Logger.getLogger(DummyModule.class.getName()).log(Level.SEVERE, null, ex);
          map2.put("Exception", ex.getClass().getName());
          map2.put("Message", ex.getMessage());
      }
      return map2;
  }

  @Override
  public boolean login() throws LoginException {

    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("login");
    callbacks[1] = new PasswordCallback("password", true);

    try {
      handler.handle(callbacks);
      String name = ((NameCallback) callbacks[0]).getName();
      String password = String.valueOf(((PasswordCallback) callbacks[1])
          .getPassword());
      Context initContext = new InitialContext();
      DataSource ds = (DataSource)initContext.lookup("java:openejb/Resource/churchDB");
      Connection conn = ds.getConnection();
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
     } catch (RuntimeException | SQLException | NamingException | IOException | UnsupportedCallbackException e) {
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
