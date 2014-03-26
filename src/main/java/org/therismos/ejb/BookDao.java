package org.therismos.ejb;

import java.util.ArrayList;
import java.util.Date;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.therismos.entity.Book;
import org.therismos.entity.BookCopy;
import org.therismos.entity.BookCopyPK;
import org.therismos.entity.Member1;

/**
 *
 * @author Administrator
 */
@javax.ejb.Stateless
public class BookDao {

    @PersistenceContext
    private EntityManager em;
    
    public BookCopy searchCopyByCode(String bookCode) throws NumberFormatException, PersistenceException {
        BookCopyPK pk = new BookCopyPK(bookCode.trim());
        return em.find(BookCopy.class, pk);
    }
    
    public Book searchBookById(int id) throws NumberFormatException, PersistenceException {
        return em.find(Book.class, id);
    }
    
    /**
     * 
     * @param pk the primary key of the copy to be updated
     * @param status2 the new status
     * @param member the member involved, or null if nil
     * @param startDate the start time specified, defaults to now
     * @param endDate the end time specified, defaults to now
     * @param remark the new remark
     * @throws PersistenceException 
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateBookCopyStatus(BookCopyPK pk, int status2, Member1 member, Date startDate, Date endDate, String remark) throws PersistenceException {
        BookCopy c = em.find(BookCopy.class, pk);
        switch (status2) {
            case BookCopy.LOANED:
                if (member == null) throw new java.lang.IllegalArgumentException("No member provided");
                c.setUserId(member.getId());
                c.setStartdate(startDate);
                c.setEnddate(endDate);
                break;
            case BookCopy.ONSHELF:
                c.setUserId(Member1.XXXid);
                break;
            default:
                throw new java.lang.UnsupportedOperationException("Unsupported status: "+status2);
        }
        if (startDate==null) c.setStartdate(new Date());
        if (endDate==null) c.setEnddate(new Date());
        c.setStatus(status2);
        c.setRemark(remark);
        em.merge(c);
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateBook(Book c) throws PersistenceException {
        em.merge(c);
    }
    
    public java.util.List<Book> fullTextSearch(String crit) throws PersistenceException {
        ArrayList<Book> foundBooks = new ArrayList<>();
        try {
            BookCopy c = this.searchCopyByCode(crit);
            if (c!=null)
                foundBooks.add(c.getBook());
        }
        catch (NumberFormatException | PersistenceException ignored) {}
        // If found by code, no need to continue
        if (!foundBooks.isEmpty()) return foundBooks;
        foundBooks.addAll(em.createNamedQuery("Book.findLikeIndexStr", Book.class).setParameter("indexStr", crit+"%").getResultList());
        foundBooks.addAll(em.createNamedQuery("Book.findLikeTitle", Book.class).setParameter("title", "%"+crit+"%").getResultList());
        return foundBooks;
    }

    public java.util.List<String> suggest(String query, String type) throws PersistenceException {
        return em.createNamedQuery(type+".suggest", String.class).setParameter("nameFrag", "%"+query+"%").getResultList();
    }
}
