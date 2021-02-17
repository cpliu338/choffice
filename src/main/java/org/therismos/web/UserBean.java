package org.therismos.web;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.therismos.jaas.UserPrincipal;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
@javax.enterprise.context.SessionScoped
public class UserBean implements java.io.Serializable {
    private UserPrincipal user;
    private Map userMap;
    Logger logger = Logger.getLogger(UserBean.class.getName());
            
    @Resource(name="naspath")// type="java.lang.String")
    String naspath;
    @Resource(name="naspath2")// type="java.lang.String")
    String naspath2;
    @Resource(name="datapath")// type="java.lang.String")
    String datapath;
    
    public UserBean() {
        userMap = Collections.EMPTY_MAP;
    }
    
    @javax.annotation.PostConstruct
    public void init() {
        logger.log(Level.INFO, "Naspath is {0}, datapath is {1}", new Object[]{
            naspath, datapath});        
    }

    public File getBasePath() {
        try {
            File f = new File(datapath);
            if (f.isDirectory() && f.canExecute())
                return f;
            throw new RuntimeException ("Fail to look up datapath directory");
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public File getNasPath() {
        try {
            File f = new File(naspath);
            if (f.isDirectory() && f.canExecute())
                return f;
            throw new RuntimeException ("Fail to look up Naspath directory");
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public File getNasPath2() {
        try {
            File f = new File(naspath2);
            if (f.isDirectory() && f.canExecute())
                return f;
            throw new RuntimeException ("Fail to look up Naspath2 directory");
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public Map getUserMap() {
        return userMap;
    }
    
    public Locale getLocale() {
        return Locale.TRADITIONAL_CHINESE;
    }
    
    static final String[] groups = {"deacons","librarians","staff"};
   
    public String logout() {
        user = null;
        userMap = Collections.EMPTY_MAP;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ec.getSession(true);
        session.invalidate(); 
        this.user = null;
        this.userMap.clear();
        return "/index?faces-redirect=true";
    }
    
    public boolean isLibrarian() {
        return isInRole("librarians");
    }
    
    public boolean isInRole(String r) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        return req.isUserInRole(r);
    }
    
    public boolean isLoggedIn() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        if (req.getUserPrincipal() == null) {
            user = null;
            userMap = Collections.EMPTY_MAP;
        Logger.getLogger(UserBean.class.getName()).log(Level.FINE, "Not logged in");
            return false;
        }
        // if just logged in via j_security check, set user
        user = (UserPrincipal) req.getUserPrincipal();
        userMap = user.getMap();
        Logger.getLogger(UserBean.class.getName()).log(Level.FINE, "User:"+user.getName());
        return user!=null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return user == null ? "" : user.getName();
    }

}
