import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static Connection conn;
    static Scanner scanner = new Scanner(System.in);
    static int numRecords = 10000000;


    static void createTable(String s) {
        try {
            String table = "";
            System.out.println("컬럼과 도메인을 순서대로 입력하세요:");

            while (true) {
                String column = scanner.next() + " ";
                String domain = scanner.next().toUpperCase() + ",";
                table += column;
                table += domain;
                System.out.println("다른 컬럼이 있다면 1번 아니면 2번");
                int select = scanner.nextInt();
                if (select == 2) {
                    break;
                }
            }

            String create = "CREATE TABLE IF NOT EXISTS "+ s + "(id INT NOT NULL AUTO_INCREMENT," +
                     table +
                    "PRIMARY KEY(id))";

            PreparedStatement pstmt = conn.prepareStatement(create);
            pstmt.execute();

            return;
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }

    static void insert(String s){
        try {
            PreparedStatement pstmt = conn.prepareStatement(s);

            String insert = "INSERT INTO "+ s + "(title, genre, runtime, year, rating, actors, rate, release_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet resultSet = metaData.getColumns(null, null, s, null);

            while (resultSet.next()) {
                String dataType = resultSet.getString("DATA_TYPE");
//                if (dataType.equals(""))
            }

            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }
    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/";
            String user = "root";
            String password = "pubugi0314";
            conn = DriverManager.getConnection(url, user, password);

            // 스키마 생성
            String schemaName = "Movie_Inquiry";
            String schema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            PreparedStatement pstmt = conn.prepareStatement(schema);
            pstmt.execute();
            pstmt.close();
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + schemaName, user, password);

            while (true) {
                System.out.println("1.테이블 생성\n2.레코드 삽입\n3.질의처리");
                String tableName = null;
                if (scanner.next().equals("1")) {
                    tableName = scanner.next();
                    createTable(tableName);
                } else if (scanner.next().equals("2")) {
                    // 데이터 삽입
                    tableName = scanner.next();
                    insert(tableName);

                    DatabaseMetaData metaData = conn.getMetaData();
                    metaData.getColumns(null, null, tableName, null);

                    ResultSet resultSet = pstmt.getResultSet();

                    int[] genreBitmap = new int[numRecords / 32 + 1];
                    int[] releaseBitmap = new int[numRecords / 32 + 1];

                    FileOutputStream fileOutputStream;


                } else if (scanner.next().equals("3")) {

                } else {
                    conn.close();
                    return;
                }

            }

        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}