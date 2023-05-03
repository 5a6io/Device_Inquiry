import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/";
            String user = "root";
            String password = "pubugi0314";
            Connection conn = DriverManager.getConnection(url, user, password);
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet schemas = dbm.getSchemas();
            boolean existSchema = false;

            // 스키마 생성
            String schemaName = "Movie_Inquiry";
            String schema = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            PreparedStatement pstmt = conn.prepareStatement(schema);
            pstmt.execute();
            pstmt.close();
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + schemaName, user, password);

            // 테이블 생성
            String tableName = "movies";
            String create = "CREATE TABLE IF NOT EXISTS "+ tableName + "(" +
                        "serial_num INT NOT NULL AUTO_INCREMENT," +
                        "title VARCHAR(45) NOT NULL," +
                        "genre VARCHAR(5)," +
                        "runtime INT," +
                        "year INT," +
                        "rating NUMERIC(3,2)," +
                        "actors VARCHAR(45)," +
                        "rate VARCHAR(7)," +
                        "release_status BOOLEAN NOT NULL," +
                        "PRIMARY KEY(serial_num, title)," +
                        "INDEX idx_movies_serial(serial_num) USING BTREE)";

            pstmt = conn.prepareStatement(create);
            pstmt.execute();

            // 데이터 삽입
            String insert = "INSERT INTO movies (title, genre, runtime, year, rating, actors, rate, release_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insert);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("영화정보입력:");
                String title = scanner.next();
                if (title.equals("break")) {
                    break;
                }
                pstmt.setString(1, title);

                String genre = scanner.next();
                if (genre.equals("null")) {
                    pstmt.setNull(2, Types.CHAR);
                } else {
                    pstmt.setString(2, genre);
                }

                int runtime = scanner.nextInt();
                if (runtime == 0) {
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setInt(3, runtime);
                }

                int year = scanner.nextInt();
                if (year == 0) {
                    pstmt.setNull(4, Types.INTEGER);
                } else {
                    pstmt.setInt(4, year);
                }

                double rating = scanner.nextDouble();
                if (rating == 0) {
                    pstmt.setNull(5, Types.DOUBLE);
                } else {
                    pstmt.setDouble(5, rating);
                }

                String actors = scanner.next();
                if (actors.equals("null")) {
                    pstmt.setNull(6, Types.CHAR);
                } else {
                    pstmt.setString(6, actors);
                }

                String rate = scanner.next();
                if (rate.equals("null")) {
                    pstmt.setNull(7, Types.CHAR);
                } else {
                    pstmt.setString(7, rate);
                }

                boolean releaseStatus = scanner.nextBoolean();
                pstmt.setBoolean(8, releaseStatus);

                pstmt.executeUpdate();
            }

            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("SQLExeption: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}