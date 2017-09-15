import com.github.mustachejava.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IndexHandler implements HttpHandler {

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
        Path index = Paths.get("web/index.html");
//        if (Files.exists(index)) {
//            Stream<String> indexStream = Files.lines(index);
//            StringBuilder indexHtml = new StringBuilder();
//            indexStream.forEach(line -> indexHtml.append(line + "\n"));

        if (Files.exists(index)) {
            byte[] indexHtmlBytes = Files.readAllBytes(index);
            HashMap<String, Object> scopes = new HashMap<>();

            Statement stmt = MyHTTPServer.conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from board_list");
//            int idx = Integer.parseInt(rs.getInt(1));

            List<HashMap<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>();
                row.put("num", rs.getInt("num"));
                row.put("title", rs.getNString("title"));
                row.put("name", rs.getNString("name"));
                row.put("content", rs.getNString("content"));
                rows.add(row);
            }

            scopes.put("board_list", rows);

            MustacheFactory mf = new DefaultMustacheFactory();
            String indexHtml = new String(indexHtmlBytes, StandardCharsets.UTF_8);
            Mustache mustache = mf.compile(new StringReader(indexHtml), "?");

            StringWriter a = new StringWriter();
            mustache.execute(a, scopes).flush();

            return a.toString();
        } else {
            return "Not found";
        }
    }
}
