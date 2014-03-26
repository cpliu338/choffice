package org.therismos.entity;

import java.io.Serializable;
import javax.persistence.Basic;
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
 * @author cpliu
 */
@Entity
@Table(name = "members")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Member1.findAll", query = "SELECT m FROM Member1 m"),
    @NamedQuery(name = "Member1.findById", query = "SELECT m FROM Member1 m WHERE m.id = :id"),
    @NamedQuery(name = "Member1.findByNickname", query = "SELECT m FROM Member1 m WHERE m.nickname = :nickname"),
    @NamedQuery(name = "Member1.findByName", query = "SELECT m FROM Member1 m WHERE m.name = :name"),
    @NamedQuery(name = "Member1.findAllGroupname", query = "SELECT DISTINCT m.groupname FROM Member1 m"),
    @NamedQuery(name = "Member1.findByGroupname", query = "SELECT m FROM Member1 m WHERE m.groupname = :groupname")
})
public class Member1 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    private String nickname;
    @Basic(optional = false)
    private String name;
    private Integer level;
    private String pwd;
    @Basic(optional = false)
    private String groupname;
    @Basic(optional = false)
    private char photo;
    @Basic(optional = false)
    private char print;
    
    public static final int XXXid = 801;

    public Member1() {
    }

    public Member1(Integer id) {
        this.id = id;
    }

    public Member1(Integer id, String nickname, String name, String groupname, char photo, char print) {
        this.id = id;
        this.nickname = nickname;
        this.name = name;
        this.groupname = groupname;
        this.photo = photo;
        this.print = print;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public char getPhoto() {
        return photo;
    }

    public void setPhoto(char photo) {
        this.photo = photo;
    }

    public char getPrint() {
        return print;
    }

    public void setPrint(char print) {
        this.print = print;
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
        if (!(object instanceof Member1)) {
            return false;
        }
        Member1 other = (Member1) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.therismos.entity.Member1[ id=" + id + " ]";
    }
    
    public static int getIdFromBarcode(int code) {
        return (code-10000000)/107;
    }
    
    public int getBarcode() {
        return 10000000+107*this.id;
    }
    
}
