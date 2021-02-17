package org.therismos.model;

import java.util.*;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

/**
 *
 * @author cpliu
 */
public class AccountModel {
    
    private int id;
    private String code;
    private String name;
    private String name_chi;
    private String detail;
    private ObjectId _id;
    
    @Override
    public String toString() {
        return String.format("%s : %s (%s)", code, name_chi, detail);
    }
    
    public AccountModel(int id, String name) {
        this.id=id; this.name=name;
    }
    
    public AccountModel() {
        this.id=-1; name="Invalid";
    }

    /**
     * @return the id
     */
    public ObjectId getId() {
        return _id;
    }

    /**
     * @param id the id to set
     */
    public void setId(ObjectId id) {
        this._id = id;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the detail
     */
    public String getDetail() {
        return detail;
    }

    /**
     * @param detail the detail to set
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * @return the name_chi
     */
    public String getName_chi() {
        return name_chi;
    }

    /**
     * @param name_chi the name_chi to set
     */
    public void setName_chi(String name_chi) {
        this.name_chi = name_chi;
    }    
}