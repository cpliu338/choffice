package org.therismos.ejb;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.*;
import org.therismos.entity.Account;
/**
 *
 * @author cpliu
 */
@Stateless
public class AccountService implements java.io.Serializable {
    
    @PersistenceContext
    private EntityManager em;
    
    public static final Logger logger = Logger.getLogger(AccountService.class.getName());
    
    public AccountService() {}
    
    public List<Account> getAccountsBelow(String summaryCode){
        String qry = "SELECT a from Account a WHERE "
                + String.format("(a.code LIKE '%s__' OR a.code LIKE '%s__0') ", summaryCode, summaryCode)
                + "AND a.code NOT LIKE '__0' ORDER BY a.code";
        logger.log(Level.INFO, "Query: {0}", qry);
        return em.createQuery(qry, Account.class).getResultList();
    }
    
    public Account getAccountByCode(String code) {
        try {
            return em.createNamedQuery("Account.findByCode", Account.class)
                    .setParameter("code", code).getSingleResult();
        }
        catch (javax.persistence.NoResultException ex) { return null;}
    }
    
    public double reckon(String code, Date cutoff) {
        String code2 = code;
        if (code.endsWith("0")) {
            code2 = code.substring(0, code.length()-1).concat("%");
        }
        String qry = "SELECT a FROM Account a WHERE a.code LIKE :code";
        logger.log(Level.INFO, "Code2 for "+code+" is "+code2);
        List<Integer> acc_ids = new ArrayList<>();
        for (Account a : em.createQuery(qry, Account.class).setParameter("code", code2).getResultList()) {
            acc_ids.add(a.getId());
        }
        Date yearstart = new Date(cutoff.getTime());
        yearstart.setMonth(0);
        yearstart.setDate(1);
        qry = "SELECT SUM(e.amount) from Entry e WHERE " +
                "e.date1 <= :cutoff AND e.date1>=:yearstart AND " +
                "e.account.id IN (:ids)";
        Object o = em.createQuery(qry).setParameter("yearstart", yearstart)
                .setParameter("cutoff", cutoff).setParameter("ids", acc_ids).getSingleResult();
        if (o == null) {
            logger.log(Level.INFO, "Sum query returns zero");
            return 0.0;
        }
        logger.log(Level.INFO, "Sum query returns "+o.toString());
        return Double.parseDouble(o.toString());        
    }
}
