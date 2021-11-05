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

    public ArrayList<PostBean> getPage(int page, int rpp, String order, String direction, String filter) throws SQLException {
        String[] campos = new String[]{"id", "titulo", "cuerpo", "fecha", "etiquetas", "visible"};
        PreparedStatement oPreparedStatement;
        ResultSet oResultSet;
        String strSQL = "SELECT * FROM post";
        String strSQLOrder = "";
        int offset;
        if (page > 0 && rpp > 0) {
            offset = (rpp * page) - rpp;
        } else {
            throw new InternalServerErrorException("Página incorrecta");
        }
        if (filter != null && filter.length() > 0) {
            if (Helper.isIntegerParsable(filter)) {
                strSQL += " WHERE id = ? ";
            } else {
                strSQL += " WHERE titulo LIKE ? OR cuerpo LIKE ? OR etiquetas LIKE ? ";
            }
        }
        if (order != null && order.length() > 0 && Helper.contains(campos, order)) {
            strSQL += " ORDER BY " + order;
            if (direction != null && direction.length() > 0 && direction.equalsIgnoreCase("asc")) {
                strSQL += " ASC";
            } else {
                strSQL += " DESC";
            }
        }
        strSQL += " LIMIT ? OFFSET ?";
        oPreparedStatement = oConnection.prepareStatement(strSQL);
        if (filter != null && filter.length() > 0) {
            if (Helper.isIntegerParsable(filter)) {
                oPreparedStatement.setInt(1, Integer.parseInt(filter));
                oPreparedStatement.setInt(2, rpp);
                oPreparedStatement.setInt(3, offset);
            } else {
                oPreparedStatement.setString(1, "%" + filter + "%");
                oPreparedStatement.setString(2, "%" + filter + "%");
                oPreparedStatement.setString(3, "%" + filter + "%");
                oPreparedStatement.setInt(4, rpp);
                oPreparedStatement.setInt(5, offset);
            }
        } else {
            oPreparedStatement.setInt(1, rpp);
            oPreparedStatement.setInt(2, offset);
        }
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

    public int getCount(String filter) throws SQLException {
        PreparedStatement oPreparedStatement;
        ResultSet oResultSet;
        String strSQL = "SELECT count(*) FROM post";
        if (filter != null && filter.length() > 0) {
            if (Helper.isIntegerParsable(filter)) {
                strSQL += " WHERE id = ? ";
            } else {
                strSQL += " WHERE titulo LIKE ? OR cuerpo LIKE ? OR etiquetas LIKE ? ";
            }
        }
        oPreparedStatement = oConnection.prepareStatement(strSQL);
        if (filter != null && filter.length() > 0) {
            if (Helper.isIntegerParsable(filter)) {
                oPreparedStatement.setInt(1, Integer.parseInt(filter));
            } else {
                oPreparedStatement.setString(1, "%" + filter + "%");
                oPreparedStatement.setString(2, "%" + filter + "%");
                oPreparedStatement.setString(3, "%" + filter + "%");
            }
        }
        oResultSet = oPreparedStatement.executeQuery();
        if (oResultSet.next()) {
            return oResultSet.getInt(1);
        } else {
            throw new InternalServerErrorException("Error en getCount");
        }
    }

}
