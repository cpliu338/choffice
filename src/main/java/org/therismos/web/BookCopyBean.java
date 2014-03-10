package org.therismos.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.logging.*;
import org.therismos.entity.Book;
import org.therismos.entity.BookCopy;
import org.therismos.entity.BookCopyPK;
import org.therismos.entity.Member1;

/**
 *
 * @author cpliu
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class BookCopyBean extends BookBaseBean implements java.io.Serializable {

    private BookCopy copy;
    private String bookCode;
    private String memberCode;
    private Member1 member;

    public Member1 getMember() {
        return member;
    }

    public void setMember(Member1 member) {
        this.member = member;
    }
    
    public BookCopyBean() {
        super();
        blankCopy();
    }
    
    private void blankCopy() {
        copy = new BookCopy(0,0);
        book = new Book();
        book.setTitle("");
        copy.setBook(book);
        copy.setStatus(BookCopy.ONSHELF);
    }
    
    public String getStatusText() {
        return "copy."+copy.getStatus();
    }
    
    public void setActionString(String s) {
        actionString = s;
    }
    
    private String actionString;
    public String getActionString() {
        return actionString;
    }
    
    private void updateActionString() {
        actionString = "";
        if (copy != null && copy.getBookCopyPK().getCallNo()!=0) {
            switch (copy.getStatus()) {
                case BookCopy.LOANED:
                    actionString = super.findMemberById(copy.getUserId()).getName() + "->" + copy.getBook().getTitle() 
                        + "(" + copy.getBookCopyPK().getCopyNo() + ")";
                    break;
                case BookCopy.ONSHELF:
                    actionString = (member == null) ? "" :
                        copy.getBook().getTitle() 
                        + "(" + copy.getBookCopyPK().getCopyNo() + ")" + "->" +
                        member.getName();
                    break;
                default: actionString = "Status: "+copy.getStatus();
            }
        }
        logger.log(Level.INFO, "Returning {0}", actionString);
    }
    
    public void updateStatus() {
    }
    private static Logger logger = Logger.getLogger(BookCopyBean.class.getName());
    
    public void search() {
        logger.log(Level.INFO, "book code {0}", bookCode);
        if (bookCode.length()>0) {
            // TODO check for wrong book code format
            BookCopyPK pk = new BookCopyPK(bookCode.trim());
            if (pk.getCallNo()!=-1 && (pk.getCallNo()!=copy.getBookCopyPK().getCallNo() || pk.getCopyNo()!=copy.getBookCopyPK().getCopyNo())) {
                // need to update book copy
                logger.log(Level.INFO, "Searching");
                this.searchByBookCode();
            }
        }
        logger.log(Level.INFO, "member code {0}", memberCode);
        if (memberCode.length()>0) {
            try {
                int id = Integer.parseInt(memberCode.trim());
                if (id != copy.getUserId())
                    this.searchByMemberCode();
            }
            catch (NumberFormatException ex) {
            }
        }
    }
    
    public void searchByMemberCode() {
        FacesMessage msg = new FacesMessage();
        try {
            member = super.findMemberByCode(Integer.parseInt(memberCode));
            if (member == null) {
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
                msg.setSummary("Not found");
                msg.setDetail(memberCode);
                blankCopy();
            }
            else {
                msg.setSeverity(FacesMessage.SEVERITY_INFO);
                msg.setSummary("Found");
                msg.setDetail(member.getGroupname());
            }
        }
        catch (RuntimeException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary("Invalid code");
            msg.setDetail(memberCode);
            blankCopy();
        }
        this.updateActionString();
        FacesContext.getCurrentInstance().addMessage("borrowForm", msg);
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
        this.updateActionString();
        FacesContext.getCurrentInstance().addMessage("borrowForm", msg);
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
