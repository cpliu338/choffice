package org.therismos.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrator
 */
@Entity
@Table(name = "lib_books")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Book.findAll", query = "SELECT b FROM Book b"),
    @NamedQuery(name = "Book.findByCallNo", query = "SELECT b FROM Book b WHERE b.callNo = :callNo"),
    @NamedQuery(name = "Book.findLikeTitle", query = "SELECT b FROM Book b WHERE b.title LIKE :title"),
    @NamedQuery(name = "Book.findByAuthorNo", query = "SELECT b FROM Book b WHERE b.authorNo = :authorNo"),
    @NamedQuery(name = "Book.findByPublisherNo", query = "SELECT b FROM Book b WHERE b.publisherNo = :publisherNo"),
    @NamedQuery(name = "Book.findByCatId", query = "SELECT b FROM Book b WHERE b.catId = :catId"),
    @NamedQuery(name = "Book.findByOwnerId", query = "SELECT b FROM Book b WHERE b.ownerId = :ownerId"),
    @NamedQuery(name = "Book.findLikeIndexStr", query = "SELECT b FROM Book b WHERE b.indexStr LIKE :indexStr")})
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "call_no")
    private Integer callNo;
    @Basic(optional = false)
    private String title;
    @Basic(optional = false)
    @Column(name = "author_no")
    private int authorNo;
    @Basic(optional = false)
    @Column(name = "publisher_no")
    private int publisherNo;
    @Basic(optional = false)
    @Lob
    private String remark;
    @Basic(optional = false)
    @Column(name = "cat_id")
    private int catId;
    @Basic(optional = false)
    @Column(name = "owner_id")
    private int ownerId;
    @Basic(optional = false)
    @Column(name = "index_str")
    private String indexStr;
    
    @OneToMany(mappedBy="book")
    private List<BookCopy> copies;

    public List<BookCopy> getCopies() {
        return copies;
    }

    public Book() {
    }

    public Book(Integer callNo) {
        this.callNo = callNo;
    }

    public Book(Integer callNo, String title, int authorNo, int publisherNo, String remark, int catId, int ownerId, String indexStr) {
        this.callNo = callNo;
        this.title = title;
        this.authorNo = authorNo;
        this.publisherNo = publisherNo;
        this.remark = remark;
        this.catId = catId;
        this.ownerId = ownerId;
        this.indexStr = indexStr;
    }

    public Integer getCallNo() {
        return callNo;
    }

    public void setCallNo(Integer callNo) {
        this.callNo = callNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAuthorNo() {
        return authorNo;
    }

    public void setAuthorNo(int authorNo) {
        this.authorNo = authorNo;
    }

    public int getPublisherNo() {
        return publisherNo;
    }

    public void setPublisherNo(int publisherNo) {
        this.publisherNo = publisherNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getCatId() {
        return catId;
    }

    public void setCatId(int catId) {
        this.catId = catId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getIndexStr() {
        return indexStr;
    }

    public void setIndexStr(String indexStr) {
        this.indexStr = indexStr;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (callNo != null ? callNo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Book)) {
            return false;
        }
        Book other = (Book) object;
        if ((this.callNo == null && other.callNo != null) || (this.callNo != null && !this.callNo.equals(other.callNo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.Book[ callNo=" + callNo + " ]";
    }
    
}
