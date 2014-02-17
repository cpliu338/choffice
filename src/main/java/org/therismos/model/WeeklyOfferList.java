package org.therismos.model;

import java.util.ArrayList;
import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Administrator
 */
@XmlRootElement(name="offers")
public class WeeklyOfferList {
    private Date date1;
    private ArrayList<WeeklyOfferItem> offers;
    
    public WeeklyOfferList() {
        offers = new ArrayList<>();
    }
    /**
     * @return the date1
     */
    @XmlAttribute(name = "date")
    public Date getDate1() {
        return date1;
    }

    /**
     * @param date1 the date1 to set
     */
    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    /**
     * @return the offers
     */
    @XmlElement(name = "offer")
    public ArrayList<WeeklyOfferItem> getOffers() {
        return offers;
    }

    /**
     * @param offers the offers to set
     */
    public void setOffers(ArrayList<WeeklyOfferItem> offers) {
        this.offers = offers;
    }
    
    public void addOffer(WeeklyOfferItem w) {
        offers.add(w);
    }

}
