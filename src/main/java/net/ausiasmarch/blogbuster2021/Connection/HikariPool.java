package net.ausiasmarch.blogbuster2021.Connection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import net.ausiasmarch.blogbuster2021.Helper.Helper;

public class HikariPool {

    private HikariConnection oHikariConnection = null;
    Properties oProperties = null;

    public HikariPool() throws ClassNotFoundException, IOException {
        Class.forName("com.mysql.jdbc.Driver");
        oProperties = Helper.loadResourceProperties();
    }

    public HikariConnection getHikariPool() {
        if (oHikariConnection == null) {
            System.out.print("Abriendo pool");
            oHikariConnection = new HikariConnection(
                   Helper.getConnectionChain(oProperties.getProperty("database.host"),
                          oProperties.getProperty("database.port"),
                          oProperties.getProperty("database.dbname")),
                   oProperties.getProperty("database.username"),
                   oProperties.getProperty("database.password"),
                   Integer.parseInt(oProperties.getProperty("databaseMinPoolSize")),
                   Integer.parseInt(oProperties.getProperty("databaseMaxPoolSize"))
            );
        }
        return oHikariConnection;
    }   

}
