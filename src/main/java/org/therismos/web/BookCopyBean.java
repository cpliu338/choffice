package org.therismos.web;

import java.util.Date;
import java.util.logging.*;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.PersistenceException;
import org.therismos.ejb.*;
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
    private BookDao bookDao;
    @EJB
    private MemberDao memberDao;

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
    private boolean checkinable;
    private boolean retirable;

    public boolean isCheckinable() {
        return checkinable;
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
                    actionString = memberDao.findMemberById(copy.getUserId()).getName() + "->" + copy.getBook().getTitle() 
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
    public void retire() {
        FacesMessage msg = new FacesMessage();
        try {
            bookDao.updateBookCopyStatus(new BookCopyPK(this.bookCode.trim()), BookCopy.RETIRED, null, 
                    null, null, "");
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary(bundle.getString("legend.retire"));
            msg.setDetail(this.bookCode);//.actionString);
            this.blankCopy();
        }
        catch (PersistenceException ex) {
            logger.log(Level.WARNING, "Database error");
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    public void return1() {
        FacesMessage msg = new FacesMessage();
        try {
            bookDao.updateBookCopyStatus(new BookCopyPK(this.bookCode.trim()), BookCopy.ONSHELF, null, 
                    null, null, "");
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary(bundle.getString("legend.checkin"));
            msg.setDetail(this.actionString);
            this.blankCopy();
        }
        catch (PersistenceException ex) {
            logger.log(Level.WARNING, "Database error");
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail(ex.getMessage());
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void borrow() {
        FacesMessage msg = new FacesMessage();
        try {
            bookDao.updateBookCopyStatus(new BookCopyPK(this.bookCode.trim()), BookCopy.LOANED, member, 
                    new Date(), new Date(System.currentTimeMillis()+14*86400000L), "");
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary(bundle.getString("legend.borrow"));
            msg.setDetail(this.actionString);
            this.blankCopy();
        }
        catch (PersistenceException ex) {
            logger.log(Level.WARNING, null, ex);
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setDetail("Database error");
            msg.setSummary(ex.getClass().getName());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    private static final Logger logger = Logger.getLogger(BookCopyBean.class.getName());
    
    public void search() {
        logger.log(Level.INFO, "book code {0}", bookCode);
        this.copy = bookDao.searchCopyByCode(bookCode);
    }
    
    public void searchByMemberCode() {
        FacesMessage msg = new FacesMessage();
        try {
            memberCode = bookDao.convert(memberCode).trim();
            member = memberDao.findMemberByCode(Integer.parseInt(memberCode));
            returnable = copy != null && (copy.getStatus() == BookCopy.LOANED);
            borrowable = (copy.getStatus() == BookCopy.ONSHELF);
        }
        catch (NumberFormatException | PersistenceException ex) {
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary(String.format("%s %s", bundle.getString("legend.member"),
                    bundle.getString("legend.notfound"))); //"Invalid member code");
            msg.setDetail(memberCode);
            blankCopy();
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        this.updateActionString();
    }
    
    public void searchByBookCode() {
        FacesMessage msg = new FacesMessage();
        try {
            bookCode = bookDao.convert(bookCode).trim();
            copy = bookDao.searchCopyByCode(bookCode);
            returnable = (copy.getStatus() == BookCopy.LOANED);
            borrowable = (copy.getStatus() == BookCopy.ONSHELF && this.member!=null);
            checkinable = (copy.getStatus() == BookCopy.STOCKTAKING);
            retirable =  (copy.getStatus() == BookCopy.ONSHELF || copy.getStatus() == BookCopy.STOCKTAKING);
            logger.log(Level.INFO, "Copy found: {0}", copy.getBook().getTitle());
        }
        catch (NumberFormatException | PersistenceException | NullPointerException ex) {
            logger.log(Level.SEVERE, null, ex);
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary(String.format("%s %s", bundle.getString("legend.barcode"),
                    bundle.getString("legend.notfound"))); //"Invalid book code");
            msg.setDetail(this.bookCode);
            blankCopy();
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        this.updateActionString();
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

    /**
     * @return the retirable
     */
    public boolean isRetirable() {
        return retirable;
    }
}
