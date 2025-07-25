package org.therismos.web;

import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.therismos.bean.ApplicationBean;
import org.therismos.entity.Member1;

/**
 *
 * @author cp_liu
 */
@ViewScoped
@Named
public class MemberBean implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    
    static final Logger LOG = Logger.getLogger(MemberBean.class.getName());
    private int id;
    @jakarta.annotation.Resource
    DataSource dataSource;
    @jakarta.inject.Inject
    ApplicationBean appBean;
    
    private Member1 member;
    private List<Member1> members;
    private String group;
    
    private final List<Member1> membersInCart;
    private final List<Integer> idsInCart;
    
    public MemberBean() {
        group = "";
        members = Collections.EMPTY_LIST;
        this.membersInCart = new ArrayList<>();
        this.idsInCart = new ArrayList<>();
    }
    
    public void setMembers() {
        String sql = "SELECT * FROM members m LIMIT 3";
        try {
            members = getRunner().query(sql, getHandler()); /*
            members = em.createNamedQuery("Member1.findAll", Member1.class)
            .setMaxResults(3).getResultList();        */
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            members = Collections.EMPTY_LIST;
        }
    }

    private QueryRunner getRunner() {
        return new QueryRunner(dataSource);
    }

    private BeanListHandler<Member1> getHandler() {
        return new BeanListHandler<>(Member1.class);
    }
    
    public List<Member1> getMembersInCart() {
        return membersInCart;
    }
    
    public void refreshCart() {
        membersInCart.clear();
        String sql = "SELECT * FROM members m WHERE m.id IN (%s) ORDER m.id";
        try {
            membersInCart.addAll(
                    appBean.queryWithInClause(sql, idsInCart, Member1.class)
                    //getRunner().query(sql, getHandler(), idsInCart)
            );
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public SelectItem[] getMembers() {
        int size = members.size();
        SelectItem[] items_local = new SelectItem[size];
        for (int i=0; i<size; i++) {
            Member1 m = members.get(i);
            items_local[i] = new SelectItem(m.getId(), m.getName());
        }
        return items_local;
    }
    
    private SelectItem[] items;
    
    public SelectItem[] getSelectItems() {
        return items;
    }
    
    @jakarta.annotation.PostConstruct
    public void init() {
        items = new SelectItem[0];
        List l = Collections.EMPTY_LIST;// em.createNamedQuery("Member1.findAllGroupname").getResultList();
        try {
            l =getRunner().query(
                    "SELECT DISTINCT m.groupname FROM members m", new ColumnListHandler <String>());
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        int size = l.size();
        items = new SelectItem[size];
        for (int i=0; i<size; i++) {
            items[i] = new SelectItem(l.get(i).toString());
        }
    }
    
    public String getDebug() {
        return appBean.getProperty("db.user");
    }
    
    public void handleGroupChange() { 
        LOG.log(Level.INFO, "group: {0}", group);
        try {
            members = getRunner().query("SELECT * FROM members m WHERE m.groupname = ?", getHandler(), group);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            members = Collections.EMPTY_LIST;
        }
    }

    public List<Integer> getIdsInCart() {
        return idsInCart;
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
        BeanHandler<Member1> handler = new BeanHandler(Member1.class);
        try {
            member = getRunner().query("SELECT * FROM members m WHERE m.id = ?", handler, this.id);
            //member = em.createNamedQuery("Member1.findById", Member1.class).setParameter("id", this.id).getSingleResult();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
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
