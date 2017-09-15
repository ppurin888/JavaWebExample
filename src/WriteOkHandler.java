import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.sun.net.httpserver.*;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class WriteOkHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            System.out.println(httpExchange.getRemoteAddress());
            int num = getResponse(httpExchange);
            System.out.println(num);
            if (num != -1)
                httpExchange.getResponseHeaders().add("Location", "/board_info?num=" + num);
            else
                httpExchange.getResponseHeaders().add("Location", "/board_write");
            httpExchange.sendResponseHeaders(302, 0);
            httpExchange.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public int getResponse(HttpExchange httpExchange) throws Exception {
//        Statement stmt = MyHTTPServer.conn.createStatement();

        BufferedInputStream bis = new BufferedInputStream(httpExchange.getRequestBody());

        byte[] bodyBytes = new byte[Integer.parseInt(httpExchange.getRequestHeaders().getFirst("Content-Length"))];
        bis.read(bodyBytes);

        String body = URLDecoder.decode(new String(bodyBytes, StandardCharsets.UTF_8), "UTF-8");

        Map<String, String> contents = new HashMap<>();
        String[] queryParameters = body.split("&");
        for (String parameter : queryParameters) {
            String[] tuple = parameter.split("=");
            contents.put(tuple[0], tuple[1]);
        }

        try {
            MyHTTPServer.conn.setAutoCommit(false);
            String sql = "INSERT INTO board_list (title, name, content)"+" values (?,?,?)";
            PreparedStatement preparedStmt = MyHTTPServer.conn.prepareStatement(sql);
            preparedStmt.setNString(1, contents.get("title"));
            preparedStmt.setNString(2, contents.get("name"));
            preparedStmt.setNString(3, contents.get("content"));

            preparedStmt.execute();
            preparedStmt.close();

            Statement stmt = MyHTTPServer.conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from board_list order by num desc limit 1");
            rs.next();
            int num = rs.getInt("num");
            rs.close();
            stmt.close();

            MyHTTPServer.conn.commit();

            return num;
        } catch (SQLException e){
            System.out.println(e.toString());

            return -1;
        }
    }
}
