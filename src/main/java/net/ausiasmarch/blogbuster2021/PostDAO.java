package net.ausiasmarch.blogbuster2021;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class PostDAO {

    private Connection oConnection = null;

    public PostDAO(Connection oConnection) {
        this.oConnection = oConnection;
    }

    private LocalDateTime convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public PostBean getOne(int id) throws SQLException {
        String srtSQL = "SELECT * FROM post WHERE id=?";
        PreparedStatement oPreparedStatement = oConnection.prepareStatement(srtSQL);
        oPreparedStatement.setInt(1, id);
        ResultSet oResultSet = oPreparedStatement.executeQuery();
        PostBean oPostBean =null;
        if (oResultSet.next()) {
            oPostBean = new PostBean();
            oPostBean.setId(id);
            oPostBean.setTitulo(oResultSet.getString("titulo"));
            oPostBean.setCuerpo(oResultSet.getString("cuerpo"));
            oPostBean.setFecha(convertToLocalDateViaInstant(oResultSet.getDate("fecha")));
            oPostBean.setEtiquetas(oResultSet.getString("etiquetas"));
            oPostBean.setVisible(oResultSet.getBoolean("visible"));            
        } 
        return oPostBean;        
    }

}
