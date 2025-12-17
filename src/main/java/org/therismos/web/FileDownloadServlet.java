package org.therismos.web;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author cp_liu
 */
@WebServlet("/download")
public class FileDownloadServlet extends HttpServlet {
    
    @jakarta.annotation.Resource
    String datapath;
    final static String DOWNLOADDIR = "downloads";

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
        try ( OutputStream os = response.getOutputStream()) {
            String filename = request.getParameter("file");
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            switch (ext) {
                case "pdf" -> response.setContentType("application/pdf");
                case "xlsx" -> response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                default -> response.setContentType("application/octet-stream");
            }
            Path path = Paths.get(datapath, DOWNLOADDIR, filename);
            Files.copy(path, os);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Get download file as attachment";
    }

}
