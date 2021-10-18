package net.ausiasmarch.blogbuster2021;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PostService {

    HttpServletRequest oRequest = null;
    HikariConnection oConnectionPool = null;
    Gson oGson = null;
    
    
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

    public PostService(HttpServletRequest request, HikariConnection oConnectionPool) {
        oRequest = request;
        this.oConnectionPool = oConnectionPool;
        //https://stackoverflow.com/questions/22310143/java-8-localdatetime-deserialized-using-gson
        oGson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
        }).create();
    }

    public String getOne() throws SQLException {
        UserBean oUserBean = null;
        Integer id = Integer.parseInt(oRequest.getParameter("id"));
        Connection oConnection = oConnectionPool.newConnection();
        PostDAO oPostDao = new PostDAO(oConnection);
        PostBean oPostBean = oPostDao.getOne(id);
        oConnection.close();
        return oGson.toJson(oPostBean);
    }

    public String delete() {
        return "";
    }

    public String create() throws IOException {
        HttpSession oSession = oRequest.getSession();
        UserBean oUserBean = (UserBean) oSession.getAttribute("usuario");
        if (oUserBean != null) {            
            if (oUserBean.getLogin() != null) {
                if (oUserBean.getLogin().equalsIgnoreCase("admin")) {
                    String payloadRequest = getBody(oRequest);
                    PostBean oPostBean = new PostBean();
                    try {
                        oPostBean = oGson.fromJson(payloadRequest, oPostBean.getClass());
                        try ( Connection oConnection = oConnectionPool.newConnection()) {
                            PostDAO oPostDao = new PostDAO(oConnection);
                            int iResult = oPostDao.create(oPostBean);
                            response.setStatus(HttpServletResponse.SC_OK);
                            out.print(oGson.toJson(iResult));
                        } catch (Exception ex) {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            out.print(oGson.toJson(ex.getMessage()));
                        }
                    } catch (Exception ex) {
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print(oGson.toJson(ex.getMessage()));
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(oGson.toJson("Unauthorized"));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(oGson.toJson("Unauthorized"));
        }
    }

    public String update() {
        return "";
    }

    public String getPage() {
        return "";
    }

}
