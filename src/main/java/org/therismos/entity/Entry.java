package org.therismos.entity;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.dbutils.QueryRunner;
import javax.sql.DataSource;

/**
 * POJO for entry entity got by dbutils BeanListHandler
 * @author cp_liu
 */
public class Entry {
    private long id;
    private java.sql.Date date1;
    private long transref;
    private BigDecimal amount;
    private String detail;
    private String extra1;
    private long account_id;
    
    public static Entry findById(DataSource ds, long id) throws SQLException {
        String sql = "SELECT * FROM entries WHERE id=?";
        return new QueryRunner(ds).query(sql, new org.apache.commons.dbutils.handlers.BeanHandler<Entry>(Entry.class), id);
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the date1
     */
    public java.sql.Date getDate1() {
        return date1;
    }

    /**
     * @param date1 the date1 to set
     */
    public void setDate1(java.sql.Date date1) {
        this.date1 = date1;
    }

    /**
     * @return the transref
     */
    public long getTransref() {
        return transref;
    }

    /**
     * @param transref the transref to set
     */
    public void setTransref(long transref) {
        this.transref = transref;
    }

    /**
     * @return the amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    /**
     * @return the account_id
     */
    public long getAccount_id() {
        return account_id;
    }

    /**
     * @param account_id the account_id to set
     */
    public void setAccount_id(long account_id) {
        this.account_id = account_id;
    }
}
