package net.ausiasmarch.blogbuster2021;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Control extends HttpServlet {

    Properties properties = new Properties();
    HikariConnection oConnectionPool = null;

    private void opDelay(Integer iLast) {
        try {
            Thread.sleep(iLast);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void loadResourceProperties() throws FileNotFoundException, IOException {
        // https://stackoverflow.com/questions/44499306/how-to-read-application-properties-file-without-environment?noredirect=1&lq=1
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try ( InputStream resourceStream = loader.getResourceAsStream("application.properties")) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doCORS(HttpServletRequest oRequest, HttpServletResponse oResponse) {
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
            // https://stackoverflow.com/questions/56479150/access-blocked-by-cors-policy-response-to-preflight-request-doesnt-pass-access
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

    private String getConnectionChain(String databaseHost, String databasePort, String databaseName) {
        return "jdbc:mysql://" + databaseHost + ":" + databasePort + "/"
                + databaseName + "?autoReconnect=true&useSSL=false";
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

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doCORS(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doCORS(request, response);
        Gson oGson = new Gson();
        String dbversion = null;
        try ( PrintWriter out = response.getWriter()) {            
            try ( Connection oConnection = oConnectionPool.newConnection()) {
                opDelay(new Random().nextInt(5000) + 1);
                Statement stmt = oConnection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT version()");
                if (rs.next()) {
                    dbversion = "Database Version : " + rs.getString(1);
                } else {
                    throw new Exception("Error al obtener la versión de la base de datos");
                }
                /*  Quan al try li passen paràmetres, aquest els tanca automàticament
                    Font => https://stackoverflow.com/questions/42998026/java-try-with-resources-on-parameters
                    oConnection.close();
                 */
            } catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(oGson.toJson(ex.getMessage()));                
            } 
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(oGson.toJson(dbversion));
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(oGson.toJson(ex.getMessage()));
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
