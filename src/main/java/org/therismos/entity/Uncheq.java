package org.therismos.entity;

import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * Bean for each unpresented cheque in pending list MongoCollection reconcile
 * @author cp_liu
 */
public class Uncheq {
    @BsonProperty("id")
    private int entryId;
    private double amount;
    private String extra1;

    /**
     * @return the entryId
     */
    public int getEntryId() {
        return entryId;
    }

    /**
     * @param id the entryId to set
     */
    public void setEntryId(int id) {
        this.entryId = id;
    }

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return the extra1
     */
    public String getExtra1() {
        return extra1;
    }

    /**
     * @param extra1 the extra1 to set
     */
    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }
}
