package org.therismos.ejb;

import javax.persistence.*;
import org.therismos.entity.Member1;

/**
 *
 * @author Administrator
 */
@javax.ejb.Stateless
public class MemberDao {

    @PersistenceContext
    private EntityManager em;
    
    public Member1 findMemberByCode(int code) {
        return findMemberById(Member1.getIdFromBarcode(code));
    }

    public Member1 findMemberById(int id) throws PersistenceException {
        return em.find(Member1.class, id);
    }
}
