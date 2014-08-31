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
    private String indexStr, author1, publisher1, title;
    private boolean creative;
    private List<Integer> copies;

    public List<Integer> getCopies() {
        return copies;
    }

    public boolean isCreative() {
        return creative;
    }

    public void setCreative(boolean creative) {
        this.creative = creative;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher1() {
        return publisher1;
    }

    public void setPublisher1(String publisher1) {
        this.publisher1 = publisher1;
    }

    public String getAuthor1() {
        return author1;
    }

    public void setAuthor1(String author1) {
        this.author1 = author1;
    }
    
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
            title = "";
            author1 = "";
            publisher1 = "";
            copies = Collections.EMPTY_LIST;
        }
        if (bookid>0 && book.getCallNo() != bookid) { // book id changed
            Book book1 = dao.searchBookById(bookid);
            if (book1 != null) {
                book = book1;
                indexStr = book1.getIndexStr();
                title = book1.getTitle();
                author1 = book1.getAuthor().getNameChi();
                publisher1 = book1.getPublisher().getNameChi();
                copies = new ArrayList<>();
                int codebase = (9000000+bookid)*100;
                for (BookCopy c : dao.findCopies(bookid)) {
                    copies.add(codebase + c.getBookCopyPK().getCopyNo());
                }
            }
        }
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
        copies = Collections.EMPTY_LIST;
    }
    
//    @javax.annotation.PostConstruct
    public void fullTextSearch() {
//        book = new Book(0);
        foundBooks = dao.fullTextSearch(crit);
    }
    
    public String addBookCopy() {
        dao.addBookCopy(bookid);
        return String.format("book?id=%d&faces-redirect=true", bookid);
    }
    
    public String createBook() {
        FacesMessage msg = new FacesMessage();
        String ret = null;
        try {
            if (this.title.isEmpty()) 
                throw new RuntimeException("Empty book title");
            if (!creative && dao.checkBookByTitle(title)!=null)
                throw new RuntimeException("Duplicated book title, override by setting create mode");
            book = new Book();
            book.setTitle(title);
            dao.createBook(book, this.indexStr, this.author1, this.publisher1, this.creative);
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("Created "+book.getCallNo());
            msg.setDetail(title);
            ret = String.format("book?id=%d&faces-redirect=true", book.getCallNo());
        }
        catch (RuntimeException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
            ret = null;
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        return ret;
    }
    
    public void saveIndexStr() {
        FacesMessage msg = new FacesMessage();
        try {
            book = dao.searchBookById(bookid);
            book.setIndexStr(this.indexStr);
            dao.updateBook(book);
            logger.log(Level.INFO, "Save indexStr for: {0}", book.getTitle());
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
    
    public void saveAuthor() {
        FacesMessage msg = new FacesMessage();
        msg.setSeverity(FacesMessage.SEVERITY_INFO);
        try {
            book = dao.searchBookById(bookid);
            msg.setDetail(book.getTitle());
            Author a = dao.getAuthorByNameChi(author1);
            if (a!=null) {
                book.setAuthor(a);
                dao.updateBook(book);
                msg.setSummary("Saved "+author1);
            }
            else if (this.creative) {
                dao.createAuthor(author1);
                msg.setSummary("created "+author1);
            }
            else {
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setSummary("Must set create mode to create "+author1);
            }
            creative = false;
        }
        catch (NumberFormatException | PersistenceException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void savePublisher() {
        FacesMessage msg = new FacesMessage();
        msg.setSeverity(FacesMessage.SEVERITY_INFO);
        try {
            book = dao.searchBookById(bookid);
            msg.setDetail(book.getTitle());
            Publisher p = dao.getPublisherByNameChi(publisher1);
            if (p!=null) {
                book.setPublisher(p);
                dao.updateBook(book);
                msg.setSummary("Saved "+publisher1);
                msg.setDetail(book.getPublisher().getNameChi());
            }
            else if (this.creative) {
                dao.createPublisher(publisher1);
                msg.setSummary("created "+publisher1);
            }
            else {
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setSummary("Must set create mode to create "+publisher1);
            }
            creative = false;
        }
        catch (NumberFormatException | PersistenceException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void saveTitle() {
        FacesMessage msg = new FacesMessage();
        msg.setSeverity(FacesMessage.SEVERITY_INFO);
        try {
            book = dao.searchBookById(bookid);
            msg.setDetail(book.getTitle());
            title = title.trim();
            if (title.length()>0) {
                book.setTitle(title);
                dao.updateBook(book);
                msg.setSummary("Saved ");
            }
            else {
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                msg.setSummary("Title must not be empty");
            }
            creative = false;
        }
        catch (NumberFormatException | PersistenceException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public List<String> suggestAuthor(String query) {
        return dao.suggest(query, "Author");
    }
    
    public List<String> suggestPublisher(String query) {
        return dao.suggest(query, "Publisher");
    }
    
    public List<String>suggestBook(String query) {
        return dao.suggestBook(query);
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
