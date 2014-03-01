package org.therismos.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrator
 */
@Entity
@Table(name = "lib_copies")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookCopy.findByCallNo", query = "SELECT b FROM BookCopy b WHERE b.bookCopyPK.callNo = :callNo"),
    @NamedQuery(name = "BookCopy.findByStatus", query = "SELECT b FROM BookCopy b WHERE b.status = :status"),
    @NamedQuery(name = "BookCopy.findByStartdate", query = "SELECT b FROM BookCopy b WHERE b.startdate = :startdate"),
    @NamedQuery(name = "BookCopy.findByEnddate", query = "SELECT b FROM BookCopy b WHERE b.enddate = :enddate"),
    @NamedQuery(name = "BookCopy.findByUserId", query = "SELECT b FROM BookCopy b WHERE b.userId = :userId")
})
public class BookCopy implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected BookCopyPK bookCopyPK;
    @Basic(optional = false)
    private int status;
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startdate;
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date enddate;
    @Basic(optional = false)
    @Column(name = "user_id")
    private int userId;
    @Basic(optional = false)
    private String remark;
    @ManyToOne
    @JoinColumn(name="call_no", insertable=false, updatable=false)
    private Book book;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookCopy() {
    }

    public BookCopy(BookCopyPK bookCopyPK) {
        this.bookCopyPK = bookCopyPK;
    }

    public BookCopy(BookCopyPK bookCopyPK, int status, Date startdate, Date enddate, int userId, String remark) {
        this.bookCopyPK = bookCopyPK;
        this.status = status;
        this.startdate = startdate;
        this.enddate = enddate;
        this.userId = userId;
        this.remark = remark;
    }

    public BookCopy(int callNo, int copyNo) {
        this.bookCopyPK = new BookCopyPK(callNo, copyNo);
    }

    public BookCopyPK getBookCopyPK() {
        return bookCopyPK;
    }

    public void setBookCopyPK(BookCopyPK bookCopyPK) {
        this.bookCopyPK = bookCopyPK;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bookCopyPK != null ? bookCopyPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookCopy)) {
            return false;
        }
        BookCopy other = (BookCopy) object;
        if ((this.bookCopyPK == null && other.bookCopyPK != null) || (this.bookCopyPK != null && !this.bookCopyPK.equals(other.bookCopyPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.BookCopy[ bookCopyPK=" + bookCopyPK + " ]";
    }
    
}
