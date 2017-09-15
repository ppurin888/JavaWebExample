import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            System.out.println(httpExchange.getRemoteAddress());
            String response = getResponse(httpExchange);
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

    public String getResponse(HttpExchange httpExchange) throws Exception {
        Path index = Paths.get("web/board_info.html");

        if (Files.exists(index)) {
            byte[] indexHtmlBytes = Files.readAllBytes(index);
            HashMap<String, Object> scopes = new HashMap<>();


            Statement stmt = MyHTTPServer.conn.createStatement();
            Map<String, String> queryParameters = new HashMap<>();
            String uriQuery = httpExchange.getRequestURI().getQuery();
            String[] parameters = uriQuery.split("&");
            for (String p : parameters) {
                String[] parameterTuple = p.split("=");
                queryParameters.put(parameterTuple[0], parameterTuple[1]);
            }

            HashMap<String, Object> row = new HashMap<>();
            ResultSet rs = stmt.executeQuery("select * from board_list"+" where num = " + queryParameters.get("num"));
            try {
                rs.next();
                row.put("title", rs.getNString("title"));
                row.put("name", rs.getNString("name"));
                row.put("content", rs.getNString("content"));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            scopes.put("article", row);

            MustacheFactory mf = new DefaultMustacheFactory();
            String infoHtml = new String(indexHtmlBytes, StandardCharsets.UTF_8);
            Mustache mustache = mf.compile(new StringReader(infoHtml), "?");

            StringWriter a = new StringWriter();
            mustache.execute(a, scopes).flush();

            return a.toString();
        } else {
            return "Not found";
        }
    }


}
