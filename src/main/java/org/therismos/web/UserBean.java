package org.therismos.web;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.therismos.authen.LoginBean;
import org.therismos.entity.Account;
import org.therismos.entity.Offer;

/**
 *
 * @author cpliu
 */
@ManagedBean
@SessionScoped
public class UserBean implements java.io.Serializable {
    @ManagedProperty(value="#{loginBean}")
    private LoginBean loginBean;
    private String name;
    private String pwd;
    private String debug;
    private final java.util.Map<String,Object> user;

    public Map<String, Object> getUser() {
        return user;
    }
    private String[] groups = {"deacons","librarians","staff"};
    
    public UserBean() {
        user = new java.util.HashMap<String, Object>();
        name = "";
        try {
            name = InitialContext.doLookup("java:/comp/env/jdbc/churchDB").getClass().toString();
            if (name.length()>0) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
                if (emf == null) 
                    throw new RuntimeException("cannot create PU");
                EntityManager em = emf.createEntityManager();
                if (em == null) 
                    throw new RuntimeException("cannot create Entity Manager");
                emf.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
            name = String.format("%s:%s",ex.getClass().getName(), ex.getMessage());
        }
    }
    
    public String getTarget() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession)fc.getExternalContext().getSession(true);
        return (String)session.getAttribute("target");
    }
    
    public void logout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ec.getSession(true);
        session.removeAttribute("user");
        user.clear();
        HttpServletRequest req = (HttpServletRequest)ec.getRequest();
        try {
            req.getSession().getServletContext().getRequestDispatcher("/index.jsf").forward(req, (HttpServletResponse)ec.getResponse());
        } catch (ServletException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void action() {
        user.put("uid", name);
        user.put("pwd", pwd);
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage();
        try {
            loginBean.authenticate(user);
            HttpSession session = (HttpSession)fc.getExternalContext().getSession(true);
            session.removeAttribute("user");
            if (user.containsKey("dn")) {
                debug = user.get("dn").toString();
                loginBean.setGroups(user, groups);
                session.setAttribute("user", user);
                msg.setSeverity(FacesMessage.SEVERITY_INFO);
                msg.setDetail("Logged in");
                String target = this.getTarget();
                if (target != null && target.endsWith(".jsf"))
                    fc.getExternalContext().redirect(target);
                else
                    fc.getExternalContext().redirect("/choffice");
            }
            else {
                debug = loginBean.getDebug();
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setDetail("Wrong password");
            }
        } catch (Exception ex) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
            debug = ex.getClass().getName();
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setDetail("Invalid password");
        }
            fc.addMessage(null, msg);
    }
    

    /**
     * @param loginBean the loginBean to set
     */
    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /**
     * @return the debug
     */
    public String getDebug() {
        return debug;
    }
}
