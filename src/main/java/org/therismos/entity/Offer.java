package org.therismos.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
 * @author cpliu
 */
@Entity
@Table(name = "offers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Offer.findAll", query = "SELECT o FROM Offer o"),
    @NamedQuery(name = "Offer.findById", query = "SELECT o FROM Offer o WHERE o.id = :id"),
    @NamedQuery(name = "Offer.findByDate1", query = "SELECT o FROM Offer o WHERE o.date1 = :date1"),
    @NamedQuery(name = "Offer.findByAmount", query = "SELECT o FROM Offer o WHERE o.amount = :amount"),
    @NamedQuery(name = "Offer.findByPosted", query = "SELECT o FROM Offer o WHERE o.posted = :posted"),
    @NamedQuery(name = "Offer.findByReceipt", query = "SELECT o FROM Offer o WHERE o.receipt = :receipt")})
public class Offer implements Serializable {
    private static final long serialVersionUID = 1L;
        private Integer id;
    /*
    @Basic(optional = false)
    @Column(name = "member_id")
    private int memberId;
    */
        private Date date1;
        private Account account;
        private double amount;
        private boolean posted;
        private int receipt;

    public Offer() {
    }

    public Offer(Integer id) {
        this.id = id;
    }

    public Offer(Integer id, Date date1, Account account, double amount, boolean posted, int receipt) {
        // Missing member1
        this.id = id;
        this.date1 = date1;
        this.account = account;
        this.amount = amount;
        this.posted = posted;
        this.receipt = receipt;
    }

@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    private Member1 member1;
    @ManyToOne
    @JoinColumn(name="member_id")
    public Member1 getMember1() {
        return member1;
    }
    public void setMember1(Member1 member1) {
        this.member1 = member1;
    }

    @Basic(optional = false)
    @Temporal(TemporalType.DATE)
    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    @ManyToOne
    @JoinColumn(name="account_id")
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

@Basic(optional = false)
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

@Basic(optional = false)
    public boolean getPosted() {
        return posted;
    }

    public void setPosted(boolean posted) {
        this.posted = posted;
    }

@Basic(optional = false)
    public int getReceipt() {
        return receipt;
    }

    public void setReceipt(int receipt) {
        this.receipt = receipt;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Offer)) {
            return false;
        }
        Offer other = (Offer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.Offer[ id=" + id + " ]";
    }
    
}
