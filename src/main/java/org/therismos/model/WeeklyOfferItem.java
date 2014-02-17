package org.therismos.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.therismos.entity.Offer;

/**
 *
 * @author Administrator
 */
@XmlRootElement(name="offer")
public class WeeklyOfferItem {

    private String name;
//    @XmlElement(name = "account")
    private String account;
//    @XmlElement(name = "amount")
    private Double amount;
    private boolean anonymous;
    
    static public WeeklyOfferItem fromOffer(Offer o) {
        WeeklyOfferItem w = new WeeklyOfferItem();
        w.setAccount(o.getAccount().getNameChi());
        w.setAmount(o.getAmount());
        w.setName(o.getMember1().getName());
        w.setAnonymous(false);
        return w;
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
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @return the amount
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * @return the anonymous
     */
    @XmlAttribute(name = "anonymous")
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * @param anonymous the anonymous to set
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
    
}
