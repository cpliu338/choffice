package org.therismos.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.util.logging.*;
import javax.ejb.EJB;
import org.therismos.ejb.BookDao;
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
    @EJB
    private BookDao dao;

    public Member1 getMember() {
        return member;
    }

    public void setMember(Member1 member) {
        this.member = member;
    }
    
    private boolean borrowable;

    public boolean isBorrowable() {
        return borrowable;
    }

    public boolean isReturnable() {
        return returnable;
    }
    
    private boolean returnable;
    
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
    
    public void return1() {
        FacesMessage msg = new FacesMessage();
        try {
            if (copy == null || copy.getStatus()!=BookCopy.LOANED)
                throw new RuntimeException("Cannot return");
            copy.setStatus(BookCopy.ONSHELF);
            copy.setStartdate(new java.util.Date());
            em.merge(copy);
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("Returned");
            msg.setDetail(this.actionString);
        }
        catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Database error");
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    @javax.persistence.PersistenceContext
    private EntityManager em;
    
    public void borrow() {
        FacesMessage msg = new FacesMessage();
        try {
            if (copy == null || copy.getStatus()!=BookCopy.ONSHELF || member ==null)
                throw new RuntimeException("Cannot borrow");
            if (em==null) throw new RuntimeException("Cannot create EntityManager");
            copy.setStatus(BookCopy.LOANED);
            copy.setUserId(member.getId());
            copy.setStartdate(new java.util.Date());
            copy.setEnddate(new java.util.Date(System.currentTimeMillis()+14*86400000L));
            em.merge(copy);
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("Borrowed");
            msg.setDetail(this.actionString);
        }
        catch (RuntimeException ex) {
            logger.log(Level.WARNING, "Database error");
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    private static Logger logger = Logger.getLogger(BookCopyBean.class.getName());
    
    public void search() {
        logger.log(Level.INFO, "book code {0}", bookCode);
        this.copy = dao.searchCopyByCode(bookCode);
    }
    
    public void searchByMemberCode() {
        FacesMessage msg = new FacesMessage();
        try {
            member = super.findMemberByCode(Integer.parseInt(memberCode));
            if (member == null) {
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
                msg.setSummary(super.bundle.getString("legend.notfound"));
                msg.setDetail(memberCode);
                blankCopy();
            }
            else {
                returnable = copy != null && (copy.getStatus() == BookCopy.LOANED);
                borrowable = (copy.getStatus() == BookCopy.ONSHELF);
//                msg.setSeverity(FacesMessage.SEVERITY_INFO);
//                msg.setSummary("Found");
//                msg.setDetail(member.getGroupname());
            }
        }
        catch (RuntimeException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary("Invalid member code");
            msg.setDetail(memberCode);
            blankCopy();
        }
        this.updateActionString();
        if (copy == null)
            FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void searchByBookCode() {
        FacesMessage msg = new FacesMessage();
        try {
            BookCopyPK pk = new BookCopyPK(bookCode.trim());
            copy = em.find(BookCopy.class, pk);
            if (copy == null) {
                msg.setSeverity(FacesMessage.SEVERITY_WARN);
                msg.setSummary(super.bundle.getString("legend.notfound"));
                msg.setDetail(""+pk.getCallNo()+":"+pk.getCopyNo());
                blankCopy();
            }
            else {
                returnable = (copy.getStatus() == BookCopy.LOANED);
                borrowable = (copy.getStatus() == BookCopy.ONSHELF && this.member!=null);
//                msg.setSeverity(FacesMessage.SEVERITY_INFO);
//                msg.setSummary("Found");
//                msg.setDetail(copy.getBook().getTitle());
            }
        }
        catch (NumberFormatException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary("Invalid book code");
            msg.setDetail(this.bookCode);
        }
        this.updateActionString();
        if (copy == null)
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
