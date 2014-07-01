package org.therismos.model;

import com.mongodb.*;
import java.util.*;
import org.bson.BSONObject;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;

/**
 *
 * @author USER
 */
public class BudgetModel implements DBObject {
    private final HashMap<String, Object> store;

    @Override
    public String toString() {
        return "BudgetModel{" + "code=" + getCode() + ", year=" + getYear() + ", name_chi=" + getName_chi() 
                //+ ", subitems=" + subitems + ", entries=" + entries 
                + '}';
    }

    public BudgetModel() {
        this.store = new HashMap<>();
        store.put("_id", null);
        store.put("code", "");
        store.put("year", 0);
        store.put("name_chi", "");
        store.put("subitems", new BasicDBList());
        store.put("entries", new BasicDBList());
        store.put("remarks", "");
    }

    /**
     * @return the _id
     */
    public ObjectId getId() {
        return (ObjectId)store.get("_id");
    }

    /**
     * @param _id the _id to set
     */
    public void setId(ObjectId _id) {
        store.put("_id", _id);
    }

    /**
     * @return the code
     */
    public String getCode() {
        return (String)store.get("code");
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        store.put("code", code);
    }

    /**
     * @return the year
     */
    public int getYear() {
        return (Integer)store.get("year");
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        store.put("year", year);
    }

    /**
     * @return the name_chi
     */
    public String getName_chi() {
        return (String)store.get("name_chi");
    }

    /**
     * @param name_chi the name_chi to set
     */
    public void setName_chi(String name_chi) {
        store.put("name_chi", name_chi);
    }

    /**
     * @return the subitems
     */
    public BasicDBList getSubitems() {
        return (BasicDBList)store.get("subitems");
    }

    /**
     * @param subitems the subitems to set
     */
    public void setSubitems(BasicDBList subitems) {
        store.put("subitems", subitems);
    }

    /**
     * @return the entries
     */
    public BasicDBList getEntries() {
        return (BasicDBList)store.get("entries");
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(BasicDBList entries) {
        store.put("entries", entries);
    }
    
    public void clearEntries() {
        getEntries().clear();
    }
    
    public void clearSubitems() {
        getSubitems().clear();
    }
    
    public void addToEntries(DateTime date, double amount) {
        getEntries().add(new BasicDBObject(org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd").print(date),
        amount));
    }
    public void setSubitemsByString(String l) {
        String[] tokens = l.split(",");
        getSubitems().clear();
        for (String token : tokens) {
            getSubitems().add(Integer.parseInt(token.trim()));
        }
    }

    @Override
    public void markAsPartialObject() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPartialObject() {
        return false;
    }

    @Override
    public Object put(String string, Object o) {
        return store.put(string, o);
    }

    @Override
    public void putAll(BSONObject bsono) {
        for (String key : bsono.keySet()) {
            if (store.containsKey(key))
                store.put(key, bsono.get(key));
        }
    }

    @Override
    public void putAll(Map map) {
        store.putAll(map);
    }

    @Override
    public Object get(String string) {
        return store.get(string);
    }

    @Override
    public Map toMap() {
        return store;
    }

    @Override
    public Object removeField(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean containsKey(String string) {
        return store.containsKey(string);
    }

    @Override
    public boolean containsField(String string) {
        return store.containsKey(string);
    }

    @Override
    public Set<String> keySet() {
        return store.keySet();
    }
    
    public String getRemarks() {
        return (String)store.get("remarks");
    }

    public void setRemarks(String remarks) {
        store.put("remarks", remarks);
    }
}
