import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.*;

public class Main {

    private static Connection conn;
    static Scanner scanner = new Scanner(System.in);

    static int totalRecords = 100000; // 전체 레코드 수
    static int bufferSize = 100; // 버퍼 크기
    static int totalBlocks = 1000; // 전체 블록 수
    private static String[] manufacturerList = {"Samsung", "Apple", "Google", "Xiaomi"};
    private static String[] appleDeviceList = {"", "SE", " Pro", " Pro Max"};
    private static String[] samsungDeviceList = {"Galaxy S", "Galaxy Z 플립", "Galaxy Z 폴드", "Galaxy A"};
    private static String[] samsungDeviceList2 = {"", "+", " Ultra"};
    private static String[] googleDeviceList = {"Pixel XL ", "Pixel "};
    private static String[] xiaomiDeviceList = {"Redmi ", "Redmi Note "};

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

            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }

    static void insert(String s){
        try {
            List<Record> buffer = new ArrayList<>();
            String insert = "INSERT INTO "+ s + "(manufacturer, model, price) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insert);

            for (int i = 0; i < totalBlocks; i++) {
                // 데이터를 읽어온 후 Record 객체로 변환하는 로직
                for (int j = 0; j < bufferSize; j++) {
                    Record record = new Record();
                    Random random = new Random();
                    String manufacturer = manufacturerList[random.nextInt(4)];
                    String model = "";
                    float price = 0.0f;
                    if (manufacturer == manufacturerList[0]) {
                        String device = samsungDeviceList[random.nextInt(4)];
                        model = device + random.nextInt(100);
                        if (device == samsungDeviceList[0]) {
                            model += samsungDeviceList2[random.nextInt(3)];
                        }
                    } else if (manufacturer == manufacturerList[1]) {
                        String device = appleDeviceList[random.nextInt(4)];
                        model = "Iphone ";
                        if (device == appleDeviceList[1]) {
                            model += device + random.nextInt(100);
                        } else {
                            model += (random.nextInt(100) + device);
                        }
                    } else if (manufacturer == manufacturerList[2]) {
                        model = googleDeviceList[random.nextInt(2)] + random.nextInt(100);
                    } else {
                        model = xiaomiDeviceList[random.nextInt(2)] + random.nextInt(100);

                    }
                    price = random.nextFloat() * (250.0f - 50.0f) + 50.0f;

                    record.setRecord(manufacturer, model, price);
                    buffer.add(record);
                }

                if (buffer.size() == bufferSize) {
                    for (Record b:buffer) {
                        pstmt.setString(1, b.manufacturer);
                        pstmt.setString(2, b.model);
                        pstmt.setFloat(3, b.price);
                        pstmt.executeUpdate();
                    }
                    buffer.clear();
                }
            }

// 마지막으로 버퍼에 남아 있는 레코드를 디스크에 쓰기
            if (!buffer.isEmpty()) {
                for (Record b:buffer) {
                    pstmt.setString(1, b.manufacturer);
                    pstmt.setString(2, b.model);
                    pstmt.setFloat(3, b.price);
                    pstmt.executeUpdate();
                }
                buffer.clear();
            }

            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }

    static void createBitmapIndex(String s) {
        System.out.println("컬럼명 입력:");
        String columns = scanner.next();
        while (true) {
            System.out.println("또 다른 컬럼이 있다면 1 아니면 2");
            int select = scanner.nextInt();
            if (select == 2) {
                break;
            } else {
                columns += ("," + scanner.next());
            }
        }

        System.out.println("Now Create BitmapIndex...");

        try {
            String sql = "select " + columns + " from " + s;
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();

            String manufacturer = "create table if not exists manufacturerBitmapIndex(samsungIndex bit(1), appleIndex bit(1), googleIndex bit(1), xiaomiIndex bit(1))";
            String price = "create table if not exists priceBitmapIndex(less100Index bit(1), exceed100Index bit(1), exceed150Index bit(1), exceed200Index bit(1))";
            PreparedStatement manufacturerPstmt = conn.prepareStatement(manufacturer);
            manufacturerPstmt.execute();
            PreparedStatement prictPstmt = conn.prepareStatement(price);
            prictPstmt.execute();

            String insertManufacturer = "insert into manufacturerBitmapIndex(samsungIndex,appleIndex,googleIndex,xiaomiIndex) values (?,?,?,?)";
            String insertPrice = "insert into priceBitmapIndex(less100Index,exceed100Index,exceed150Index,exceed200Index) values (?,?,?,?)";
            manufacturerPstmt = conn.prepareStatement(insertManufacturer);
            prictPstmt = conn.prepareStatement(insertPrice);

            ResultSet resultSet = pstmt.getResultSet();

            while (resultSet.next()) {
                if (resultSet.getString(1).equals(manufacturerList[0])){
                    manufacturerPstmt.setInt(1, 0b1);
                    manufacturerPstmt.setInt(2, 0b0);
                    manufacturerPstmt.setInt(3, 0b0);
                    manufacturerPstmt.setInt(4, 0b0);
                } else if (resultSet.getString(1).equals(manufacturerList[1])) {
                    manufacturerPstmt.setInt(1, 0b0);
                    manufacturerPstmt.setInt(2, 0b1);
                    manufacturerPstmt.setInt(3, 0b0);
                    manufacturerPstmt.setInt(4, 0b0);
                } else if (resultSet.getString(1).equals(manufacturerList[2])) {
                    manufacturerPstmt.setInt(1, 0b0);
                    manufacturerPstmt.setInt(2, 0b0);
                    manufacturerPstmt.setInt(3, 0b1);
                    manufacturerPstmt.setInt(4, 0b0);
                } else {
                    manufacturerPstmt.setInt(1, 0b0);
                    manufacturerPstmt.setInt(2, 0b0);
                    manufacturerPstmt.setInt(3, 0b0);
                    manufacturerPstmt.setInt(4, 0b1);
                }

                if (resultSet.getFloat(2) < 100.0f) {
                    prictPstmt.setInt(1, 0b1);
                    prictPstmt.setInt(2, 0b0);
                    prictPstmt.setInt(3, 0b0);
                    prictPstmt.setInt(4, 0b0);
                } else if (100.0f <= resultSet.getFloat(2) && resultSet.getFloat(2) < 150.0f) {
                    prictPstmt.setInt(1, 0b0);
                    prictPstmt.setInt(2, 0b1);
                    prictPstmt.setInt(3, 0b0);
                    prictPstmt.setInt(4, 0b0);
                } else if (150 <= resultSet.getFloat(2) && resultSet.getFloat(2) < 200) {
                    prictPstmt.setInt(1, 0b0);
                    prictPstmt.setInt(2, 0b0);
                    prictPstmt.setInt(3, 0b1);
                    prictPstmt.setInt(4, 0b0);
                } else {
                    prictPstmt.setInt(1, 0b0);
                    prictPstmt.setInt(2, 0b0);
                    prictPstmt.setInt(3, 0b0);
                    prictPstmt.setInt(4, 0b1);
                }

                manufacturerPstmt.executeUpdate();
                prictPstmt.executeUpdate();
            }

            manufacturerPstmt.close();
            prictPstmt.close();
            resultSet.close();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }

        System.out.println("BitmapIndexes are created now");
    }

    static void query() {
        try {
            String sql = "select * from devices";
            PreparedStatement pstmt = conn.prepareStatement(sql);

        } catch (SQLException e) {
            System.out.println("SQLException = " + e);
        }
        
    }
    public static void main(String[] args) {


        try {
            String url = "jdbc:mysql://localhost:3306/";
            String user = "root";
            String password = "pubugi0314";
            conn = DriverManager.getConnection(url, user, password);

            // 스키마 생성
            String schemaName = "smartphone";
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
                } else if (select.equals("3")) {
                    // 비트맵 인덱스 생성
                    System.out.println("테이블명 입력:");
                    tableName = scanner.next();
                    createBitmapIndex(tableName);
                } else if (select.equals("4")) {
                    query();
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