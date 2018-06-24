package org.therismos.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
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
@Table(name = "entries")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Entry.findBetweenDates", query = "SELECT e FROM Entry e WHERE e.date1 BETWEEN :date1 AND :date2 ORDER BY e.account.code,e.date1"),
    @NamedQuery(name = "Entry.findByTransref", query = "SELECT e FROM Entry e WHERE e.transref = :transref ORDER BY e.account.id"),
    @NamedQuery(name = "Entry.findUnchequesBeforeDate", query = "SELECT e FROM Entry e WHERE e.date1 <= :enddate AND e.account.id = :accountid AND e.extra1 LIKE '$%'"),
    @NamedQuery(name = "Entry.aggregate", query = "SELECT e.account.id, SUM(e.amount) FROM Entry e WHERE e.account.id IN :acclist AND (e.date1 BETWEEN :start AND :end) GROUP BY e.account.id ORDER BY e.account.id"),
    @NamedQuery(name = "Entry.findByAccount", query = "SELECT e FROM Entry e WHERE e.account.id=:account_id AND (e.date1 BETWEEN :start AND :end) ORDER BY e.date1")})
public class Entry implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Temporal(TemporalType.DATE)
    private Date date1;
    private Integer transref;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    private BigDecimal amount;
    private String detail;
    private String extra1;
//    @Column(name = "account_id")
//    private Integer accountId;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    
    public Entry() {
    }

    public Entry(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Integer getTransref() {
        return transref;
    }

    public void setTransref(Integer transref) {
        this.transref = transref;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
        if (!(object instanceof Entry)) {
            return false;
        }
        Entry other = (Entry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.Entry[ id=" + id + " ]";
    }
    
}
