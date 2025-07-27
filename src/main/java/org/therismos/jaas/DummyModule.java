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
import org.apache.commons.dbutils.QueryRunner;
/**
 * Updated for Jakarta EE 10
 * JAAS module using MariaDB and md5 hashed password
 * @author cp_liu
 */
public class DummyModule implements LoginModule {

    /**
     * @return the subject
     */
    public Subject getSubject() {
        return subject;
    }

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
  private boolean debug;
  private DataSource ds;
  private String ds_properties;
  private static final Logger LOG = Logger.getLogger(DummyModule.class.getName());

    /**
     * This is for testing using a SimpleDatasource
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
        debug = false;
        if (options != null) {
            for (String key : options.keySet()) {
                LOG.info(() -> key + ":" + options.get(key).toString());
            }            
        }
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
            LOG.log(Level.SEVERE, null, ex);
        }
  }
  
  @Override
  public boolean login() throws LoginException {

    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("login");
    callbacks[1] = new PasswordCallback("password", true);
    String sql = "SELECT m.name, m.nickname, g.role from members m LEFT JOIN groups g on m.nickname=g.user "
            + "WHERE m.nickname=? and m.pwd=?";
    QueryRunner runner = new QueryRunner(ds);
    try {
        handler.handle(callbacks);
        String name = ((NameCallback) callbacks[0]).getName();
        String password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());
        List<Map<String, Object>> results = runner.query(sql, new org.apache.commons.dbutils.handlers.MapListHandler(), name, DigestUtils.md5Hex(password));
        if (results.isEmpty()) throw new RuntimeException("Wrong pwd");
        if (results.size() != 3) throw new RuntimeException("Wrong roles");
        for (Map<String, Object> result : results) {
            map.put("givenName", result.get("name").toString());
            map.put("nickname", result.get("nickname").toString());
            userGroups.add(result.get("role").toString());
        }
        login = name;
        return true;
    }
    catch (RuntimeException | SQLException | IOException | UnsupportedCallbackException e) {
        throw new LoginException(e.getMessage());
    }

  }
  
  @Override
  public boolean commit() throws LoginException {
    userPrincipal = new UserPrincipal(login);
    userPrincipal.setMap(map);
    subject.getPrincipals().add(userPrincipal);
    if (userGroups != null && !userGroups.isEmpty()) {
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
        getSubject().getPrincipals().remove(userPrincipal);
        getSubject().getPrincipals().remove(rolePrincipal);
    return true;
  }
    
}
