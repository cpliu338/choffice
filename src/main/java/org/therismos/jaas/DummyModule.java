package org.therismos.jaas;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

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
        props = new java.util.Properties();
        try (java.io.InputStream is = LdapLoginModule.class.getResourceAsStream("/ldap.properties")) {
            props.load(is);
            if (!props.containsKey("url")) {
                Logger.getLogger(DummyModule.class.getName()).log(Level.WARNING, "Cannot read property url");
            }
            props.getProperty("principal", "cn=manager,ou=Internal,dc=system,dc=lan");
            props.getProperty("credential", "jMSL5KNZtM+O8RB+");
            props.getProperty("url", "ldaps://192.168.11.224:636");
            props.getProperty("base", "dc=system,dc=lan");
        }
        catch (IOException ex) {
            Logger.getLogger(LdapLoginModule.class.getName()).log(Level.SEVERE, null, ex);
        }
            
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

        if(!name.equals(password)) {
                throw new LoginException("Wrong password");
        }
        map.put("givenName", name);
        map.put("sn", "Mr");
        if (name.contains("staff"))
            userGroups.add("staff");
        if (name.contains("deacon"))
            userGroups.add("deacons");
        if (name.contains("librarian"))
            userGroups.add("librarians");
        return true;
    } catch (IOException | UnsupportedCallbackException e) {
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
