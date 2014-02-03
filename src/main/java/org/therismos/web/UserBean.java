package org.therismos.web;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import org.therismos.authen.LoginBean;

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
    private java.util.Map<String,Object> user;

    public Map<String, Object> getUser() {
        return user;
    }
    private String[] groups = {"deacons","librarians","staff"};
    
    public UserBean() {
        user = new java.util.HashMap<String, Object>();
    }

    public void action() {
        user.put("uid", name);
        user.put("pwd", pwd);
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage();
        try {
            loginBean.authenticate(user);
            if (user.containsKey("dn")) {
                debug = user.get("dn").toString();
                loginBean.setGroups(user, groups);
                msg.setSeverity(FacesMessage.SEVERITY_INFO);
                msg.setDetail("Logged in");
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
