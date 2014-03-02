package org.therismos.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.therismos.entity.Offer;
import org.therismos.model.WeeklyOfferItem;
import org.therismos.model.WeeklyOfferList;

/**
 *
 * @author Administrator
 */
public class WeeklyOfferServlet extends HttpServlet {
    private List<Offer> offers;
    
//    @Override
//    public void init(ServletConfig config) throws ServletException {
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            dbUrl = config.getInitParameter("dbUrl");
//            dbUser = config.getInitParameter("dbUser");
//            dbPassword = config.getInitParameter("dbPassword");
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    private void task(Date d) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("churchPU");
        if (emf != null) {
            EntityManager em = emf.createEntityManager();
            offers = em.createQuery("SELECT o FROM Offer o WHERE o.date1=:date1 ORDER BY o.account.code",Offer.class)
                    .setParameter("date1", d).getResultList();
        }
        else {
            offers = java.util.Collections.EMPTY_LIST;
        }
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        if (offers == null) {
            offers = java.util.Collections.EMPTY_LIST;
        }
        else {
            offers.clear();
        }
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        WeeklyOfferList offerlist = new WeeklyOfferList();
        offerlist.setDate1(new Date(System.currentTimeMillis())); // dummy to be overwritten below
        try {
            Date d = fmt.parse(request.getParameter("datestr"));
Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.INFO, "datestr {0}", d);
            offerlist.setDate1(d);
            this.task(d);
        } catch (RuntimeException | ParseException ex) {
Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(404);
            return;
        }
        PrintWriter out = response.getWriter();
        for (Offer o : offers) {
            offerlist.addOffer(WeeklyOfferItem.fromOffer(o));
        }
        try {
            JAXBContext context = JAXBContext.newInstance(WeeklyOfferList.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out
            m.marshal(offerlist, out);
        } catch (JAXBException ex) {
            Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("JAXB Exception");
        } finally {            
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
