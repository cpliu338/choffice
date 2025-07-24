package org.therismos.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.*;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.therismos.jaas.UserPrincipal;

/**
 * Updated for Jakarta EE 10
 * TODO to be tested
 * The Session Bean for a user
 * @author cpliu
 */
@Named
@SessionScoped
public class UserBean implements java.io.Serializable {
    private UserPrincipal user;
    private Map userMap;
    static private Logger LOG = Logger.getLogger(UserBean.class.getName());
    
    @jakarta.annotation.Resource
    private String datapath;
    @jakarta.annotation.Resource
    private String naspath;
    @jakarta.annotation.Resource
    private String naspath2;
    
    public UserBean() {
        
    }
    
    public File getBasePath() {
        return new File(datapath);
    }

    public File getNasPath() {
        return new File(naspath);
    }
    
    public File getNasPath2() {
        return new File(naspath2);
    }
    
    public Map getUserMap() {
        refreshUser();
        return userMap;
    }
    
    public Locale getLocale() {
        return Locale.TRADITIONAL_CHINESE;
    }
    
//    static final String[] groups = {"deacons","librarians","staff"};
//    static public String[] getGroups() {return groups;}
    
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
    
    public boolean isInRole(String r) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        return req.isUserInRole(r);
    }

//    @jakarta.annotation.PostConstruct
    private void refreshUser() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        // if just logged in via j_security check, set user
        user = (UserPrincipal) req.getUserPrincipal();
        userMap =  (user == null) ? Collections.EMPTY_MAP : user.getMap();
    }
    
    public boolean isLoggedIn() {
        refreshUser();
        return user!=null;
    }

    /**
     * @return the name
     */
    public String getName() {
        refreshUser();
        return user == null ? "" : 
            (userMap.containsKey("givenName") ? userMap.get("givenName").toString() : user.getName());
    }

}
