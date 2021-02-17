package org.therismos.authen;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.lang.CharSet;

/**
 *
 * @author cpliu
 */
@ManagedBean
@ApplicationScoped
public class LoginBean implements java.io.Serializable {
    private String debug="OK";
    private String principal;
    private String credential;
    private String url ;
    private String base;
    private DirContext ctx;
    private String[] roles;
    
    public LoginBean() {
        roles = new String[0];
    }
    
    @PostConstruct
    public void init() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        principal=ec.getInitParameter("ldap.principal");
        
        //"cn=manager,ou=Internal,dc=system,dc=lan";
        credential =ec.getInitParameter("ldap.credential");//jMSL5KNZtM+O8RB+";
        url =ec.getInitParameter("ldap.url");//ldaps://192.168.11.224:636";
        base=ec.getInitParameter("ldap.base");//dc=system,dc=lan";
        roles = ec.getInitParameter("ldap.roles").split(",");
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
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void authenticate(java.util.Map<String,Object> user1) throws NamingException, NoSuchAlgorithmException, UnsupportedEncodingException {
        this.refreshContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<SearchResult> results = ctx.search(base, 
            "uid="+user1.get("uid"), // search filter
            searchControls);
        SearchResult searchResult = null;
        if(results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            //make sure there is not another item available, there should be only 1 match
            if(results.hasMoreElements()) {
                throw new java.lang.IllegalArgumentException("Matched multiple users for the accountName: " + user1.get("uid"));
            }
            String hashed = searchResult.getAttributes().get("clearSHA1Password").get().toString();
            ctx.close();
            String pwd = user1.get("pwd").toString();
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
              cript.reset();
              cript.update(pwd.getBytes("utf8"));
              debug = Hex.encodeHexString(cript.digest());
            boolean loggedIn = debug.equalsIgnoreCase(hashed);
            if (!loggedIn)
                throw new java.lang.RuntimeException("Wrong password");
            user1.put("authenticated", loggedIn);
            user1.put("pwd",""); // clear password for security
            user1.put("sn",searchResult.getAttributes().get("sn").get());
            user1.put("givenName", searchResult.getAttributes().get("givenName").get());
            user1.put("dn", searchResult.getNameInNamespace());
        }
        else { // no such user
            ctx.close();
            throw new java.lang.RuntimeException("No such user: " + user1.get("uid"));
        }
    }
    
    public void setGroups(java.util.Map<String,Object> user1, String [] groups) throws NamingException {
        this.refreshContext();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        SearchResult searchResult = null;
        for (String r : roles) {
            NamingEnumeration<SearchResult> results = ctx.search(base, 
                "cn="+r, // search filter
                searchControls);
            if(results.hasMoreElements()) {
                searchResult = (SearchResult) results.nextElement();
                //make sure there is not another item available, there should be only 1 match
                if(results.hasMoreElements()) {
                    user1.remove("role:"+r);
                    throw new java.lang.IllegalArgumentException("Matched multiple groups for the accountName: " + principal);
                }
            }
            Attribute attr = searchResult.getAttributes().get("member");
            NamingEnumeration members = attr.getAll();
            while (members.hasMore()) {
                String s = members.next().toString();
                Logger.getLogger(LoginBean.class.getName()).log(Level.INFO, "{0}",
                s);
                if (s.equals(user1.get("dn"))) {
                    user1.put("role:"+r, true);
                Logger.getLogger(LoginBean.class.getName()).log(Level.INFO, "{0}",
                        "found");
                    break;
                }
            }
	}
	    // Close the context when we're done
	    ctx.close();
    }

    /**
     * @return the name
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * @param principal the name to set
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    /**
     * @return the debug
     */
    public String getDebug() {
        return debug;
    }

    /**
     * @return the credential
     */
    public String getCredential() {
        return credential;
    }

    /**
     * @param credential the credential to set
     */
    public void setCredential(String credential) {
        this.credential = credential;
    }
}
