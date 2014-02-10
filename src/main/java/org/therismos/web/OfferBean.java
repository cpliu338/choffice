package org.therismos.web;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.therismos.entity.Account;
import org.therismos.entity.Offer;

/**
 *
 * @author cpliu
 */
@ManagedBean
public class OfferBean {
    
    private int batch;
    private List offers;

    public OfferBean() {
        offers = java.util.Collections.EMPTY_LIST;
        batch = 1;
    }
    
    @PostConstruct 
    public void init() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        if (emf != null) {
            EntityManager em = emf.createEntityManager();
            offers = em.createQuery("SELECT o.member1.name,SUM(o.amount) FROM Offer o WHERE o.receipt=1 GROUP BY o.member1.id")
                    .getResultList();
//            Object[] arr = (Object[]) list.get(0);
//            Logger.getLogger("OfferBean").log(Level.INFO, "{0}, {1}", new Object[]{arr[0],arr[1]});
            em.close();
            emf.close();
        }
        else {
            throw new RuntimeException("cannot create PU");
        }
    }

    /**
     * @return the batch
     */
    public int getBatch() {
        return batch;
    }

    /**
     * @param batch the batch to set
     */
    public void setBatch(int batch) {
        this.batch = batch;
    }

    /**
     * @return the offers
     */
    public List<Offer> getOffers() {
        return offers;
    }

    /**
     * @param offers the offers to set
     */
    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
    
}
