package org.therismos.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.therismos.entity.*;

/**
 *
 * @author Administrator
 */
@ManagedBean
@ViewScoped
public class BookBean extends BookBaseBean implements java.io.Serializable {
    private String crit;
    ArrayList<Book> foundBooks;

    public String getCrit() {
        return crit;
    }

    public void setCrit(String crit) {
        this.crit = crit;
    }

    public List<Book> getFoundBooks() {
        return foundBooks;
    }

    public BookBean() {
        crit = "";
        book = new Book();
        foundBooks = new ArrayList<>();
    }
    
    public void save() {
        FacesMessage msg = new FacesMessage();
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
            if (emf==null) throw new RuntimeException("Cannot create EntityManagerFactory");
            EntityManager em = emf.createEntityManager();
            if (em==null) throw new RuntimeException("Cannot create EntityManager");
            Book b = em.createNamedQuery("Book.findByCallNo", Book.class).setParameter("callNo", book.getCallNo()).getSingleResult();
            if (b==null) throw new RuntimeException("Cannot find this book");
            b.setIndexStr(book.getIndexStr());
            javax.persistence.EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.merge(b);
            tx.commit();
            em.close();
            emf.close();
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("Saved ");
            msg.setDetail(b.getIndexStr());
        }
        catch (RuntimeException ex) {
            Logger.getLogger(BookBean.class.getName()).log(Level.WARNING, book.toString());
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage("form2", msg);
    }
    
    public void search() {
        foundBooks.clear();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        if (emf==null) return;
        EntityManager em = emf.createEntityManager();
        if (em==null) return;
        int callno;
        try {
            callno = Integer.parseInt(crit.trim())/ 100 - 9000000;
            foundBooks.add(
                em.createNamedQuery("Book.findByCallNo", Book.class).setParameter("callNo", callno).getSingleResult()
            );
        }
        catch (NumberFormatException | javax.persistence.NoResultException ex) {
        }
        String crit_trim = crit.trim();
        Logger.getLogger(BookBean.class.getName()).log(Level.INFO, "Crit: {0}", crit_trim);
        if (crit_trim.length()>1) {
            foundBooks.addAll(em.createNamedQuery("Book.findLikeIndexStr", Book.class).setParameter("indexStr", crit_trim+"%").getResultList());
            foundBooks.addAll(em.createNamedQuery("Book.findLikeTitle", Book.class).setParameter("title", "%"+crit_trim+"%").getResultList());
        }
        if (foundBooks ==null || foundBooks.isEmpty()) {
            book = new Book();
            FacesMessage msg = new FacesMessage();
            msg.setDetail("Books not found");
            msg.setSummary("Alert");
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            FacesContext.getCurrentInstance().addMessage("form1", msg);
        }
        em.close();
        emf.close();
    }

    public List<String> suggestPublisher(String query) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        if (emf==null) return Collections.EMPTY_LIST;
        EntityManager em = emf.createEntityManager();
        if (em==null) return Collections.EMPTY_LIST;
        try {
            return em.createNamedQuery("Publisher.suggest", String.class).setParameter("nameFrag", "%"+query.trim()+"%").getResultList();
        }
        finally {
            em.close();
            emf.close();
        }
    }
}
