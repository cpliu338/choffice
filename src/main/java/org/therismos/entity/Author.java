/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.therismos.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrator
 */
@Entity
@Table(name = "lib_authors")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Author.findAll", query = "SELECT a FROM Author a"),
    @NamedQuery(name = "Author.findById", query = "SELECT a FROM Author a WHERE a.id = :id"),
    @NamedQuery(name = "Author.findByNameChi", query = "SELECT a FROM Author a WHERE a.nameChi = :nameChi"),
    @NamedQuery(name = "Author.suggest", query = "SELECT a.nameChi FROM Author a WHERE a.nameChi LIKE :nameFrag ORDER BY a.nameChi")})
public class Author implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name_chi")
    private String nameChi;
    @Basic(optional = false)
    @Column(name = "name_eng")
    private String nameEng;

    public Author() {
    }

    public Author(Integer id) {
        this.id = id;
    }

    public Author(Integer id, String nameChi, String nameEng) {
        this.id = id;
        this.nameChi = nameChi;
        this.nameEng = nameEng;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameChi() {
        return nameChi;
    }

    public void setNameChi(String nameChi) {
        this.nameChi = nameChi;
    }

    public String getNameEng() {
        return nameEng;
    }

    public void setNameEng(String nameEng) {
        this.nameEng = nameEng;
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
        if (!(object instanceof Author)) {
            return false;
        }
        Author other = (Author) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.Author[ id=" + id + " ]";
    }
    
}
