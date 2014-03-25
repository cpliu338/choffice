package org.therismos.ejb;

import java.util.ArrayList;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.therismos.entity.Book;
import org.therismos.entity.BookCopy;
import org.therismos.entity.BookCopyPK;

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
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateBookCopy(BookCopy c) throws PersistenceException {
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
