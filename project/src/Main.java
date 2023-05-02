import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Main extends JFrame {

    public Main() {
        String[] b1_list = {};
        String[] b2_list = {};
        JComboBox b1 = new JComboBox<>(b1_list);
        JComboBox b2 = new JComboBox<>(b2_list);
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();

        setTitle("영화 정보 조회");
        setSize(700, 600);

        setLayout(null);
        p1.setBounds(0, 8, 700, 50);
        p2.setBounds(0, 50, 700, 500);

        p1.add(b1);
        p1.add(b2);

        p1.setLayout(new FlowLayout());


        add(p1);
        add(p2);

        setVisible(true);

        try{
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Movie?" +
                    "useUnicode=true&useJDBCCompliantTimezoneShift=true?useLegacyDatetimeCode=false" +
                    "&serverTimezone=UTC", "root", "pubugi0314");

            Statement stmt = connection.createStatement();
            String s = "select * from movies";

            ResultSet resultSet = stmt.executeQuery(s);

            while (resultSet.next()) {

            }

            b1.addActionListener(e -> {
                JComboBox c = (JComboBox) e.getSource();
                String h_type = (String) c.getSelectedItem();


            });

        } catch (SQLException e) {
            System.out.println("SQLException: " + e);
        }
    }
    public static void main(String[] args) {
        Main m = new Main();
    }
}