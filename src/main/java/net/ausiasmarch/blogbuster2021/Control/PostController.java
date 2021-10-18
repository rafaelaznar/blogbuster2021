package net.ausiasmarch.blogbuster2021.Control;

import net.ausiasmarch.blogbuster2021.Connection.HikariConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.ausiasmarch.blogbuster2021.Helper.Helper;
import net.ausiasmarch.blogbuster2021.Service.PostService;

public class PostController extends HttpServlet {

    HikariConnection oConnectionPool = null;
    Properties oProperties = null;

    @Override
    public void init() throws ServletException {
        // https://stackoverflow.com/questions/13638978/java-servlets-overriding-initservletconfig-config
        ServletContext oServletContext = getServletContext();
        oConnectionPool = (HikariConnection) oServletContext.getAttribute("pool");
        oProperties = (Properties) oServletContext.getAttribute("properties");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
        try ( PrintWriter out = response.getWriter()) {
            if (request.getParameter("id") != null) {
                PostService oPostService = new PostService(request, oConnectionPool);
                try {
                    String result = oPostService.getOne();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(result);
                } catch (SQLException ex) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(ex.getMessage());
                }
            } else {
                if (request.getParameter("page") != null && request.getParameter("rpp") != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print("post.getpage");
                } else {
                    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    out.print("Method Not Allowed");
                }
            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
        try ( PrintWriter out = response.getWriter()) {            
            try {
                PostService oPostService = new PostService(request, oConnectionPool);
                String result = oPostService.create();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(result);
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(ex.getMessage());
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        try ( PrintWriter out = response.getWriter()) {
            try {
                PostService oPostService = new PostService(request, oConnectionPool);
                String result = oPostService.delete();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(result);
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(ex.getMessage());
            }
        }

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
        try ( PrintWriter out = response.getWriter()) {
            try {
                PostService oPostService = new PostService(request, oConnectionPool);
                String result = oPostService.update();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(result);
            } catch (SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(ex.getMessage());
            }

        }
    }

    @Override
    public void destroy() {
        try {
            oConnectionPool.closePool();
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
    }

}
