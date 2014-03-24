package org.therismos.web;

import java.util.*;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.PersistenceException;
import org.therismos.ejb.BookDao;
import org.therismos.entity.*;

/**
 *
 * @author Administrator
 */
@ManagedBean
@javax.faces.bean.ViewScoped
public class BookBean extends BookBaseBean implements java.io.Serializable {
    private String indexStr;
    private String crit;
    List<Book> foundBooks;
    @javax.ejb.EJB
    private BookDao dao;
    
    private int bookid;
    private static final Logger logger = Logger.getLogger(BookBean.class.getName());

    public int getBookid() {
        return bookid;
    }

    public void setBookid(int bookid) {
        this.bookid = bookid;
        if (book==null) {
            book = new Book(0);
        }
//        logger.log(Level.INFO, "Old Book id: {0}", book.getCallNo());
        if (book.getCallNo() != bookid) { // book id changed
            book = dao.searchBookById(bookid);
        }
        this.setIndexStr(book.getIndexStr());
//        logger.log(Level.INFO, "New Book id: {0}", book.getCallNo());
    }

    public String getIndexStr() {
        return indexStr;
    }

    public void setIndexStr(String crit) {
        this.indexStr = crit;
    }

    public List<Book> getFoundBooks() {
        return foundBooks;
    }

    public BookBean() {
        indexStr = "";
        foundBooks = new ArrayList<>();
        crit = "";
    }
    
//    @javax.annotation.PostConstruct
    public void fullTextSearch() {
//        book = new Book(0);
        foundBooks = dao.fullTextSearch(crit);
    }
    
    public void saveIndexStr() {
        FacesMessage msg = new FacesMessage();
        try {
            book = dao.searchBookById(bookid);
            book.setIndexStr(this.indexStr);
            logger.log(Level.INFO, "Getting author: {0}", book.getAuthor().getNameChi());
            dao.updateBook(book);
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("Saved ");
            msg.setDetail(book.getIndexStr());
        }
        catch (NumberFormatException | PersistenceException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
//    public void save() {
//        FacesMessage msg = new FacesMessage();
//            if (em==null) throw new RuntimeException("Cannot create EntityManager");
//        try {
//            javax.persistence.EntityTransaction tx = em.getTransaction();
//            tx.begin();
//            Book b = em.createNamedQuery("Book.findByCallNo", Book.class).setParameter("callNo", book.getCallNo()).getSingleResult();
//            if (b==null) throw new RuntimeException("Cannot find this book");
//            Logger.getLogger(BookBean.class.getName()).log(Level.INFO, "Finding author: {0}", book.getAuthor().getNameChi());
//            List<Author> l = em.createNamedQuery("Author.findByNameChi", Author.class).setParameter("nameChi", book.getAuthor().getNameChi()).getResultList();
//            if (l.isEmpty()) throw new RuntimeException("Cannot find this author");
//            b.setIndexStr(book.getIndexStr());
//            b.setAuthor(l.get(0));
//            em.merge(b);
//            tx.commit();
//            msg.setSeverity(FacesMessage.SEVERITY_INFO);
//            msg.setSummary("Saved ");
//            msg.setDetail(b.getIndexStr());
//            init();
//        }
//        catch (RuntimeException ex) {
//            Logger.getLogger(BookBean.class.getName()).log(Level.WARNING, book.toString());
//            msg.setSeverity(FacesMessage.SEVERITY_WARN);
//            msg.setDetail(ex.getMessage());
//            msg.setSummary(ex.getClass().getName());
//        }
//        finally {
//            em.close();
//            FacesContext.getCurrentInstance().addMessage("form2", msg);
//        }
//    }
    
//    public void search() {
//        Logger.getLogger(BookBean.class.getName()).log(Level.INFO, "Searching: {0}", indexStr);
//        foundBooks.clear();
//        EntityManager em = EMF.createEntityManager();
//        if (em==null) return;
//        int callno;
//        try {
//            callno = Integer.parseInt(indexStr.trim())/ 100 - 9000000;
//            foundBooks.add(
//                em.createNamedQuery("Book.findByCallNo", Book.class).setParameter("callNo", callno).getSingleResult()
//            );
//        }
//        catch (NumberFormatException | javax.persistence.NoResultException ex) {
//        }
//        String crit_trim = indexStr.trim();
//        if (crit_trim.length()>1) {
//            foundBooks.addAll(em.createNamedQuery("Book.findLikeIndexStr", Book.class).setParameter("indexStr", crit_trim+"%").getResultList());
//            foundBooks.addAll(em.createNamedQuery("Book.findLikeTitle", Book.class).setParameter("title", "%"+crit_trim+"%").getResultList());
//        }
//        if (foundBooks ==null || foundBooks.isEmpty()) {
//            book = new Book(0);
//            FacesMessage msg = new FacesMessage();
//            msg.setDetail("Books not found");
//            msg.setSummary("Alert");
//            msg.setSeverity(FacesMessage.SEVERITY_WARN);
//            FacesContext.getCurrentInstance().addMessage("form1", msg);
//        }
//        em.close();
//    }

    public List<String> suggestAuthor(String query) {
        return dao.suggest(query, "Author");
    }
    
    public List<String> suggestPublisher(String query) {
        return dao.suggest(query, "Publisher");
    }

    /**
     * @return the crit
     */
    public String getCrit() {
        return crit;
    }

    /**
     * @param crit the crit to set
     */
    public void setCrit(String crit) {
        this.crit = crit;
    }
}
