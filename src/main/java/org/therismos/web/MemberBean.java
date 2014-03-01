package org.therismos.web;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.therismos.entity.Member1;

/**
 *
 * @author cpliu
 */
@ManagedBean
@ViewScoped
public class MemberBean implements java.io.Serializable {
    
    private int id;
    private EntityManagerFactory emf;
    private Member1 member;
    private List<Member1> members;
    private String group;

    public MemberBean() {
        group = "";
        members = java.util.Collections.EMPTY_LIST;
    }
    
    public void setMembers() {
        emf = Persistence.createEntityManagerFactory("churchPU");
        EntityManager em = emf.createEntityManager();
        if (em==null) return;
        try {
            members = em.createNamedQuery("Member1.findAll", Member1.class).setMaxResults(3).getResultList();
        }
        catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            em.close();
        }
        emf.close();
        
    }
    
    public SelectItem[] getMembers() {
        int size = members.size();
        SelectItem[] items = new SelectItem[size];
        for (int i=0; i<size; i++) {
            Member1 m = members.get(i);
            items[i] = new SelectItem(m.getId(), m.getName());
        }
        return items;
    }

    public SelectItem[] getSelectItems() {
        emf = Persistence.createEntityManagerFactory("churchPU");
        EntityManager em = emf.createEntityManager();
        SelectItem[] items = new SelectItem[0];
        if (em==null) return items;  
        try {
            List l = em.createNamedQuery("Member1.findAllGroupname").getResultList();
            int size = l.size();
            items = new SelectItem[size];
            for (int i=0; i<size; i++) {
                items[i] = new SelectItem(l.get(i).toString());
            }
        }
        catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            em.close();
        }
        emf.close();
        return items;
    }
    
//    @PostConstruct
    public void handleGroupChange() { 
        emf = Persistence.createEntityManagerFactory("churchPU");
        EntityManager em = emf.createEntityManager();
        if (em==null) return;
        try {
            members = em.createNamedQuery("Member1.findByGroupname", Member1.class).setParameter("groupname", group).getResultList();
        }
        catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            em.close();
        }
        emf.close();
    }

    public void init() {
        emf = Persistence.createEntityManagerFactory("churchPU");
        EntityManager em = emf.createEntityManager();
        if (em==null) return;
        try {
            members = em.createNamedQuery("Member1.findAll", Member1.class).setMaxResults(10).getResultList();
        }
        catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            em.close();
        }
        emf.close();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
        emf = Persistence.createEntityManagerFactory("churchPU");
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Id:{0}", this.id);
        EntityManager em = emf.createEntityManager();
        if (em==null) return;
        try {
            member = em.createNamedQuery("Member1.findById", Member1.class)
                    .setParameter("id", this.id).getSingleResult();
        }
        catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            em.close();
        }
    }

    /**
     * @return the member
     */
    public Member1 getMember() {
        return member;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

}
