package org.therismos.bean;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class Record {

    private Date startDate;
    private Date endDate;
    private int id;
    private double amount;
    private String name;
    private int batch;
    
    public Record(Date startDate, Date endDate, int id, String name, double amount, int batch) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.batch = batch;
    }
    
    public Record(Date startDate, Date endDate, ResultSet rs) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.batch = 1;
            /*
            SELECT `member_id`, `members`.`name`, SUM(`amount`)
            FROM `offers`,`members`
            WHERE `members`.id=offers.member_id AND `offers`.`date1` < '2011-04-01' AND `offers`.`date1` >= '2010-04-01'
            GROUP BY `member_id` ORDER BY `member_id`
             */
        try {
            id = rs.getInt(1);
            name = rs.getString(2);
            amount = rs.getDouble(3);
        } catch (SQLException ex) {
            Logger.getLogger(Record.class.getName()).log(Level.SEVERE, null, ex);
            id = 0;
            name = "Undefined";
            amount = 0.0;
        }

    }

    public String getIdString() {
        return String.format("Ref%d:%d:%d", startDate.getYear()+1900, batch, id);
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
     * @param batch the batch to set
     */
    public void setBatch(int batch) {
        this.batch = batch;
    }

}
