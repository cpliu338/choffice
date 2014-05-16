package org.therismos.web;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.therismos.jaas.UserPrincipal;

/**
 *
 * @author cpliu
 */
@ManagedBean
@SessionScoped
public class UserBean implements java.io.Serializable {
    private UserPrincipal user;
    private Map userMap;
    
    public UserBean() {
        userMap = Collections.EMPTY_MAP;
    }

    public File getBasePath() {
        javax.naming.Context initCtx;
        String base1;
        try {
            initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            base1 = envCtx.lookup("datapath").toString();
            return new File(base1);
        } catch (javax.naming.NamingException ex) {
            Logger.getLogger(PublisherBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public File getNasPath() {
        javax.naming.Context initCtx;
        String base1;
        try {
            initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            base1 = envCtx.lookup("naspath").toString();
            return new File(base1);
        } catch (javax.naming.NamingException ex) {
            Logger.getLogger(PublisherBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public Map getUserMap() {
        return userMap;
    }
    
    public Locale getLocale() {
        return Locale.TRADITIONAL_CHINESE;
    }
    
    public static final String[] groups = {"deacons","librarians","staff"};
   
    public String logout() {
        user = null;
        userMap = Collections.EMPTY_MAP;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ec.getSession(true);
        session.invalidate(); 
        this.user = null;
        this.userMap.clear();
        return "/index?faces-redirect=true";
//        HttpServletRequest req = (HttpServletRequest)ec.getRequest();
//        try {
//            req.getSession().getServletContext().getRequestDispatcher("/index.jsf").forward(req, (HttpServletResponse)ec.getResponse());
//        } catch (ServletException | IOException ex) {
//            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
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
            return false;
        }
        // if just logged in via j_security check, set user
        user = (UserPrincipal) req.getUserPrincipal();
        userMap = user.getMap();
        return user!=null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return user == null ? "" : user.getName();
    }

}
