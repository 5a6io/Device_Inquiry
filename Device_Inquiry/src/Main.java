import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Main {

    private static Connection conn;
    static Scanner scanner = new Scanner(System.in);

    static int bufferSize = 100; // 버퍼 크기
    static int totalBlocks = 1000; // 전체 블록 수
    private static final String[] manufacturerList = {"Samsung", "Apple", "Google", "Xiaomi"};
    private static final String[] appleDeviceList = {"", "SE", " Pro", " Pro Max"};
    private static final String[] samsungDeviceList = {"Galaxy S", "Galaxy Z 플립", "Galaxy Z 폴드", "Galaxy A"};
    private static final String[] samsungDeviceList2 = {"", "+", " Ultra"};
    private static final String[] googleDeviceList = {"Pixel XL ", "Pixel "};
    private static final String[] xiaomiDeviceList = {"Redmi ", "Redmi Note "};

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

            String create = "CREATE TABLE IF NOT EXISTS " + s + "(id INT NOT NULL AUTO_INCREMENT," +
                    table +
                    "PRIMARY KEY(id))";

            PreparedStatement pstmt = conn.prepareStatement(create);
            pstmt.execute();

            pstmt.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }

    static void insert(String s) {
        try {
            List<Record> buffer = new ArrayList<>();
            String insert = "INSERT INTO " + s + "(manufacturer, model, price) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insert);
            System.out.println("1. 데이터 직접 생성 2. 데이터 자동 생성");
            int select = scanner.nextInt();
            if (select == 1) {
                while (true) {
                    String manufacturer = scanner.next();
                    String model = scanner.next();
                    Float price = scanner.nextFloat();
                    pstmt.setString(1, manufacturer);
                    pstmt.setString(2, model);
                    pstmt.setFloat(3, price);
                    pstmt.executeUpdate();
                    System.out.println("또 다른 데이터 삽입이면 1번 아니면 2번");
                    int num = scanner.nextInt();
                    if (num == 2) {
                        pstmt.close();
                        return;
                    }
                }
            } else if (select == 2) {
                for (int i = 0; i < totalBlocks; i++) {
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
                        for (Record b : buffer) {
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
                    for (Record b : buffer) {
                        pstmt.setString(1, b.manufacturer);
                        pstmt.setString(2, b.model);
                        pstmt.setFloat(3, b.price);
                        pstmt.executeUpdate();
                    }
                    buffer.clear();
                }
            } else {
                System.out.println("처음으로 돌아갑니다.");
                pstmt.close();
                return;
            }

            System.out.println("Created Data Automatically");
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

            ResultSet resultSet = pstmt.getResultSet();
            BufferedWriter samsungIndex = new BufferedWriter(new FileWriter("samsungIndex.txt", StandardCharsets.UTF_8));
            BufferedWriter appleIndex = new BufferedWriter(new FileWriter("appleIndex.txt", StandardCharsets.UTF_8));
            BufferedWriter googleIndex = new BufferedWriter(new FileWriter("googleIndex.txt", StandardCharsets.UTF_8));
            BufferedWriter xiaomiIndex = new BufferedWriter(new FileWriter("xiaomiIndex.txt", StandardCharsets.UTF_8));
            BufferedWriter less100Index = new BufferedWriter(new FileWriter("less100Index.txt", StandardCharsets.UTF_8));
            BufferedWriter exceed100Index = new BufferedWriter(new FileWriter("exceed100Index.txt", StandardCharsets.UTF_8));
            BufferedWriter exceed150Index = new BufferedWriter(new FileWriter("exceed150Index.txt", StandardCharsets.UTF_8));
            BufferedWriter exceed200Index = new BufferedWriter(new FileWriter("exceed200Index.txt", StandardCharsets.UTF_8));

            while (resultSet.next()) {
                if (resultSet.getString(1).equals(manufacturerList[0])) {
                    samsungIndex.write("1");
                    appleIndex.write("0");
                    googleIndex.write("0");
                    xiaomiIndex.write("0");
                } else if (resultSet.getString(1).equals(manufacturerList[1])) {
                    samsungIndex.write("0");
                    appleIndex.write("1");
                    googleIndex.write("0");
                    xiaomiIndex.write("0");
                } else if (resultSet.getString(1).equals(manufacturerList[2])) {
                    samsungIndex.write("0");
                    appleIndex.write("0");
                    googleIndex.write("1");
                    xiaomiIndex.write("0");
                } else if (resultSet.getString(1).equals(manufacturerList[3])) {
                    samsungIndex.write("0");
                    appleIndex.write("0");
                    googleIndex.write("0");
                    xiaomiIndex.write("1");
                }

                if (resultSet.getFloat(2) < 100.0f) {
                    less100Index.write("1");
                    exceed100Index.write("0");
                    exceed150Index.write("0");
                    exceed200Index.write("0");
                } else if (100.0f <= resultSet.getFloat(2) && resultSet.getFloat(2) < 150.0f) {
                    less100Index.write("0");
                    exceed100Index.write("1");
                    exceed150Index.write("0");
                    exceed200Index.write("0");
                } else if (150.0f <= resultSet.getFloat(2) && resultSet.getFloat(2) < 200.0f) {
                    less100Index.write("0");
                    exceed100Index.write("0");
                    exceed150Index.write("1");
                    exceed200Index.write("0");
                } else if (resultSet.getFloat(2) > 200.0f) {
                    less100Index.write("0");
                    exceed100Index.write("0");
                    exceed150Index.write("0");
                    exceed200Index.write("1");
                }

                samsungIndex.newLine();
                appleIndex.newLine();
                googleIndex.newLine();
                xiaomiIndex.newLine();
                less100Index.newLine();
                exceed100Index.newLine();
                exceed150Index.newLine();
                exceed200Index.newLine();
            }

            samsungIndex.close();
            appleIndex.close();
            googleIndex.close();
            xiaomiIndex.close();
            less100Index.close();
            exceed100Index.close();
            exceed150Index.close();
            exceed200Index.close();

            resultSet.close();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }

        System.out.println("BitmapIndexes are created now");
    }

    static void query() {
        try {
            String sql = "select * from devices";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(bufferSize);
            pstmt.execute();

            ResultSet resultSet = pstmt.getResultSet();

            BitSet appleReader = readBitsetFromFile("appleIndex.txt");
            BitSet samsungReader = readBitsetFromFile("samsungIndex.txt");
            BitSet googleReader = readBitsetFromFile("googleIndex.txt");
            BitSet xiaomiReader = readBitsetFromFile("xiaomiIndex.txt");
            BitSet less100Reader = readBitsetFromFile("less100Index.txt");
            BitSet exceed100Reader = readBitsetFromFile("exceed100Index.txt");
            BitSet exceed150Reader = readBitsetFromFile("exceed150Index.txt");
            BitSet exceed200Reader = readBitsetFromFile("exceed200Index.txt");

            while (true) {
                System.out.println("조회할 제조사 선택\n1.삼성 2.애플 3.구글 4.샤오미 5.취소");
                int select = scanner.nextInt();
                BitSet bitmapIndex;
                if (select == 1) {
                    int index = 0;
                    bitmapIndex = (BitSet) samsungReader.clone();
                    System.out.println("조회할 가격 범위 선택\n1.100이하 2.100~150 3.150~200 4.200이상 5.취소");
                    int num = scanner.nextInt();
                    if (num == 1) {
                        bitmapIndex.and(less100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 2) {
                        bitmapIndex.and(exceed100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 3) {
                        bitmapIndex.and(exceed150Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 4) {
                        bitmapIndex.and(exceed200Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else {
                        break;
                    }
                } else if (select == 2) {
                    int index = 0;
                    bitmapIndex = (BitSet) appleReader.clone();
                    System.out.println("조회할 가격 범위 선택\n1.100이하 2.100~150 3.150~200 4.200이상 5.취소");
                    int num = scanner.nextInt();
                    if (num == 1) {
                        bitmapIndex.and(less100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 2) {
                        bitmapIndex.and(exceed100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 3) {
                        bitmapIndex.and(exceed150Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 4) {
                        bitmapIndex.and(exceed200Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else {
                        break;
                    }
                } else if (select == 3) {
                    int index = 0;
                    bitmapIndex = (BitSet) googleReader.clone();
                    System.out.println("조회할 가격 범위 선택\n1.100이하 2.100~150 3.150~200 4.200이상 5.취소");
                    int num = scanner.nextInt();
                    if (num == 1) {
                        bitmapIndex.and(less100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 2) {
                        bitmapIndex.and(exceed100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 3) {
                        bitmapIndex.and(exceed150Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 4) {
                        bitmapIndex.and(exceed200Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else {
                        break;
                    }
                } else if (select == 4) {
                    int index = 0;
                    bitmapIndex = (BitSet) xiaomiReader.clone();
                    System.out.println("조회할 가격 범위 선택\n1.100이하 2.100~150 3.150~200 4.200이상 5.취소");
                    int num = scanner.nextInt();
                    if (num == 1) {
                        bitmapIndex.and(less100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 2) {
                        bitmapIndex.and(exceed100Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 3) {
                        bitmapIndex.and(exceed150Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else if (num == 4) {
                        bitmapIndex.and(exceed200Reader);
                        System.out.println("조회 가능한 모델 수(단위: 건): " + bitmapIndex.cardinality());
                        while (resultSet.next()) {
                            if (bitmapIndex.get(index)) {
                                System.out.print("모델명: " + resultSet.getString(3));
                                System.out.println(" | 가격: " + resultSet.getString(4));
                            }
                            index++;
                        }
                        System.out.println();
                    } else {
                        break;
                    }
                } else {
                    break;
                }

                resultSet.close();
                pstmt.close();
            }
        } catch (SQLException e) {
            System.out.println("SQLException = " + e);
        }
    }

    static BitSet readBitsetFromFile(String filepath) {
        BitSet bitSet = new BitSet();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                if (line.equals("1")) {
                    bitSet.set(index);
                }
                index++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bitSet;
    }

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

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
            conn = DriverManager.getConnection(url + schemaName + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false" +
                    "&serverTimezone=UTC", user, password);

            while (true) {
                System.out.println("1.테이블 생성\n2.레코드 삽입\n3.비트맵인덱스 생성\n4.질의처리\n5.종료");
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
        catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException = " + e.getMessage());
        }
    }
}