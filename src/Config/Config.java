
package Config;

import java.sql.*;
import java.util.*;

public class Config {
    private static Connection con;

    
    public static Connection connectDB() {
        try {
            if (con == null || con.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:WaterBilling.db");
                //System.out.println("Database connected!");
            }
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return con;
    }

   
    public void addRecord(String sql, Object... values) {
        try (PreparedStatement pstmt = connectDB().prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (Exception e) {
            System.out.println("Add Error: " + e.getMessage());
        }
    }

    
    public void updateRecord(String sql, Object... values) {
        try (PreparedStatement pstmt = connectDB().prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (Exception e) {
            System.out.println("Update Error: " + e.getMessage());
        }
    }


public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
    if (columnHeaders.length != columnNames.length) {
        System.out.println("Error: Mismatch between column headers and column names.");
        return;
    }

    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
         ResultSet rs = pstmt.executeQuery()) {

        // Print headers with borders
        StringBuilder headerLine = new StringBuilder();
        headerLine.append("--------------------------------------------------------------------------------\n| ");
        for (String header : columnHeaders) {
            headerLine.append(String.format("%-20s | ", header));
        }
        headerLine.append("\n--------------------------------------------------------------------------------");

        System.out.println(headerLine);

        
        while (rs.next()) {
            StringBuilder row = new StringBuilder("| ");
            for (String colName : columnNames) {
                String value = rs.getString(colName);
                row.append(String.format("%-20s | ", value != null ? value : ""));
            }
            System.out.println(row);
        }

        System.out.println("--------------------------------------------------------------------------------");

    } catch (SQLException e) {
        System.out.println("Error retrieving records: " + e.getMessage());
    }
}


    // FETCH RECORDS (pang login/checking)
    public List<Map<String, Object>> fetchRecords(String sql, Object... values) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement pstmt = connectDB().prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>(columns);
                for (int i = 1; i <= columns; i++) {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (Exception e) {
            System.out.println("Fetch Error: " + e.getMessage());
        }
        return list;
    }
}
