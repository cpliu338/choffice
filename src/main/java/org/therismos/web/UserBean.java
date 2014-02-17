package org.therismos.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public Map getUserMap() {
        return userMap;
    }
    
    public static final String[] groups = {"deacons","librarians","staff"};
   
    public void logout() {
        user = null;
        userMap = Collections.EMPTY_MAP;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ec.getSession(true);
        session.invalidate();
        HttpServletRequest req = (HttpServletRequest)ec.getRequest();
        try {
            req.getSession().getServletContext().getRequestDispatcher("/index.jsp").forward(req, (HttpServletResponse)ec.getResponse());
        } catch (ServletException | IOException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        return user == null ? "Guest" : user.getName();
    }

}
