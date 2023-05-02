import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/Movie?" +
                    "useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false" +
                    "&serverTimezone=UTC", "root", "pubugi0314");

            String sql = "INSERT INTO movies (title, genre, runtime, year, rating, actors, rate, release_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = connection.prepareStatement(sql);

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
            connection.close();

        } catch (SQLException e) {
            System.out.println("SQLExeption: " + e);
        }

    }
}