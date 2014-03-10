package org.therismos.web;

import java.util.ResourceBundle;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.therismos.entity.Book;
import org.therismos.entity.Member1;

/**
 *
 * @author cpliu
 */
public class BookBaseBean {
    
    protected ResourceBundle bundle;
    
    protected BookBaseBean() {
        bundle = ResourceBundle.getBundle("messages", java.util.Locale.TRADITIONAL_CHINESE);
//        Logger.getLogger(BookBaseBean.class.getName()).log(Level.WARNING, bundle.getString("app"));
    }
    
    protected Book book;
    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    /**
     * @param book the book to set
     */
    public void setBook(Book book) {
        this.book = book;
    }
    
    public Member1 findMemberByCode(int code) {
        return this.findMemberById(Member1.getIdFromBarcode(code));
    }

    public Member1 findMemberById(int id) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        EntityManager em=null;
        try {
            em = emf.createEntityManager();
            return em.find(Member1.class, id);
        }
        finally {
            if (em!=null) em.close();
            if (emf!=null) emf.close();
        }
    }
}
