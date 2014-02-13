package org.therismos.jaas;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.codec.binary.Hex;
import org.therismos.authen.BlindTrustSSLFactory;
import org.therismos.authen.LoginBean;

/**
 *
 * @author Administrator
 */
public class LdapLoginModule implements LoginModule {

  private CallbackHandler handler;
  private Subject subject;
  private UserPrincipal userPrincipal;
  private RolePrincipal rolePrincipal;
  private String login;
  private List<String> userGroups;
    private String principal;
    private String credential;
    private String url ;
    private String base;
    private DirContext ctx;
//    private String[] roles;

  @Override
  public void initialize(Subject subject,
      CallbackHandler callbackHandler,
      Map<String, ?> sharedState,
      Map<String, ?> options) {
        principal="cn=manager,ou=Internal,dc=system,dc=lan";
        credential = "jMSL5KNZtM+O8RB+";
        url ="ldaps://192.168.11.224:636";
        base="dc=system,dc=lan";
        userGroups = new ArrayList<String>();
    handler = callbackHandler;
    this.subject = subject;
  }
    private void refreshContext() {
	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.PROVIDER_URL, url);

	// Authenticate as S. User and password "mysecret"
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, principal);
	env.put(Context.SECURITY_CREDENTIALS, credential);
        env.put("java.naming.ldap.factory.socket", BlindTrustSSLFactory.class.getName());
        try {
            ctx = new InitialLdapContext(env,null);
        } catch (NamingException ex) {
            ctx = null;
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

        this.refreshContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(base, 
            "uid="+name, // search filter
            searchControls);
        SearchResult searchResult;
        if(results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                throw new LoginException("Matched multiple users for the accountName: " + name);
            }
            String hashed = searchResult.getAttributes().get("clearSHA1Password").get().toString();
            ctx.close();
            String pwd;
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
              cript.reset();
              cript.update(password.getBytes("utf8"));
              pwd = Hex.encodeHexString(cript.digest());
            boolean loggedIn = pwd.equalsIgnoreCase(hashed);
            if (!loggedIn)
                throw new LoginException("Wrong password");
        login = searchResult.getNameInNamespace();
//                searchResult.getAttributes().get("sn").get()+" "+
//                searchResult.getAttributes().get("givenName").get();
        String[] roles = {"staff","deacons","librarians"};
        this.checkGroups(roles);
        userGroups.add("staff");
        if (name.startsWith("admin"))
            userGroups.add("admin");
        return true;
        }
        else { // no such user
            ctx.close();
            throw new LoginException("Authentication failed");
        }
    } catch (IOException | UnsupportedCallbackException | NamingException | NoSuchAlgorithmException e) {
      throw new LoginException(e.getMessage());
    }

  }

  @Override
  public boolean commit() throws LoginException {

    userPrincipal = new UserPrincipal(login);
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
  
    public void checkGroups(String [] groups) throws NamingException {
        this.refreshContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        SearchResult searchResult = null;
        for (String r : groups) {
            NamingEnumeration<SearchResult> results = ctx.search(base, 
                "cn="+r, // search filter
                searchControls);
            if(results.hasMoreElements()) {
                searchResult = (SearchResult) results.nextElement();
                //make sure there is not another item available, there should be only 1 match
//                if(results.hasMoreElements()) {
//                    throw new java.lang.IllegalArgumentException("Matched multiple groups for the accountName: " + principal);
//                }
            }
            else continue;
            Attribute attr = searchResult.getAttributes().get("member");
            NamingEnumeration members = attr.getAll();
            while (members.hasMore()) {
                String s = members.next().toString();
                Logger.getLogger(LdapLoginModule.class.getName()).log(Level.INFO, "{0}",
                s);
                if (s.equals(login)) {
//                    user1.put("role:"+r, true);
                    userGroups.add(r);
                Logger.getLogger(LdapLoginModule.class.getName()).log(Level.INFO, "{0}",
                        "found");
                    break;
                }
            }
	}
	    // Close the context when we're done
	    ctx.close();
    }

}
