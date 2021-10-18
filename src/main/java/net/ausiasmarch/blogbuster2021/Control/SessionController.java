package net.ausiasmarch.blogbuster2021.Control;

import net.ausiasmarch.blogbuster2021.Connection.HikariConnection;
import net.ausiasmarch.blogbuster2021.Bean.UserBean;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.ausiasmarch.blogbuster2021.Connection.HikariPool;
import net.ausiasmarch.blogbuster2021.Helper.Helper;

public class SessionController extends HttpServlet {

    HikariConnection oConnectionPool = null;

    @Override
    public void init() throws ServletException {
        // https://stackoverflow.com/questions/13638978/java-servlets-overriding-initservletconfig-config
        try {
            oConnectionPool = (HikariConnection) new HikariPool().getDatasource();
        } catch (ClassNotFoundException | IOException ex) {
            System.out.print(ex.getMessage());
        }
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
        Gson oGson = new Gson();
        try ( PrintWriter out = response.getWriter()) {
            HttpSession oSession = request.getSession();
            UserBean oUserBean = (UserBean) oSession.getAttribute("usuario");
            if (oUserBean != null && oUserBean.getLogin() != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(oGson.toJson(oUserBean.getLogin()));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(oGson.toJson("Unauthorized"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
        HttpSession oSession = request.getSession();
        String payloadRequest = Helper.getRequestBody(request);
        UserBean oUserBean = new UserBean();
        Gson oGson = Helper.getGson();
        oUserBean = oGson.fromJson(payloadRequest, oUserBean.getClass());
        try ( PrintWriter out = response.getWriter()) {
            if (oUserBean.getLogin() != null && oUserBean.getPassword() != null) {
                if (oUserBean.getLogin().equalsIgnoreCase("admin") && oUserBean.getPassword().equalsIgnoreCase("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918")) { //admin
                    oSession.setAttribute("usuario", oUserBean);
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(oGson.toJson("Welcome"));
                } else {
                    if (oUserBean.getLogin().equalsIgnoreCase("user") && oUserBean.getPassword().equalsIgnoreCase("04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb")) { //user
                        oSession.setAttribute("usuario", oUserBean);
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(oGson.toJson("Welcome"));
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        out.print(oGson.toJson("Auth Error"));
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(oGson.toJson("Auth Error"));
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
        Helper.doCORS(request, response);
        Gson oGson = Helper.getGson();
        HttpSession oSession = request.getSession();
        try ( PrintWriter out = response.getWriter()) {
            oSession.invalidate();
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(oGson.toJson("Session closed"));
        }
    }

    @Override
    public void destroy() {
        try {
            oConnectionPool.closeConnection();
        } catch (SQLException ex) {
            System.out.print(ex.getMessage());
        }
    }
}
