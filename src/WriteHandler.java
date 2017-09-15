import com.github.mustachejava.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class WriteHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            System.out.println(httpExchange.getRemoteAddress());
            String response = getResponse();
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            httpExchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream out = httpExchange.getResponseBody();
            out.write(responseBytes);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getResponse() throws Exception {
        Path index = Paths.get("web/board_write.html");
//        if (Files.exists(index)) {
//            Stream<String> indexStream = Files.lines(index);
//            StringBuilder indexHtml = new StringBuilder();
//            indexStream.forEach(line -> indexHtml.append(line + "\n"));


        if (Files.exists(index)) {
            byte[] indexHtmlBytes = Files.readAllBytes(index);
            HashMap<String, Object> scopes = new HashMap<>();

            Statement stmt = MyHTTPServer.conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from board_list");

//            try {
//                String sql = "INSERT INTO board_list (title, name, content)"+" values (?,?,?)";
//                PreparedStatement preparedStmt = MyHTTPServer.conn.prepareStatement(sql);
//                preparedStmt.setString(1, rs.getNString("title"));
//                preparedStmt.setString(2, rs.getNString("name"));
//                preparedStmt.setString(3, rs.getNString("content"));
//
//                preparedStmt.executeUpdate();
//                preparedStmt.close();
//
//            } catch (SQLException e){
//                System.out.println(e.toString());
//            }

            MustacheFactory mf = new DefaultMustacheFactory();
            String writeHtml = new String(indexHtmlBytes, StandardCharsets.UTF_8);
            Mustache mustache = mf.compile(new StringReader(writeHtml), "?");

            StringWriter a = new StringWriter();
            mustache.execute(a, scopes).flush();

            return a.toString();
        } else {
            return "Not found";
        }
    }
}
