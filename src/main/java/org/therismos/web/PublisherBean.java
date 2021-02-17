package org.therismos.web;

import java.util.*;

/**
 * Dummy to be deleted
 * @author Administrator
 */
@javax.inject.Named
@javax.faces.view.ViewScoped
public class PublisherBean implements java.io.Serializable {
    private ArrayList<String> foundPublishers;
    private String crit;
    
    public PublisherBean() {
        foundPublishers = new ArrayList<>();
    }
    public void save() {    
        crit = "";
        foundPublishers.clear();
    }
    public void search() {
        foundPublishers.clear();
        foundPublishers.add("Dummy 1");
    }

    /**
     * @return the foundBooks
     */
    public ArrayList<String> getFoundPublishers() {
        return foundPublishers;
    }

    /**
     * @param foundBooks the foundBooks to set
     */
    public void setFoundPublishers(ArrayList<String> foundPublishers) {
        this.foundPublishers = foundPublishers;
    }

    /**
     * @return the crit
     */
    public String getCrit() {
        return crit;
    }

    /**
     * @param crit the crit to set
     */
    public void setCrit(String crit) {
        this.crit = crit;
    }

}
