package net.ausiasmarch.blogbuster2021.Listener;

import net.ausiasmarch.blogbuster2021.Helper.Helper;
import net.ausiasmarch.blogbuster2021.Connection.HikariConnection;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//https://www.arquitecturajava.com/servletcontextlistener/
public class Listener implements ServletContextListener {

    HikariConnection oConnectionPool=null;
    
    @Override
    public void contextInitialized(ServletContextEvent oServletContextEvent) {
        try {
            ServletContext oServletContext = oServletContextEvent.getServletContext();
            Class.forName("com.mysql.jdbc.Driver");
            Properties oProperties = Helper.loadResourceProperties();
            HikariConnection oConnectionPool = new HikariConnection(
                   Helper.getConnectionChain(oProperties.getProperty("database.host"), oProperties.getProperty("database.port"), oProperties.getProperty("database.dbname")),
                   oProperties.getProperty("database.username"),
                   oProperties.getProperty("database.password"),
                   Integer.parseInt(oProperties.getProperty("databaseMinPoolSize")),
                   Integer.parseInt(oProperties.getProperty("databaseMaxPoolSize"))
            );
            oServletContext.setAttribute("pool", oConnectionPool);
            oServletContext.setAttribute("properties", oProperties);
            System.out.print("Aplicacion web arrancada");
        } catch (ClassNotFoundException ex) {
            System.out.print("Error en driver mysql JDBC");
        } catch (IOException ex) {
            System.out.print("Error al cargar fichero de propiedades");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            oConnectionPool.closePool();
        } catch (SQLException ex) {
            System.out.print("Error al cerrar el pool de conexiones");
        }
        System.out.print("Aplicacion web parada");
    }
}
