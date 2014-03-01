package org.therismos.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Administrator
 */
@Embeddable
public class BookCopyPK implements Serializable {
    @Basic(optional = false)
    @Column(name = "call_no")
    private int callNo;
    @Basic(optional = false)
    @Column(name = "copy_no")
    private int copyNo;

    public BookCopyPK() {
    }

    public BookCopyPK(int callNo, int copyNo) {
        this.callNo = callNo;
        this.copyNo = copyNo;
    }

    public int getCallNo() {
        return callNo;
    }

    public void setCallNo(int callNo) {
        this.callNo = callNo;
    }

    public int getCopyNo() {
        return copyNo;
    }

    public void setCopyNo(int copyNo) {
        this.copyNo = copyNo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) callNo;
        hash += (int) copyNo;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookCopyPK)) {
            return false;
        }
        BookCopyPK other = (BookCopyPK) object;
        if (this.callNo != other.callNo) {
            return false;
        }
        if (this.copyNo != other.copyNo) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.BookCopyPK[ callNo=" + callNo + ", copyNo=" + copyNo + " ]";
    }
    
}
