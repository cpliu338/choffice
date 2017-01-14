package org.therismos.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.therismos.entity.Offer;
import org.therismos.model.WeeklyOfferItem;
import org.therismos.model.WeeklyOfferList;

/**
 *
 * @author Administrator
 */
public class WeeklyOfferServlet extends HttpServlet {
    private List<Offer> offers;
    private List<Row> rows;
    
    @javax.persistence.PersistenceContext
    private EntityManager em;
    
    private void task(Date d) {
//        if (offers == null) offers = new java.util.ArrayList<>();
        if (em != null) {
            offers.addAll(em.createQuery("SELECT o FROM Offer o WHERE o.date1=:date1 ORDER BY o.account.code",Offer.class)
                    .setParameter("date1", d).getResultList());
        }
        else {
            offers = java.util.Collections.EMPTY_LIST;
        }
    }

    /**
     * Processes requests for get XML
     * <code>GET</code> and
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGetXml(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMdd");
        WeeklyOfferList offerlist = new WeeklyOfferList();
        offerlist.setDate1(new Date(System.currentTimeMillis())); // dummy to be overwritten below
        try {
            Date d = fmt.parse(request.getParameter("datestr"));
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
    
    private XSSFWorkbook generateExcelReport() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Offer");
        // Set column widths
        sheet.setColumnWidth(0, 13*256);
        sheet.setColumnWidth(1, 13*256);
        sheet.setColumnWidth(2, 5*256);
        sheet.setColumnWidth(3, 13*256);
        sheet.setColumnWidth(4, 13*256);
        // Set Cell Styles
        CellStyle styleHeader1 = workbook.createCellStyle();
        Font fontHeader1 =workbook.createFont();
        fontHeader1.setFontHeight((short)360);
        fontHeader1.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHeader1.setFont(fontHeader1);
        styleHeader1.setAlignment(CellStyle.ALIGN_CENTER);
        CellStyle styleHeader2 = workbook.createCellStyle();
        styleHeader2.setFont(fontHeader1);
        styleHeader2.setBorderBottom(CellStyle.BORDER_MEDIUM);
        styleHeader2.setAlignment(CellStyle.ALIGN_CENTER);
        CellStyle styleSubHeadName = workbook.createCellStyle();
        Font fontBold = workbook.createFont();
        fontBold.setFontHeight((short)240);
        fontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleSubHeadName.setFont(fontBold);
        styleSubHeadName.setBorderBottom(CellStyle.BORDER_DOUBLE);
        CellStyle styleSubHeadAmount = workbook.createCellStyle();
        styleSubHeadAmount.setFont(fontBold);
        styleSubHeadAmount.setBorderBottom(CellStyle.BORDER_DOUBLE);
        styleSubHeadAmount.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        Font fontNormal = workbook.createFont();
        fontNormal.setFontHeight((short)240);
        fontNormal.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        CellStyle styleOfferName = workbook.createCellStyle();
        styleOfferName.setFont(fontNormal);
        CellStyle styleOfferAmount = workbook.createCellStyle();
        styleOfferAmount.setFont(fontNormal);
        styleOfferAmount.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        /* rows has offset 2, i.e. the first element is row[2] */
        rows = new ArrayList<>();
        // The key of the following maps are account ids
        Map<Integer, Double> accums = new HashMap<>();
        Map<Integer, String> accountNames = new HashMap<>();
        Map<Integer, List<Offer>> sortedOffers = new HashMap<>();
        Properties translate = new Properties();
        try {
            translate.load(WeeklyOfferServlet.class.getResourceAsStream("/config.properties"));
        } catch (IOException ex) {
            Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.SEVERE, null, ex);
            return workbook;
        }
        // Write the headers
        Row row = sheet.createRow(0);
        row.setHeight((short)(20*24)); // 24 pt height
        Cell cell = row.createCell(0);
        cell.setCellValue(translate.getProperty("legend.header","Therismos"));
        cell.setCellStyle(styleHeader1);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,4));
        row = sheet.createRow(1);
        row.setHeight((short)(20*24));
        cell = row.createCell(0);
        if (offers.isEmpty()) {
            cell.setCellValue("Empty");
            return workbook;
        }
        else {
            Offer o1 = offers.get(0);
            String fmt = translate.getProperty("legend.offerReport","Offers for {0,date,medium}");
            //java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
            cell.setCellValue(java.text.MessageFormat.format(fmt,
                    o1.getDate1()));
            cell.setCellStyle(styleHeader2);
        }
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,4));
        // Iterate offers to sortOffers, accumulate to accums
        for (Offer o : offers) {
            int id = o.getAccount().getId();
            if (accountNames.containsKey(id)) {
                accums.put(id, accums.get(id) + o.getAmount());
                sortedOffers.get(id).add(o);
            }
            else {
                accums.put(id, o.getAmount());
                accountNames.put(id, o.getAccount().getNameChi());
                List<Offer> list = new ArrayList<>();
                list.add(o);
                sortedOffers.put(id, list);
            }
        }
        // Write the worksheet
        // rowno is the row no for the left cluster (General Offer 41001)
        int rowno = 0;
        /* rowno is the row no for the right cluster (Other than General Offer 41001)
        Right cluster is offset 3 columns to the right*/
        int rowno2 = 0;
        Iterator<Integer> ids = accountNames.keySet().iterator();
        while (ids.hasNext()) {
            int id = ids.next();
            int colOffset = (id==41001) ? 0 : 3;
            if (id==41001)
                row = getRow(sheet, rowno++);
            else
                row = getRow(sheet, rowno2++);
            cell = row.createCell(colOffset);
            cell.setCellValue(accountNames.get(id));
            cell.setCellStyle(styleSubHeadName);
            cell = row.createCell(colOffset+1);
            cell.setCellValue(accums.get(id));
            cell.setCellStyle(styleSubHeadAmount);
            for (Offer o : sortedOffers.get(id)) {
                if (id==41001)
                    row = getRow(sheet, rowno++);
                else
                    row = getRow(sheet, rowno2++);
                cell = row.createCell(colOffset);
                cell.setCellValue(o.getMember1().getName());
                cell.setCellStyle(styleOfferName);
                cell = row.createCell(colOffset+1);
                cell.setCellValue(o.getAmount());
                cell.setCellStyle(styleOfferAmount);
            }
            if (id!=41001)
                rowno2++; // skip one line after a group
        }
        return workbook;
    }
    
    /**
     * Get or create a row
     * @param sheet the Worksheet
     * @param rowno the row no, with an offset = 2
     * @return the gotten or created row
     */
    private Row getRow(Sheet sheet, int rowno) {
        if (rows.size()<=rowno) {
            for (int offset=rows.size(); offset<=rowno; offset++) {
                rows.add(sheet.createRow(offset+2));
            }
        }
        return rows.get(rowno);
    }
    /**
     * Processes requests for get Excel
     * <code>GET</code> and
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGetExcel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //PrintWriter out = response.getWriter();
        
        response.setContentType("application/vnd.ms-excel");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String datestr = request.getParameter("datestr");
Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.INFO, "datestr {0}", datestr);
        try {
            Date d = fmt.parse(datestr);
            this.task(d);
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"Offer%s.xlsx\"", fmt.format(d)));
            XSSFWorkbook workbook = generateExcelReport();
            workbook.write(response.getOutputStream());
        } catch (RuntimeException | ParseException ex) {
Logger.getLogger(WeeklyOfferServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(404);
        }
    }
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
        if (offers == null) {
            offers = new ArrayList<>();
        }
        else {
            offers.clear();
        }
        String type = request.getParameter("type");
        if (type.contains("xls")) {
            doGetExcel(request, response);
        }
        else
            doGetXml(request, response);
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
        //processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Weekly Offer report with xml or xlsx flavor";
    }
}
