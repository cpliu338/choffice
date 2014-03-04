package org.therismos.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.therismos.entity.Book;
import org.therismos.entity.BookCopy;
import org.therismos.entity.BookCopyPK;

/**
 *
 * @author cpliu
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class BookCopyBean implements java.io.Serializable {

    private BookCopy copy;
    private String bookCode;
    private String memberCode;
    
    public BookCopyBean() {blankCopy();}
    
    private void blankCopy() {
                copy = new BookCopy(0,0);
                Book book = new Book();
                book.setTitle("");
                copy.setBook(book);
                copy.setStatus(BookCopy.ONSHELF);
    }
    
    public String getStatusText() {
        return "copy."+copy.getStatus();
    }
    
    public void update() {
        
    }
    
    public void searchByBookCode() {
        FacesMessage msg = new FacesMessage();
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        if (emf==null) return;
        EntityManager em = emf.createEntityManager();
        if (em==null) {
            emf.close(); return;
        }
        try {
            BookCopyPK pk = new BookCopyPK(bookCode.trim());
            copy = em.find(BookCopy.class, pk);
            if (copy == null) {
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
                msg.setSummary("Not found");
                msg.setDetail(pk.toString());
                blankCopy();
            }
            else {
                msg.setSeverity(FacesMessage.SEVERITY_INFO);
                msg.setSummary("Found");
                msg.setDetail(copy.getBook().getTitle());
            }
        }
        catch (NumberFormatException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary("Invalid code");
            msg.setDetail(this.bookCode);
        }
        em.close();
        emf.close();
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * @return the copy
     */
    public BookCopy getCopy() {
        return copy;
    }

    /**
     * @param copy the copy to set
     */
    public void setCopy(BookCopy copy) {
        this.copy = copy;
    }

    /**
     * @return the bookCode
     */
    public String getBookCode() {
        return bookCode;
    }

    /**
     * @param bookCode the bookCode to set
     */
    public void setBookCode(String bookCode) {
        this.bookCode = bookCode;
    }

    /**
     * @return the memberCode
     */
    public String getMemberCode() {
        return memberCode;
    }

    /**
     * @param memberCode the memberCode to set
     */
    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }
}
