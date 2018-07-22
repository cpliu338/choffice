package org.therismos.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import org.therismos.entity.Author;
import org.therismos.entity.Book;
import org.therismos.entity.BookCopy;
import org.therismos.entity.BookCopyPK;
import org.therismos.entity.Member1;
import org.therismos.entity.Publisher;

/**
 *
 * @author Administrator
 */
@javax.ejb.Stateless
public class BookDao {

    @PersistenceContext
    private EntityManager em;
    
    /**
     * Convert 0A1A to 0011
     * @param code input code with A
     * @return output code without A
     */
    public String convert(String code) {
    	// = "10A0A107";
    	StringBuilder buf = new StringBuilder(code);
    	for (int i=buf.lastIndexOf("A"); i > 0; i = buf.lastIndexOf("A")) {
            	buf.setCharAt(i, code.charAt(i-1));
    	}
    	return buf.toString();
    }

            
    public BookCopy searchCopyByCode(String bookCode) throws NumberFormatException, PersistenceException {
        BookCopyPK pk = new BookCopyPK(bookCode);
        return em.find(BookCopy.class, pk);
    }
    
    public Book searchBookById(int id) throws NumberFormatException, PersistenceException {
        return em.find(Book.class, id);
    }
    
    public int findNextNewCallno() {
        List<Book> found = em.createNamedQuery("Book.findAll", Book.class).getResultList();
        if (found==null || found.isEmpty())
            return 1;
        return found.get(0).getCallNo()+1;
    }
    
    public Author getAuthorByNameChi(String namechi) {
        Author a = null;
        try {
            a = em.createNamedQuery("Author.findByNameChi", Author.class).setParameter("nameChi", namechi).getSingleResult();
        }
        catch (RuntimeException ignored) {}
        return a;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Author createAuthor(String namechi) {
        Author a = getAuthorByNameChi(namechi);
        if (a==null) {
            a = new Author();
            a.setNameChi(namechi);
            a.setNameEng(namechi);
            em.persist(a); 
            return a;
        }
        else throw new javax.persistence.EntityExistsException("Author already exists");
    }
    
    public Publisher getPublisherByNameChi(String namechi) {
        Publisher p = null;
        try {
            p = em.createNamedQuery("Publisher.findByNameChi", Publisher.class).setParameter("nameChi", namechi).getSingleResult();
        }
        catch (RuntimeException ignored) {}
        return p;
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Publisher createPublisher(String namechi) {
        Publisher p = getPublisherByNameChi(namechi);
        if (p==null) {
            p = new Publisher();
            p.setNameChi(namechi);
            p.setNameEng(namechi);
            em.persist(p); 
            return p;
        }
        else throw new javax.persistence.EntityExistsException("Already exists");
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
            case BookCopy.RETIRED:
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
    @RolesAllowed({"deacons","librarians"})
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
    
    public Book checkBookByTitle(String title) {
        try {
            return em.createNamedQuery("Book.findLikeTitle", Book.class).setParameter("title", title).getResultList().get(0);
        }
        catch (RuntimeException ex) {
            return null;
        }
    }
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Book createBook(Book b, String indexStr, String author, String publisher, boolean creative) {
        Author a = this.getAuthorByNameChi(author.trim());
        if (a==null && creative)
            a=createAuthor(author);
        b.setAuthor(a==null ? em.find(Author.class, 1149) : a);
        Publisher p = this.getPublisherByNameChi(publisher.trim());
        if (p==null && creative)
            p = createPublisher(publisher);
        b.setPublisher(p==null ? em.find(Publisher.class, 402) : p);
        b.setIndexStr(indexStr);
        b.setRemark("");
        em.persist(b);
        BookCopy c = new BookCopy();
        c.setBookCopyPK(new BookCopyPK(b.getCallNo(),1));
        c.setBook(b);
        c.setStatus(BookCopy.STOCKTAKING);
        c.setUserId(Member1.XXXid);
        c.setStartdate(new java.util.Date());
        c.setEnddate(c.getStartdate());
        c.setRemark("");
        em.merge(c);
        em.flush(); // needed so that last inserted id gets updated
        return b;
    }
    
    public void addBookCopy(int bookid) {
        List<BookCopy> found = em.createNamedQuery("BookCopy.findByCallNo",BookCopy.class).setParameter("callNo", bookid).getResultList();
        if (found.isEmpty()) return;
        BookCopy c = new BookCopy();
        c.setBookCopyPK(new BookCopyPK(bookid,found.size()+1));
        c.setBook(em.find(Book.class, bookid));
        c.setStatus(BookCopy.STOCKTAKING);
        c.setUserId(Member1.XXXid);
        c.setStartdate(new java.util.Date());
        c.setEnddate(c.getStartdate());
        c.setRemark("");
        em.persist(c);
    }
    
    public java.util.List<String> suggestBook(String query) throws PersistenceException {
        ArrayList<String> results = new ArrayList<>();
        for (Book b : em.createNamedQuery("Book.findLikeTitle", Book.class).setParameter("title", "%"+query+"%").getResultList()) {
            results.add(b.getTitle());
        }
        return results;
    }
    
    public java.util.List<BookCopy> findCopies(int callno) {
        return em.createNamedQuery("BookCopy.findByCallNo", BookCopy.class).setParameter("callNo", callno).getResultList();
    }
}
