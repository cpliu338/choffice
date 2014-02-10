/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.therismos.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
    @NamedQuery(name = "Entry.findAll", query = "SELECT e FROM Entry e"),
    @NamedQuery(name = "Entry.findById", query = "SELECT e FROM Entry e WHERE e.id = :id"),
    @NamedQuery(name = "Entry.findByDate1", query = "SELECT e FROM Entry e WHERE e.date1 = :date1"),
    @NamedQuery(name = "Entry.findByTransref", query = "SELECT e FROM Entry e WHERE e.transref = :transref"),
    @NamedQuery(name = "Entry.findByAmount", query = "SELECT e FROM Entry e WHERE e.amount = :amount"),
    @NamedQuery(name = "Entry.findByDetail", query = "SELECT e FROM Entry e WHERE e.detail = :detail"),
    @NamedQuery(name = "Entry.findByExtra1", query = "SELECT e FROM Entry e WHERE e.extra1 = :extra1"),
    @NamedQuery(name = "Entry.findByAccountId", query = "SELECT e FROM Entry e WHERE e.accountId = :accountId")})
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
    @Column(name = "account_id")
    private Integer accountId;

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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
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
