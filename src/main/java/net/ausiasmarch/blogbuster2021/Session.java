package net.ausiasmarch.blogbuster2021;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.ausiasmarch.blogbuster2021.UserBean;

public class Session extends HttpServlet {

    Properties properties = new Properties();
    HikariConnection oConnectionPool = null;

    private void loadResourceProperties() throws FileNotFoundException, IOException {
        // https://stackoverflow.com/questions/44499306/how-to-read-application-properties-file-without-environment?noredirect=1&lq=1
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try ( InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init(config);
            loadResourceProperties();
            Class.forName("com.mysql.jdbc.Driver");
            oConnectionPool = new HikariConnection(
                    getConnectionChain(properties.getProperty("database.host"), properties.getProperty("database.port"), properties.getProperty("database.dbname")),
                    properties.getProperty("database.username"),
                    properties.getProperty("database.password"),
                    Integer.parseInt(properties.getProperty("databaseMinPoolSize")),
                    Integer.parseInt(properties.getProperty("databaseMaxPoolSize"))
            );
        } catch (ClassNotFoundException | IOException ex) {

        }
    }

    private static String getConnectionChain(String databaseHost, String databasePort, String databaseName) {
        return "jdbc:mysql://" + databaseHost + ":" + databasePort + "/"
                + databaseName + "?autoReconnect=true&useSSL=false";
    }

    private static String getBody(HttpServletRequest request) throws IOException {
        //https://stackoverflow.com/questions/14525982/getting-request-payload-from-post-request-in-java-servlet
        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

    private void doCORS(HttpServletRequest oRequest, HttpServletResponse oResponse) {
        System.out.println("Request: " + oRequest.getRequestURI());
        System.out.println("FROM IP: " + oRequest.getHeader("x-forwarded-for"));
        oResponse.setContentType("application/json;charset=UTF-8");
        if (!(oRequest.getMethod().equalsIgnoreCase("OPTIONS"))) {
            oResponse.setHeader("Cache-control", "no-cache, no-store");
            oResponse.setHeader("Pragma", "no-cache");
            oResponse.setHeader("Expires", "-1");
            oResponse.setHeader("Access-Control-Allow-Origin", oRequest.getHeader("origin"));
            oResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH");
            oResponse.setHeader("Access-Control-Max-Age", "86400");
            oResponse.setHeader("Access-Control-Allow-Credentials", "true");
            oResponse.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, "
                    + "Origin, "
                    + "Accept, "
                    + "Authorization, "
                    + "ResponseType, "
                    + "Observe, "
                    + "X-Requested-With, "
                    + "Content-Type, "
                    + "Access-Control-Expose-Headers, "
                    + "Access-Control-Request-Method, "
                    + "Access-Control-Request-Headers");
        } else {
            //https://stackoverflow.com/questions/56479150/access-blocked-by-cors-policy-response-to-preflight-request-doesnt-pass-access
            System.out.println("Pre-flight");
            oResponse.setHeader("Access-Control-Allow-Origin", oRequest.getHeader("origin"));
            oResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH");
            oResponse.setHeader("Access-Control-Max-Age", "3600");
            oResponse.setHeader("Access-Control-Allow-Credentials", "true");
            oResponse.setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, "
                    + "Origin, "
                    + "Accept, "
                    + "Authorization, "
                    + "ResponseType, "
                    + "Observe, "
                    + "X-Requested-With, "
                    + "Content-Type, "
                    + "Access-Control-Expose-Headers, "
                    + "Access-Control-Request-Method, "
                    + "Access-Control-Request-Headers");
            oResponse.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doCORS(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("goGET Request: " + request.getRequestURI());
        doCORS(request, response);
        Gson oGson = new Gson();
        try ( PrintWriter out = response.getWriter()) {
            String op = request.getParameter("op");
            if (op != null) {
                HttpSession oSession = request.getSession();
                switch (op) {
                    case "check":
                        response.setStatus(HttpServletResponse.SC_OK);

//                        UserBean oUserBean = (UserBean) oSession.getAttribute("usuario");
//                        String login1=oUserBean.getLogin();
//                        out.print(oGson.toJson(login1));
                        out.print(oGson.toJson((String) ((UserBean) oSession.getAttribute("usuario")).getLogin()));
                        break;
                    case "get":
                        UserBean oUserBean1 = (UserBean) oSession.getAttribute("usuario");
                        String name = null;
                        if (oUserBean1 != null) {
                            name = oUserBean1.getLogin();
                        }
//                        NullPointerException is a run-time exception which is not recommended to catch it, but instead avoid it:
//                        https://stackoverflow.com/questions/15146339/catching-nullpointerexception-in-java  
//                        String name;
//                        try {
//                            name = ((UserBean) oSession.getAttribute("usuario")).getLogin();
//                        } catch (Exception ex) {
//                            name = null;
//                        }
                        if (name != null) {
                            if (name.equalsIgnoreCase("admin")) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                out.print(oGson.toJson("QWERTY"));
                            } else {
                                response.setStatus(HttpServletResponse.SC_OK);
                                out.print(oGson.toJson("ASDFG"));
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            out.print(oGson.toJson("Unauthorized"));
                        }
                        break;

                    case "connect":                   
                        String dbversion = null;
                        try ( Connection oConnection = oConnectionPool.newConnection()) {
                            Statement stmt = oConnection.createStatement();
                            ResultSet rs = stmt.executeQuery("SELECT version()");
                            if (rs.next()) {
                                dbversion = "Database Version : " + rs.getString(1);
                            } else {
                                throw new Exception("Error al obtener la versión de la base de datos");
                            }
                            oConnection.close();
                        } catch (Exception ex) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print(oGson.toJson(ex.getMessage()));
                        }
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(oGson.toJson(dbversion));
                        break;
                        
                    default:
                        response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        out.print(oGson.toJson("Method Not Allowed"));
                        break;
                }
            } else {
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                out.print(oGson.toJson("Method Not Allowed"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doCORS(request, response);
        Gson oGson = new Gson();
        try ( PrintWriter out = response.getWriter()) {
            HttpSession oSession = request.getSession();

            String payloadRequest = getBody(request);
            UserBean oUserBean = new UserBean();
            oUserBean = oGson.fromJson(payloadRequest, oUserBean.getClass());

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
        doCORS(request, response);
        Gson oGson = new Gson();
        try ( PrintWriter out = response.getWriter()) {
            HttpSession oSession = request.getSession();
            oSession.invalidate();
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(oGson.toJson("Session closed"));
        }
    }

}
