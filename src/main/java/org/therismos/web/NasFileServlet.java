package org.therismos.web;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.therismos.bean.ApplicationBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * Let a remote client download a file, first login (valid JSESSIONID cookie with role)
 * https://{domain}/{context}/nas/path/to/file
 * find the file underneath ${naspath2}/backups
 * 
 * @author cp_liu
 */
@WebServlet(name = "NasFileServlet", urlPatterns = {"/nas/*"})
public class NasFileServlet extends HttpServlet {

    @jakarta.inject.Inject
    ApplicationBean appBean;
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        try /*(PrintWriter out = response.getWriter())*/ {
            String path = request.getPathInfo();
//            out.println("PathInfo " + path);
            Path p = Paths.get(appBean.getNaspath2(), path).toRealPath();
            java.io.File file = p.toFile();
            if (!file.canRead())
                response.sendError(404, file.getAbsolutePath() + " is not readable");
            else if (!file.isFile()) 
                response.sendError(404, file.getAbsolutePath() + " is not a file ");
            else {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                (new java.io.FileInputStream(file)).transferTo(response.getOutputStream());
            }
            
        }
        catch (java.nio.file.NoSuchFileException ex) {
            response.sendError(404, "Not found for :" + request.getPathInfo());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
        response.sendError(405, "Method Not Allowed");
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
