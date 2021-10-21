package net.ausiasmarch.blogbuster2021.Dao;

import net.ausiasmarch.blogbuster2021.Bean.PostBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import net.ausiasmarch.blogbuster2021.Exception.InternalServerErrorException;
import net.ausiasmarch.blogbuster2021.Helper.Helper;

public class PostDAO {

    private Connection oConnection = null;

    public PostDAO(Connection oConnection) {
        this.oConnection = oConnection;
    }

    public PostBean getOne(int id) throws SQLException {
        String srtSQL = "SELECT * FROM post WHERE id=?";
        PreparedStatement oPreparedStatement = oConnection.prepareStatement(srtSQL);
        oPreparedStatement.setInt(1, id);
        ResultSet oResultSet = oPreparedStatement.executeQuery();
        PostBean oPostBean = null;
        if (oResultSet.next()) {
            oPostBean = new PostBean();
            oPostBean.setId(id);
            oPostBean.setTitulo(oResultSet.getString("titulo"));
            oPostBean.setCuerpo(oResultSet.getString("cuerpo"));
            oPostBean.setFecha(oResultSet.getTimestamp("fecha").toLocalDateTime());
            oPostBean.setEtiquetas(oResultSet.getString("etiquetas"));
            oPostBean.setVisible(oResultSet.getBoolean("visible"));
        }
        oResultSet.close();
        oPreparedStatement.close();
        return oPostBean;
    }

    public int delete(int id) throws SQLException {
        String srtSQL = "DELETE FROM post WHERE id=?";
        PreparedStatement oPreparedStatement = oConnection.prepareStatement(srtSQL);
        oPreparedStatement.setInt(1, id);
        int result = oPreparedStatement.executeUpdate();
        oPreparedStatement.close();
        return result;
    }

    public long create(PostBean oPostBean) throws SQLException {
        long newID = -1L;
        String srtSQL = "INSERT INTO post (titulo,cuerpo,fecha,etiquetas,visible) VALUES (?,?,?,?,?)";
        PreparedStatement oPreparedStatement = oConnection.prepareStatement(srtSQL, PreparedStatement.RETURN_GENERATED_KEYS);
        oPreparedStatement.setString(1, oPostBean.getTitulo());
        oPreparedStatement.setString(2, oPostBean.getCuerpo());
        //https://stackoverflow.com/questions/18614836/using-setdate-in-preparedstatement
        //   "fecha": "2021-01-01 12:23",
        oPreparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(oPostBean.getFecha()));
        oPreparedStatement.setString(4, oPostBean.getEtiquetas());
        oPreparedStatement.setBoolean(5, oPostBean.getVisible());
        int iResult = oPreparedStatement.executeUpdate();
        ResultSet rs = oPreparedStatement.getGeneratedKeys();
        if (rs.next()) {
            newID = rs.getLong(1);
        }
        oPreparedStatement.close();
        return newID;
    }

    public int update(PostBean oPostBean) throws SQLException {
        String srtSQL = "UPDATE post SET titulo = ?, cuerpo = ? , fecha = ?, etiquetas = ?, visible = ? WHERE id = ?";
        PreparedStatement oPreparedStatement = oConnection.prepareStatement(srtSQL, PreparedStatement.RETURN_GENERATED_KEYS);
        oPreparedStatement.setString(1, oPostBean.getTitulo());
        oPreparedStatement.setString(2, oPostBean.getCuerpo());
        //https://stackoverflow.com/questions/18614836/using-setdate-in-preparedstatement
        //   "fecha": "2021-01-01 12:23",
        oPreparedStatement.setTimestamp(3, java.sql.Timestamp.valueOf(oPostBean.getFecha()));
        oPreparedStatement.setString(4, oPostBean.getEtiquetas());
        oPreparedStatement.setBoolean(5, oPostBean.getVisible());
        oPreparedStatement.setInt(6, oPostBean.getId());
        int iResult = oPreparedStatement.executeUpdate();
        oPreparedStatement.close();
        return iResult;
    }

    public ArrayList<PostBean> getPage(int page, int rpp) throws SQLException {
        PreparedStatement oPreparedStatement;
        ResultSet oResultSet;
        int offset;
        if (page > 0 && rpp > 0) {
            offset = (rpp * page) - rpp;
        } else {
            throw new InternalServerErrorException("Página incorrecta");
        }
        oPreparedStatement = oConnection.prepareStatement("SELECT * FROM post LIMIT ? OFFSET ?");
        oPreparedStatement.setInt(1, rpp);
        oPreparedStatement.setInt(2, offset);
        oResultSet = oPreparedStatement.executeQuery();
        ArrayList<PostBean> oPostBeanList = new ArrayList<>();
        while (oResultSet.next()) {
            PostBean oPostBean = new PostBean();
            oPostBean.setId(oResultSet.getInt("id"));
            oPostBean.setTitulo(oResultSet.getString("titulo"));
            oPostBean.setCuerpo(oResultSet.getString("cuerpo"));            
            oPostBean.setFecha(oResultSet.getTimestamp("fecha").toLocalDateTime());
            oPostBean.setEtiquetas(oResultSet.getString("etiquetas"));
            oPostBean.setVisible(oResultSet.getBoolean("visible"));
            oPostBeanList.add(oPostBean);
        }
        return oPostBeanList;
    }
    
    public int getCount() throws SQLException{
        PreparedStatement oPreparedStatement;
        ResultSet oResultSet;
        oPreparedStatement = oConnection.prepareStatement("SELECT count(*) FROM post");
        oResultSet = oPreparedStatement.executeQuery();
        if (oResultSet.next()) {
            return oResultSet.getInt(1);
        } else {
            throw new InternalServerErrorException("Error en getCount");
        }        
    }

}
