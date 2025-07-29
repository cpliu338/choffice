package org.therismos.entity;

import org.bson.types.ObjectId;

/**
 * Bean for MongoCollection reconcile
 * @author cp_liu
 */
public class Reconcile {
    private ObjectId id;
    private String accountId;
    private String start;
    private String end;
    private double bookBalance;
    private double uncheq;
    private java.util.List<Uncheq> pending;
    
    public Reconcile() {
        pending = java.util.Collections.EMPTY_LIST;
    }

    /**
     * @return the id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the start
     */
    public String getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * @return the bookBalance
     */
    public double getBookBalance() {
        return bookBalance;
    }

    /**
     * @param bookBalance the bookBalance to set
     */
    public void setBookBalance(double bookBalance) {
        this.bookBalance = bookBalance;
    }

    /**
     * @return the uncheq
     */
    public double getUncheq() {
        return uncheq;
    }

    /**
     * @param uncheq the uncheq to set
     */
    public void setUncheq(double uncheq) {
        this.uncheq = uncheq;
    }

    /**
     * @return the pending
     */
    public java.util.List<Uncheq> getPending() {
        return pending;
    }

    /**
     * @param pending the pending to set
     */
    public void setPending(java.util.List<Uncheq> pending) {
        this.pending = pending;
    }
}
