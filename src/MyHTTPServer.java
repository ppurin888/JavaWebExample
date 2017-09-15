import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.sql.*;

public class MyHTTPServer  {

    public static Connection conn;

    static {
        Statement stmt;

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 연결 에러.");
        } catch (Exception etc) {
            System.out.println(etc.getMessage());
        }
        try {
            String url = "jdbc:mysql://localhost:3306/board";
            String userId = "root";
            String userPass = "1234";

            conn = DriverManager.getConnection(url, userId, userPass);
            stmt = conn.createStatement();
            ResultSet rs;
            rs = stmt.executeQuery("SHOW DATABASES");
            if (stmt.execute("SHOW DATABASES")) {
                rs = stmt.getResultSet();
            }

            while (rs.next()) {
                String str = rs.getNString(1);
                System.out.println(str);
            }
            System.out.println("연결");
            stmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException : " + e.getMessage());
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println("MyHTTPServer Started");
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8680), 0
        );

        server.createContext("/index", new IndexHandler());
        server.createContext("/board_write", new WriteHandler());
        server.createContext("/board_info", new InfoHandler());
        server.createContext("/write_ok", new WriteOkHandler());
        server.start();
    }
}
