import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static Connection conn;
    static Scanner scanner = new Scanner(System.in);
    static int numRecords = 10000000;

    static String[] genre = {"액션", "애니메이션", "드라마", "모험", "코미디", "범죄", "로맨스", "스릴러", "공포", "판타지", "미스터리"};

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
            String insert = "INSERT INTO "+ s + "(title, genre, runtime, year, rating, rate, release_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insert);

            File file = new File("Movie_Inquiry\\movies.txt");

            try {
                for (int i = 0; i < 1000; i++) {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                    String line = bufferedReader.readLine();
                    if (i != 0) {
                        while (line != null) {
                            String[] words = line.split(",");
                            if (!words[0].isBlank())
                                pstmt.setString(1, words[0] + i);
                            else
                                pstmt.setNull(1, Types.CHAR);

                            if (!words[1].isBlank())
                                pstmt.setString(2, words[1]);
                            else
                                pstmt.setNull(2, Types.CHAR);

                            if (!words[2].isBlank())
                                pstmt.setInt(3, Integer.parseInt(words[2]));
                            else
                                pstmt.setNull(3, Types.INTEGER);

                            if (!words[3].isBlank())
                                pstmt.setInt(4, Integer.parseInt(words[3]));
                            else
                                pstmt.setNull(4, Types.INTEGER);

                            if (!words[4].isBlank()) {
                                Random random = new Random();
                                double rating = 0 + (10 - 0) * random.nextDouble();
                                pstmt.setDouble(5, rating);
                            } else
                                pstmt.setNull(5, Types.DOUBLE);

                            if (!words[5].isBlank()) {
                                Random random = new Random();
                                int idx = 0 + (10 - 0) * random.nextInt();
                                pstmt.setString(6, genre[idx]);
                            } else {
                                pstmt.setNull(6, Types.CHAR);
                            }

                            Random random = new Random();
                            boolean status = random.nextBoolean();

                            pstmt.setBoolean(7, status);

                            pstmt.executeUpdate();
                        }

                        bufferedReader.reset();

                    } else {
                        while (line != null) {
                            String[] words = line.split(",");
                            if (!words[0].isBlank())
                                pstmt.setString(1, words[0]);
                            else
                                pstmt.setNull(1, Types.CHAR);

                            if (!words[1].isBlank())
                                pstmt.setString(2, words[1]);
                            else
                                pstmt.setNull(2, Types.CHAR);

                            if (!words[2].isBlank())
                                pstmt.setInt(3, Integer.parseInt(words[2]));
                            else
                                pstmt.setNull(3, Types.INTEGER);

                            if (!words[3].isBlank())
                                pstmt.setInt(4, Integer.parseInt(words[3]));
                            else
                                pstmt.setNull(4, Types.INTEGER);

                            if (!words[4].isBlank())
                                pstmt.setDouble(5, Double.parseDouble(words[4]));
                            else
                                pstmt.setNull(5, Types.DOUBLE);

                            if (!words[5].isBlank())
                                pstmt.setString(6, words[5]);
                            else
                                pstmt.setNull(6, Types.CHAR);

                            pstmt.setBoolean(7, Boolean.parseBoolean(words[6]));

                            pstmt.executeUpdate();
                        }

                        bufferedReader.reset();
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }

        return;
    }
    public static void main(String[] args) {

        try {


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
                System.out.println("1.테이블 생성\n2.레코드 삽입\n3.비트맵인덱스 생성\n4.질의처리");
                String tableName = null;
                String select = scanner.next();
                if (select.equals("1")) {
                    System.out.println("테이블명 입력:");
                    tableName = scanner.next();
                    createTable(tableName);
                } else if (select.equals("2")) {
                    // 데이터 삽입
                    System.out.println("테이블명 입력:");
                    tableName = scanner.next();
                    insert(tableName);

//                    ResultSet resultSet = pstmt.getResultSet();
//
//                    int[] genreBitmap = new int[numRecords / 32 + 1];
//                    int[] releaseBitmap = new int[numRecords / 32 + 1];
//

                } else if (select.equals("3")) {

                } else {
                    conn.close();
                    return;
                }

            }

        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }
}