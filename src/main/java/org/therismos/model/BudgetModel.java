package org.therismos.model;

import java.util.logging.*;
import java.util.*;

/**
 *
 * @author USER
 */
public class BudgetModel {

    private String code;
    private int year;
    private String remarks;
    private String name_chi;
    final private TreeSet<Map<String, Object>> entries;
    final private TreeSet<Map<String, Object>> subitems;
    
    static final Logger logger = Logger.getLogger(BudgetModel.class.getName());
    
    @Override
    public String toString() {
        return "BudgetModel{" + "code=" + getCode() + ", year=" + getYear() + ", name_chi=" + getName_chi() 
                + subitems.size() + " subitems=" + entries.size() + " entries="
                + '}';
    }

    public BudgetModel() {
        code = "";
        year = Calendar.getInstance().get(Calendar.YEAR);
        name_chi = "";
        remarks = "";
        entries = new TreeSet<>(new Comparator<Map>() {
            @Override
            public int compare(Map t, Map t1) {
                return t.get("date").toString().compareTo(t1.get("date").toString());
            }
        });
        subitems = new TreeSet<>(new Comparator<Map>() {
            @Override
            public int compare(Map t, Map t1) {
                return t.get("code").toString().compareTo(t1.get("code").toString());
            }
        });
    }

    public void clearEntries() {
        getEntries().clear();
    }
    
    public void clearSubitems() {
        getSubitems().clear();
    }
    
    public void addToEntries(String date, Number amount) {
        HashMap<String, Object> m = new HashMap<>();
        m.put("date", date);
        m.put("amount", amount);
        entries.add(m);
    }
    
    public void addToSubitems(String code, String name_chi) {
        HashMap<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("name_chi", name_chi);
        subitems.add(m);
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
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
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

    /**
     * @return the entries
     */
    public TreeSet<Map<String, Object>> getEntries() {
        return entries;
    }

    /**
     * @return the subitems
     */
    public TreeSet<Map<String, Object>> getSubitems() {
        return subitems;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
