package org.therismos.model;

import com.mongodb.DBObject;
import java.util.*;
import org.bson.BSONObject;
import org.bson.types.ObjectId;

/**
 *
 * @author cpliu
 */
public class AccountModel implements DBObject {
    
    public static final ArrayList<String> keys;
    static {
        keys = new ArrayList<String> ();
        keys.add("id");
        keys.add("code");
        keys.add("name");
        keys.add("name_chi");
        keys.add("detail");
    };
    
    private ObjectId _id;
    private int id;
    private String code;
    private String name;
    private String name_chi;
    private String detail;
    
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
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
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

    @Override
    public void markAsPartialObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isPartialObject() {
        return false;
    }

    @Override
    public Object put(String string, Object o) {
        if ("_id".equals(string)) {
            _id = (ObjectId)o;
        } 
        else if ("id".equals(string)) {
            id = ((Number)o).intValue();
        } 
        else if ("code".equals(string)) {
            code = o.toString();
        }
        else if ("name".equals(string)) {
            name = (String)o;
        }
        else if ("name_chi".equals(string)) {
            name_chi = (String)o;
        }
        else if ("detail".equals(string)) {
            detail = (String)o;
        }
        return o;
    }

    @Override
    public void putAll(BSONObject bsono) {
        Iterator<String> it = bsono.keySet().iterator();
        while (it.hasNext()) {
            String o = it.next(); 
            this.put(o, bsono.get(o));
        }
    }

    @Override
    public void putAll(Map map) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object get(String string) {
        if ("_id".equals(string)) {
            return _id;
        } 
        else if ("id".equals(string)) {
            return id;
        } 
        else if ("code".equals(string)) {
            return code;
        }
        else if ("name".equals(string)) {
            return name;
        }
        else if ("name_chi".equals(string)) {
            return name_chi;
        }
        else if ("detail".equals(string)) {
            return detail;
        }
        return null;
    }

    @Override
    public Map toMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object removeField(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsKey(String string) {
        return containsField(string);
    }

    @Override
    public boolean containsField(String string) {
        return keys.contains(string);
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<String>(AccountModel.keys);
    }
    
}