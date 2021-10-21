package net.ausiasmarch.blogbuster2021.Connection;

import java.io.IOException;
import java.util.Properties;
import net.ausiasmarch.blogbuster2021.Helper.Helper;

public class HikariPool {

    private static HikariConnection oHikariConnection = null;
    private static Properties oProperties = null;

    public static HikariConnection getHikariPool() throws ClassNotFoundException, IOException {
        if (oHikariConnection == null) {
            Class.forName("com.mysql.jdbc.Driver");
            oProperties = Helper.loadResourceProperties();            
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
